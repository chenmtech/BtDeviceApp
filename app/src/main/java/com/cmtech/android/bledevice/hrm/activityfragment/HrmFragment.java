package com.cmtech.android.bledevice.hrm.activityfragment;

import static android.app.Activity.RESULT_OK;
import static com.cmtech.android.bledeviceapp.data.record.AnnSymbol.ANN_AFIB;
import static com.cmtech.android.bledeviceapp.data.record.AnnSymbol.ANN_COMMENT;
import static com.cmtech.android.bledeviceapp.data.record.AnnSymbol.ANN_SB;
import static com.cmtech.android.bledeviceapp.data.record.AnnSymbol.ANN_SYMBOL_DESCRIPTION_MAP;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
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
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.view.OnWaveViewListener;
import com.cmtech.android.bledeviceapp.view.ScanEcgView;
import com.vise.log.ViseLog;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.view
 * ClassName:      HrmFragment
 * Description:    心率(电)带 fragment
 * Author:         chenm
 * CreateDate:     2020-02-04 06:06
 * UpdateUser:     chenm
 * UpdateDate:     2020-02-04 06:06
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HrmFragment extends DeviceFragment implements OnHrmListener, OnWaveViewListener {
    // 设置配置返回码
    private static final int RC_CONFIG = 1;

    // 设备
    private HrmDevice device;

    // 心率/心电模式转换开关
    private TextView tvSwitchMode;

    //------------------------------------------下面为心率模式下包含的
    // FrameLayout
    private FrameLayout flInHrMode;

    // 心率模式下的心率值
    private TextView tvHrInHrMode;

    // 心率记录Fragment
    private HrRecordFragment hrRecFrag;

    // HrRecordFragment Container
    private FragmentContainerView hrRecFragContainer;

    //------------------------------------------下面为心电模式下包含的
    // FrameLayout
    private FrameLayout flInEcgMode;

    // ECG View
    private ScanEcgView ecgView;

    // 心电模式下的心率值
    private TextView tvHrInEcgMode;

    // 心律注解
    private TextView tvRhythmAnn;

    // 是否暂停显示的提示
    private TextView tvPauseHint;

    // 心电记录Fragment
    private EcgRecordFragment ecgRecFrag;

    // Container
    private FragmentContainerView ecgRecFragContainer;
    ////////////////////////////////////////////////////////////////////

    //private boolean isEcgOn = false;

    // 报警声生成器
    private final ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 200);

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
        tvPauseHint = view.findViewById(R.id.tv_pause_hint);
        tvRhythmAnn = view.findViewById(R.id.tv_rhythm_annotation);

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
                if(cfg!=null)
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
    }

    @Override
    public void openConfigureActivity() {
        HrmCfg cfg = device.getConfig();

        Intent intent = new Intent(getActivity(), HrmCfgActivity.class);
        intent.putExtra("hrm_cfg", cfg);
        startActivityForResult(intent, RC_CONFIG);
    }

    @Override
    public void onHrDataUpdated(final BleHeartRateData hrData) {
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
    public void onHrStatisticInfoUpdated(BleHrRecord record) {
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
    public void onHrSensLocUpdated(final int loc) {
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
    public void onHrCtrlPtUpdated(final int ctrl) {

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
    public void onEcgAnnotationUpdated(String annSymbol, String annContent) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String annStr;
                    // 如果annotation是comment，就显示content，否则显示description
                    if(annSymbol.equals(ANN_COMMENT)) {
                        annStr = annContent;
                    } else {
                        annStr = ANN_SYMBOL_DESCRIPTION_MAP.get(annSymbol);
                    }

                    // 显示
                    if(MyApplication.isRunInForeground()) {
                        tvRhythmAnn.setText(annStr);
                        // 在ECGView中显示注解
                        ecgView.showAnnotation(annStr);
                    }

                    // 报警
                    boolean warnSb = annSymbol.equals(ANN_SB) && device.getConfig().isWarnSb();
                    boolean warnAfib = annSymbol.equals(ANN_AFIB) && device.getConfig().isWarnAfib();
                    if(warnSb || warnAfib) {
                        MyApplication.getTts().speak(annStr);
                        toneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 200);
                        new Handler().postDelayed(new Runnable(){
                            public void run() {
                                toneGenerator.stopTone();
                            }
                        }, 200);
                    }
                }
            });
        }
    }

    @Override
    public void onShowStateUpdated(boolean show) {
        if(show) {
            tvPauseHint.setVisibility(View.GONE);
        } else {
            tvPauseHint.setVisibility(View.VISIBLE);
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
