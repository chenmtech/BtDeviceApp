package com.cmtech.android.bledevice.ecgmonitor.model;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * EcgHrProcessor: 心率直方图
 * Created by bme on 2019/1/9.
 */

public class EcgHrHistogram extends BarChart {
    private BarDataSet hrBarDateSet;
    private List<BarEntry> hrBarEntries = new ArrayList<>();
    private List<String> hrBarXStrings = new ArrayList<>();

    public EcgHrHistogram(Context context) {
        super(context);
        initialize();
    }

    public EcgHrHistogram(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public EcgHrHistogram(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    // 更新
    public void update(double[] hrHistogram) {
        updateHrBarData(hrHistogram);
        hrBarDateSet.setValues(hrBarEntries);
        invalidate();
    }

    @Override
    public void invalidate() {
        BarData data = new BarData(hrBarDateSet);
        setData(data);
        super.invalidate();
    }


    private void initialize() {
        /***图表设置***/
        //背景颜色
        setBackgroundColor(Color.WHITE);
        //不显示图表网格
        setDrawGridBackground(false);
        //背景阴影
        setDrawBarShadow(false);
        setHighlightFullBarEnabled(false);
        //显示边框
        setDrawBorders(true);
        //设置动画效果
        animateY(1000, Easing.Linear);
        animateX(1000, Easing.Linear);

        /***XY轴的设置***/
        //X轴设置显示位置在底部
        XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setDrawAxisLine(false);
        xAxis.setValueFormatter(new StringAxisValueFormatter(hrBarXStrings));

        YAxis leftAxis = getAxisLeft();
        YAxis rightAxis = getAxisRight();
        //保证Y轴从0开始，不然会上移一点
        leftAxis.setAxisMinimum(0f);
        rightAxis.setAxisMinimum(0f);
        leftAxis.setDrawAxisLine(false);
        rightAxis.setDrawAxisLine(false);

        /***折线图例 标签 设置***/
        Legend legend = getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(11f);
        //显示位置
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);

        Description description = new Description();
        description.setEnabled(false);
        setDescription(description);

        setDrawValueAboveBar(true);

        //updateHrBarData(null);

        initBarDataSet("心率值统计", Color.BLUE, Color.BLACK);
    }

    /**
     * 柱状图初始化设置 一个BarDataSet 代表一列柱状图
     *
     * @param barColor      柱状图颜色
     */
    private void initBarDataSet(String legendString, int barColor, int dataColor) {
        hrBarDateSet = new BarDataSet(hrBarEntries, legendString);

        hrBarDateSet.setColor(barColor);
        hrBarDateSet.setFormLineWidth(1f);
        hrBarDateSet.setFormSize(15.f);
        hrBarDateSet.setDrawValues(true);
        hrBarDateSet.setValueTextSize(14f);
        hrBarDateSet.setValueTextColor(dataColor);
        hrBarDateSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return String.format("%d%%", (int)(value*100));
            }
        });
    }

    private void updateHrBarData(double[] hrData) {
        hrBarXStrings.clear();
        hrBarEntries.clear();
        if(hrData == null) {
            hrBarXStrings.add("无有效心率");
            hrBarEntries.add(new BarEntry(0, 0.0f));
        } else {
            for (int i = 0; i < hrData.length; i++) {
                String key = String.valueOf(i * 10) + '-' + String.valueOf((i + 1) * 10);
                hrBarXStrings.add(key);
                hrBarEntries.add(new BarEntry(i, (float) hrData[i]));
            }
        }
    }


    private class StringAxisValueFormatter implements IAxisValueFormatter {
        private List<String> mStrs;
        /**
             * 对字符串类型的坐标轴标记进行格式化
             * @param strs
             */
        StringAxisValueFormatter(List<String> strs){
            this.mStrs =strs;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axisBase) {
            int index = (int) value;
            if (index < 0 || index >= mStrs.size()) {
                return "";
            } else {
                return mStrs.get(index);
            }
        }
    }
}
