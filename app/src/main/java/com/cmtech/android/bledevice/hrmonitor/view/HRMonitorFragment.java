package com.cmtech.android.bledevice.hrmonitor.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.ble.core.DeviceState;
import com.cmtech.android.bledevice.hrmonitor.model.BleHeartRateData;
import com.cmtech.android.bledevice.hrmonitor.model.BleHrRecord10;
import com.cmtech.android.bledevice.hrmonitor.model.HRMonitorDevice;
import com.cmtech.android.bledevice.hrmonitor.model.HRMonitorConfiguration;
import com.cmtech.android.bledevice.hrmonitor.model.HrCtrlPanelAdapter;
import com.cmtech.android.bledevice.hrmonitor.model.OnHRMonitorDeviceListener;
import com.cmtech.android.bledevice.view.OnWaveViewListener;
import com.cmtech.android.bledevice.view.ScanEcgView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.activity.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.cmtech.android.bledevice.hrmonitor.view.HRMCfgActivity.RESULT_CHANGE_ECG_LOCK;
import static com.cmtech.android.bledevice.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.view
 * ClassName:      HRMonitorFragment
 * Description:    heart rate monitor fragment
 * Author:         chenm
 * CreateDate:     2020-02-04 06:06
 * UpdateUser:     chenm
 * UpdateDate:     2020-02-04 06:06
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HRMonitorFragment extends DeviceFragment implements OnHRMonitorDeviceListener, OnWaveViewListener {
    private HRMonitorDevice device; // device

    private ScanEcgView ecgView; // EcgView
    private TextView tvHrEcgOff; // hr when ecg off
    private TextView tvHrEcgOn; // hr when ecg on
    private TextView tvMessage; // message
    private Switch swEcg; // ecg on/off
    private FrameLayout flEcgOff; // frame layout when ecg off
    private FrameLayout flEcgOn; // frame layout when ecg on

    private HrCtrlPanelAdapter fragAdapter;
    private final HrRecordFragment hrRecFrag = new HrRecordFragment(); // heart rate record Fragment
    private final HrDebugFragment debugFrag = new HrDebugFragment(); // debug fragment
    private final EcgRecordFragment ecgRecFrag = new EcgRecordFragment(); // ecg record fragment

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

        tvHrEcgOff = view.findViewById(R.id.tv_hr_ecg_off);
        tvHrEcgOn = view.findViewById(R.id.tv_hr_ecg_on);
        tvMessage = view.findViewById(R.id.tv_message);

        flEcgOff = view.findViewById(R.id.fl_no_ecg);
        flEcgOff.setVisibility(View.VISIBLE);
        flEcgOn = view.findViewById(R.id.fl_with_ecg);
        flEcgOn.setVisibility(View.GONE);

        ecgView = view.findViewById(R.id.scanview_ecg);
        ecgView.setup(device.getSampleRate(), device.getCaliValue(), DEFAULT_ZERO_LOCATION);

        swEcg = view.findViewById(R.id.switch_ecg);
        swEcg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setEcgShow(isChecked);
            }
        });

        ViewPager pager = view.findViewById(R.id.vp_ecg_control_panel);
        TabLayout layout = view.findViewById(R.id.tl_ecg_control_panel);
        List<Fragment> fragmentList = new ArrayList<Fragment>(Arrays.asList(debugFrag, hrRecFrag));
        List<String> titleList = new ArrayList<>(Arrays.asList(HrDebugFragment.TITLE, HrRecordFragment.TITLE));
        fragAdapter = new HrCtrlPanelAdapter(getChildFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(3);
        layout.setupWithViewPager(pager);

        device.setListener(this);
        ecgView.setListener(this);

        // 打开设备
        MainActivity activity = (MainActivity) getActivity();
        device.open(activity.getNotiService());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1) { // cfg return
            if(resultCode == RESULT_CHANGE_ECG_LOCK) {
                if(device.getState() != DeviceState.CONNECT) {
                    Toast.makeText(getContext(), "设备未连接，无法切换心电功能。", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean ecgLock = data.getBooleanExtra("ecg_lock", true);
                device.setEcgLock(ecgLock);
            } else if(resultCode == RESULT_OK) {
                HRMonitorConfiguration cfg = (HRMonitorConfiguration) data.getSerializableExtra("hr_cfg");
                device.updateConfig(cfg);
            }
        }
    }

    @Override
    public void openConfigureActivity() {
        HRMonitorConfiguration cfg = device.getConfig();

        Intent intent = new Intent(getActivity(), HRMCfgActivity.class);
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
                    debugFrag.updateHrMeas(hrData.toString());

                    int bpm = hrData.getBpm();
                    tvHrEcgOn.setText(String.valueOf(bpm));
                    tvHrEcgOff.setText(String.valueOf(bpm));

                    HRMonitorConfiguration cfg = device.getConfig();
                    if(cfg.isWarn()) {
                        String warnStr = null;
                        if(bpm > cfg.getHrHigh())
                            warnStr = "心率过高";
                        else if(bpm < cfg.getHrLow()) {
                            warnStr = "心率过低";
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
                    debugFrag.updateHrSensLoc(String.valueOf(loc));
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
                        swEcg.setVisibility(View.GONE);
                        flEcgOff.setVisibility(View.VISIBLE);
                        flEcgOn.setVisibility(View.GONE);
                        ecgView.stop();
                        fragAdapter.removeFragment(ecgRecFrag);
                    } else {
                        swEcg.setVisibility(View.VISIBLE);
                        swEcg.setChecked(false);
                        fragAdapter.addFragment(ecgRecFrag, EcgRecordFragment.TITLE);
                    }
                }
            });
        }
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

    private void setEcgShow(boolean isStart) {
        if(isStart) {
            flEcgOff.setVisibility(View.GONE);
            flEcgOn.setVisibility(View.VISIBLE);
            ecgView.start();
            ecgView.initialize();
        } else {
            flEcgOff.setVisibility(View.VISIBLE);
            flEcgOn.setVisibility(View.GONE);
            ecgView.stop();
        }
        device.setEcgShow(isStart);
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
