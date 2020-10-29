package com.cmtech.android.bledevice.hrm.activityfragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.activity
 * ClassName:      HrDebugFragment
 * Description:    hr debug Fragment
 * Author:         chenm
 * CreateDate:     2020-03-06 上午5:40
 * UpdateUser:     更新者
 * UpdateDate:     2020-03-06 上午5:40
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HrDebugFragment extends Fragment {
    public static final String TITLE = "心率调试";
    private TextView tvSensLoc; // sensor location
    private TextView tvHRMeas; // HR measurement data

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_hrm_debug, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvHRMeas = view.findViewById(R.id.tv_hr_meas);
        tvSensLoc = view.findViewById(R.id.tv_sens_loc);
    }

    public void updateHrMeas(String hrMeas) {
        tvHRMeas.setText(hrMeas);
    }

    public void updateHrSensLoc(String sensLoc) {
        tvSensLoc.setText(sensLoc);
    }
}
