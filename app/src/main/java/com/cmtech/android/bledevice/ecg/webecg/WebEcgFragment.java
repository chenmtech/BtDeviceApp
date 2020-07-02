package com.cmtech.android.bledevice.ecg.webecg;

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

import com.cmtech.android.bledevice.ecg.activity.EcgMonitorConfigureActivity;
import com.cmtech.android.bledevice.view.ScanEcgView;
import com.cmtech.android.bledeviceapp.adapter.CtrlPanelAdapter;
import com.cmtech.android.bledevice.ecg.device.EcgConfiguration;
import com.cmtech.android.bledevice.ecg.device.EcgHttpBroadcast;
import com.cmtech.android.bledevice.ecg.enumeration.EcgLeadType;
import com.cmtech.android.bledevice.ecg.enumeration.EcgMonitorState;
import com.cmtech.android.bledevice.ecg.fragment.EcgHrStatisticsFragment;
import com.cmtech.android.bledevice.ecg.fragment.EcgRecordFragment;
import com.cmtech.android.bledevice.ecg.interfac.IEcgDevice;
import com.cmtech.android.bledevice.ecg.process.hr.HrStatisticsInfo;
import com.cmtech.android.bledevice.view.OnWaveViewListener;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.activity.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.cmtech.android.bledevice.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

/**
  *
  * ClassName:      WebEcgFragment
  * Description:    网络心电监护仪Fragment
  * Author:         chenm
  * CreateDate:     2018/3/13 下午4:52
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/8 下午4:52
  * UpdateRemark:   优化代码
  * Version:        1.0
 */

public class WebEcgFragment extends DeviceFragment implements IEcgDevice.OnEcgDeviceListener, OnWaveViewListener {
    private static final String TAG = "WebEcgFragment";

    private TextView tvSampleRate; // 采样率
    private TextView tvLeadType; // 导联类型
    private TextView tvCaliValue1mV; // 1mV值
    private TextView tvHeartRate; // 心率值
    private TextView tvPauseMessage; // 暂停显示消息
    private ScanEcgView ecgView; // 心电波形View
    private AudioTrack hrAbnormalWarnAudio; // 心率异常报警声音
    private final EcgRecordFragment recordFragment = new EcgRecordFragment(); // 信号记录Fragment
    private final EcgHrStatisticsFragment hrFragment = new EcgHrStatisticsFragment(); // 心率统计Fragment
    private WebEcgDevice device; // 设备

    public WebEcgFragment() {
        super();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if(!(getDevice() instanceof WebEcgDevice)) {
            throw new IllegalStateException("The device type is wrong.");
        }
        device = (WebEcgDevice) getDevice();
        recordFragment.setDevice(device);
        return inflater.inflate(R.layout.fragment_ecg, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvSampleRate = view.findViewById(R.id.tv_ecg_sample_rate);
        tvLeadType = view.findViewById(R.id.tv_ecg_lead_type);
        tvCaliValue1mV = view.findViewById(R.id.tv_ecg_1mv_cali_value);
        tvHeartRate = view.findViewById(R.id.tv_ecg_hr);
        tvPauseMessage = view.findViewById(R.id.tv_message);
        ecgView = view.findViewById(R.id.roll_ecg_view);

        tvSampleRate.setText(String.valueOf(device.getSampleRate()));
        tvLeadType.setText(String.format("L%s", device.getLeadType().getDescription()));
        tvCaliValue1mV.setText(String.format(Locale.getDefault(), "%d/%d", device.getValue1mV(), device.getValue1mV()));
        tvHeartRate.setText("");

        initEcgView();
        ViewPager pager = view.findViewById(R.id.hrm_control_panel_viewpager);
        TabLayout layout = view.findViewById(R.id.hrm_control_panel_tab);
        List<Fragment> fragmentList = new ArrayList<Fragment>(Arrays.asList(hrFragment, recordFragment));
        List<String> titleList = new ArrayList<>(Arrays.asList(EcgHrStatisticsFragment.TITLE, EcgRecordFragment.TITLE));
        CtrlPanelAdapter fragAdapter = new CtrlPanelAdapter(getChildFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(1);
        layout.setupWithViewPager(pager);

        device.setEcgMonitorListener(this);
        ecgView.setListener(this);

        // 打开设备
        MainActivity activity = (MainActivity) getActivity();
        device.open(activity.getNotifyService());
    }

    private void initEcgView() {
        ecgView.setup(device.getSampleRate(), device.getValue1mV(), DEFAULT_ZERO_LOCATION);
    }

    @Override
    public void close() {
        if(device != null) {
            if (getContext() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle("保存记录").setMessage("是否保存记录？");
                builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (device != null) {
                            device.setSaveRecord(true);
                        }
                        WebEcgFragment.super.close();
                    }
                }).setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (device != null) {
                            device.setSaveRecord(false);
                        }
                        WebEcgFragment.super.close();
                    }
                });
                builder.create().show();
            }
        }
    }

    @Override
    public void openConfigureActivity() {
        Intent intent = new Intent(getActivity(), EcgMonitorConfigureActivity.class);
        intent.putExtra("device_configuration", device.getConfig());
        intent.putExtra("device_name", device.getName());
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: // 修改设备配置返回码
                if(resultCode == RESULT_OK) {
                    EcgConfiguration config = (EcgConfiguration) data.getSerializableExtra("configuration");
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
        if(hrAbnormalWarnAudio != null)
            hrAbnormalWarnAudio.stop();

        ecgView.stop();
    }

    @Override
    public void onStateUpdated(final EcgMonitorState state) {
        updateDeviceState(state);
    }

    private void updateDeviceState(final EcgMonitorState state) {

    }

    @Override
    public void onSampleRateUpdated(final int sampleRate) {
        tvSampleRate.setText(String.valueOf(sampleRate));
    }

    @Override
    public void onLeadTypeUpdated(final EcgLeadType leadType) {
        tvLeadType.setText(String.format("L%s", leadType.getDescription()));
    }

    @Override
    public void onValue1mVUpdated(final int value1mV, final int value1mVAfterCalibration) {
        tvCaliValue1mV.setText(String.format(Locale.getDefault(), "%d/%d", value1mV, value1mVAfterCalibration));
    }

    @Override
    public void onRecordStateUpdated(final boolean isRecord) {
        recordFragment.setRecordStatus(isRecord);
    }

    @Override
    public void onBroadcastStateUpdated(boolean isBroadcast) {

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
    public void onEcgSignalShowStateUpdated(boolean isStart) {
        if(isStart) {
            ecgView.start();
        } else {
            ecgView.stop();
        }
    }

    @Override
    public void onEcgSignalRecordSecondUpdated(final int second) {
        recordFragment.setSignalSecNum(second);
    }

    @Override
    public void onHrUpdated(final int hr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvHeartRate.setText(String.valueOf(hr));
            }
        });
    }

    @Override
    public void onHrStatisticsInfoUpdated(final HrStatisticsInfo hrStaticsInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hrFragment.updateHrInfo(hrStaticsInfo);
            }
        });
    }

    @Override
    public void onBroadcastInitialized(List<EcgHttpBroadcast.Receiver> receivers) {

    }

    @Override
    public void onReceiverUpdated() {

    }

    @Override
    public void onAbnormalHrNotified() {
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
            tvPauseMessage.setVisibility(View.GONE);
        } else {
            tvPauseMessage.setVisibility(View.VISIBLE);
        }
    }
}
