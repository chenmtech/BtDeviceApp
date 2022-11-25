/*
 * * SignalScanView: ��ɨ��ķ�ʽ������ʾ��������ʾ�ź�
 * Ŀǰû�м����κ��˲�����
 * Wrote by chenm, BME, GDMC
 * 2013.09.25
 */
package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.cmtech.android.bledeviceapp.R;

public abstract class WaveView extends View {
    //----------------------------------------------常量
    private static final int[] WAVE_COLORS = new int[]{Color.YELLOW, Color.WHITE}; // 波形颜色

    private static final int SMALL_GRID_NUM_PER_LARGE_GRID = 5; // 每个大栅格包含多少个小栅格
    private static final int DEFAULT_SIZE = 100; // 缺省View的大小
    private static final int DEFAULT_PIXEL_PER_DATA = 2; // 缺省横向每个数据占的像素数
    private static final int DEFAULT_PIXEL_PER_GRID = 10; // 每个小栅格的像素个数
    private static final int DEFAULT_BACKGROUND_COLOR = Color.BLACK; // 背景色
    private static final int DEFAULT_LARGE_GRID_COLOR = Color.RED; // 大栅格线颜色
    private static final int DEFAULT_SMALL_GRID_COLOR = Color.RED; // 小栅格线颜色
    private static final int DEFAULT_ZERO_LINE_WIDTH = 4; // 零位置线宽
    private static final int DEFAULT_LARGE_GRID_LINE_WIDTH = 2; // 大栅格线宽
    private static final int DEFAULT_SMALL_GRID_LINE_WIDTH = 0; // 小栅格线宽，0代表头发丝风格
    private static final int DEFAULT_WAVE_WIDTH = 2; // 波形线宽

    //--------------------------------------------实例变量
    protected int viewWidth = DEFAULT_SIZE; //视图宽度
    protected int viewHeight = DEFAULT_SIZE; //视图高度
    protected Bitmap backBitmap;  //背景bitmap
    private final int bgColor; // 背景颜色
    private final int largeGridColor; // 大栅格线颜色
    private final int smallGridColor; // 小栅格线颜色
    private final int zeroLineWidth; // 零线宽度
    private final int largeGridLineWidth; // 大栅格线宽
    private final int smallGridLineWidth; // 小栅格线宽
    private final int waveWidth = DEFAULT_WAVE_WIDTH; // 波形线宽
    private final boolean showGridLine; // 是否显示栅格线
    protected int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // 每个栅格的像素个数

    protected int pixelPerData = DEFAULT_PIXEL_PER_DATA; // 横向分辨率，即X方向每个数据点占多少个像素，pixel/data

    protected int initX; //起始横坐标
    protected int preX; //画线的前一个点横坐标

    //-------------------------------------与波形个数相关的变量
    protected int waveNum; // 波形数
    protected Paint[] wavePaints; // 波形画笔
    protected int[] initYs; //波形的起始纵坐标
    protected int[] preYs; //波形线的前一个点纵坐标
    protected float[] aduPerPixel; //纵向分辨率，即Y方向每个像素代表的信号ADU值，ADU/pixel

    private int[] waveColors; // 波形颜色
    private float[] zeroLocs; //波形零值位置占视图高度的百分比
    ///////////////////////////////////////////////////////////////////////

    protected OnWaveViewListener listener; // 监听器

    public WaveView(Context context) {
        super(context);

        showGridLine = true;
        bgColor = DEFAULT_BACKGROUND_COLOR;
        largeGridColor = DEFAULT_LARGE_GRID_COLOR;
        smallGridColor = DEFAULT_SMALL_GRID_COLOR;
        zeroLineWidth = DEFAULT_ZERO_LINE_WIDTH;
        largeGridLineWidth = DEFAULT_LARGE_GRID_LINE_WIDTH;
        smallGridLineWidth = DEFAULT_SMALL_GRID_LINE_WIDTH;

        initWave(1);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        showGridLine = styledAttributes.getBoolean(R.styleable.WaveView_show_grid_line, true);
        bgColor = styledAttributes.getColor(R.styleable.WaveView_background_color, DEFAULT_BACKGROUND_COLOR);
        largeGridColor = styledAttributes.getColor(R.styleable.WaveView_large_grid_line_color, DEFAULT_LARGE_GRID_COLOR);
        smallGridColor = styledAttributes.getColor(R.styleable.WaveView_small_grid_line_color, DEFAULT_SMALL_GRID_COLOR);
        zeroLineWidth = styledAttributes.getInt(R.styleable.WaveView_zero_line_width, DEFAULT_ZERO_LINE_WIDTH);
        largeGridLineWidth = styledAttributes.getInt(R.styleable.WaveView_large_grid_line_width, DEFAULT_LARGE_GRID_LINE_WIDTH);
        smallGridLineWidth = styledAttributes.getInt(R.styleable.WaveView_small_grid_line_width, DEFAULT_SMALL_GRID_LINE_WIDTH);

        waveNum = styledAttributes.getInt(R.styleable.WaveView_wave_num, 1);
        initWave(waveNum);

        styledAttributes.recycle();
    }

