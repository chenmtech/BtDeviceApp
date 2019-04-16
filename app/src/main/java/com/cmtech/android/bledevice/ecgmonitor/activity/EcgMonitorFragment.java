package com.cmtech.android.bledevice.ecgmonitor.activity;

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
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledevice.core.BleDeviceFragment;
import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgControllerAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDeviceConfig;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorState;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgMonitorListener;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrRecorder;
import com.cmtech.android.bledevice.view.ScanWaveView;
import com.cmtech.android.bledeviceapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

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

public class EcgMonitorFragment extends BleDeviceFragment implements IEcgMonitorListener {
    private static final String TAG = "EcgMonitorFragment";

    private TextView tvSampleRate; // 采样率
    private TextView tvLeadType; // 导联类型
    private TextView tvValue1mV; // 1mV定标值
    private TextView tvHeartRate; // 心率值
    //private TextView tvRecordTime; // 记录信号时长
    //private TextView tvAverageHr; // 平均心率
    //private TextView tvMaxHr; // 最大心率

    private ImageButton ibResetHrLineChart; // 重置心率图
    //private ImageButton ibRecord; // 切换记录信号状态

    private ScanWaveView ecgView; // 心电波形View

    //private LinearLayout llSignalOperator; // 信号操控布局
    //private LinearLayout llHrAnalysis; // 心率分析布局

    //private RecyclerView rvMarker; // 标记recycleview

    //private EcgMarkerAdapter markerAdapter; // ecg标记adapter

    //private EcgHrLineChart hrLineChart;

    private AudioTrack hrWarnAudio; // 心率报警声音

    private EcgControllerAdapter controllerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private List<Fragment> fragmentList = new ArrayList<>();

    private List<String> titleList = new ArrayList<>();

    private EcgSamplingSignalFragment samplingSignalFragment = new EcgSamplingSignalFragment();
    private EcgHrStatisticsFragment hrStatisticsFragment = new EcgHrStatisticsFragment();

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
        //tvRecordTime = view.findViewById(R.id.tv_ecg_signal_recordtime);
        //rvMarker = view.findViewById(R.id.rv_ecg_marker);
        //ibRecord = view.findViewById(R.id.ib_ecg_record);
        //llSignalOperator = view.findViewById(R.id.ll_sample_ecgsignal);
        //llHrAnalysis = view.findViewById(R.id.ll_hr_statistics);
        //hrLineChart = view.findViewById(R.id.linechart_hr);


        tvSampleRate.setText(String.valueOf(device.getSampleRate()));

        tvLeadType.setText(String.format("L%s", device.getLeadType().getDescription()));

        setCalibrationValue(device.getValue1mVBeforeCalibrate(), device.getValue1mVAfterCalibrate());

        tvHeartRate.setText("");
        tvHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(llHrAnalysis.getVisibility() == View.INVISIBLE) {
                    device.updateHrInfo();

                    llHrAnalysis.setVisibility(View.VISIBLE);
                    llSignalOperator.setVisibility(View.INVISIBLE);
                }
                else {
                    llHrAnalysis.setVisibility(View.INVISIBLE);
                    llSignalOperator.setVisibility(View.VISIBLE);
                }*/
            }
        });

        initialEcgView();


        /*LinearLayoutManager markerLayoutManager = new LinearLayoutManager(getContext());
        markerLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        rvMarker.setLayoutManager(markerLayoutManager);
        if(getContext() != null)
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

        // 根据设备的isRecord初始化Record按钮
        setSignalRecordStatus(device.isRecordEcgSignal());
        ibRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                device.setEcgSignalRecord(!device.isRecordEcgSignal());
            }
        });*/

        //tvAverageHr = view.findViewById(R.id.tv_average_hr_value);
        //tvMaxHr = view.findViewById(R.id.tv_max_hr_value);

        /*ibResetHrLineChart = view.findViewById(R.id.ib_reset_histogram);
        ibResetHrLineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device.resetHrInfo();
            }
        });*/

        viewPager = view.findViewById(R.id.vp_ecg_controller);
        tabLayout = view.findViewById(R.id.tl_ecg_controller);
        fragmentList.add(samplingSignalFragment);
        fragmentList.add(hrStatisticsFragment);
        titleList.add("信号采集");
        titleList.add("心率信息");
        controllerAdapter = new EcgControllerAdapter(getChildFragmentManager(), getContext(), fragmentList, titleList);
        viewPager.setAdapter(controllerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        samplingSignalFragment.setDevice(device);

        //setSignalSecNum(device.getEcgSignalRecordSecond());

        setDeviceState(device.getState());

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
            case 1: // 设置设备配置返回码
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

        if(hrWarnAudio != null)
            hrWarnAudio.stop();
    }

    @Override
    public void onDeviceStateUpdated(final EcgMonitorState state) {
        setDeviceState(state);
    }

    private void setDeviceState(final EcgMonitorState state) {

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
    public void onCalibrationValueChanged(final int calibrationValueBefore, final int calibrationValueAfter) {
        setCalibrationValue(calibrationValueBefore, calibrationValueAfter);
    }

    private void setCalibrationValue(final int calibrationValueBefore, final int calibrationValueAfter) {
        tvValue1mV.setText(String.format(Locale.getDefault(), "%d/%d", calibrationValueBefore, calibrationValueAfter));
    }

    @Override
    public void onSignalRecordStateUpdated(final boolean isRecord) {
        samplingSignalFragment.setSignalRecordStatus(isRecord);
    }

    /*private void setSignalRecordStatus(final boolean isRecord) {
        int imageId = (isRecord) ? R.mipmap.ic_ecg_record_start : R.mipmap.ic_ecg_record_stop;

        ibRecord.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), imageId));

        markerAdapter.setEnabled(isRecord);
    }*/

    @Override
    public void onEcgViewUpdated(final int xPixelPerData, final float yValuePerPixel, final int gridPixels) {
        updateEcgView(xPixelPerData, yValuePerPixel, gridPixels);
    }

    private void updateEcgView(final int xPixelPerData, final float yValuePerPixel, final int gridPixels) {
        ecgView.setResolution(xPixelPerData, yValuePerPixel);
        ecgView.setGridPixels(gridPixels);
        ecgView.setZeroLocation(0.5);
        ecgView.initView();
    }

    private void initialEcgView() {
        updateEcgView(device.getXPixelPerData(), device.getYValuePerPixel(), device.getPixelPerGrid());
    }

    @Override
    public void onEcgSignalChanged(final int ecgSignal) {
        ecgView.showData(ecgSignal);
    }

    @Override
    public void onSignalSecNumChanged(final int second) {
        setSignalSecNum(second);
    }

    private void setSignalSecNum(final int second) {
        samplingSignalFragment.setSignalSecNum(second);
    }

    @Override
    public void onEcgHrChanged(final int hr) {
        tvHeartRate.setText(String.valueOf(hr));
    }

    @Override
    public void onEcgHrInfoUpdated(List<Short> filteredHrList, List<EcgHrRecorder.HrHistogramElement<Float>> normHistogram, short maxHr, short averageHr) {
        //tvAverageHr.setText(String.valueOf(averageHr));
        //tvMaxHr.setText(String.valueOf(maxHr));

        //hrLineChart.showLineChart(filteredHrList, "心率变化图", Color.BLUE);
    }

    @Override
    public void onNotifyHrAbnormal() {
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

}
