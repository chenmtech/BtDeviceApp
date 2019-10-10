package com.cmtech.android.bledevice.ecgmonitor.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgControllerAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorConfig;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorState;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.EcgHrStaticsInfoAnalyzer;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.viewcomponent.ScanWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.BleFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice.VALUE_1MV_AFTER_CALIBRATION;

/**
  *
  * ClassName:      EcgMonitorFragment
  * Description:    心电监护仪界面
  * Author:         chenm
  * CreateDate:     2018/3/13 下午4:52
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/8 下午4:52
  * UpdateRemark:   优化代码
  * Version:        1.0
 */

public class EcgMonitorFragment extends BleFragment implements EcgMonitorDevice.OnEcgMonitorListener {
    private static final String TAG = "EcgMonitorFragment";

    private TextView tvSampleRate; // 采样率
    private TextView tvLeadType; // 导联类型
    private TextView tvValue1mV; // 1mV定标值
    private TextView tvHeartRate; // 心率值
    private TextView tvBattery;
    private ScanWaveView ecgView; // 心电波形View
    private AudioTrack hrWarnAudio; // 心率报警声音
    private EcgSignalRecordFragment samplingSignalFragment = new EcgSignalRecordFragment();
    private EcgHrStatisticsFragment hrStatisticsFragment = new EcgHrStatisticsFragment();
    private List<Fragment> fragmentList = new ArrayList<>(Arrays.asList(hrStatisticsFragment, samplingSignalFragment));
    private List<String> titleList = new ArrayList<>(Arrays.asList("心率分析", "信号采集"));
    private EcgMonitorDevice device; // 设备

