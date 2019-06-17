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

import com.cmtech.android.bledeviceapp.R;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * ScanWaveView: 扫描式的波形显示视图，用于心电信号采集时的实时显示
 * Created by bme on 2018/12/06.
 */

public class ScanWaveView extends View {
    private static final int DEFAULT_SIZE = 100; // 缺省View的大小
    private static final int DEFAULT_XPIXELPERDATA = 2; // 缺省的X方向的分辨率
    private static final float DEFAULT_YVALUEPERPIXEL = 1.0f; // 缺省的Y方向的分辨率
    private static final double DEFAULT_ZERO_LOCATION = 0.5; // 缺省的零线位置在Y方向的高度的比例
    private static final int DEFAULT_PIXELSPERGRID = 10; // 每个栅格的像素个数
    private static final int DEFAULT_BACKGROUND_COLOR = Color.BLACK;
    private static final int DEFAULT_GRID_COLOR = Color.RED;
    private static final int DEFAULT_WAVE_COLOR = Color.YELLOW;

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

    private final int gridColor; // 栅格颜色

    private int waveColor; // 画线颜色

    private final int defaultWaveColor; // 缺省的画线颜色

    private PorterDuffXfermode srcOverMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);

    private PorterDuffXfermode srcInMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    // View初始化主要需要设置下面4个参数
    private int gridPixels = DEFAULT_PIXELSPERGRID; // 栅格像素个数

    private int xPixelPerData = DEFAULT_XPIXELPERDATA; //X方向分辨率，表示X方向每个数据点占多少个像素，pixel/data

    private float yValuePerPixel = DEFAULT_YVALUEPERPIXEL; //Y方向分辨率，表示Y方向每个像素代表的信号值，value/pixel

    private double zeroLocation = DEFAULT_ZERO_LOCATION; //表示零值位置占视图高度的百分比

    private boolean showGridLine = true; // 是否显示栅格线

    private boolean isFirstData = false; // 是否是第一个数据

    private boolean isUpdated = true;

    private GestureDetector gestureDetector;

    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            isUpdated = !isUpdated;
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

    private LinkedBlockingQueue<Integer> dataCache = new LinkedBlockingQueue<>();

    //private Thread showThread;

    private ScheduledExecutorService showService;

    public ScanWaveView(Context context) {
        super(context);

        backgroundColor = DEFAULT_BACKGROUND_COLOR;

        gridColor = DEFAULT_GRID_COLOR;

        waveColor = defaultWaveColor = DEFAULT_WAVE_COLOR;

        initPaint();

        gestureDetector = new GestureDetector(context, gestureListener);

        gestureDetector.setIsLongpressEnabled(false);
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

        waveColor = defaultWaveColor = a.getColor(R.styleable.WaveView_wave_color, DEFAULT_WAVE_COLOR);

        showGridLine = a.getBoolean(R.styleable.WaveView_show_gridline, true);

        //最后记得将TypedArray对象回收
        a.recycle();

        initPaint();

        gestureDetector = new GestureDetector(context, gestureListener);

        gestureDetector.setIsLongpressEnabled(false);
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

    public void setGridPixels(int gridPixels) {
        this.gridPixels = gridPixels;
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

        initialize();
    }



    private void initPaint() {
        bmpPaint.setXfermode(srcOverMode);

        bmpPaint.setAlpha(255);

        bmpPaint.setStrokeWidth(2);

    }

    public void initialize()
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

        isUpdated = true;

        dataCache.clear();
    }

    public void start(final int period) {
        if(showService == null) {
            showService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "MyThread_wave_show");
                }
            });

            showService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try{
                        showData(dataCache.take());
                        showData(dataCache.take());
                        showData(dataCache.take());
                        postInvalidate();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, period*3, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        /*if(showThread != null) {
            showThread.interrupt();
            try {
                showThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        if(showService != null) {
            showService.shutdownNow();

            while(!showService.isTerminated()) {
                try {
                    showService.awaitTermination(1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addData(int data) {
        try {
            dataCache.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void showData(int data) {
        int dataY = initY - Math.round(data / yValuePerPixel);

        if (isFirstData) {
            preY = dataY;
            isFirstData = false;
        } else {
            if (isUpdated) {
                drawPointOnForeCanvas(dataY);
                //postInvalidate();
            }
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
            deleteRect.set(initX, 0, initX + gridPixels, viewHeight);
        }
        else	//画线
        {
            curX += xPixelPerData;
            bmpPaint.setColor(waveColor);
            foreCanvas.drawLine(preX, preY, curX, curY, bmpPaint);

            deleteRect.set(curX +1, 0, curX + gridPixels, viewHeight);
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
        paint.setColor(gridColor);

        // 画零位线
        paint.setStrokeWidth(4);
        backCanvas.drawLine(initX, initY, initX + viewWidth, initY, paint);

        paint.setStrokeWidth(1);

        // 画水平线
        int vCoordinate = initY - gridPixels;
        int i = 1;
        while(vCoordinate > 0) {
            backCanvas.drawLine(initX, vCoordinate, initX + viewWidth, vCoordinate, paint);
            vCoordinate -= gridPixels;
            if(++i == 5) {
                paint.setStrokeWidth(2);
                i = 0;
            }
            else
                paint.setStrokeWidth(1);
        }
        paint.setStrokeWidth(1);
        vCoordinate = initY + gridPixels;
        i = 1;
        while(vCoordinate < viewHeight) {
            backCanvas.drawLine(initX, vCoordinate, initX + viewWidth, vCoordinate, paint);
            vCoordinate += gridPixels;
            if(++i == 5) {
                paint.setStrokeWidth(2);
                i = 0;
            }
            else
                paint.setStrokeWidth(1);
        }

        // 画垂直线
        paint.setStrokeWidth(1);
        int hCoordinate = initX + gridPixels;
        i = 1;
        while(hCoordinate < viewWidth) {
            backCanvas.drawLine(hCoordinate, 0, hCoordinate, viewHeight, paint);
            hCoordinate += gridPixels;
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
		c.drawLine(0, initY, 2*gridPixels, initY, mainPaint);
		c.drawLine(2*gridPixels, initY, 2*gridPixels, initY-10*gridPixels, mainPaint);
		c.drawLine(2*gridPixels, initY-10*gridPixels, 7*gridPixels, initY-10*gridPixels, mainPaint);
        c.drawLine(7*gridPixels, initY-10*gridPixels, 7*gridPixels, initY, mainPaint);
        c.drawLine(7*gridPixels, initY, 10*gridPixels, initY, mainPaint);*/

        //mainPaint.setColor(waveColor);
        //mainPaint.setStrokeWidth(2);
    }
}
