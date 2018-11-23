/*
 * * SignalScanView: ��ɨ��ķ�ʽ������ʾ��������ʾ�ź�
 * Ŀǰû�м����κ��˲�����
 * Wrote by chenm, BME, GDMC
 * 2013.09.25
 */
package com.cmtech.android.bledevice.waveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.cmtech.android.bledeviceapp.R;

import java.util.concurrent.LinkedBlockingQueue;


public class ScanWaveView extends View {

    private static final int DEFAULT_SIZE = 100;                // 缺省View的大小
    private static final int DEFAULT_XRES = 2;                  // 缺省的X方向的分辨率
    private static final float DEFAULT_YRES = 1.0f;	            // 缺省的Y方向的分辨率
    private static final double DEFAULT_ZERO_LOCATION = 0.5;   // 缺省的零线位置在Y方向的高度的比例
    private static final int DEFAULT_GRID_WIDTH = 10;           // 背景栅格像素宽度

    private static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private static final int DEFAULT_GRID_COLOR = Color.RED;
    private static final int DEFAULT_WAVE_COLOR = Color.BLACK;

    private int viewWidth = 100;					//视图宽度
    private int viewHeight = 100;				    //视图高度
    private int initX, initY;			        //画图起始位置
    private int preX, preY;				    //画线的前一个点坐标
    private int curX, curY;				    //画线的当前点坐标

    private final Paint bmpPaint = new Paint();
    private Bitmap backBitmap;  //背景bitmap
    private Bitmap foreBitmap;	//前景bitmap
    private Canvas foreCanvas;	//前景canvas
    private PorterDuffXfermode srcOverMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
    private PorterDuffXfermode srcInMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    private Rect deleteRect = new Rect();

    private final int backgroundColor;
    private final int gridColor;
    private final int waveColor;

    //private final LinkedBlockingQueue<Integer> viewData = new LinkedBlockingQueue<Integer>();	//要显示的信号数据对象的引用
    private int viewData = 0;

    // View初始化主要需要设置下面4个参数
    private int gridWidth = DEFAULT_GRID_WIDTH;                // 栅格像素宽度
    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    private int xRes = DEFAULT_XRES;						//X方向分辨率，表示屏幕X方向每个数据点占多少个像素，pixel/data
    private float yRes = DEFAULT_YRES;					//Y方向分辨率，表示屏幕Y方向每个像素代表的信号值的变化，DeltaSignal/pixel
    public int getXRes()
    {
        return xRes;
    }
    public float getYRes()
    {
        return yRes;
    }
    public void setRes(int xRes, float yRes)
    {
        if((xRes < 1) || (yRes < 0)) return;
        this.xRes = xRes;
        this.yRes = yRes;
    }

    private double zeroLocation = DEFAULT_ZERO_LOCATION;			//表示零值位置占视图高度的百分比
    public void setZeroLocation(double zeroLocation)
    {
        this.zeroLocation = zeroLocation;
        initY = (int)(viewHeight * this.zeroLocation);
    }

    private boolean showGridLine = true;

    private boolean isFirstData = false;

    public ScanWaveView(Context context) {
        super(context);

        backgroundColor = DEFAULT_BACKGROUND_COLOR;
        gridColor = DEFAULT_GRID_COLOR;
        waveColor = DEFAULT_WAVE_COLOR;

        initPaint();
    }

