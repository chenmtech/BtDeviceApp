package com.cmtech.android.bledevice.hrm.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.ble.core.DeviceState;
import com.cmtech.android.bledevice.hrm.model.BleHeartRateData;
import com.cmtech.android.bledevice.record.BleHrRecord10;
import com.cmtech.android.bledevice.hrm.model.HrmDevice;
import com.cmtech.android.bledevice.hrm.model.HrmCfg;
import com.cmtech.android.bledevice.hrm.model.OnHrmListener;
import com.cmtech.android.bledevice.view.OnWaveViewListener;
import com.cmtech.android.bledevice.view.ScanEcgView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.cmtech.android.bledeviceapp.adapter.CtrlPanelAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.cmtech.android.bledevice.hrm.view.HrmCfgActivity.RESULT_ECG_LOCK_CHANGED;
import static com.cmtech.android.bledevice.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.view
 * ClassName:      HrmFragment
 * Description:    heart rate monitor fragment
 * Author:         chenm
 * CreateDate:     2020-02-04 06:06
 * UpdateUser:     chenm
 * UpdateDate:     2020-02-04 06:06
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HrmFragment extends DeviceFragment implements OnHrmListener, OnWaveViewListener {
    private HrmDevice device; // device

    private ScanEcgView ecgView; // EcgView
    private TextView tvHrWhenEcgOff; // hr when ecg off
    private TextView tvHrWhenEcgOn; // hr when ecg on
    private TextView tvMessage; // message
    private TextView tvEcgSwitch;
    private FrameLayout flWhenEcgOff; // frame layout when ecg off
    private FrameLayout flWhenEcgOn; // frame layout when ecg on

    private ViewPager pager;
    private CtrlPanelAdapter fragAdapter;
    private final HrDebugFragment debugFrag = new HrDebugFragment(); // debug fragment
    private final HrRecordFragment hrRecFrag = new HrRecordFragment(); // heart rate record Fragment
    private final EcgRecordFragment ecgRecFrag = new EcgRecordFragment(); // ecg record fragment

    private boolean isEcgOn = false;

    public HrmFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (HrmDevice) getDevice();
        device.setContext(getContext());
        return inflater.inflate(R.layout.fragment_device_hrm, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvHrWhenEcgOff = view.findViewById(R.id.tv_hr_ecg_off);
        tvHrWhenEcgOn = view.findViewById(R.id.tv_hr_ecg_on);
        tvMessage = view.findViewById(R.id.tv_message);

        flWhenEcgOff = view.findViewById(R.id.fl_ecg_off);
        flWhenEcgOff.setVisibility(View.VISIBLE);
        flWhenEcgOn = view.findViewById(R.id.fl_ecg_on);
        flWhenEcgOn.setVisibility(View.GONE);

        ecgView = view.findViewById(R.id.ecg_view);
        ecgView.setup(device.getSampleRate(), device.getCaliValue(), DEFAULT_ZERO_LOCATION);

        tvEcgSwitch = view.findViewById(R.id.tv_switch_ecg);
        tvEcgSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(device != null && device.getState() == DeviceState.CONNECT) {
                    device.setEcgOn(!isEcgOn);
                }
            }
        });

        pager = view.findViewById(R.id.hrm_control_panel_viewpager);
        TabLayout layout = view.findViewById(R.id.hrm_control_panel_tab);
        List<Fragment> fragmentList = new ArrayList<Fragment>(Arrays.asList(hrRecFrag));
        String title = getResources().getString(HrRecordFragment.TITLE_ID);
        List<String> titleList = new ArrayList<>(Arrays.asList(title));
        fragAdapter = new CtrlPanelAdapter(getChildFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(3);
        layout.setupWithViewPager(pager);

        device.setListener(this);
        ecgView.setListener(this);

        // 打开设备
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null)
            device.open(activity.getNotiService());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1) { // cfg return
            if(resultCode == RESULT_ECG_LOCK_CHANGED) {
                if(device.getState() != DeviceState.CONNECT) {
                    Toast.makeText(getContext(), R.string.cannot_change_ecg_lock, Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean ecgLock = data.getBooleanExtra("ecg_lock", true);
                device.setEcgLock(ecgLock);
            } else if(resultCode == RESULT_OK) {
                HrmCfg cfg = (HrmCfg) data.getSerializableExtra("hr_cfg");
                device.updateConfig(cfg);
            }
        }
    }

    @Override
    public void openConfigureActivity() {
        HrmCfg cfg = device.getConfig();

        Intent intent = new Intent(getActivity(), HrmCfgActivity.class);
        intent.putExtra("ecg_lock", device.isEcgLock());
        intent.putExtra("hr_cfg", cfg);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onHRUpdated(final BleHeartRateData hrData) {
        if(hrData != null && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //debugFrag.updateHrMeas(hrData.toString());

                    int bpm = hrData.getBpm();
                    tvHrWhenEcgOn.setText(String.valueOf(bpm));
                    tvHrWhenEcgOff.setText(String.valueOf(bpm));

                    HrmCfg cfg = device.getConfig();
                    if(cfg.isWarn()) {
                        String warnStr = null;
                        if(bpm > cfg.getHrHigh())
                            warnStr = getResources().getString(R.string.hr_too_high);
                        else if(bpm < cfg.getHrLow()) {
                            warnStr = getResources().getString(R.string.hr_too_low);
                        }
                        if(warnStr != null)
                            warnUsingTTS(warnStr);
                    }
                }
            });
        }
    }

    @Override
    public void onHRStatisticInfoUpdated(final List<Short> hrList, final short hrMax, final short hrAve, List<BleHrRecord10.HrHistogramElement<Integer>> hrHistogram) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hrRecFrag.updateHrInfo(hrList, hrMax, hrAve);
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
                    //debugFrag.updateHrSensLoc(String.valueOf(loc));
                }
            });
        }
    }

    @Override
    public void onHRCtrlPtUpdated(final int ctrl) {

    }

    @Override
    public void onFragmentUpdated(final int sampleRate, final int value1mV, final double zeroLocation, final boolean ecgLock) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ecgView.setup(sampleRate, value1mV, zeroLocation);
                    if(ecgLock) {
                        tvEcgSwitch.setVisibility(View.GONE);
                        flWhenEcgOff.setVisibility(View.VISIBLE);
                        flWhenEcgOn.setVisibility(View.GONE);
                        ecgView.stop();
                        fragAdapter.removeFragment(ecgRecFrag);
                        pager.setCurrentItem(fragAdapter.getCount()-1);
                    } else {
                        tvEcgSwitch.setVisibility(View.VISIBLE);
                        fragAdapter.addFragment(ecgRecFrag, getResources().getString(EcgRecordFragment.TITLE_ID));
                    }
                }
            });
        }
    }

    @Override
    public void onHrRecordStatusUpdated(boolean isRecord) {
        hrRecFrag.updateHrRecordStatus(isRecord);
    }

    @Override
    public void onEcgSignalShowed(final int ecgSignal) {
        ecgView.showData(ecgSignal);
    }

    @Override
    public void onEcgSignalRecorded(boolean isRecord) {
        ecgRecFrag.updateRecordStatus(isRecord);
    }

    @Override
    public void onEcgOnStatusUpdated(boolean isOpen) {
        if(isEcgOn == isOpen) return;

        if (isOpen) {
            flWhenEcgOff.setVisibility(View.GONE);
            flWhenEcgOn.setVisibility(View.VISIBLE);
            ecgView.start();
            ecgView.initialize();
            tvEcgSwitch.setText(R.string.close);
        } else {
            flWhenEcgOff.setVisibility(View.VISIBLE);
            flWhenEcgOn.setVisibility(View.GONE);
            ecgView.stop();
            tvEcgSwitch.setText(R.string.open);
        }

        isEcgOn = isOpen;
    }

    @Override
    public void onEcgRecordTimeUpdated(int second) {
        ecgRecFrag.setEcgRecordTime(second);
    }

    @Override
    public void onShowStateUpdated(boolean isShow) {
        if(isShow) {
            tvMessage.setVisibility(View.GONE);
        } else {
            tvMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(device != null)
            device.removeListener();

        ecgView.stop();
    }

    public void warnUsingTTS(String warnStr) {
        MyApplication.getTTS().speak(warnStr);
    }

    public void setHrRecord(boolean isRecord) {
        if(device != null) {
            device.setHrRecord(isRecord);
        }
    }

    public void setEcgRecord(boolean isRecord) {
        if(device != null) {
            device.setEcgRecord(isRecord);
        }
    }

}
