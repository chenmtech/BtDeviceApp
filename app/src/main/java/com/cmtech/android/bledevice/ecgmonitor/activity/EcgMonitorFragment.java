package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgMarkerAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgHrHistogram;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDeviceConfig;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorState;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgMonitorListener;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgAbnormal;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrRecorder;
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
import static android.graphics.Color.WHITE;
import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;

/**
 * EcgMonitorFragment: 心电带设备Fragment
 * Created by bme on 2018/3/13.
 */

public class EcgMonitorFragment extends BleDeviceFragment implements IEcgMonitorListener {
    private static final String TAG = "EcgMonitorFragment";
    private static final int COLOR_WHEN_REST = WHITE; // 安静状态下的波形颜色

    private TextView tvSampleRate; // 采样率
    private TextView tvLeadType; // 导联类型
    private TextView tvValue1mV; // 1mV定标值
    private TextView tvHeartRate; // 心率值
    private TextView tvRecordTime; // 记录时间
    private ImageButton ibSwitchSampleStatus; // 切换采样状态
    private ImageButton ibRecord; // 切换记录状态
    private CheckBox cbIsFilter; // 是否滤波
    private ImageButton ibStayRest; // 是否处于安静状态
    private TextView tvAverageHr; // 平均心率
    private TextView tvMaxHr; // 最大心率
    private ImageButton ibResetHistogram; // 重置心率直方图
    private ScanWaveView ecgView; // 心电波形View
    private FrameLayout flEcgView;
    private RelativeLayout rlHrStatistics;
    private RecyclerView rvEcgMarker; // ecg标记recycleview
    private EcgMarkerAdapter markerAdapter; // ecg标记adapter
    private AudioTrack hrWarnAudio; // 心率报警声音
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
                if(rlHrStatistics.getVisibility() == View.INVISIBLE) {
                    device.updateHrInfo();
                    rlHrStatistics.setVisibility(View.VISIBLE);
                    flEcgView.setVisibility(View.INVISIBLE);
                }
                else {
                    rlHrStatistics.setVisibility(View.INVISIBLE);
                    flEcgView.setVisibility(View.VISIBLE);
                }
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

        rvEcgMarker = view.findViewById(R.id.rv_ecg_marker);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvEcgMarker.setLayoutManager(linearLayoutManager);
        rvEcgMarker.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        List<EcgAbnormal> ecgAbnormals = new ArrayList<>(Arrays.asList(EcgAbnormal.values()));
        markerAdapter = new EcgMarkerAdapter(ecgAbnormals, new EcgMarkerAdapter.OnMarkerClickListener() {
            @Override
            public void onMarkerClicked(EcgAbnormal marker) {
                if(device != null)
                    device.addAppendixContent("第" + device.getRecordDataNum() / device.getSampleRate() + "秒，" + marker.getDescription());
            }
        });
        rvEcgMarker.setAdapter(markerAdapter);

        ibRecord = view.findViewById(R.id.ib_ecg_record);
        ibRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isRecord = !device.isRecord();
                device.setRecord(isRecord);
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

        ibStayRest = view.findViewById(R.id.ib_stay_rest);
        ibStayRest.setOnTouchListener(new View.OnTouchListener() {
            private int begin = 0;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case ACTION_DOWN:
                        ecgView.setWaveColor(COLOR_WHEN_REST);
                        //device.addAppendixContent("开始安静状态" + "@" + device.getRecordDataNum());
                        begin = (int)(device.getRecordDataNum()/device.getSampleRate());
                        break;
                    case ACTION_UP:
                    case ACTION_CANCEL:
                        ecgView.restoreDefaultWaveColor();
                        int end = (int)(device.getRecordDataNum()/device.getSampleRate());
                        device.addAppendixContent("第" + begin + '-' + end + "秒，" + "处于安静状态");
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        flEcgView = view.findViewById(R.id.fl_ecgview);
        rlHrStatistics = view.findViewById(R.id.rl_hr_statistics);
        hrHistogram = view.findViewById(R.id.bc_hr_histogram);
        tvAverageHr = view.findViewById(R.id.tv_average_hr_value);
        tvMaxHr = view.findViewById(R.id.tv_max_hr_value);

        ibResetHistogram = view.findViewById(R.id.ib_reset_histogram);
        ibResetHistogram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device.resetHrInfo();
            }
        });

        // 根据设备的isRecord初始化Record按钮
        updateRecordStatus(device.isRecord());
        device.setEcgMonitorListener(EcgMonitorFragment.this);
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
            case 1: // 修改设备配置返回
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
            device.removeEcgMonitorListener();

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

        markerAdapter.setEnabled(isRecord);

        if(isRecord)
            ibStayRest.setVisibility(View.VISIBLE);
        else
            ibStayRest.setVisibility(View.INVISIBLE);

        ibStayRest.setEnabled(isRecord);
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
    public void updateEcgHrInfo(List<Integer> filteredHrList, List<EcgHrRecorder.HrHistogramElement<Float>> normHistogram, int maxHr, int averageHr) {
        hrHistogram.update(normHistogram);
        tvAverageHr.setText(String.valueOf(averageHr));
        tvMaxHr.setText(String.valueOf(maxHr));
    }

    @Override
    public void notifyHrAbnormal() {
        ViseLog.e("Hr Warn!");

        if(hrWarnAudio == null) {
            initHrWarnAudioTrack();
            hrWarnAudio.play();
        } else {
            switch(hrWarnAudio.getPlayState()) {
                case AudioTrack.PLAYSTATE_PAUSED:
                case AudioTrack.PLAYSTATE_PLAYING:
                    hrWarnAudio.stop();
                    hrWarnAudio.reloadStaticData();
                    hrWarnAudio.play();
                    break;
                case AudioTrack.PLAYSTATE_STOPPED:
                    hrWarnAudio.reloadStaticData();
                    hrWarnAudio.play();
                    break;
            }
        }
    }

    private void initHrWarnAudioTrack() {
        int length = 4000;
        int fs = 1000;
        RealSeq sinSeq = SeqUtil.createSinSeq(127.0, fs, 0, 44100, length);
        byte[] wave = new byte[length];
        for(int i = 0; i < wave.length; i++) {
            wave[i] = (byte)(double)sinSeq.get(i);
        }

        hrWarnAudio = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT, length, AudioTrack.MODE_STATIC);
        hrWarnAudio.write(wave, 0, wave.length);
        hrWarnAudio.write(wave, 0, wave.length);
    }

}