    public EcgMonitorFragment() {
        super();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        device = (EcgMonitorDevice) getDevice();
        return inflater.inflate(R.layout.fragment_ecgmonitor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvSampleRate = view.findViewById(R.id.tv_ecg_samplerate);
        tvLeadType = view.findViewById(R.id.tv_ecg_leadtype);
        tvValue1mV = view.findViewById(R.id.tv_ecg_1mv);
        tvHeartRate = view.findViewById(R.id.tv_ecg_hr);
        ecgView = view.findViewById(R.id.rwv_ecgview);
        tvBattery = view.findViewById(R.id.tv_ecg_battery);
        tvSampleRate.setText(String.valueOf(device.getSampleRate()));
        tvLeadType.setText(String.format("L%s", device.getLeadType().getDescription()));
        setCalibrationValue(device.getValue1mVBeforeCalibration(), VALUE_1MV_AFTER_CALIBRATION);
        tvHeartRate.setText("");
        initialEcgView();
        ViewPager fragViewPager = view.findViewById(R.id.vp_ecg_controller);
        TabLayout fragTabLayout = view.findViewById(R.id.tl_ecg_controller);
        EcgControllerAdapter fragAdapter = new EcgControllerAdapter(getChildFragmentManager(), getContext(), fragmentList, titleList);
        fragViewPager.setAdapter(fragAdapter);
        fragTabLayout.setupWithViewPager(fragViewPager);
        samplingSignalFragment.setDevice(device);
        updateDeviceState(device.getEcgMonitorState());
        device.setEcgMonitorListener(this);
    }

    @Override
    public void close() {
        final Dialog alertDialog = new AlertDialog.Builder(getContext()).
                setTitle("保存记录").
                setMessage("是否保存记录？").
                setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(device != null) {
                            device.setSaveEcgFile(true);
                        }
                        EcgMonitorFragment.super.close();
                    }
                }).setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(device != null) {
                            device.setSaveEcgFile(false);
                        }
                        EcgMonitorFragment.super.close();
                    }
                }).create();
        alertDialog.show();
    }

    @Override
    public void openConfigActivity() {
        Intent intent = new Intent(getActivity(), EcgMonitorConfigureActivity.class);
        intent.putExtra("configuration", device.getConfig());
        intent.putExtra("devicenickname", device.getNickName());
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: // 设置设备配置返回码
                if(resultCode == RESULT_OK) {
                    EcgMonitorConfig config = (EcgMonitorConfig) data.getSerializableExtra("configuration");
                    device.updateConfig(config);
                }
                break;

                default:
                    break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(device != null)
            device.removeEcgMonitorListener();

        if(hrWarnAudio != null)
            hrWarnAudio.stop();

        stopShow();
    }

    @Override
    public void onEcgMonitorStateUpdated(final EcgMonitorState state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateDeviceState(state);
            }
        });
    }

    private void updateDeviceState(final EcgMonitorState state) {

    }

    @Override
    public void onSampleRateChanged(final int sampleRate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvSampleRate.setText(String.valueOf(sampleRate));
            }
        });
    }

    @Override
    public void onLeadTypeChanged(final EcgLeadType leadType) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLeadType.setText(String.format("L%s", leadType.getDescription()));
            }
        });
    }

    @Override
    public void onCalibrationValueChanged(final int calibrationValueBefore, final int calibrationValueAfter) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setCalibrationValue(calibrationValueBefore, calibrationValueAfter);
            }
        });
    }

    private void setCalibrationValue(final int calibrationValueBefore, final int calibrationValueAfter) {
        tvValue1mV.setText(String.format(Locale.getDefault(), "%d/%d", calibrationValueBefore, calibrationValueAfter));
    }

    @Override
    public void onSignalRecordStateUpdated(final boolean isRecord) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                samplingSignalFragment.setSignalRecordStatus(isRecord);
            }
        });

    }

    @Override
    public void onEcgViewUpdated(final int xPixelPerData, final float yValuePerPixel, final int gridPixels) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateEcgView(xPixelPerData, yValuePerPixel, gridPixels);
            }
        });
    }

    private void updateEcgView(final int xPixelPerData, final float yValuePerPixel, final int gridPixels) {
        ecgView.setResolution(xPixelPerData, yValuePerPixel);
        ecgView.setPixelPerGrid(gridPixels);
        ecgView.setZeroLocation(0.5);
        ecgView.initialize();
    }

    private void initialEcgView() {
        updateEcgView(device.getXPixelPerData(), device.getYValuePerPixel(), device.getPixelPerGrid());
    }

    private void startShow(int sampleRate) {
        ecgView.start();
    }

    private void stopShow() {
        ecgView.stop();
    }

    @Override
    public void onEcgSignalUpdated(final int ecgSignal) {
        ecgView.showData(ecgSignal);
    }

    @Override
    public void onEcgSignalShowStarted(int sampleRate) {
        startShow(sampleRate);
    }

    @Override
    public void onEcgSignalShowStoped() {
        stopShow();
    }

    @Override
    public void onSignalSecNumChanged(final int second) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                samplingSignalFragment.setSignalSecNum(second);
            }
        });
    }

    @Override
    public void onEcgHrChanged(final int hr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvHeartRate.setText(String.valueOf(hr));
            }
        });

    }

    @Override
    public void onEcgHrStaticsInfoUpdated(final EcgHrStaticsInfoAnalyzer hrStaticsInfoAnalyzer) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hrStatisticsFragment.updateHrInfo(hrStaticsInfoAnalyzer);
            }
        });

    }

    @Override
    public void onBatteryChanged(final int bat) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(tvBattery.getVisibility() == View.GONE) {
                    tvBattery.setVisibility(View.VISIBLE);
                }
                tvBattery.setText(String.valueOf(bat));
            }
        });
    }

    @Override
    public void onHrAbnormalNotified() {
        if(hrWarnAudio == null) {
            initHrWarnAudioTrack();
        } else {
            switch(hrWarnAudio.getPlayState()) {
                case AudioTrack.PLAYSTATE_PAUSED:
                case AudioTrack.PLAYSTATE_PLAYING:
                    hrWarnAudio.stop();
                    break;
            }
            hrWarnAudio.reloadStaticData();
        }
        hrWarnAudio.play();
    }

    private void initHrWarnAudioTrack() {
        int length = 4000;
        int f = 1000;
        int fs = 44100;
        float mag = 127.0f;
        double omega = 2 * Math.PI * f/fs;

        byte[] wave = new byte[length];
        for(int i = 0; i < length; i++) {
            wave[i] = (byte) (mag * Math.sin(omega * i));
        }

        hrWarnAudio = new AudioTrack(AudioManager.STREAM_MUSIC, fs,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT, length, AudioTrack.MODE_STATIC);
        hrWarnAudio.write(wave, 0, wave.length);
        hrWarnAudio.write(wave, 0, wave.length);
    }

    private void runOnUiThread(Runnable runnable) {
        if(getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }

}
