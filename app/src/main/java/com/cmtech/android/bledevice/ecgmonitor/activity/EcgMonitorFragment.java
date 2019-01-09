package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgHrHistogram;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDeviceConfig;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorState;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgMonitorObserver;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgAbnormal;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.view.ScanWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledevice.core.BleDeviceFragment;
import com.cmtech.dsp.seq.RealSeq;
import com.cmtech.dsp.util.SeqUtil;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * EcgMonitorFragment: 心电带设备Fragment
 * Created by bme on 2018/3/13.
 */

public class EcgMonitorFragment extends BleDeviceFragment implements IEcgMonitorObserver {
    private static final String TAG = "EcgMonitorFragment";

    private TextView tvSampleRate; // 采样率
    private TextView tvLeadType; // 导联类型
    private TextView tvValue1mV; // 1mV定标值
    private TextView tvHeartRate; // 心率值
    private TextView tvRecordTime; // 记录时间
    private ImageButton ibSwitchSampleStatus; // 切换采样状态
    private ImageButton ibRecord; // 切换记录状态
    private CheckBox cbIsFilter; // 是否滤波
    private CheckBox cbIsRest; // 是否处于安静状态
    private TextView tvTotalBeatTimes; // 总心跳次数
    private ImageButton ibResetHistogram; // 重置心率直方图
    private ScanWaveView ecgView; // 心电波形View
    private FrameLayout flEcgView;
    private RelativeLayout rlHrStatistics;
    private List<Button> commentBtnList = new ArrayList<>(); // 标记留言的Button
    int[] commentBtnId = new int[]{R.id.btn_ecgfeel_0, R.id.btn_ecgfeel_1, R.id.btn_ecgfeel_2};
    private AudioTrack audioTrack;
    private EcgMonitorDevice device; // 设备
    private EcgHrHistogram hrHistogram; // 心率直方图

    public EcgMonitorFragment() {

    }

    public static BleDeviceFragment newInstance(String macAddress) {
        BleDeviceFragment fragment = new EcgMonitorFragment();
        return BleDeviceFragment.pushMacAddressIntoFragmentArgument(macAddress, fragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (EcgMonitorDevice) getDevice();

        return inflater.inflate(R.layout.fragment_ecgmonitor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvSampleRate = view.findViewById(R.id.tv_ecg_samplerate);
        tvSampleRate.setText(String.valueOf(device.getSampleRate()));

        tvLeadType = view.findViewById(R.id.tv_ecg_leadtype);
        tvLeadType.setText(String.format("L%s", device.getLeadType().getDescription()));

        tvValue1mV = view.findViewById(R.id.tv_ecg_1mv);
        tvValue1mV.setText(String.format(Locale.getDefault(), "%d/%d", device.getValue1mVBeforeCalibrate(), device.getValue1mVAfterCalibrate()));

        tvHeartRate = view.findViewById(R.id.tv_ecg_hr);
        tvHeartRate.setText(String.valueOf(0));
        tvHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analysisHrStatistics();
            }
        });

        ecgView = view.findViewById(R.id.rwv_ecgview);
        updateEcgView(device.getxPixelPerData(), device.getyValuePerPixel(), device.getPixelPerGrid());

        tvRecordTime = view.findViewById(R.id.tv_ecg_recordtime);
        tvRecordTime.setText(DateTimeUtil.secToTime(device.getRecordSecond()));

