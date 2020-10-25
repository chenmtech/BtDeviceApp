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
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.FixSizeLinkedList;

import java.util.ArrayList;
import java.util.List;

/**
 * RollWaveView: 卷轴滚动式的波形显示视图
 * Created by bme on 2018/12/06.
 */

public class RollWaveView extends View {
    private static final int DEFAULT_SIZE = 100; // 缺省View的大小
    private static final int DEFAULT_PIXEL_PER_DATA = 2; // 缺省横向每个数据占的像素数
    private static final float DEFAULT_VALUE_PER_PIXEL = 1.0f; // 缺省纵向每个像素代表的数值
    public static final float DEFAULT_ZERO_LOCATION = 0.5f; // 缺省的零值位置在纵向的高度比
    private static final int DEFAULT_PIXEL_PER_GRID = 10; // 每个小栅格的像素个数
    private static final int SMALL_GRID_NUM_PER_LARGE_GRID = 5; // 每个大栅格包含多少个小栅格
    private static final int DEFAULT_BACKGROUND_COLOR = Color.BLACK; // 背景色
    private static final int DEFAULT_LARGE_GRID_LINE_COLOR = Color.RED; // 大栅格线颜色
    private static final int DEFAULT_SMALL_GRID_LINE_COLOR = Color.RED; // 小栅格线颜色
    protected static final int DEFAULT_WAVE_COLOR = Color.YELLOW; // 波形颜色
    private static final int DEFAULT_ZERO_LINE_WIDTH = 4; // 零位置线宽
    private static final int DEFAULT_LARGE_GRID_LINE_WIDTH = 2; // 大栅格线宽
    private static final int DEFAULT_SMALL_GRID_LINE_WIDTH = 0; // 小栅格线宽，0代表头发丝风格
    private static final int DEFAULT_WAVE_WIDTH = 2; // 波形线宽

    private int viewWidth = DEFAULT_SIZE; //视图宽度
    private int viewHeight = DEFAULT_SIZE;  //视图高度
    protected int initX, initY;	 //画图起始位置
    protected int preX, preY; //画线的前一个点坐标
    protected final Paint forePaint = new Paint(); // 波形画笔
    protected Bitmap backBitmap; //背景bitmap
    private Bitmap foreBitmap; //前景bitmap
    protected Canvas foreCanvas; //前景canvas

    private final boolean showGridLine; // 是否显示栅格线

    private final int bgColor; // 背景颜色
    private final int gridColor; // 栅格线颜色
    private final int waveColor; // 波形颜色

    private int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // 一个栅格的像素宽度
    protected int pixelPerData = DEFAULT_PIXEL_PER_DATA;	 //X方向分辨率，表示屏幕X方向每个数据点占多少个像素，pixel/data
    protected float valuePerPixel = DEFAULT_VALUE_PER_PIXEL; //Y方向分辨率，表示屏幕Y方向每个像素代表的信号值的变化，DeltaSignal/pixel
    private double zeroLocation = DEFAULT_ZERO_LOCATION; //表示零值位置占视图高度的百分比

    protected int dataNumInView; // X方向上一屏包含的数据点数

    protected List<Integer> viewData = new FixSizeLinkedList<>(1); //要显示的信号数据对象的引用

    protected OnRollWaveViewListener listener;

    public RollWaveView(Context context) {
        super(context);

        showGridLine = true;
        bgColor = DEFAULT_BACKGROUND_COLOR;
        gridColor = DEFAULT_LARGE_GRID_LINE_COLOR;
        waveColor = DEFAULT_WAVE_COLOR;
    }

