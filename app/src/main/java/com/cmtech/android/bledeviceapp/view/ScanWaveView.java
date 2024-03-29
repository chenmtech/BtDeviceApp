/*
 * * SignalScanView: ��ɨ��ķ�ʽ������ʾ��������ʾ�ź�
 * Ŀǰû�м����κ��˲�����
 * Wrote by chenm, BME, GDMC
 * 2013.09.25
 */
package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.cmtech.android.ble.utils.ExecutorUtil;

import java.util.concurrent.ExecutorService;

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

public class ScanWaveView extends WaveView {
    protected int curX; // 波形线的当前横坐标
    private int[] curYs; //每个波形线的当前纵坐标
    private final Rect delRect = new Rect(); // 要抹去的小矩形

    private Bitmap waveBitmap;	//波形bitmap
    protected Canvas waveCanvas;	//波形canvas

    private boolean first = true; // 是否是第一个数据
    protected boolean showWave = true; // 是否显示波形，还是暂停显示

    //private final PorterDuffXfermode srcOverMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
    //private final PorterDuffXfermode srcInMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    protected GestureDetector gestureDetector; // 手势探测器
    private class MyGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }
        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }
        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            setShowWave(!showWave); // 是否暂停显示
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
    }

    private ExecutorService showService; // 显示服务

    public ScanWaveView(Context context) {
        super(context);
        gestureDetector = new GestureDetector(context, new MyGestureListener());
        gestureDetector.setIsLongpressEnabled(false);
    }

    public ScanWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new MyGestureListener());
        gestureDetector.setIsLongpressEnabled(false);
    }

    @Override
    public void initWave() {
        super.initWave();
        curYs = new int[waveNum];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(waveBitmap != null)
            canvas.drawBitmap(waveBitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    // 是否显示波形
    public void setShowWave(boolean showWave) {
        if(this.showWave != showWave) {
            this.showWave = showWave;
            if (listener != null) {
                listener.onShowStateUpdated(showWave);
            }
        }
    }

    // 重置view
    // resetBackground: 是否重绘背景bitmap
    @Override
    public void resetView(boolean resetBgbitmap)
    {
        super.resetView(resetBgbitmap);

        // 创建波形bitmap和canvas
        waveBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);
        waveCanvas = new Canvas(waveBitmap);

        curX = initX;
        for(int i = 0; i < waveNum; i++)
            curYs[i] = initYs[i];

        first = true;
        showWave = true;
        setShowWave(showWave);
    }

    @Override
    public void initWavePaint() {
        super.initWavePaint();
        //for(int i = 0; i < waveNum; i++)
        //    wavePaints[i].setXfermode(srcOverMode);
    }

    private void drawData(int[] data, boolean updateView) {
        // 将ADU数据转化为波形纵坐标，像素为单位
        int[] dataYs = new int[waveNum];
        for(int i = 0; i < waveNum; i++)
            dataYs[i] = initYs[i] - Math.round(data[i] / aduPerPixel[i]);

        if (first) {
            for(int i = 0; i < waveNum; i++)
                preYs[i] = dataYs[i];
            first = false;
        } else {
            drawDataOnWaveCanvas(dataYs);
            if(updateView)
                postInvalidate();
        }
    }

    private void drawDataOnWaveCanvas(int[] dataYs)
    {
        for(int i = 0; i < waveNum; i++)
            curYs[i] = dataYs[i];

        if(preX == viewWidth)	//最后一个像素，抹去第一列
        {
            curX = initX;
            delRect.set(initX, 0, initX + pixelPerGrid, viewHeight);
        }
        else	//画线
        {
            curX += pixelPerData;
            for(int i = 0; i < waveNum; i++)
                waveCanvas.drawLine(preX, preYs[i], curX, curYs[i], wavePaints[i]);
            delRect.set(curX +1, 0, curX + pixelPerGrid, viewHeight);
        }
        preX = curX;
        for(int i = 0; i < waveNum; i++)
            preYs[i] = curYs[i];
        //抹去矩形区域delRect
        //wavePaints[0].setXfermode(srcInMode);
        waveCanvas.drawBitmap(bgBitmap, delRect, delRect, null);
        //wavePaints[0].setXfermode(srcOverMode);
    }

    //---------------------------------------------------实现抽象方法
    // 开始显示
    @Override
    public void startShow() {
        if(ExecutorUtil.isDead(showService)) {
            showService = ExecutorUtil.newSingleExecutor("MT_Wave_Show");
        }
    }

    // 停止显示
    @Override
    public void stopShow() {
        ExecutorUtil.shutdownAndAwaitTerminate(showService);
    }

    @Override
    public void addData(final int[] data, boolean show) {
        if(showWave && !ExecutorUtil.isDead(showService)) {
            showService.execute(new Runnable() {
                @Override
                public void run() {
                    drawData(data, show);
                }
            });
        }
    }

    // 显示数据
    public void addData(final int[] data) {
        addData(data, true);
    }
}