        ibSwitchSampleStatus = view.findViewById(R.id.ib_ecgreplay_startandstop);
        updateState(device.getState());
        ibSwitchSampleStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                device.switchSampleState();
            }
        });

        for(int i = 0; i < commentBtnId.length; i++) {
            Button button = view.findViewById(commentBtnId[i]);
            final String commentDescription = EcgAbnormal.getDescriptionFromCode(i);
            button.setText(commentDescription);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    device.addComment(device.getRecordSecond(), commentDescription);
                }
            });
            commentBtnList.add(button);
        }

        ibRecord = view.findViewById(R.id.ib_ecg_record);
        // 根据设备的isRecord初始化Record按钮
        updateRecordStatus(device.isRecord());
        ibRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isRecord = !device.isRecord();
                device.setRecord(isRecord);

                /*for(Button button : commentBtnList) {
                    button.setEnabled(isRecord);
                }*/
            }
        });

        cbIsFilter = view.findViewById(R.id.cb_ecg_filter);
        cbIsFilter.setChecked(device.isFilter());
        cbIsFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                device.setFilter(b);
            }
        });

        cbIsRest = view.findViewById(R.id.cb_stay_rest);
        cbIsRest.setChecked(false);
        cbIsRest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    ecgView.setWaveColor(Color.YELLOW);
                } else {
                    ecgView.restoreDefaultWaveColor();
                }
            }
        });

        flEcgView = view.findViewById(R.id.fl_ecgview);
        rlHrStatistics = view.findViewById(R.id.rl_hr_statistics);
        hrHistogram = view.findViewById(R.id.bc_hr_histogram);
        tvTotalBeatTimes = view.findViewById(R.id.tv_hr_totaltimes);

        ibResetHistogram = view.findViewById(R.id.ib_reset_histogram);
        ibResetHistogram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device.resetHrStatistics();
                updateHrHistogram(null);
            }
        });

        device.registerEcgMonitorObserver(EcgMonitorFragment.this);
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
            case 1:
                if(resultCode == RESULT_OK) {
                    EcgMonitorDeviceConfig config = (EcgMonitorDeviceConfig) data.getSerializableExtra("configuration");
                    device.setConfig(config);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(device != null)
            device.removeEcgMonitorObserver();

    }

    @Override
    public void updateState(final EcgMonitorState state) {
        if(state.canStart()) {
            ibSwitchSampleStatus.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_play_48px));
            ibSwitchSampleStatus.setClickable(true);
        } else if(state.canStop()) {
            ibSwitchSampleStatus.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_pause_48px));
            ibSwitchSampleStatus.setClickable(true);
        } else {
            ibSwitchSampleStatus.setClickable(false);
        }
    }

    @Override
    public void updateSampleRate(final int sampleRate) {
        tvSampleRate.setText(String.valueOf(sampleRate));
    }

    @Override
    public void updateLeadType(final EcgLeadType leadType) {
        tvLeadType.setText(String.format("L%s", leadType.getDescription()));
    }

    @Override
    public void updateCalibrationValue(final int calibrationValueBefore, final int calibrationValueAfter) {
        tvValue1mV.setText(String.format(Locale.getDefault(), "%d/%d", calibrationValueBefore, calibrationValueAfter));
    }

    @Override
    public void updateRecordStatus(final boolean isRecord) {
        int imageId;
        if (isRecord)
            imageId = R.mipmap.ic_ecg_record_start;
        else
            imageId = R.mipmap.ic_ecg_record_stop;
        ibRecord.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), imageId));
        for(Button button : commentBtnList) {
            button.setEnabled(isRecord);
        }
    }

    @Override
    public void updateEcgView(final int xPixelPerData, final float yValuePerPixel, final int gridPixels) {
        ecgView.setResolution(xPixelPerData, yValuePerPixel);
        ecgView.setGridPixels(gridPixels);
        ecgView.setZeroLocation(0.5);
        ecgView.initView();
    }

    @Override
    public void updateEcgSignal(final int ecgSignal) {
        ecgView.showData(ecgSignal);
    }

    @Override
    public void updateRecordSecond(final int second) {
        tvRecordTime.setText(DateTimeUtil.secToTime(second));
    }

    @Override
    public void updateEcgHr(final int hr) {
        tvHeartRate.setText(String.valueOf(hr));
    }

    @Override
    public void notifyHrAbnormal() {
        ViseLog.e("Hr Warn!");

        if(audioTrack == null) {
            initHrWarnAudioTrack();
            audioTrack.play();
        } else {
            switch(audioTrack.getPlayState()) {
                case AudioTrack.PLAYSTATE_PAUSED:
                case AudioTrack.PLAYSTATE_PLAYING:
                    audioTrack.stop();
                    audioTrack.reloadStaticData();
                    audioTrack.play();
                    break;
                case AudioTrack.PLAYSTATE_STOPPED:
                    audioTrack.reloadStaticData();
                    audioTrack.play();
                    break;
            }
        }
    }

    private void analysisHrStatistics() {
        if(device.getHrStatistics() != null) {
            int[] hrHistogram = Arrays.copyOf(device.getHrStatistics(), device.getHrStatistics().length);
            if(rlHrStatistics.getVisibility() == View.INVISIBLE) {
                updateHrHistogram(hrHistogram);
                rlHrStatistics.setVisibility(View.VISIBLE);
                flEcgView.setVisibility(View.INVISIBLE);
            }
            else {
                rlHrStatistics.setVisibility(View.INVISIBLE);
                flEcgView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateHrHistogram(int[] hrHistData) {
        hrHistogram.update(hrHistData);
        int sum = 0;

        if(hrHistData != null) {
            for (int num : hrHistData) {
                sum += num;
            }
        }
        tvTotalBeatTimes.setText(String.valueOf(sum));
    }

    private void initHrWarnAudioTrack() {
        int length = 4000;
        int fs = 1000;
        RealSeq sinSeq = SeqUtil.createSinSeq(127.0, fs, 0, 44100, length);
        byte[] wave = new byte[length];
        for(int i = 0; i < wave.length; i++) {
            wave[i] = (byte)(double)sinSeq.get(i);
        }

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT, length, AudioTrack.MODE_STATIC);
        audioTrack.write(wave, 0, wave.length);
        audioTrack.write(wave, 0, wave.length);
    }


}
