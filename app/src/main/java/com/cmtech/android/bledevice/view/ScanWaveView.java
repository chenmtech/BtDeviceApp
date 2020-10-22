/*
 * * SignalScanView: ��ɨ��ķ�ʽ������ʾ��������ʾ�ź�
 * Ŀǰû�м����κ��˲�����
 * Wrote by chenm, BME, GDMC
 * 2013.09.25
 */
package com.cmtech.android.bledevice.view;

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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 *
 * ClassName:      ScanWaveView
 * Description:    以扫描的方式显示波形的View
 * Author:         chenm
 * CreateDate:     2018-12-06 11:16
 *
 * UpdateUser:     chenm
 * UpdateDate:     2019-06-19 11:16
 * UpdateRemark:   添加显示服务，用独立的线程实现显示功能
 * Version:        1.0
 *
 */

public class ScanWaveView extends View {
    private static final int DEFAULT_SIZE = 100; // 缺省View的大小
    private static final int DEFAULT_PIXEL_PER_DATA = 2; // 缺省横向每个数据占的像素数
    private static final float DEFAULT_VALUE_PER_PIXEL = 1.0f; // 缺省纵向每个像素代表的数值
    public static final double DEFAULT_ZERO_LOCATION = 0.5; // 缺省的零值位置在纵向的高度比
    private static final int DEFAULT_PIXEL_PER_GRID = 10; // 每个栅格的像素个数
    private static final int SMALL_GRID_NUM_IN_LARGE_GRID = 5; // 一个大的栅格包含多少个小的栅格
    private static final int DEFAULT_BACKGROUND_COLOR = Color.BLACK; // 背景色
    private static final int DEFAULT_LARGE_GRID_LINE_COLOR = Color.RED; // 大栅格线颜色
    private static final int DEFAULT_SMALL_GRID_LINE_COLOR = Color.RED; // 小栅格线颜色
    private static final int DEFAULT_WAVE_COLOR = Color.YELLOW; // 波形颜色
    private static final int DEFAULT_ZERO_LINE_WIDTH = 4;
    private static final int DEFAULT_LARGE_GRID_LINE_WIDTH = 2;
    private static final int DEFAULT_SMALL_GRID_LINE_WIDTH = 0;

    private int viewWidth = 100; //视图宽度
    private int viewHeight = 100; //视图高度
    private int initX, initY; //画图起始坐标
    private int preX, preY; //画线的前一个点坐标
    private int curX, curY; //画线的当前点坐标
    private final Rect deleteRect = new Rect(); // 要抹去的小矩形
    private final Paint bmpPaint = new Paint(); // 画线笔
    private Bitmap backBitmap;  //背景bitmap
    private Bitmap foreBitmap;	//前景bitmap
    private Canvas foreCanvas;	//前景canvas
    private final int backgroundColor; // 背景颜色
    private final int largeGridLineColor; // 大栅格线颜色
    private final int smallGridLineColor; // 小栅格线颜色
    private int waveColor; // 波形颜色
    private final int defaultWaveColor; // 缺省的波形颜色
    private final PorterDuffXfermode srcOverMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
    private final PorterDuffXfermode srcInMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    // View初始化主要需要设置下面4个参数
    private int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // 每个栅格的像素个数
    private int xPixelPerData = DEFAULT_PIXEL_PER_DATA; //X方向分辨率，表示X方向每个数据点占多少个像素，pixel/data
    private float yValuePerPixel = DEFAULT_VALUE_PER_PIXEL; //Y方向分辨率，表示Y方向每个像素代表的信号值，value/pixel
    private double zeroLocation = DEFAULT_ZERO_LOCATION; //表示零值位置占视图高度的百分比

    private final int zeroLineWidth;
    private final int largeGridLineWidth;
    private final int smallGridLineWidth;

    private boolean showGridLine = true; // 是否显示栅格线
    private boolean isFirstData = false; // 是否是第一个数据
    private boolean isShowed = true;

    public void setShowed(boolean isShowed) {
        if(this.isShowed != isShowed) {
            this.isShowed = isShowed;
            if (listener != null) {
                listener.onShowStateUpdated(isShowed);
            }
        }
    }

    protected OnWaveViewListener listener;


