package com.cmtech.android.bledevice.hrm.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.cmtech.android.bledeviceapp.R;
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

import static com.cmtech.android.bledevice.hrm.model.BleHrRecord10.HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH;


public class HrLineChart extends LineChart {
    private static final int DEFAULT_X_VALUE_INTERVAL = 10;

    public HrLineChart(Context context) {
        super(context);
        initialize();
    }

    public HrLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public HrLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public void setXAxisValueFormatter(final int interval) {
        XAxis xAxis = getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return DateTimeUtil.secToTime((int)value * interval);
            }
        });
    }

    private void initialize() {
        /***图表设置***/
        //是否展示网格线
        setDrawGridBackground(false);
        //是否显示边界
        setDrawBorders(false);
        //是否可以拖动
        setDragEnabled(false);
        //是否有触摸事件
        setTouchEnabled(false);
        //设置XY轴动画效果
        animateY(1000);
        animateX(1000);

        /***XY轴的设置***/
        XAxis xAxis = getXAxis();

        //X轴设置显示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return DateTimeUtil.secToTime((int)value* DEFAULT_X_VALUE_INTERVAL);
            }
        });

        xAxis.setLabelCount(4,false);


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

        /***折线图例 标签 设置***/
        Legend legend = getLegend();
        legend.setEnabled(false);
        //设置显示类型，LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看LegendForm 即可
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        //显示位置 左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);

        Description description = new Description();
        description.setEnabled(false);
        setDescription(description);
    }

    /**
     * 曲线初始化设置 一个LineDataSet 代表一条曲线
     *
     * @param lineDataSet 线条
     * @param color       线条颜色
     * @param mode
     */
    private void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode) {
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        //设置曲线值的圆点是实心还是空心
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(10f);
        //设置折线图填充
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormSize(15.f);
        if (mode == null) {
            //设置曲线展示为圆滑曲线（如果不设置则默认折线）
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            lineDataSet.setMode(mode);
        }
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);
    }

    /**
     * 展示曲线
     *
     * @param dataList 数据集合
     * @param name     曲线名称
     * @param color    曲线颜色
     */
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
        Drawable drawable = getResources().getDrawable(R.drawable.hr_linechart_fade);
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
        Drawable drawable = getResources().getDrawable(R.drawable.hr_linechart_fade);
        setChartFillDrawable(drawable);
    }

    /**
     * 设置线条填充背景颜色
     *
     * @param drawable
     */
    public void setChartFillDrawable(Drawable drawable) {
        if (getData() != null && getData().getDataSetCount() > 0) {
            LineDataSet lineDataSet = (LineDataSet)getData().getDataSetByIndex(0);
            //避免在 initLineDataSet()方法中 设置了 lineDataSet.setDrawFilled(false); 而无法实现效果
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillDrawable(drawable);
            invalidate();
        }
    }

}
