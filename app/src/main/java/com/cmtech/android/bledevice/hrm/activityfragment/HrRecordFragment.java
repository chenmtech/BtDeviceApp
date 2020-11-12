package com.cmtech.android.bledevice.hrm.activityfragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.view.MyLineChart;
import com.cmtech.android.bledeviceapp.R;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.hrm.model.HrmDevice.INVALID_HEART_RATE;
import static com.cmtech.android.bledeviceapp.data.record.BleHrRecord.HR_MA_FILTER_SPAN;

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
public class HrRecordFragment extends Fragment {
    public static final int TITLE_ID = R.string.hr_record;
    private EditText etHrAve; // average heart rate value
    private EditText etHrMax; // max heart rate value
    private MyLineChart hrLineChart; // heart rate line chart
    private ImageButton ibRecordCtrl;
    private TextView tvRecordStatus;
    private boolean recording = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_record_hrm_hr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etHrMax = view.findViewById(R.id.et_hr_max_value);
        etHrAve = view.findViewById(R.id.et_hr_ave_value);
        hrLineChart = view.findViewById(R.id.hr_line_chart);
        hrLineChart.setXAxisValueFormatter(HR_MA_FILTER_SPAN);
        updateHrInfo(new ArrayList<>(), INVALID_HEART_RATE, INVALID_HEART_RATE);

        TextView tvYUnit = view.findViewById(R.id.line_chart_y_unit);
        tvYUnit.setText(R.string.BPM);

        tvRecordStatus = view.findViewById(R.id.tv_record_status);
        ibRecordCtrl = view.findViewById(R.id.ib_record_control);
        ibRecordCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert getParentFragment() != null;
                ((HrmFragment)getParentFragment()).setHrRecord(!recording);
            }
        });
    }

    public void updateHrInfo(List<Short> hrList, short hrMax, short hrAve) {
        if(hrMax <= 0)
            etHrMax.setText(R.string.ellipsis);
        else
            etHrMax.setText(String.valueOf(hrMax));

        if(hrAve <= 0)
            etHrAve.setText(R.string.ellipsis);
        else
            etHrAve.setText(String.valueOf(hrAve));

        hrLineChart.showShortLineChart(hrList, getResources().getString(R.string.hr_linechart), Color.BLUE);
    }

    public void updateRecordStatus(boolean record) {
        Drawable drawable;
        if(record) {
            drawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_stop_32px);
            tvRecordStatus.setText("停止记录");
        } else {
            drawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_start_32px);
            tvRecordStatus.setText("开始记录");
        }
        ibRecordCtrl.setImageDrawable(drawable);
        recording = record;
    }
}
