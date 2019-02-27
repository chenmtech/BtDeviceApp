package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * ColorRollWaveView: 带标记颜色的卷轴滚动式的波形显示视图
 * Created by bme on 2019/2/26.
 */

public class ColorRollWaveView extends RollWaveView {
    protected static final int MARKED_WAVE_COLOR = Color.WHITE; // 缺省的标记波形颜色

    private List<Boolean> markerList = new ArrayList<>(); //要显示的信号数据是否处于标记中

    public ColorRollWaveView(Context context) {
        super(context);
    }

    public ColorRollWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void clearData() {
        super.clearData();
        markerList.clear();
    }

    @Override
    public void addData(Integer data) {
        addData(data, false);
    }

    public void addData(Integer data, boolean isMarked) {
        super.addData(data);
        markerList.add(isMarked);
    }

    @Override
    public synchronized void showData(List<Integer> data) {
        for(Integer d : data) {
            addData(d);
        }
        drawDataOnForeCanvas();
        invalidate();
    }

    public synchronized void showData(List<Integer> data, List<Boolean> markers) {
        viewData.addAll(data);
        markerList.addAll(markers);

        drawDataOnForeCanvas();
        invalidate();
    }

    @Override
    protected boolean drawDataOnForeCanvas()
    {
        foreCanvas.drawBitmap(backBitmap, 0, 0, null);

        Integer[] data = viewData.toArray(new Integer[0]);
        Boolean[] markers = markerList.toArray(new Boolean[0]);
        int dataNum = data.length;
        if(dataNum <= 1) return true;

        int begin = dataNum - dataNumXDirection;
        if(begin < 0) {
            begin = 0;
        }

        clearData();
        addData(data[begin], markers[begin]);
        preX = initX;
        preY = initY - Math.round(data[begin]/yRes);
        Path path = new Path();
        path.moveTo(preX, preY);
        wavePaint.setColor((markers[begin]) ? MARKED_WAVE_COLOR : DEFAULT_WAVE_COLOR);
        for(int i = begin+1; i < dataNum; i++) {
            addData(data[i], markers[i]);
            preX += xRes;
            preY = initY - Math.round(data[i]/yRes);
            path.lineTo(preX, preY);
            if(markers[i] != markers[i-1]) {
                foreCanvas.drawPath(path, wavePaint);
                path = new Path();
                path.moveTo(preX, preY);
                wavePaint.setColor((markers[i]) ? MARKED_WAVE_COLOR : DEFAULT_WAVE_COLOR);
            }
        }
        foreCanvas.drawPath(path, wavePaint);

        return true;
    }
}