    private GestureDetector gestureDetector;
    private final GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }
        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }
        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            setShowed(!isShowed);
            return false;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float v, float v1) {
            return false;
        }
        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };

    private ExecutorService showService; // 显示服务

    public ScanWaveView(Context context) {
        super(context);
        backgroundColor = DEFAULT_BACKGROUND_COLOR;
        largeGridLineColor = DEFAULT_LARGE_GRID_LINE_COLOR;
        smallGridLineColor = DEFAULT_SMALL_GRID_LINE_COLOR;
        waveColor = defaultWaveColor = DEFAULT_WAVE_COLOR;

        zeroLineWidth = DEFAULT_ZERO_LINE_WIDTH;
        largeGridLineWidth = DEFAULT_LARGE_GRID_LINE_WIDTH;
        smallGridLineWidth = DEFAULT_SMALL_GRID_LINE_WIDTH;

        initialize(context);
    }

    public ScanWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        showGridLine = styledAttributes.getBoolean(R.styleable.WaveView_show_grid_line, true);
        backgroundColor = styledAttributes.getColor(R.styleable.WaveView_background_color, DEFAULT_BACKGROUND_COLOR);
        largeGridLineColor = styledAttributes.getColor(R.styleable.WaveView_large_grid_line_color, DEFAULT_LARGE_GRID_LINE_COLOR);
        smallGridLineColor = styledAttributes.getColor(R.styleable.WaveView_large_grid_line_color, DEFAULT_SMALL_GRID_LINE_COLOR);
        waveColor = defaultWaveColor = styledAttributes.getColor(R.styleable.WaveView_wave_color, DEFAULT_WAVE_COLOR);

        zeroLineWidth = styledAttributes.getInt(R.styleable.WaveView_zero_line_width, DEFAULT_ZERO_LINE_WIDTH);
        largeGridLineWidth = styledAttributes.getInt(R.styleable.WaveView_large_grid_line_width, DEFAULT_LARGE_GRID_LINE_WIDTH);
        smallGridLineWidth = styledAttributes.getInt(R.styleable.WaveView_small_grid_line_width, DEFAULT_SMALL_GRID_LINE_WIDTH);

        styledAttributes.recycle();

        initialize(context);
    }

    public void initialize(Context context) {
        initPaint();
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);
    }

    public int getxPixelPerData() {
        return xPixelPerData;
    }

    // 设置分辨率
    public void setResolution(int xPixelPerData, float yValuePerPixel)
    {
        if((xPixelPerData < 1) || (yValuePerPixel < 0)) {
            throw new IllegalArgumentException();
        }
        this.xPixelPerData = xPixelPerData;
        this.yValuePerPixel = yValuePerPixel;
    }

    public void setZeroLocation(double zeroLocation)
    {
        this.zeroLocation = zeroLocation;
        initY = (int)(viewHeight * this.zeroLocation);
    }

    public void setPixelPerGrid(int pixelPerGrid) {
        this.pixelPerGrid = pixelPerGrid;
    }
    public void setWaveColor(int waveColor) {
        this.waveColor = waveColor;
    }
    public void restoreDefaultWaveColor() {
        this.waveColor = defaultWaveColor;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
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
        reset();
    }

    private void initPaint() {
        bmpPaint.setXfermode(srcOverMode);
        bmpPaint.setAlpha(255);
        bmpPaint.setStrokeWidth(2);
    }

    /**
     * 重置
     */
    public void reset()
    {
        //创建背景Bitmap
        createBackBitmap();
        //将背景bitmap复制为前景bitmap
        foreBitmap = backBitmap.copy(Bitmap.Config.ARGB_8888,true);
        foreCanvas = new Canvas(foreBitmap);
        // 初始化画图起始位置
        preX = curX = initX;
        preY = curY = initY;
        isFirstData = true;
        setShowed(true);
    }

    /**
     * 开始显示
     */
    public void start() {
        if(showService == null || showService.isTerminated()) {
            ViseLog.e("启动ScanWaveView");

            showService = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "MT_Wave_Show");
                }
            });
        }
    }

    /**
     * 停止显示
     */
    public void stop() {
        ViseLog.e("停止ScanWaveView");

        //ExecutorUtil.shutdownNowAndAwaitTerminate(showService);
        if (showService != null && !showService.isTerminated()) {
            showService.shutdown();

            try {
                while (!showService.awaitTermination(1, TimeUnit.SECONDS)) {
                    ViseLog.e("The thread pool is not terminated. Wait again");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                showService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 显示一个数据
     * @param data: 要显示的数据
     */
    public void showData(final int data) {
        if(isShowed && !ExecutorUtil.isDead(showService)) {
            showService.execute(new Runnable() {
                @Override
                public void run() {
                    drawData(data);
                }
            });
        }
    }

    private void drawData(int data) {
        int dataY = initY - Math.round(data / yValuePerPixel);
        if (isFirstData) {
            preY = dataY;
            isFirstData = false;
        } else {
            drawPointOnForeCanvas(dataY);
            postInvalidate();
        }
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

    private void drawPointOnForeCanvas(int dataY)
    {
        curY = dataY;
        if(preX == viewWidth)	//最后一个像素，抹去第一列
        {
            curX = initX;
            deleteRect.set(initX, 0, initX + pixelPerGrid, viewHeight);
        }
        else	//画线
        {
            curX += xPixelPerData;
            bmpPaint.setColor(waveColor);
            foreCanvas.drawLine(preX, preY, curX, curY, bmpPaint);
            deleteRect.set(curX +1, 0, curX + pixelPerGrid, viewHeight);
        }
        preX = curX;
        preY = curY;
        //抹去前面一个矩形区域
        bmpPaint.setXfermode(srcInMode);
        foreCanvas.drawBitmap(backBitmap, deleteRect, deleteRect, bmpPaint);
        bmpPaint.setXfermode(srcOverMode);
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

        // 画零位线
        setPaint(paint, largeGridLineColor, zeroLineWidth);
        backCanvas.drawLine(initX, initY, initX + viewWidth, initY, paint);

        // 画水平线
        int deltaY;
        for(int drawed = 0; drawed < 2; drawed++ ) {
            if(drawed == 0) {
                deltaY = -pixelPerGrid;
            } else {
                deltaY = pixelPerGrid;
            }

            setPaint(paint, smallGridLineColor, smallGridLineWidth);
            int y = initY + deltaY;
            int n = 1;
            while((drawed == 0 && y >= 0) || (drawed == 1 && y <= viewHeight) ) {
                backCanvas.drawLine(initX, y, initX + viewWidth, y, paint);
                y += deltaY;
                if(++n == SMALL_GRID_NUM_IN_LARGE_GRID) {
                    setPaint(paint, largeGridLineColor, largeGridLineWidth);
                    n = 0;
                }
                else {
                    setPaint(paint, smallGridLineColor, smallGridLineWidth);
                }
            }
        }

        // 画垂直线
        setPaint(paint, largeGridLineColor, largeGridLineWidth);
        backCanvas.drawLine(initX, 0, initX, viewHeight, paint);
        setPaint(paint, smallGridLineColor, smallGridLineWidth);

        int x = initX + pixelPerGrid;
        int n = 1;
        while(x <= viewWidth) {
            backCanvas.drawLine(x, 0, x, viewHeight, paint);
            x += pixelPerGrid;
            if(++n == SMALL_GRID_NUM_IN_LARGE_GRID) {
                setPaint(paint, largeGridLineColor, largeGridLineWidth);
                n = 0;
            }
            else {
                setPaint(paint, smallGridLineColor, smallGridLineWidth);
            }
        }

        // 画定标脉冲
/*        mainPaint.setStrokeWidth(2);
		mainPaint.setColor(Color.BLACK);
		c.drawLine(0, initY, 2*pixelPerGrid, initY, mainPaint);
		c.drawLine(2*pixelPerGrid, initY, 2*pixelPerGrid, initY-10*pixelPerGrid, mainPaint);
		c.drawLine(2*pixelPerGrid, initY-10*pixelPerGrid, 7*pixelPerGrid, initY-10*pixelPerGrid, mainPaint);
        c.drawLine(7*pixelPerGrid, initY-10*pixelPerGrid, 7*pixelPerGrid, initY, mainPaint);
        c.drawLine(7*pixelPerGrid, initY, 10*pixelPerGrid, initY, mainPaint);*/

        //mainPaint.setColor(waveColor);
        //mainPaint.setStrokeWidth(2);
    }

    private void setPaint(Paint paint, int color, int lineWidth) {
        paint.setColor(color);
        paint.setStrokeWidth(lineWidth);
    }

    public void setListener(OnWaveViewListener listener) {
        this.listener = listener;
    }
}