    public void initWave(int waveNum) {
        this.waveNum = waveNum;
        waveColors = new int[waveNum];
        wavePaints = new Paint[waveNum];
        zeroLocs = new float[waveNum];
        aduPerPixel = new float[waveNum];
        initYs = new int[waveNum];
        preYs = new int[waveNum];
        for(int i = 0; i < waveNum; i++) {
            waveColors[i] = WAVE_COLORS[i%WAVE_COLORS.length];
            wavePaints[i] = new Paint();
            zeroLocs[i] = (1.0f+2*i) / (2*waveNum);
            aduPerPixel[i] = 1.0f;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = getWidth();
        viewHeight = getHeight();
        resetView(true);
    }

    public int getPixelPerData() {
        return pixelPerData;
    }

    /**
     * 设置视图的可调参数，并重绘视图
     * @param zeroLocs 每个波形的零值位置，占视图高度的百分比
     * @param aduPerPixel 纵向分辨率，即每个像素对应的数据ADU值。每个波形可以不一样
     * @param pixelPerData 横向分辨率，即每个数据对应的像素个数
     * @param pixelPerGrid 每个小栅格占像素个数
     */
    protected void setup(float[] zeroLocs, float[] aduPerPixel, int pixelPerData, int pixelPerGrid) {
        if(zeroLocs.length != aduPerPixel.length)
            throw new IllegalArgumentException();

        if((pixelPerData < 1))
            throw new IllegalArgumentException();

        for (float v : aduPerPixel) {
            if (v <= 0.0)
                throw new IllegalArgumentException();
        }

        initWave(zeroLocs.length);

        for(int i = 0; i < waveNum; i++) {
            this.zeroLocs[i] = zeroLocs[i];
            initYs[i] = (int) (viewHeight * this.zeroLocs[i]);
            this.aduPerPixel[i] = aduPerPixel[i];
        }

        this.pixelPerData = pixelPerData;
        this.pixelPerGrid = pixelPerGrid;

        resetView(true);
    }

    public void setListener(OnWaveViewListener listener) {
        this.listener = listener;
    }

    // 重置view
    // resetBackground: 是否重绘背景bitmap
    public void resetView(boolean resetBackground)
    {
        initX = 0;
        for(int i = 0; i < waveNum; i++) {
            initYs[i] = (int) (viewHeight * this.zeroLocs[i]);
        }

        //重新创建背景Bitmap
        if(resetBackground) {
            backBitmap = createBackBitmap();
            setBackground(new BitmapDrawable(getResources(), backBitmap));
        }

        // 初始化画图起始位置
        preX = initX;
        for(int i = 0; i < waveNum; i++) {
            preYs[i] = initYs[i];
        }

        initWavePaint();

        postInvalidate();
    }

    // 初始化波形画笔
    public void initWavePaint() {
        for(int i = 0; i < wavePaints.length; i++) {
            wavePaints[i].setAlpha(255);
            wavePaints[i].setStrokeWidth(waveWidth);
            wavePaints[i].setColor(waveColors[i]);
        }
    }

    // 开始显示
    public abstract void startShow();

    // 停止显示
    public abstract void stopShow();

    // 添加单个数据
    public abstract void addData(final int[] data, boolean show);

    //创建背景Bitmap
    private Bitmap createBackBitmap()
    {
        Bitmap backBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);
        Canvas backCanvas = new Canvas(backBitmap);
        backCanvas.drawColor(bgColor);

        if(!showGridLine) return null;

        Paint paint = new Paint();

        // 画零位线
        setPaint(paint, largeGridColor, zeroLineWidth);
        for(int i = 0; i < waveNum; i++)
            backCanvas.drawLine(initX, initYs[i], initX + viewWidth, initYs[i], paint);

        // 画水平线
        int deltaY;
        int waveHalfHeight = viewHeight/(waveNum*2);

        for(int drawed = 0; drawed < 2; drawed++ ) {
            if(drawed == 0) { // 零线以上
                deltaY = -pixelPerGrid;
            } else { // 零线以下
                deltaY = pixelPerGrid;
            }

            for(int i = 0; i < waveNum; i++) {
                int uplimit = initYs[i]-waveHalfHeight;
                int downlimit = initYs[i]+waveHalfHeight;
                setPaint(paint, smallGridColor, smallGridLineWidth);
                int y = initYs[i] + deltaY;
                int n = 1;
                while ((drawed == 0 && y >= uplimit) || (drawed == 1 && y <= downlimit)) {
                    backCanvas.drawLine(initX, y, initX + viewWidth, y, paint);
                    y += deltaY;
                    if (++n == SMALL_GRID_NUM_PER_LARGE_GRID) {
                        setPaint(paint, largeGridColor, largeGridLineWidth);
                        n = 0;
                    } else {
                        setPaint(paint, smallGridColor, smallGridLineWidth);
                    }
                }
            }
        }

        // 画垂直线
        setPaint(paint, largeGridColor, largeGridLineWidth);
        backCanvas.drawLine(initX, 0, initX, viewHeight, paint);
        setPaint(paint, smallGridColor, smallGridLineWidth);

        int x = initX + pixelPerGrid;
        int n = 1;
        while(x <= viewWidth) {
            backCanvas.drawLine(x, 0, x, viewHeight, paint);
            x += pixelPerGrid;
            if(++n == SMALL_GRID_NUM_PER_LARGE_GRID) {
                setPaint(paint, largeGridColor, largeGridLineWidth);
                n = 0;
            }
            else {
                setPaint(paint, smallGridColor, smallGridLineWidth);
            }
        }

        return backBitmap;
    }

    private void setPaint(Paint paint, int color, int lineWidth) {
        paint.setColor(color);
        paint.setStrokeWidth(lineWidth);
    }
}
