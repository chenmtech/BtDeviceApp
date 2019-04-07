package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cmtech.android.bledevice.core.BleDeviceFragment;
import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgMarkerAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgHrLineChart;
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

public class EcgMonitorFragment extends BleDeviceFragment implements IEcgMonitorListener {
    private static final String TAG = "EcgMonitorFragment";

    private TextView tvSampleRate; // 采样率
    private TextView tvLeadType; // 导联类型
    private TextView tvValue1mV; // 1mV定标值
    private TextView tvHeartRate; // 心率值
    private TextView tvRecordTime; // 记录信号时长
    private TextView tvAverageHr; // 平均心率
    private TextView tvMaxHr; // 最大心率

    private ImageButton ibResetHrLineChart; // 重置心率图
    private ImageButton ibRecord; // 切换记录信号状态

    private ScanWaveView ecgView; // 心电波形View

    private LinearLayout llSignalOperator; // 信号操控布局
    private LinearLayout llHrAnalysis; // 心率分析布局

    private RecyclerView rvMarker; // 标记recycleview

    private EcgMarkerAdapter markerAdapter; // ecg标记adapter

    private EcgHrLineChart hrLineChart;

    private AudioTrack hrWarnAudio; // 心率报警声音

    private EcgMonitorDevice device; // 设备

    public EcgMonitorFragment() {

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
        tvRecordTime = view.findViewById(R.id.tv_ecg_recordtime);
        rvMarker = view.findViewById(R.id.rv_ecg_marker);
        ibRecord = view.findViewById(R.id.ib_ecg_record);
        llSignalOperator = view.findViewById(R.id.ll_signal_operator);
        llHrAnalysis = view.findViewById(R.id.ll_hr_statistics);
        hrLineChart = view.findViewById(R.id.linechart_hr);


        tvSampleRate.setText(String.valueOf(device.getSampleRate()));

        tvLeadType.setText(String.format("L%s", device.getLeadType().getDescription()));

        tvValue1mV.setText(String.format(Locale.getDefault(), "%d/%d", device.getValue1mVBeforeCalibrate(), device.getValue1mVAfterCalibrate()));

        tvHeartRate.setText(String.valueOf(0));
        tvHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(llHrAnalysis.getVisibility() == View.INVISIBLE) {
                    device.updateHrInfo();

                    llHrAnalysis.setVisibility(View.VISIBLE);
                    llSignalOperator.setVisibility(View.INVISIBLE);
                }
                else {
                    llHrAnalysis.setVisibility(View.INVISIBLE);
                    llSignalOperator.setVisibility(View.VISIBLE);
                }
            }
        });

        onUpdateEcgView(device.getxPixelPerData(), device.getyValuePerPixel(), device.getPixelPerGrid());

        tvRecordTime.setText(DateTimeUtil.secToTime(device.getEcgSignalRecordSecond()));

        onUpdateState(device.getState());
        
        LinearLayoutManager markerLayoutManager = new LinearLayoutManager(getContext());
        markerLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        rvMarker.setLayoutManager(markerLayoutManager);
        rvMarker.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        List<EcgAbnormal> ecgAbnormals = new ArrayList<>(Arrays.asList(EcgAbnormal.values()));
        markerAdapter = new EcgMarkerAdapter(ecgAbnormals, new EcgMarkerAdapter.OnMarkerClickListener() {
            @Override
            public void onMarkerClicked(EcgAbnormal marker) {
                if(device != null)
                    device.addCommentContent(DateTimeUtil.secToTimeInChinese((int)(device.getEcgSignalRecordDataNum() / device.getSampleRate())) + '，' + marker.getDescription() + '；');
            }
        });

        rvMarker.setAdapter(markerAdapter);


        ibRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isRecord = !device.isRecordEcgSignal();
                device.setEcgSignalRecord(isRecord);
            }
        });



        tvAverageHr = view.findViewById(R.id.tv_average_hr_value);
        tvMaxHr = view.findViewById(R.id.tv_max_hr_value);



        /*ibResetHrLineChart = view.findViewById(R.id.ib_reset_histogram);
        ibResetHrLineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device.resetHrInfo();
            }
        });*/

        // 根据设备的isRecord初始化Record按钮
        onUpdateEcgSignalRecordStatus(device.isRecordEcgSignal());

        device.setEcgMonitorListener(EcgMonitorFragment.this);
    }

    @Override
    public void close() {
        final Dialog alertDialog = new AlertDialog.Builder(getContext()).
                setTitle("保存记录").
                setMessage("您要保存本次记录吗？").
                setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(device != null) {
                            device.setSaveEcgFile(true);
                        }
                        EcgMonitorFragment.super.close();
                    }
                }).
                setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).
                setNegativeButton("不保存", new DialogInterface.OnClickListener() {
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
    public void onUpdateState(final EcgMonitorState state) {

    }

    @Override
    public void onUpdateSampleRate(final int sampleRate) {
        tvSampleRate.setText(String.valueOf(sampleRate));
    }

    @Override
    public void onUpdateLeadType(final EcgLeadType leadType) {
        tvLeadType.setText(String.format("L%s", leadType.getDescription()));
    }

    @Override
    public void onUpdateCalibrationValue(final int calibrationValueBefore, final int calibrationValueAfter) {
        tvValue1mV.setText(String.format(Locale.getDefault(), "%d/%d", calibrationValueBefore, calibrationValueAfter));
    }

    @Override
    public void onUpdateEcgSignalRecordStatus(final boolean isRecord) {
        int imageId;
        if (isRecord)
            imageId = R.mipmap.ic_ecg_record_start;
        else
            imageId = R.mipmap.ic_ecg_record_stop;

        ibRecord.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), imageId));

        markerAdapter.setEnabled(isRecord);
    }

    @Override
    public void onUpdateEcgView(final int xPixelPerData, final float yValuePerPixel, final int gridPixels) {
        ecgView.setResolution(xPixelPerData, yValuePerPixel);
        ecgView.setGridPixels(gridPixels);
        ecgView.setZeroLocation(0.5);
        ecgView.initView();
    }

    @Override
    public void onUpdateEcgSignal(final int ecgSignal) {
        ecgView.showData(ecgSignal);
    }

    @Override
    public void onUpdateEcgSignalRecordSecond(final int second) {
        tvRecordTime.setText(DateTimeUtil.secToTime(second));
    }

    @Override
    public void onUpdateEcgHr(final int hr) {
        tvHeartRate.setText(String.valueOf(hr));
    }

    @Override
    public void onUpdateEcgHrInfo(List<Integer> filteredHrList, List<EcgHrRecorder.HrHistogramElement<Float>> normHistogram, int maxHr, int averageHr) {
        tvAverageHr.setText(String.valueOf(averageHr));
        tvMaxHr.setText(String.valueOf(maxHr));

        hrLineChart.showLineChart(filteredHrList, "心率时序图", Color.BLUE);
        Drawable drawable = getResources().getDrawable(R.drawable.hr_linechart_fade);
        hrLineChart.setChartFillDrawable(drawable);
    }

    @Override
    public void onNotifyHrAbnormal() {
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
