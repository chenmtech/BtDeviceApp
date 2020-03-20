package com.cmtech.android.bledevice.hrmonitor.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrmonitor.model.HrStatisticsInfo;
import com.cmtech.android.bledeviceapp.R;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.activity
 * ClassName:      HrSequenceFragment
 * Description:    Ecg心率统计Fragment
 * Author:         chenm
 * CreateDate:     2019/4/15 上午5:40
 * UpdateUser:     更新者
 * UpdateDate:     2019/4/15 上午5:40
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HrTimeFragment extends Fragment {
    public static final String TITLE = "心率变化";
    private TextView tvHrAve; // average heart rate value
    private TextView tvHrMax; // max heart rate value
    private HrLineChart hrLineChart; // heart rate line chart

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_hr_sequence, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvHrMax = view.findViewById(R.id.tv_hr_max_value);
        tvHrMax.setText("0");
        tvHrAve = view.findViewById(R.id.tv_hr_ave_value);
        tvHrAve.setText("0");
        hrLineChart = view.findViewById(R.id.linechart_hr);
    }

    public void updateHrInfo(HrStatisticsInfo hrInfoObject) {
        tvHrAve.setText(String.valueOf(hrInfoObject.getHrAve()));
        tvHrMax.setText(String.valueOf(hrInfoObject.getHrMax()));
        hrLineChart.showLineChart(hrInfoObject.getHrAveList(), TITLE, Color.BLUE);
    }
}
