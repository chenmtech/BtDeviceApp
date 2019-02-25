package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.graphics.Path;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * ColorRollWaveView: 带颜色的卷轴滚动式的波形显示视图
 * Created by bme on 2019/2/26.
 */

public class ColorRollWaveView extends RollWaveView {
    private List<Integer> viewDataColor = new ArrayList<>(); //要显示的信号数据的颜色

    public ColorRollWaveView(Context context) {
        super(context);
    }

    public ColorRollWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void clearData() {
        super.clearData();
        viewDataColor.clear();
    }

    @Override
    public void addData(Integer data) {
        addData(data, DEFAULT_WAVE_COLOR);
    }

    public void addData(Integer data, int color) {
        super.addData(data);
        viewDataColor.add(color);
    }

    @Override
    public synchronized void showData(List<Integer> data) {
        for(Integer d : data) {
            addData(d);
        }
        drawDataOnForeCanvas();
        invalidate();
    }

    public synchronized void showData(List<Integer> data, List<Integer> colors) {
        for(int i = 0; i < data.size(); i++) {
            addData(data.get(i), colors.get(i));
        }
        drawDataOnForeCanvas();
        invalidate();
    }

    @Override
    protected boolean drawDataOnForeCanvas()
    {
        foreCanvas.drawBitmap(backBitmap, 0, 0, null);

        Integer[] data = viewData.toArray(new Integer[0]);
        int dataNum = data.length;
        if(dataNum <= 1) return true;

        int begin = dataNum - dataNumXDirection;
        if(begin < 0) {
            begin = 0;
        }

        viewData.clear();
        viewData.add(data[begin]);
        preX = initX;
        preY = initY - Math.round(data[begin]/yRes);
        Path path = new Path();
        path.moveTo(preX, preY);
        for(int i = begin+1; i < dataNum; i++) {
            viewData.add(data[i]);
            preX += xRes;
            preY = initY - Math.round(data[i]/yRes);
            path.lineTo(preX, preY);
        }

        foreCanvas.drawPath(path, wavePaint);
        return true;
    }
}
