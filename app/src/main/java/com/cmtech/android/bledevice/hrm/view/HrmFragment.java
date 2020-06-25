package com.cmtech.android.bledevice.hrm.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.ble.core.DeviceState;
import com.cmtech.android.bledevice.hrm.model.BleHeartRateData;
import com.cmtech.android.bledevice.hrm.model.HrmCfg;
import com.cmtech.android.bledevice.hrm.model.HrmDevice;
import com.cmtech.android.bledevice.hrm.model.OnHrmListener;
import com.cmtech.android.bledevice.record.BleHrRecord10;
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
    private TextView tvHrInHrMode; // hr view in HR Mode
    private TextView tvHrInEcgMode; // hr view in ECG Mode
    private TextView tvMessage; // message
    private TextView tvSwitchMode; // switch Mode
    private FrameLayout flInHrMode; // frame layout in HR Mode
    private FrameLayout flInEcgMode; // frame layout in ECG Mode

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

        tvHrInHrMode = view.findViewById(R.id.tv_hr_in_hr_mode);
        tvHrInEcgMode = view.findViewById(R.id.tv_hr_in_ecg_mode);
        tvMessage = view.findViewById(R.id.tv_message);

        flInHrMode = view.findViewById(R.id.fl_in_hr_mode);
        flInHrMode.setVisibility(View.VISIBLE);
        flInEcgMode = view.findViewById(R.id.fl_in_ecg_mode);
        flInEcgMode.setVisibility(View.GONE);

        ecgView = view.findViewById(R.id.ecg_view);
        ecgView.setup(device.getSampleRate(), device.getCaliValue(), DEFAULT_ZERO_LOCATION);

        tvSwitchMode = view.findViewById(R.id.tv_switch_mode);
        tvSwitchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(device.getState() != DeviceState.CONNECT) {
                    Toast.makeText(getContext(), R.string.cannot_switch_mode, Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.switch_mode)
                        .setMessage(R.string.reconnect_after_disconnect)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                device.setMode(!device.inHrMode());
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
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
            device.open(activity.getNotifyService());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1) { // cfg return
            if(resultCode == RESULT_OK) {
                HrmCfg cfg = (HrmCfg) data.getSerializableExtra("hr_cfg");
                device.updateConfig(cfg);
            }
        }
    }

    @Override
    public void openConfigureActivity() {
        HrmCfg cfg = device.getConfig();

        Intent intent = new Intent(getActivity(), HrmCfgActivity.class);
        intent.putExtra("ecg_lock", device.inHrMode());
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
                    tvHrInEcgMode.setText(String.valueOf(bpm));
                    tvHrInHrMode.setText(String.valueOf(bpm));

                    HrmCfg cfg = device.getConfig();
                    if(cfg.needWarn()) {
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
    public void onFragmentUpdated(final int sampleRate, final int value1mV, final double zeroLocation, final boolean inHrMode) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ecgView.setup(sampleRate, value1mV, zeroLocation);
                    if(inHrMode) {
                        flInHrMode.setVisibility(View.VISIBLE);
                        flInEcgMode.setVisibility(View.GONE);
                        ecgView.stop();
                        fragAdapter.removeFragment(ecgRecFrag);
                        pager.setCurrentItem(fragAdapter.getCount()-1);
                        tvSwitchMode.setText(R.string.ecg_switch_off);
                    } else {
                        fragAdapter.addFragment(ecgRecFrag, getResources().getString(EcgRecordFragment.TITLE_ID));
                        tvSwitchMode.setText(R.string.ecg_switch_on);
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
    public void onEcgOnStatusUpdated(final boolean ecgOn) {
        if(getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ecgOn) {
                    flInHrMode.setVisibility(View.GONE);
                    flInEcgMode.setVisibility(View.VISIBLE);
                    ecgView.start();
                    ecgView.initialize();
                } else {
                    flInHrMode.setVisibility(View.VISIBLE);
                    flInEcgMode.setVisibility(View.GONE);
                    ecgView.stop();
                }
            }
        });
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
