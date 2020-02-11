package com.cmtech.android.bledevice.hrmonitor.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrmonitor.model.BleHeartRateData;
import com.cmtech.android.bledevice.hrmonitor.model.HRMonitorDevice;
import com.cmtech.android.bledevice.hrmonitor.model.IHRMonitorDeviceListener;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.activity.MainActivity;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.view
 * ClassName:      HRMonitorFragment
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020-02-04 06:06
 * UpdateUser:     更新者
 * UpdateDate:     2020-02-04 06:06
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HRMonitorFragment extends DeviceFragment implements IHRMonitorDeviceListener {
    private HRMonitorDevice device;

    private TextView tvHeartRate;
    private TextView tvHRStatus;
    private EditText etSensLoc;
    private EditText etHRMeas;

    public HRMonitorFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (HRMonitorDevice) getDevice();

        return inflater.inflate(R.layout.fragment_hrmonitor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvHeartRate = view.findViewById(R.id.tv_heart_rate);
        tvHRStatus = view.findViewById(R.id.tv_heart_rate_status);
        etHRMeas = view.findViewById(R.id.et_hr_meas);
        etSensLoc = view.findViewById(R.id.et_sens_loc);

        device.setListener(this);

        // 打开设备
        MainActivity activity = (MainActivity) getActivity();
        device.open(activity.getNotifyService());
    }

    @Override
    public void openConfigureActivity() {

    }

    @Override
    public void onHRMeasureUpdated(final BleHeartRateData hrData) {
        if(hrData != null && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    etHRMeas.setText(hrData.toString());
                    int bpm = hrData.getBpm();
                    tvHeartRate.setText(""+bpm);
                    if(bpm <= 30) {
                        tvHRStatus.setText("心率过低");
                    } else if(bpm >= 190) {
                        tvHRStatus.setText("心率过快");
                    } else {
                        tvHRStatus.setText("心率正常");
                    }
                }
            });
        }
    }

    @Override
    public void onHRSensLocUpdated(final int loc) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    etSensLoc.setText(String.valueOf(loc));
                }
            });
        }
    }

    @Override
    public void onHRCtrlPtUpdated(final int ctrl) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if(device != null)
            device.removeListener();
    }
}
