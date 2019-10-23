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
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.EcgHrStatisticsInfo;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.viewcomponent.ScanWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.BleFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.calibrator.IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION;

/**
  *
  * ClassName:      EcgMonitorFragment
  * Description:    心电监护仪Fragment
  * Author:         chenm
  * CreateDate:     2018/3/13 下午4:52
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/8 下午4:52
  * UpdateRemark:   优化代码
  * Version:        1.0
 */

public class EcgMonitorFragment extends BleFragment implements EcgMonitorDevice.OnEcgMonitorListener, ScanWaveView.OnScanWaveViewListener {
    private static final String TAG = "EcgMonitorFragment";

    private TextView tvSampleRate; // 采样率
    private TextView tvLeadType; // 导联类型
    private TextView tvValue1mV; // 1mV值
    private TextView tvHeartRate; // 心率值
    private TextView tvPauseShowing;
    private ScanWaveView ecgView; // 心电波形View
    private AudioTrack hrAbnormalWarnAudio; // 心率异常报警声音
    private final EcgSignalRecordFragment signalRecordFragment = new EcgSignalRecordFragment(); // 信号记录Fragment
    private final EcgHrStatisticsFragment hrStatisticsFragment = new EcgHrStatisticsFragment(); // 心率统计Fragment
    private final List<Fragment> fragmentList = new ArrayList<>(Arrays.asList(signalRecordFragment, hrStatisticsFragment));
    private final List<String> titleList = new ArrayList<>(Arrays.asList("信号记录", "心率统计"));
    private EcgMonitorDevice device; // 设备