    public ScanWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //第二个参数就是我们在attrs.xml文件中的<declare-styleable>标签
        //即属性集合的标签，在R文件中名称为R.styleable+name
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveView);

        //第一个参数为属性集合里面的属性，R文件名称：R.styleable+属性集合名称+下划线+属性名称
        //第二个参数为，如果没有设置这个属性，则设置的默认的值
        backgroundColor = a.getColor(R.styleable.WaveView_background_color, DEFAULT_BACKGROUND_COLOR);
        gridColor = a.getColor(R.styleable.WaveView_grid_color, DEFAULT_GRID_COLOR);
        waveColor = a.getColor(R.styleable.WaveView_wave_color, DEFAULT_WAVE_COLOR);
        showGridLine = a.getBoolean(R.styleable.WaveView_show_gridline, true);

        //最后记得将TypedArray对象回收
        a.recycle();

        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.drawColor(backgroundColor);

        canvas.drawBitmap(foreBitmap, 0, 0, null);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(calculateMeasure(widthMeasureSpec), calculateMeasure(heightMeasureSpec));

        viewWidth = getWidth();
        viewHeight = getHeight();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewWidth = getWidth();
        viewHeight = getHeight();

        initView();
    }

    private void initPaint() {
        bmpPaint.setXfermode(srcOverMode);
        bmpPaint.setAlpha(255);
        bmpPaint.setStrokeWidth(2);
    }

    public void initView()
    {
        // 清除缓存区
        //viewData.clear();
        viewData = 0;

        //创建背景Bitmap
        createBackBitmap();

        //将背景bitmap复制为前景bitmap
        foreBitmap = backBitmap.copy(Bitmap.Config.ARGB_8888,true);
        foreCanvas = new Canvas(foreBitmap);

        // 初始化画图起始位置
        preX = curX = initX;
        preY = curY = initY;

        isFirstData = true;
    }

    public synchronized void showData(Integer data) {
        if(isFirstData) {
            preY = initY - Math.round(data/yRes);
            isFirstData = false;
            return;
        }
        //viewData.offer(data);
        viewData = data;

        drawPointOnForeCanvas();

        postInvalidate();
    }

    private int calculateMeasure(int measureSpec)
    {
        int size = (int)(DEFAULT_SIZE * getResources().getDisplayMetrics().density);
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if(specMode == MeasureSpec.EXACTLY)
        {
            size = specSize;
        }
        else if(specMode == MeasureSpec.AT_MOST)
        {
            size = Math.min(size, specSize);
        }
        return size;
    }

    private boolean drawPointOnForeCanvas()
    {
        Integer value = 0;

        /*try {
            value = viewData.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        value = viewData;

        curY = initY - Math.round(value/yRes);

        if(preX == viewWidth)	//最后一个像素，抹去第一列
        {
            curX = initX;
            deleteRect.set(initX, 0, initX +gridWidth, viewHeight);
        }
        else	//画线
        {
            curX += xRes;
            bmpPaint.setColor(waveColor);
            foreCanvas.drawLine(preX, preY, curX, curY, bmpPaint);

            deleteRect.set(curX +1, 0, curX +gridWidth, viewHeight);
        }

        //抹去前面一个矩形区域
        bmpPaint.setXfermode(srcInMode);
        foreCanvas.drawBitmap(backBitmap, deleteRect, deleteRect, bmpPaint);
        bmpPaint.setXfermode(srcOverMode);

        preX = curX;
        preY = curY;

        return true;
    }

    private void createBackBitmap()
    {
        initX = 0;
        initY = (int)(viewHeight * zeroLocation);

        //创建背景Bitmap
        backBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);

        if(!showGridLine) return;

        Canvas backCanvas = new Canvas(backBitmap);
        Paint paint = new Paint();
        paint.setColor(gridColor);

        // 画零位线
        paint.setStrokeWidth(4);
        backCanvas.drawLine(initX, initY, initX + viewWidth, initY, paint);

        paint.setStrokeWidth(1);

        // 画水平线
        int vCoordinate = initY - gridWidth;
        int i = 1;
        while(vCoordinate > 0) {
            backCanvas.drawLine(initX, vCoordinate, initX + viewWidth, vCoordinate, paint);
            vCoordinate -= gridWidth;
            if(++i == 5) {
                paint.setStrokeWidth(2);
                i = 0;
            }
            else
                paint.setStrokeWidth(1);
        }
        paint.setStrokeWidth(1);
        vCoordinate = initY + gridWidth;
        i = 1;
        while(vCoordinate < viewHeight) {
            backCanvas.drawLine(initX, vCoordinate, initX + viewWidth, vCoordinate, paint);
            vCoordinate += gridWidth;
            if(++i == 5) {
                paint.setStrokeWidth(2);
                i = 0;
            }
            else
                paint.setStrokeWidth(1);
        }

        // 画垂直线
        paint.setStrokeWidth(1);
        int hCoordinate = initX + gridWidth;
        i = 1;
        while(hCoordinate < viewWidth) {
            backCanvas.drawLine(hCoordinate, 0, hCoordinate, viewHeight, paint);
            hCoordinate += gridWidth;
            if(++i == 5) {
                paint.setStrokeWidth(2);
                i = 0;
            }
            else
                paint.setStrokeWidth(1);
        }

        // 画定标脉冲
/*        mainPaint.setStrokeWidth(2);
		mainPaint.setColor(Color.BLACK);
		c.drawLine(0, initY, 2*gridWidth, initY, mainPaint);
		c.drawLine(2*gridWidth, initY, 2*gridWidth, initY-10*gridWidth, mainPaint);
		c.drawLine(2*gridWidth, initY-10*gridWidth, 7*gridWidth, initY-10*gridWidth, mainPaint);
        c.drawLine(7*gridWidth, initY-10*gridWidth, 7*gridWidth, initY, mainPaint);
        c.drawLine(7*gridWidth, initY, 10*gridWidth, initY, mainPaint);*/

        //mainPaint.setColor(waveColor);
        //mainPaint.setStrokeWidth(2);
    }
}
