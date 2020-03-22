package com.cmtech.android.bledevice.hrmonitor.view;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrmonitor.model.BleHeartRateData;
import com.cmtech.android.bledevice.hrmonitor.model.BleHrRecord10;
import com.cmtech.android.bledevice.hrmonitor.model.HRMonitorDevice;
import com.cmtech.android.bledevice.hrmonitor.model.HRMonitorConfiguration;
import com.cmtech.android.bledevice.hrmonitor.model.HrCtrlPanelAdapter;
import com.cmtech.android.bledevice.hrmonitor.model.OnHRMonitorDeviceListener;
import com.cmtech.android.bledevice.view.OnWaveViewListener;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.activity.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.cmtech.android.bledevice.hrmonitor.model.HRMonitorConfiguration.DEFAULT_HR_HIGH_LIMIT;
import static com.cmtech.android.bledevice.hrmonitor.model.HRMonitorConfiguration.DEFAULT_HR_LOW_LIMIT;
import static com.cmtech.android.bledevice.hrmonitor.model.HRMonitorConfiguration.DEFAULT_HR_WARN;
import static com.cmtech.android.bledevice.hrmonitor.view.HRMCfgActivity.RESULT_CHANGE_ECG_LOCK;
import static com.cmtech.android.bledevice.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.view
 * ClassName:      HRMonitorFragment
 * Description:    heart rate monitor fragment
 * Author:         chenm
 * CreateDate:     2020-02-04 06:06
 * UpdateUser:     chenm
 * UpdateDate:     2020-02-04 06:06
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HRMonitorFragment extends DeviceFragment implements OnHRMonitorDeviceListener, OnWaveViewListener {

    private HRMonitorDevice device; // device

    private ScanEcgView ecgView; // EcgView
    private TextView tvHrEcgOff; // hr when ecg off
    private TextView tvHrEcgOn; // hr when ecg on
    private TextView tvMessage; // message
    private ImageButton ibEcg; // ecg on/off
    private FrameLayout flEcgOff; // frame layout when ecg off
    private FrameLayout flEcgOn; // frame layout when ecg on

    private final HrRecordFragment recordFragment = new HrRecordFragment(); // heart rate record Fragment
    private final HrDebugFragment debugFragment = new HrDebugFragment(); // debug fragment

    private boolean isEcgChecked = false;

    private AudioTrack warnAudio; // 心率异常报警声音

    public HRMonitorFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (HRMonitorDevice) getDevice();
        return inflater.inflate(R.layout.fragment_hrmonitor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvHrEcgOff = view.findViewById(R.id.tv_hr_ecg_off);
        tvHrEcgOn = view.findViewById(R.id.tv_hr_ecg_on);
        tvMessage = view.findViewById(R.id.tv_message);

        flEcgOff = view.findViewById(R.id.fl_no_ecg);
        flEcgOff.setVisibility(View.VISIBLE);
        flEcgOn = view.findViewById(R.id.fl_with_ecg);
        flEcgOn.setVisibility(View.GONE);

        ecgView = view.findViewById(R.id.scanview_ecg);
        ecgView.setup(device.getSampleRate(), device.getCali1mV(), DEFAULT_ZERO_LOCATION);

        ibEcg = view.findViewById(R.id.ib_ecg);
        setEcgStatus(false);
        ibEcg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEcgChecked = !isEcgChecked;
                setEcgStatus(isEcgChecked);
                device.switchEcgSignal(isEcgChecked);
            }
        });

        ViewPager pager = view.findViewById(R.id.vp_ecg_control_panel);
        TabLayout layout = view.findViewById(R.id.tl_ecg_control_panel);
        List<Fragment> fragmentList = new ArrayList<Fragment>(Arrays.asList(debugFragment, recordFragment));
        List<String> titleList = new ArrayList<>(Arrays.asList(HrDebugFragment.TITLE, HrRecordFragment.TITLE));
        HrCtrlPanelAdapter fragAdapter = new HrCtrlPanelAdapter(getChildFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(2);
        layout.setupWithViewPager(pager);

        recordFragment.setDevice(device);

        device.setListener(this);
        ecgView.setListener(this);

        // 打开设备
        MainActivity activity = (MainActivity) getActivity();
        device.open(activity.getNotifyService());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1) { // cfg return
            if(resultCode == RESULT_CHANGE_ECG_LOCK) {
                boolean ecgLock = data.getBooleanExtra("ecg_lock", true);
                device.lockEcg(ecgLock);
            } else if(resultCode == RESULT_OK) {
                HRMonitorConfiguration cfg = new HRMonitorConfiguration();
                cfg.setHrLow(data.getIntExtra("hr_low", DEFAULT_HR_LOW_LIMIT));
                cfg.setHrHigh(data.getIntExtra("hr_high", DEFAULT_HR_HIGH_LIMIT));
                cfg.setWarn(data.getBooleanExtra("is_warn", DEFAULT_HR_WARN));
                device.updateConfig(cfg);
            }
        }
    }

    @Override
    public void openConfigureActivity() {
        HRMonitorConfiguration cfg = device.getConfig();

        Intent intent = new Intent(getActivity(), HRMCfgActivity.class);
        intent.putExtra("ecg_lock", device.isEcgLock());
        intent.putExtra("hr_low", cfg.getHrLow());
        intent.putExtra("hr_high", cfg.getHrHigh());
        intent.putExtra("is_warn", cfg.isWarn());
        startActivityForResult(intent, 1);
    }

    @Override
    public void onHRUpdated(final BleHeartRateData hrData) {
        if(hrData != null && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    debugFragment.updateHrMeas(hrData.toString());

                    int bpm = hrData.getBpm();
                    tvHrEcgOn.setText(String.valueOf(bpm));
                    tvHrEcgOff.setText(String.valueOf(bpm));

                    HRMonitorConfiguration cfg = device.getConfig();
                    if(cfg.isWarn() && (bpm > cfg.getHrHigh() || bpm < cfg.getHrLow())) {
                        warn();
                    }
                }
            });
        }
    }

    @Override
    public void onHRStatInfoUpdated(final List<Short> hrList, final short hrMax, final short hrAve, List<BleHrRecord10.HrHistogramElement<Integer>> hrHistogram) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recordFragment.updateHrInfo(hrList, hrMax, hrAve);
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
                    debugFragment.updateHrSensLoc(String.valueOf(loc));
                }
            });
        }
    }

    @Override
    public void onHRCtrlPtUpdated(final int ctrl) {

    }

    @Override
    public void onFragmentUpdated(final int sampleRate, final int value1mV, final double zeroLocation, final boolean ecgLock) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ecgView.setup(sampleRate, value1mV, zeroLocation);
                    if(ecgLock) {
                        ibEcg.setVisibility(View.GONE);
                        flEcgOff.setVisibility(View.VISIBLE);
                        flEcgOn.setVisibility(View.GONE);
                        ecgView.stop();
                    } else {
                        ibEcg.setVisibility(View.VISIBLE);
                        isEcgChecked = false;
                        setEcgStatus(false);
                    }
                }
            });
        }
    }

    @Override
    public void onEcgSignalShowed(final int ecgSignal) {
        ecgView.showData(ecgSignal);
    }

    @Override
    public void onShowStateUpdated(boolean isShow) {
        if(isShow) {
            tvMessage.setVisibility(View.GONE);
        } else {
            tvMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(device != null)
            device.removeListener();

        ecgView.stop();
    }

    private void setEcgStatus(boolean isChecked) {
        if(isChecked) {
            flEcgOff.setVisibility(View.GONE);
            flEcgOn.setVisibility(View.VISIBLE);
            ecgView.start();
            ecgView.initialize();
        } else {
            flEcgOff.setVisibility(View.VISIBLE);
            flEcgOn.setVisibility(View.GONE);
            ecgView.stop();
        }
    }

    public void warn() {
        if(warnAudio == null) {
            initWarnAudio();
        } else {
            switch(warnAudio.getPlayState()) {
                case AudioTrack.PLAYSTATE_PAUSED:
                case AudioTrack.PLAYSTATE_PLAYING:
                    warnAudio.stop();
                    break;
            }
            warnAudio.reloadStaticData();
        }
        warnAudio.play();
    }

    private void initWarnAudio() {
        int length = 4000;
        int f = 1000;
        int fs = 44100;
        float mag = 127.0f;
        double omega = 2 * Math.PI * f/fs;

        byte[] wave = new byte[length];
        for(int i = 0; i < length; i++) {
            wave[i] = (byte) (mag * Math.sin(omega * i));
        }

        warnAudio = new AudioTrack(AudioManager.STREAM_MUSIC, fs,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT, length, AudioTrack.MODE_STATIC);
        warnAudio.write(wave, 0, wave.length);
        warnAudio.write(wave, 0, wave.length);
    }
}
