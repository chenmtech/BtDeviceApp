/*
 * * SignalScanView: ��ɨ��ķ�ʽ������ʾ��������ʾ�ź�
 * Ŀǰû�м����κ��˲�����
 * Wrote by chenm, BME, GDMC
 * 2013.09.25
 */
package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import com.cmtech.android.bledeviceapp.util.FixSizeLinkedList;

/**
 * RollWaveView: 卷轴滚动式的波形显示视图
 * Created by bme on 2018/12/06.
 */

public abstract class RollWaveView extends WaveView {
    protected int dataNumInView; // View上一屏包含的数据点数

    protected final FixSizeLinkedList<Integer> viewData = new FixSizeLinkedList<>(1); //要显示数据缓冲

    protected OnRollWaveViewListener listener;

    public RollWaveView(Context context) {
        super(context);
    }

    public RollWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDataOnCanvas(canvas);
    }

    // 重置view
    // includeBackground: 是否重绘背景bitmap
    @Override
    public void resetView(boolean includeBackground)
    {
        super.resetView(includeBackground);

        setDataNumInView(viewWidth, pixelPerData);
    }

    @Override
    public void initWavePaint() {
        wavePaint.setStyle(Paint.Style.STROKE);
        super.initWavePaint();
    }

    public int getDataNumInView() {
        return dataNumInView;
    }

    public void setDataNumInView(int viewWidth, int pixelPerData) {
        dataNumInView = viewWidth/pixelPerData+1;
        synchronized (viewData) {
            viewData.setCapacity(dataNumInView);
        }
    }

    public void clearData() {
        synchronized (viewData) {
            viewData.clear();
        }
    }

    // 添加数据
    @Override
    public void addData(final int[] data, boolean show) {
        synchronized (viewData) {
            viewData.add(data[0]);
        }
        if(show) {
            showView();
        }
    }

    public void showView() {
        postInvalidate();
    }

    public void setListener(OnRollWaveViewListener listener) {
        this.listener = listener;
    }

    private void drawDataOnCanvas(Canvas canvas)
    {
        Path path = new Path();
        synchronized (viewData) {
            if (viewData.size() <= 1) {
                return;
            }

            int beginPos = dataNumInView - viewData.size();

            preX = initX + pixelPerData * beginPos;
            preY = initY - Math.round(viewData.get(0) / valuePerPixel);
            path.moveTo(preX, preY);
            for (int i = 1; i < viewData.size(); i++) {
                preX += pixelPerData;
                preY = initY - Math.round(viewData.get(i) / valuePerPixel);
                path.lineTo(preX, preY);
            }
        }

        canvas.drawPath(path, wavePaint);
    }
}
