package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.cmtech.android.bledevice.record.BleHrRecord10;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
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
 * HrStatisticProcessor: 心率直方图
 * Created by bme on 2019/1/9.
 */

public class HrHistogramChart extends BarChart {
    private static final List<Integer> BAR_COLORS = new ArrayList<>();
    private static final int TEXT_COLOR = Color.GRAY;

    private BarDataSet hrBarDataSet;
    private List<BarEntry> hrBarEntries = new ArrayList<>();
    private List<String> hrBarXStrings = new ArrayList<>();

    static {
        BAR_COLORS.add(Color.GRAY);
        BAR_COLORS.add(Color.GREEN);
        BAR_COLORS.add(Color.BLUE);
        BAR_COLORS.add(Color.YELLOW);
        BAR_COLORS.add(Color.MAGENTA);
        BAR_COLORS.add(Color.RED);
    }

    public HrHistogramChart(Context context) {
        super(context);
        initialize();
    }

    public HrHistogramChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public HrHistogramChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    // 更新
    public void update(List<BleHrRecord10.HrHistogramElement<Integer>> hrHistogram) {
        updateHrBarData(hrHistogram);
        hrBarDataSet.setValues(hrBarEntries);
        invalidate();
    }

    @Override
    public void invalidate() {
        BarData data = new BarData(hrBarDataSet);
        setData(data);
        super.invalidate();
    }

    private void initialize() {
        // description
        Description description = new Description();
        description.setText("");
        setDescription(description);
        description.setEnabled(false);

        // bar shadow
        setDrawBarShadow(false);

        // value above bar
        setDrawValueAboveBar(true);

        // pinch zoom
        setPinchZoom(false);

        //grid background
        setDrawGridBackground(false);

        setGridBackgroundColor(Color.WHITE);

        // X Axis
        XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new StringAxisValueFormatter(hrBarXStrings));

        YAxis leftAxis = getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setEnabled(false);

        YAxis rightAxis = getAxisRight();
        rightAxis.setAxisMinimum(0f);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setEnabled(false);

        Legend legend = getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextSize(20f);
        //显示位置
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);
        legend.setEnabled(true);

        setTouchEnabled(false);
        setNoDataText("");
        setNoDataTextColor(Color.GRAY);

        setBackgroundColor(Color.WHITE);
        setHighlightFullBarEnabled(false);

        //显示边框
        setDrawBorders(false);

        setScaleMinima(0.5f, 1.0f);

        //设置动画效果
        animateY(1000, Easing.Linear);
        animateX(1000, Easing.Linear);

        initBarDataSet(MyApplication.getStr(R.string.hr_histogram));
    }

    private void initBarDataSet(String legendString) {
        hrBarDataSet = new BarDataSet(hrBarEntries, legendString);

        hrBarDataSet.setColors(BAR_COLORS);
        hrBarDataSet.setValueTextSize(12f);
        hrBarDataSet.setValueTextColor(TEXT_COLOR);
        hrBarDataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return DateTimeUtil.secToTime((int)value);//String.format(Locale.getDefault(), "%d", (int)value);
            }
        });
    }

    private void updateHrBarData(List<BleHrRecord10.HrHistogramElement<Integer>> normHistogram) {
        hrBarXStrings.clear();
        hrBarEntries.clear();
        if(normHistogram != null && !normHistogram.isEmpty()) {
            int i = 0;
            for(BleHrRecord10.HrHistogramElement<Integer> ele : normHistogram) {
                hrBarXStrings.add(ele.getBarTitle());
                hrBarEntries.add(new BarEntry(i++, ele.getHistValue()));
            }
        }
    }


    private static class StringAxisValueFormatter implements IAxisValueFormatter {
        private List<String> values;

        StringAxisValueFormatter(List<String> values){
            this.values = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axisBase) {
            int index = (int) value;
            if (index < 0 || index >= values.size()) {
                return "";
            } else {
                return values.get(index);
            }
        }
    }
}
