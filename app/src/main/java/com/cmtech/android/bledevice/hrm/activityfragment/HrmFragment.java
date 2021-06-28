package com.cmtech.android.bledevice.hrm.activityfragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import com.cmtech.android.ble.core.DeviceConnectState;
import com.cmtech.android.bledevice.hrm.model.BleHeartRateData;
import com.cmtech.android.bledevice.hrm.model.HrmCfg;
import com.cmtech.android.bledevice.hrm.model.HrmDevice;
import com.cmtech.android.bledevice.hrm.model.OnHrmListener;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.CtrlPanelAdapter;
import com.cmtech.android.bledeviceapp.data.record.BleHrRecord;
import com.cmtech.android.bledeviceapp.fragment.DeviceFragment;
import com.cmtech.android.bledeviceapp.view.OnWaveViewListener;
import com.cmtech.android.bledeviceapp.view.ScanEcgView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.cmtech.android.bledeviceapp.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

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
    private static final int RC_CONFIG = 1;
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

    //private boolean isEcgOn = false;

    public HrmFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (HrmDevice) getDevice();
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
                if(device.getConnectState() != DeviceConnectState.CONNECT) {
                    Toast.makeText(getContext(), R.string.cannot_switch_mode, Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setTitle(R.string.switch_mode)
                        .setMessage(R.string.reconnect_after_disconnect)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                device.setMode(!device.inHrMode());
                            }
                        }).show();
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
        device.open();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_CONFIG) { // cfg return
            if(resultCode == RESULT_OK) {
                HrmCfg cfg = (HrmCfg) data.getSerializableExtra("hr_cfg");
                device.updateConfig(cfg);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        BleHrRecord record = device.getHrRecord();
        if(record != null)
            hrRecFrag.updateHrInfo(record.getHrList(), record.getHrMax(), record.getHrAve());
    }

    @Override
    public void onStop() {
        super.onStop();
        //ViseLog.e("onStop");
    }

    @Override
    public void openConfigureActivity() {
        HrmCfg cfg = device.getConfig();

        Intent intent = new Intent(getActivity(), HrmCfgActivity.class);
        intent.putExtra("hr_cfg", cfg);
        startActivityForResult(intent, RC_CONFIG);
    }

    @Override
    public void onHRUpdated(final BleHeartRateData hrData) {
        if(hrData != null && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int bpm = hrData.getBpm();
                    if(device.inHrMode()) {
                        tvHrInHrMode.setText(String.valueOf(bpm));
                    } else {
                        tvHrInEcgMode.setText(String.valueOf(bpm));
                    }
                }
            });
        }
    }

    @Override
    public void onHRStatisticInfoUpdated(BleHrRecord record) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hrRecFrag.updateHrInfo(record.getHrList(), record.getHrMax(), record.getHrAve());
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
    public void onFragmentUpdated(final int sampleRate, final int caliValue, final float zeroLocation, final boolean inHrMode) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvSwitchMode.setVisibility(View.VISIBLE);
                    if (inHrMode) {
                        flInHrMode.setVisibility(View.VISIBLE);
                        flInEcgMode.setVisibility(View.GONE);
                        ecgView.stopShow();
                        fragAdapter.removeFragment(ecgRecFrag);
                        pager.setCurrentItem(fragAdapter.getCount() - 1);
                        tvSwitchMode.setTextColor(Color.BLACK);
                        tvSwitchMode.setCompoundDrawablesWithIntrinsicBounds(null,
                                getResources().getDrawable(R.mipmap.ic_hr_24px, null), null, null);
                    } else {
                        ecgView.setup(sampleRate, caliValue, zeroLocation);
                        fragAdapter.addFragment(ecgRecFrag, getResources().getString(EcgRecordFragment.TITLE_ID));
                        tvSwitchMode.setTextColor(Color.WHITE);
                        tvSwitchMode.setCompoundDrawablesWithIntrinsicBounds(null,
                                getResources().getDrawable(R.mipmap.ic_ecg_24px, null), null, null);
                    }
                }
            });
        }
    }

    @Override
    public void onHrRecordStatusUpdated(boolean record) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hrRecFrag.updateRecordStatus(record);
                }
            });
        }
    }

    @Override
    public void onEcgSignalRecordStatusUpdated(boolean record) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ecgRecFrag.updateRecordStatus(record);
                }
            });
        }
    }

    @Override
    public void onEcgSignalShowed(final int ecgSignal) {
        ecgView.addData(ecgSignal);
    }


    @Override
    public void onEcgOnStatusUpdated(final boolean ecgOn) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ecgOn) {
                        flInHrMode.setVisibility(View.GONE);
                        flInEcgMode.setVisibility(View.VISIBLE);
                        ecgView.startShow();
                        ecgView.resetView(false);
                    } else {
                        flInHrMode.setVisibility(View.VISIBLE);
                        flInEcgMode.setVisibility(View.GONE);
                        ecgView.stopShow();
                    }
                }
            });
        }
    }

    @Override
    public void onEcgRecordTimeUpdated(int second) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ecgRecFrag.setEcgRecordTime(second);
                }
            });
        }
    }

    @Override
    public void onShowStateUpdated(boolean show) {
        if(show) {
            tvMessage.setVisibility(View.GONE);
        } else {
            tvMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(ecgView != null)
            ecgView.stopShow();

        if(device != null)
            device.removeListener();
    }

    public void setHrRecord(boolean record) {
        if(device != null) {
            device.setHrRecord(record);
        }
    }

    public void setEcgRecord(boolean record) {
        if(device != null) {
            device.setEcgRecord(record);
        }
    }

}
