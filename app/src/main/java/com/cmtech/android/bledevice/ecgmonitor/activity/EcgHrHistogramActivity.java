package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
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
import java.util.Arrays;
import java.util.List;

public class EcgHrHistogramActivity extends AppCompatActivity {
    private BarChart hrBarHistogram;
    private YAxis leftAxis;             //左侧Y轴
    private YAxis rightAxis;            //右侧Y轴
    private XAxis xAxis;                //X轴
    private Legend legend;              //图例

    private TextView tvHrTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecghr_histogram);

        Intent intent = getIntent();
        if(intent == null) { finish(); return;}
        int[] hrHistogram = intent.getIntArrayExtra("hr_histogram");
        if(hrHistogram == null) { finish(); return;}

        hrBarHistogram = findViewById(R.id.bc_hr_histogram);

        initBarChart(hrBarHistogram);

        //设置数据和X轴显示字符串
        List<Float> dataList = new ArrayList<>();
        List<String> xStrList = new ArrayList<>();
        dataList.add((float)(hrHistogram[0]+hrHistogram[1]+hrHistogram[2]));
        xStrList.add("30以下");
        for (int i = 3; i < hrHistogram.length-1; i++) {
            if(hrHistogram[i] > 5) {
                dataList.add((float) hrHistogram[i]);
                xStrList.add(String.valueOf(i * 10) + '-' + String.valueOf((i + 1) * 10));
            }
        }
        dataList.add((float)hrHistogram[hrHistogram.length-1]);
        xStrList.add("200以上");

        showBarChart(dataList, xStrList, "心率统计次数", Color.BLUE, Color.RED);

        int sum = 0;
        for(int num : hrHistogram) {
            sum += num;
        }
        tvHrTotal = findViewById(R.id.tv_ecghr_totaltimes);
        tvHrTotal.setText(String.valueOf(sum));
    }

    private void initBarChart(BarChart barChart) {
        /***图表设置***/
        //背景颜色
        barChart.setBackgroundColor(Color.WHITE);
        //不显示图表网格
        barChart.setDrawGridBackground(false);
        //背景阴影
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        //显示边框
        barChart.setDrawBorders(true);
        //设置动画效果
        barChart.animateY(1000, Easing.Linear);
        barChart.animateX(1000, Easing.Linear);

        /***XY轴的设置***/
        //X轴设置显示位置在底部
        xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);

        leftAxis = barChart.getAxisLeft();
        rightAxis = barChart.getAxisRight();
        //保证Y轴从0开始，不然会上移一点
        leftAxis.setAxisMinimum(0f);
        rightAxis.setAxisMinimum(0f);
        xAxis.setDrawAxisLine(false);
        leftAxis.setDrawAxisLine(false);
        rightAxis.setDrawAxisLine(false);

        /***折线图例 标签 设置***/
        legend = barChart.getLegend();
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
        barChart.setDescription(description);

        barChart.setDrawValueAboveBar(true);

    }

    /**
     * 柱状图始化设置 一个BarDataSet 代表一列柱状图
     *
     * @param barDataSet 柱状图
     * @param barColor      柱状图颜色
     */
    private void initBarDataSet(BarDataSet barDataSet, int barColor, int dataColor) {
        barDataSet.setColor(barColor);
        barDataSet.setFormLineWidth(1f);
        barDataSet.setFormSize(15.f);
        //不显示柱状图顶部值
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(14f);
        barDataSet.setValueTextColor(dataColor);
        barDataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) value);
            }
        });
    }

    public void showBarChart(List<Float> dateValueList, final List<String> xStrList, String name, int barColor, int dataColor) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < dateValueList.size(); i++) {
            BarEntry barEntry = new BarEntry(i, dateValueList.get(i));
            entries.add(barEntry);
        }
        // 每一个BarDataSet代表一类柱状图
        BarDataSet barDataSet = new BarDataSet(entries, name);
        initBarDataSet(barDataSet, barColor, dataColor);

        //X轴自定义值
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xStrList.get((int) value);
            }
        });

        BarData data = new BarData(barDataSet);
        hrBarHistogram.setData(data);
    }

}
