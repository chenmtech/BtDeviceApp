package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.DeviceType;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;


public class MyLineChart extends LineChart {
    private static final int DEFAULT_X_VALUE_INTERVAL = 10;

    private String legendString;

    public MyLineChart(Context context) {
        super(context);
        initialize();
    }

    public MyLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public MyLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public void setXAxisValueFormatter(final int interval) {
        XAxis xAxis = getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return DateTimeUtil.secToMinute((int)value * interval);
            }
        });
    }

    private void initialize() {
        //border
        setDrawBorders(false);

        // grid background
        setDrawGridBackground(false);

        //drag
        setDragEnabled(false);

        //touch
        setTouchEnabled(false);

        animateY(1000);
        animateX(1000);

        XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(4,false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftYAxis = getAxisLeft();
        YAxis rightYaxis = getAxisRight();

        //保证Y轴从0开始，不然会上移一点
        leftYAxis.setAxisMinimum(30f);
        rightYaxis.setAxisMinimum(30f);

        leftYAxis.setDrawGridLines(true);
        rightYaxis.setDrawGridLines(false);

        leftYAxis.enableGridDashedLine(10f, 10f, 0f);
        rightYaxis.setEnabled(false);

        leftYAxis.setLabelCount(4);

        Legend legend = getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setEnabled(true);

        Description description = new Description();
        setDescription(description);
        description.setEnabled(false);
    }

    public void showShortLineChart(List<Short> dataList, String name, int color) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            float data = dataList.get(i);
            Entry entry = new Entry(i, data);
            entries.add(entry);
        }
        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.LINEAR);
        LineData lineData = new LineData(lineDataSet);
        setData(lineData);
        setNoDataText("");
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.hr_linechart_fade);
        setChartFillDrawable(drawable);
    }

    public void showFloatLineChart(List<Float> dataList, String name, int color) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            float data = dataList.get(i);
            Entry entry = new Entry(i, data);
            entries.add(entry);
        }
        // 每一个LineDataSet代表一条线
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.LINEAR);
        LineData lineData = new LineData(lineDataSet);
        setData(lineData);
        setNoDataText("");
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.hr_linechart_fade);
        setChartFillDrawable(drawable);
    }

    private void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode) {
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormSize(15.f);
        if (mode == null) {
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            lineDataSet.setMode(mode);
        }
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);
    }

    private void setChartFillDrawable(Drawable drawable) {
        if (getData() != null && getData().getDataSetCount() > 0) {
            LineDataSet lineDataSet = (LineDataSet)getData().getDataSetByIndex(0);
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillDrawable(drawable);
            invalidate();
        }
    }

}
