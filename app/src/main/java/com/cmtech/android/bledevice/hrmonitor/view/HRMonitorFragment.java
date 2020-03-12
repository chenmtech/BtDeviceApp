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
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cmtech.android.bledevice.ecg.activity.EcgMonitorConfigureActivity;
import com.cmtech.android.bledevice.ecg.device.EcgConfiguration;
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

import static android.app.Activity.RESULT_OK;
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
    private HrStatisticsInfo hrInfo = new HrStatisticsInfo(10);  // heart rate statistics info

    private ScanEcgView ecgView; // EcgView
    private TextView tvHrEcgOff; // hr when ecg off
    private TextView tvHrEcgOn; // hr when ecg on
    private TextView tvMessage; // message
    private ToggleButton btnEcg; // toggle ecg on/off
    private FrameLayout flEcgOff; // frame layout when ecg off
    private FrameLayout flEcgOn; // frame layout when ecg on

    private final HrSequenceFragment seqFragment = new HrSequenceFragment(); // heart rate timing-sequence Fragment
    private final HrDebugFragment debugFragment = new HrDebugFragment(); // debug fragment

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

        btnEcg = view.findViewById(R.id.btn_ecg);
        btnEcg.setChecked(false);
        btnEcg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    flEcgOff.setVisibility(View.GONE);
                    flEcgOn.setVisibility(View.VISIBLE);
                    ecgView.start();
                    ecgView.initialize();
                } else {
                    flEcgOff.setVisibility(View.VISIBLE);
                    flEcgOn.setVisibility(View.GONE);
                    ecgView.stop();
                }
                device.switchEcgSignal(isChecked);
            }
        });

        flEcgOff = view.findViewById(R.id.fl_no_ecg);
        flEcgOff.setVisibility(View.VISIBLE);
        flEcgOn = view.findViewById(R.id.fl_with_ecg);
        flEcgOn.setVisibility(View.GONE);

        ecgView = view.findViewById(R.id.scanview_ecg);
        ecgView.setup(device.getSampleRate(), device.getCali1mV(), DEFAULT_ZERO_LOCATION);

        ViewPager pager = view.findViewById(R.id.vp_ecg_control_panel);
        TabLayout layout = view.findViewById(R.id.tl_ecg_control_panel);
        List<Fragment> fragmentList = new ArrayList<Fragment>(Arrays.asList(debugFragment, seqFragment));
        List<String> titleList = new ArrayList<>(Arrays.asList(HrDebugFragment.TITLE, HrSequenceFragment.TITLE));
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: // switch ecg on/off
                if(resultCode == RESULT_OK) {
                    boolean ecgSwitch = data.getBooleanExtra("ecg_on", false);
                    device.switchEcgMode(ecgSwitch);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void openConfigureActivity() {
        Intent intent = new Intent(getActivity(), HRMCfgActivity.class);
        intent.putExtra("ecg_on", device.isEcgSwitchOn());
        startActivityForResult(intent, 1);
    }

    @Override
    public void onHRUpdated(final BleHeartRateData hrData) {
        if(hrData != null && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    debugFragment.updateHrMeas(hrData.toString());

                    int bpm = hrData.getBpm();
                    tvHrEcgOn.setText(String.valueOf(bpm));
                    tvHrEcgOff.setText(String.valueOf(bpm));

                    if(hrInfo.process((short) bpm, hrData.getTime())) {
                        seqFragment.updateHrInfo(hrInfo);
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
                    debugFragment.updateHrSensLoc(String.valueOf(loc));
                }
            });
        }
    }

    @Override
    public void onHRCtrlPtUpdated(final int ctrl) {

    }

    @Override
    public void onFragmentUpdated(final int sampleRate, final int value1mV, final double zeroLocation, final boolean ecgSwitchOn) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ecgView.setup(sampleRate, value1mV, zeroLocation);
                    if(ecgSwitchOn) {
                        btnEcg.setVisibility(View.VISIBLE);
                        if(btnEcg.isChecked())
                            btnEcg.setChecked(false);
                    } else {
                        btnEcg.setVisibility(View.GONE);
                        flEcgOff.setVisibility(View.VISIBLE);
                        flEcgOn.setVisibility(View.GONE);
                        ecgView.stop();
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


}
