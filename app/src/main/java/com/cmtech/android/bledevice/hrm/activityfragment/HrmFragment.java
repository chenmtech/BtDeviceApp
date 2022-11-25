package com.cmtech.android.bledevice.hrm.activityfragment;

import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentContainerView;

import com.cmtech.android.ble.core.DeviceConnectState;
import com.cmtech.android.bledevice.hrm.model.BleHeartRateData;
import com.cmtech.android.bledevice.hrm.model.HrmCfg;
import com.cmtech.android.bledevice.hrm.model.HrmDevice;
import com.cmtech.android.bledevice.hrm.model.OnHrmListener;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BleHrRecord;
import com.cmtech.android.bledeviceapp.fragment.DeviceFragment;
import com.cmtech.android.bledeviceapp.view.OnWaveViewListener;
import com.cmtech.android.bledeviceapp.view.ScanEcgView;
import com.vise.log.ViseLog;

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
    private TextView tvRhythm;
    private TextView tvSwitchMode; // switch Mode
    private FrameLayout flInHrMode; // frame layout in HR Mode
    private FrameLayout flInEcgMode; // frame layout in ECG Mode

    private HrRecordFragment hrRecFrag; // heart rate record Fragment
    private EcgRecordFragment ecgRecFrag; // ecg record fragment
    private FragmentContainerView hrRecFragContainer; // heart rate record Fragment
    private FragmentContainerView ecgRecFragContainer; // ecg record fragment

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
        tvRhythm = view.findViewById(R.id.tv_rhythm_info);

        flInHrMode = view.findViewById(R.id.fl_in_hr_mode);
        flInHrMode.setVisibility(View.VISIBLE);
        flInEcgMode = view.findViewById(R.id.fl_in_ecg_mode);
        flInEcgMode.setVisibility(View.GONE);

        ecgView = view.findViewById(R.id.ptt_view);
        ecgView.setup(device.getSampleRate(), device.getGain());

        tvSwitchMode = view.findViewById(R.id.tv_switch_mode);
        tvSwitchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(device.getConnectState() != DeviceConnectState.CONNECT) {
                    Toast.makeText(getContext(), R.string.cannot_switch_mode, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(device.getRecordingRecord() != null) {
                    Toast.makeText(getContext(), "设备正在记录信号中，不能切换模式。", Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(R.string.switch_mode)
                        .setMessage(R.string.reconnect_after_disconnect)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                device.setMode(!device.isHrMode());
                            }
                        }).show();
            }
        });

        hrRecFrag = (HrRecordFragment) getChildFragmentManager().findFragmentByTag("frag_hrm_hr");
        ecgRecFrag = (EcgRecordFragment) getChildFragmentManager().findFragmentByTag("frag_hrm_ecg");

        hrRecFragContainer = view.findViewById(R.id.frag_hrm_hr_view);
        ecgRecFragContainer = view.findViewById(R.id.frag_hrm_ecg_view);
        ViseLog.e("onViewCreated");

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
                    if(device.isHrMode()) {
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
    public void onUIUpdated(final int sampleRate, final int gain, final boolean inHrMode) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvSwitchMode.setVisibility(View.VISIBLE);
                    if (inHrMode) {
                        flInHrMode.setVisibility(View.VISIBLE);
                        flInEcgMode.setVisibility(View.GONE);
                        ecgView.stopShow();
                        hrRecFragContainer.setVisibility(View.VISIBLE);
                        ecgRecFragContainer.setVisibility(View.GONE);
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        tvSwitchMode.setTextColor(Color.BLACK);
                        tvSwitchMode.setCompoundDrawablesWithIntrinsicBounds(null,
                                ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_hr_24px, null), null, null);
                    } else {
                        ecgView.setup(sampleRate, gain);
                        hrRecFragContainer.setVisibility(View.GONE);
                        ecgRecFragContainer.setVisibility(View.VISIBLE);
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        tvSwitchMode.setTextColor(Color.WHITE);
                        tvSwitchMode.setCompoundDrawablesWithIntrinsicBounds(null,
                                ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_ecg_24px, null), null, null);
                    }
                }
            });
        }
    }

    @Override
    public void onHRRecordStatusUpdated(boolean record) {
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
        ecgView.addData(new int[]{ecgSignal});
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
    public void onEcgRhythmDetectInfoUpdated(int label, String rhythmInfo) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvRhythm.setText(rhythmInfo);
                }
            });
        }
        ViseLog.e(rhythmInfo);
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

    // 设置ECG信号的记录状态
    public void setEcgRecord(boolean record) {
        if(device != null) {
            device.setEcgRecord(record);
        }
    }

    public void setEcgRecordNote(String note) {
        if(device != null) {
            device.setEcgRecordComment(note);
        }
    }
}
