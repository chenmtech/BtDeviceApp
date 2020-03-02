package com.cmtech.android.bledevice.hrmonitor.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrmonitor.model.BleHeartRateData;
import com.cmtech.android.bledevice.hrmonitor.model.HRMonitorDevice;
import com.cmtech.android.bledevice.hrmonitor.model.HrStatisticsInfo;
import com.cmtech.android.bledevice.hrmonitor.model.OnHRMonitorDeviceListener;
import com.cmtech.android.bledevice.view.OnWaveViewListener;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.activity.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cmtech.android.bledevice.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

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
public class HRMonitorFragment extends DeviceFragment implements OnHRMonitorDeviceListener, OnWaveViewListener {
    private static final int STANDARD_VALUE_1MV_AFTER_CALIBRATION = 65535; // 定标后标准的1mV值

    private HRMonitorDevice device;

    private ScanEcgView ecgView; // 心电波形View
    private TextView tvHeartRate;
    private TextView tvPauseMessage; // 暂停显示消息
    private final EcgHrStatisticsFragment hrFragment = new EcgHrStatisticsFragment(); // 心率统计Fragment
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
        tvPauseMessage = view.findViewById(R.id.tv_pause_message);
        ecgView = view.findViewById(R.id.scanview_ecg);
        etHRMeas = view.findViewById(R.id.et_hr_meas);
        etSensLoc = view.findViewById(R.id.et_sens_loc);


        initialEcgView();
        ViewPager pager = view.findViewById(R.id.vp_ecg_control_panel);
        TabLayout layout = view.findViewById(R.id.tl_ecg_control_panel);
        List<Fragment> fragmentList = new ArrayList<Fragment>(Arrays.asList(hrFragment));
        List<String> titleList = new ArrayList<>(Arrays.asList(EcgHrStatisticsFragment.TITLE));
        EcgCtrlPanelAdapter fragAdapter = new EcgCtrlPanelAdapter(getChildFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(2);
        layout.setupWithViewPager(pager);

        device.setListener(this);
        ecgView.setListener(this);

        // 打开设备
        MainActivity activity = (MainActivity) getActivity();
        device.open(activity.getNotifyService());
    }

    private void initialEcgView() {
        ecgView.setup(125, STANDARD_VALUE_1MV_AFTER_CALIBRATION, DEFAULT_ZERO_LOCATION);
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
    public void onEcgViewSetup(int sampleRate, int value1mV, double zeroLocation) {
        ecgView.setup(sampleRate, value1mV, zeroLocation);
    }

    @Override
    public void onEcgSignalUpdated(final int ecgSignal) {
        ecgView.showData(ecgSignal);
    }

    @Override
    public void onHrStatisticsInfoUpdated(final HrStatisticsInfo hrStatisticsInfo) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hrFragment.updateHrInfo(hrStatisticsInfo);
                }
            });
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if(device != null)
            device.removeListener();

        ecgView.stop();
    }

    @Override
    public void onShowStateUpdated(boolean isShow) {
        if(isShow) {
            tvPauseMessage.setVisibility(View.GONE);
        } else {
            tvPauseMessage.setVisibility(View.VISIBLE);
        }
    }
}
