package com.cmtech.android.bledevice.ecgmonitor.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.EcgHrStatisticsInfoAnalyzer;
import com.cmtech.android.bledeviceapp.R;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.activity
 * ClassName:      EcgHrStatisticsFragment
 * Description:    Ecg心率统计Fragment
 * Author:         chenm
 * CreateDate:     2019/4/15 上午5:40
 * UpdateUser:     更新者
 * UpdateDate:     2019/4/15 上午5:40
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class EcgHrStatisticsFragment extends Fragment {
    private TextView tvAverageHr; // 平均心率
    private TextView tvMaxHr; // 最大心率
    private EcgHrLineChart hrLineChart;
    private EcgMonitorDevice device;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_hr_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAverageHr = view.findViewById(R.id.tv_average_hr_value);
        tvMaxHr = view.findViewById(R.id.tv_max_hr_value);
        hrLineChart = view.findViewById(R.id.linechart_hr);
    }

    public void updateHrInfo(EcgHrStatisticsInfoAnalyzer hrInfoObject) {
        tvMaxHr.setText(String.valueOf(hrInfoObject.getMaxHr()));
        tvAverageHr.setText(String.valueOf(hrInfoObject.getAverageHr()));
        hrLineChart.showLineChart(hrInfoObject.getFilteredHrList(), "心率变化图", Color.BLUE);
    }
}