    public EcgMonitorFragment() {
        super();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        device = (EcgMonitorDevice) getDevice();
        signalRecordFragment.setDevice(device);
        return inflater.inflate(R.layout.fragment_ecgmonitor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvSampleRate = view.findViewById(R.id.tv_ecg_samplerate);
        tvLeadType = view.findViewById(R.id.tv_ecg_leadtype);
        tvValue1mV = view.findViewById(R.id.tv_ecg_1mv);
        tvHeartRate = view.findViewById(R.id.tv_ecg_hr);
        tvPauseShowing = view.findViewById(R.id.tv_pause_showing);
        ecgView = view.findViewById(R.id.rwv_ecgview);
        tvSampleRate.setText(String.valueOf(device.getSampleRate()));
        tvLeadType.setText(String.format("L%s", device.getLeadType().getDescription()));
        tvValue1mV.setText(String.format(Locale.getDefault(), "%d/%d", device.getValue1mV(), STANDARD_VALUE_1MV_AFTER_CALIBRATION));
        tvHeartRate.setText("");
        initialEcgView();
        ViewPager fragViewPager = view.findViewById(R.id.vp_ecg_controller);
        TabLayout fragTabLayout = view.findViewById(R.id.tl_ecg_controller);
        EcgControllerAdapter fragAdapter = new EcgControllerAdapter(getChildFragmentManager(), fragmentList, titleList);
        fragViewPager.setAdapter(fragAdapter);
        fragTabLayout.setupWithViewPager(fragViewPager);
        updateDeviceState(device.getEcgMonitorState());
        device.setEcgMonitorListener(this);
        ecgView.setListener(this);
    }

    @Override
    public void close() {
        if(device != null && device.isStopped()) {
            if (getContext() != null) {
                final Dialog alertDialog = new AlertDialog.Builder(getContext()).
                        setTitle("保存记录").
                        setMessage("是否保存记录？").
                        setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (device != null) {
                                    device.setSaveFile(true);
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
                        if (device != null) {
                            device.setSaveFile(false);
                        }
                        EcgMonitorFragment.super.close();
                    }
                }).create();
                alertDialog.show();
            }
        }
    }

    @Override
    public void openConfigureActivity() {
        Intent intent = new Intent(getActivity(), EcgMonitorConfigureActivity.class);
        intent.putExtra("configuration", device.getConfig());
        intent.putExtra("nickname", device.getNickName());
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: // 修改设备配置返回码
                if(resultCode == RESULT_OK) {
                    EcgMonitorConfig config = (EcgMonitorConfig) data.getSerializableExtra("configuration");
                    device.setConfig(config);
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
        if(hrAbnormalWarnAudio != null)
            hrAbnormalWarnAudio.stop();

        ecgView.stop();
    }

    @Override
    public void onDeviceStateUpdated(final EcgMonitorState state) {
        updateDeviceState(state);
    }

    private void updateDeviceState(final EcgMonitorState state) {

    }

    @Override
    public void onSampleRateChanged(final int sampleRate) {
        tvSampleRate.setText(String.valueOf(sampleRate));
    }

    @Override
    public void onLeadTypeChanged(final EcgLeadType leadType) {
        tvLeadType.setText(String.format("L%s", leadType.getDescription()));
    }

    @Override
    public void onValue1mVChanged(final int value1mV, final int value1mVAfterCalibration) {
        tvValue1mV.setText(String.format(Locale.getDefault(), "%d/%d", value1mV, value1mVAfterCalibration));
    }

    @Override
    public void onSignalRecordStateUpdated(final boolean isRecord) {
        signalRecordFragment.setSignalRecordStatus(isRecord);
    }

    @Override
    public void onEcgViewSetupUpdated(final int xPixelPerData, final float yValuePerPixel, final int gridPixels) {
        updateEcgViewSetup(xPixelPerData, yValuePerPixel, gridPixels);
    }

    private void updateEcgViewSetup(final int xPixelPerData, final float yValuePerPixel, final int gridPixels) {
        ecgView.setResolution(xPixelPerData, yValuePerPixel);
        ecgView.setPixelPerGrid(gridPixels);
        ecgView.setZeroLocation(0.5);
        ecgView.initialize();
    }

    private void initialEcgView() {
        updateEcgViewSetup(device.getXPixelPerData(), device.getYValuePerPixel(), device.getPixelPerGrid());
    }

    @Override
    public void onEcgSignalUpdated(final int ecgSignal) {
        ecgView.showData(ecgSignal);
    }

    @Override
    public void onEcgSignalShowStarted(int sampleRate) {
        ecgView.start();
    }

    @Override
    public void onEcgSignalShowStopped() {
        ecgView.stop();
    }

    @Override
    public void onSignalRecordSecondChanged(final int second) {
        signalRecordFragment.setSignalSecNum(second);
    }

    @Override
    public void onHrChanged(final int hr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvHeartRate.setText(String.valueOf(hr));
            }
        });
    }

    @Override
    public void onHrStaticsInfoUpdated(final EcgHrStatisticsInfo hrStaticsInfoAnalyzer) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hrStatisticsFragment.updateHrInfo(hrStaticsInfoAnalyzer);
            }
        });
    }

    @Override
    public void onBatteryChanged(final int bat) {

    }

    @Override
    public void onHrAbnormalNotified() {
        if(hrAbnormalWarnAudio == null) {
            initHrWarnAudioTrack();
        } else {
            switch(hrAbnormalWarnAudio.getPlayState()) {
                case AudioTrack.PLAYSTATE_PAUSED:
                case AudioTrack.PLAYSTATE_PLAYING:
                    hrAbnormalWarnAudio.stop();
                    break;
            }
            hrAbnormalWarnAudio.reloadStaticData();
        }
        hrAbnormalWarnAudio.play();
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

        hrAbnormalWarnAudio = new AudioTrack(AudioManager.STREAM_MUSIC, fs,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT, length, AudioTrack.MODE_STATIC);
        hrAbnormalWarnAudio.write(wave, 0, wave.length);
        hrAbnormalWarnAudio.write(wave, 0, wave.length);
    }

    private void runOnUiThread(Runnable runnable) {
        if(getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }

    @Override
    public void onShowStateUpdated(boolean isShow) {
        if(isShow) {
            tvPauseShowing.setVisibility(View.GONE);
        } else {
            tvPauseShowing.setVisibility(View.VISIBLE);
        }
    }
}
