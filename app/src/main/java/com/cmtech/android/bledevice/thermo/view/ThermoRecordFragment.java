package com.cmtech.android.bledevice.thermo.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.view.MyLineChart;
import com.cmtech.android.bledeviceapp.R;

import java.util.ArrayList;
import java.util.List;

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
public class ThermoRecordFragment extends Fragment {
    public static final String TITLE = "体温记录";
    private MyLineChart lineChart; // heart rate line chart
    private ImageButton ibStart, ibStop;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_record_thermo_temp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChart = view.findViewById(R.id.line_chart);
        lineChart.setXAxisValueFormatter(2);
        lineChart.showFloatLineChart(new ArrayList<Float>(), getResources().getString(R.string.thermo_linechart), Color.BLUE);

        TextView tvYUnit = view.findViewById(R.id.line_chart_y_unit);
        tvYUnit.setText(R.string.temperature);

        ibStart = view.findViewById(R.id.ib_record_start);
        ibStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ThermoFragment)getParentFragment()).setThermoRecord(true);
            }
        });
        ibStop = view.findViewById(R.id.ib_record_stop);
        ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ThermoFragment)getParentFragment()).setThermoRecord(false);
            }
        });
    }

    public void updateThermoLineChart(List<Float> thermo) {
        lineChart.showFloatLineChart(thermo, getResources().getString(R.string.thermo_linechart), Color.BLUE);
    }

    public void updateThermoRecordStatus(boolean isRecord) {
        if(isRecord) {
            ibStart.setVisibility(View.INVISIBLE);
            ibStop.setVisibility(View.VISIBLE);
        } else {
            ibStart.setVisibility(View.VISIBLE);
            ibStop.setVisibility(View.INVISIBLE);
        }
    }

}
