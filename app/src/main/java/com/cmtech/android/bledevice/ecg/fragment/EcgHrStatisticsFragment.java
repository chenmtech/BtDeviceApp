package com.cmtech.android.bledevice.ecg.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.cmtech.android.bledevice.ecg.process.hr.HrStatisticsInfo;
import com.cmtech.android.bledevice.ecg.view.EcgHrLineChart;
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
public class EcgHrStatisticsFragment extends Fragment {
    public static final String TITLE = "心率统计";
    private EditText etAverageHr; // 平均心率
    private EditText etMaxHr; // 最大心率
    private EcgHrLineChart hrLineChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_record_hrm_hr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etMaxHr = view.findViewById(R.id.et_hr_max_value);
        etMaxHr.setText("0");
        etAverageHr = view.findViewById(R.id.et_hr_ave_value);
        etAverageHr.setText("0");
        hrLineChart = view.findViewById(R.id.hr_line_chart);
    }

    public void updateHrInfo(HrStatisticsInfo hrInfoObject) {
        etAverageHr.setText(String.valueOf(hrInfoObject.getAverageHr()));
        etMaxHr.setText(String.valueOf(hrInfoObject.getMaxHr()));
        hrLineChart.showLineChart(hrInfoObject.getFilteredHrList(), "心率变化图", Color.BLUE);
    }
}
