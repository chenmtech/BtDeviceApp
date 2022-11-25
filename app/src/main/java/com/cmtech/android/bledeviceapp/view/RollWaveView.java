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

import java.util.ArrayList;
import java.util.List;

/**
 * RollWaveView: 卷轴滚动式的波形显示视图
 * Created by bme on 2018/12/06.
 */

public abstract class RollWaveView extends WaveView {
    protected int dataNumInView; // View一屏包含的数据点数

    protected final List<FixSizeLinkedList<Integer>> viewData; //要显示的波形数据缓冲

    protected OnRollWaveViewListener listener;

    public RollWaveView(Context context) {
        super(context);

        viewData = new ArrayList<>(1);
        viewData.add(new FixSizeLinkedList<>(1));
    }

    public RollWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);

        viewData = new ArrayList<>(waveNum);
        for(int i = 0; i < waveNum; i++)
            viewData.add(new FixSizeLinkedList<>(1));
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
        for(Paint p : wavePaints)
            p.setStyle(Paint.Style.STROKE);

        super.initWavePaint();
    }

    public int getDataNumInView() {
        return dataNumInView;
    }

    public void setDataNumInView(int viewWidth, int pixelPerData) {
        dataNumInView = viewWidth/pixelPerData+1;
        synchronized (viewData) {
            for(int i = 0; i < waveNum; i++)
                viewData.get(i).setCapacity(dataNumInView);
        }
    }

    public void clearData() {
        synchronized (viewData) {
            for(int i = 0; i < waveNum; i++)
                viewData.get(i).clear();
        }
    }

    // 添加数据
    @Override
    public void addData(final int[] data, boolean show) {
        synchronized (viewData) {
            for(int i = 0; i < waveNum; i++)
                viewData.get(i).add(data[i]);
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
        synchronized (viewData) {
            for(int i = 0; i < waveNum; i++) {
                FixSizeLinkedList<Integer> oneWaveData = viewData.get(i);
                if (oneWaveData.size() <= 1) {
                    return;
                }
                Path path = new Path();
                int beginPos = dataNumInView - oneWaveData.size();

                preX = initX + pixelPerData * beginPos;
                preYs[i] = initYs[i] - Math.round(oneWaveData.get(0) / valuePerPixel[i]);
                path.moveTo(preX, preYs[i]);
                for (int j = 1; j < oneWaveData.size(); j++) {
                    preX += pixelPerData;
                    preYs[i] = initYs[i] - Math.round(oneWaveData.get(j) / valuePerPixel[i]);
                    path.lineTo(preX, preYs[i]);
                }
                canvas.drawPath(path, wavePaints[i]);
            }
        }
    }
}
