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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.vise.log.ViseLog;

import java.util.List;
import java.util.concurrent.ExecutorService;
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

public class ScanWaveView extends WaveView {
    private int curX, curY; //画线的当前点坐标
    private final Rect deleteRect = new Rect(); // 要抹去的小矩形

    private Bitmap waveBitmap;	//波形bitmap
    private Canvas waveCanvas;	//波形canvas

    private boolean first = true; // 是否是第一个数据
    protected boolean showWave = true; // 是否显示波形

    private final PorterDuffXfermode srcOverMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
    private final PorterDuffXfermode srcInMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

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
            setShowWave(!showWave);
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
    // includeBackground: 是否重绘背景bitmap
    @Override
    public void resetView(boolean includeBackground)
    {
        super.resetView(includeBackground);

        // 创建波形bitmap和canvas
        waveBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);
        waveCanvas = new Canvas(waveBitmap);

        curX = initX;
        curY = initY;

        first = true;
        setShowWave(true);
    }

    // 开始显示
    @Override
    public void startShow() {
        if(showService == null || showService.isTerminated()) {
            ViseLog.e("启动ScanWaveView");
            showService = ExecutorUtil.newSingleExecutor("MT_Wave_Show");
        }
    }

    // 停止显示
    @Override
    public void stopShow() {
        if (showService != null && !showService.isTerminated()) {
            ViseLog.e("停止ScanWaveView");
            showService.shutdown();
            try {
                if(!showService.awaitTermination(10, TimeUnit.SECONDS)) {
                    showService.shutdownNow();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                showService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // 显示数据
    public void addData(final int[] data) {
        addData(data, true);
    }

    @Override
    public void addData(final int[] data, boolean show) {
        if(showWave && !ExecutorUtil.isDead(showService)) {
            showService.execute(new Runnable() {
                @Override
                public void run() {
                    drawData(data[0], show);
                }
            });
        }
    }

    @Override
    public void initWavePaint() {
        wavePaint.setXfermode(srcOverMode);
        super.initWavePaint();
    }

    private void drawData(int data, boolean updateView) {
        int dataY = initY - Math.round(data / valuePerPixel);
        if (first) {
            preY = dataY;
            first = false;
        } else {
            drawDataOnWaveCanvas(dataY);
            if(updateView)
                postInvalidate();
        }
    }

    private void drawData(List<Integer> data, boolean updateView) {
        for(Integer d : data) {
            drawData(d, false);
        }
        if(updateView)
            postInvalidate();
    }

    private void drawDataOnWaveCanvas(int dataY)
    {
        curY = dataY;
        if(preX == viewWidth)	//最后一个像素，抹去第一列
        {
            curX = initX;
            deleteRect.set(initX, 0, initX + pixelPerGrid, viewHeight);
        }
        else	//画线
        {
            curX += pixelPerData;
            waveCanvas.drawLine(preX, preY, curX, curY, wavePaint);
            deleteRect.set(curX +1, 0, curX + pixelPerGrid, viewHeight);
        }
        preX = curX;
        preY = curY;
        //抹去前面一个矩形区域
        wavePaint.setXfermode(srcInMode);
        waveCanvas.drawBitmap(backBitmap, deleteRect, deleteRect, wavePaint);
        wavePaint.setXfermode(srcOverMode);
    }
}