    public RollWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        showGridLine = a.getBoolean(R.styleable.WaveView_show_grid_line, true);
        bgColor = a.getColor(R.styleable.WaveView_background_color, DEFAULT_BACKGROUND_COLOR);
        gridColor = a.getColor(R.styleable.WaveView_large_grid_line_color, DEFAULT_LARGE_GRID_LINE_COLOR);
        waveColor = a.getColor(R.styleable.WaveView_wave_color, DEFAULT_WAVE_COLOR);
        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = getWidth();
        viewHeight = getHeight();
        resetView(true);
    }

    // 重置view
    // includeBackground: 是否重绘背景bitmap
    public void resetView(boolean includeBackground)
    {
        setDataNumInView(viewWidth, pixelPerData);

        clearData();

        initX = 0;
        initY = (int)(viewHeight * zeroLocation);

        //重新创建背景Bitmap
        if(includeBackground)
            createBackBitmap();

        // 创建前景bitmap和canvas
        //将背景bitmap复制为前景bitmap
        foreBitmap = backBitmap.copy(Bitmap.Config.ARGB_8888,true);
        foreCanvas = new Canvas(foreBitmap);

        // 初始化画图起始位置
        preX = initX;
        preY = initY;

        // 重置前景画笔
        initForePaint();

        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(foreBitmap, 0, 0, null);
    }

    private void initForePaint() {
        forePaint.setAlpha(255);
        forePaint.setStyle(Paint.Style.STROKE);
        forePaint.setStrokeWidth(2);
        forePaint.setColor(waveColor);
    }

    public void setPixelPerGrid(int gridWidth) {
        this.pixelPerGrid = gridWidth;
    }

    // 设置分辨率
    public void setResolution(int pixelPerData, float valuePerPixel)
    {
        if((pixelPerData < 1) || (valuePerPixel < 0)) {
            throw new IllegalArgumentException();
        }
        this.pixelPerData = pixelPerData;
        this.valuePerPixel = valuePerPixel;
    }

    public void setZeroLocation(double zeroLocation)
    {
        this.zeroLocation = zeroLocation;
        initY = (int)(viewHeight * this.zeroLocation);
    }

    public int getDataNumInView() {
        return dataNumInView;
    }

    public void setDataNumInView(int viewWidth, int pixelPerData) {
        dataNumInView = viewWidth/pixelPerData+1;
        viewData = new FixSizeLinkedList<>(dataNumInView);
    }

    public void clearData() {
        viewData.clear();
    }

    public void addData(Integer data) {
        viewData.add(data);
    }

    public synchronized void showData(List<Integer> data) {
        viewData.addAll(data);
        drawDataOnForeCanvas();
        postInvalidate();
    }

    public void setListener(OnRollWaveViewListener listener) {
        this.listener = listener;
    }

    protected boolean drawDataOnForeCanvas()
    {
        foreCanvas.drawBitmap(backBitmap, 0, 0, null);

        Integer[] data = viewData.toArray(new Integer[0]);
        int dataNum = data.length;
        if(dataNum <= 1) {
            return true;
        }

        int begin = dataNum - dataNumInView;
        if(begin < 0) {
            begin = 0;
        }

        clearData();
        addData(data[begin]);
        preX = initX;
        preY = initY - Math.round(data[begin]/ valuePerPixel);
        Path path = new Path();
        path.moveTo(preX, preY);
        for(int i = begin+1; i < dataNum; i++) {
            addData(data[i]);
            preX += pixelPerData;
            preY = initY - Math.round(data[i]/ valuePerPixel);
            path.lineTo(preX, preY);
        }

        foreCanvas.drawPath(path, forePaint);
        return true;
    }

    // 创建背景Bitmap
    private void createBackBitmap()
    {
        initX = 0;
        initY = (int)(viewHeight * zeroLocation);

        //创建背景Bitmap
        backBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);

        if(!showGridLine) return;

        Canvas backCanvas = new Canvas(backBitmap);
        backCanvas.drawColor(bgColor);

        Paint paint = new Paint();
        paint.setColor(gridColor);

        // 画零位线
        paint.setStrokeWidth(4);
        backCanvas.drawLine(initX, initY, initX + viewWidth, initY, paint);

        paint.setStrokeWidth(1);

        // 画水平线
        int vCoordinate = initY - pixelPerGrid;
        int i = 1;
        while(vCoordinate > 0) {
            backCanvas.drawLine(initX, vCoordinate, initX + viewWidth, vCoordinate, paint);
            vCoordinate -= pixelPerGrid;
            if(++i == 5) {
                paint.setStrokeWidth(2);
                i = 0;
            }
            else
                paint.setStrokeWidth(1);
        }
        paint.setStrokeWidth(1);
        vCoordinate = initY + pixelPerGrid;
        i = 1;
        while(vCoordinate < viewHeight) {
            backCanvas.drawLine(initX, vCoordinate, initX + viewWidth, vCoordinate, paint);
            vCoordinate += pixelPerGrid;
            if(++i == 5) {
                paint.setStrokeWidth(2);
                i = 0;
            }
            else
                paint.setStrokeWidth(1);
        }

        // 画垂直线
        paint.setStrokeWidth(1);
        int hCoordinate = initX + pixelPerGrid;
        i = 1;
        while(hCoordinate < viewWidth) {
            backCanvas.drawLine(hCoordinate, 0, hCoordinate, viewHeight, paint);
            hCoordinate += pixelPerGrid;
            if(++i == 5) {
                paint.setStrokeWidth(2);
                i = 0;
            }
            else
                paint.setStrokeWidth(1);
        }
    }
}
