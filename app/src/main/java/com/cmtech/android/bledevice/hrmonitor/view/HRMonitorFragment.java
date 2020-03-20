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
import android.widget.Toast;

import com.cmtech.android.bledevice.hrmonitor.model.BleHeartRateData;
import com.cmtech.android.bledevice.hrmonitor.model.BleHrRecord10;
import com.cmtech.android.bledevice.hrmonitor.model.HRMonitorDevice;
import com.cmtech.android.bledevice.hrmonitor.model.HrConfiguration;
import com.cmtech.android.bledevice.hrmonitor.model.HrStatisticsInfo;
import com.cmtech.android.bledevice.hrmonitor.model.OnHRMonitorDeviceListener;
import com.cmtech.android.bledevice.view.OnWaveViewListener;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;
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
    public static final int HR_MOVE_AVERAGE_WINDOW_WIDTH = 10;
    private HRMonitorDevice device; // device
    private HrStatisticsInfo hrInfo = new HrStatisticsInfo(HR_MOVE_AVERAGE_WINDOW_WIDTH);  // heart rate statistics info

    private ScanEcgView ecgView; // EcgView
    private TextView tvHrEcgOff; // hr when ecg off
    private TextView tvHrEcgOn; // hr when ecg on
    private TextView tvMessage; // message
    private ImageButton ibEcg; // ecg on/off
    private FrameLayout flEcgOff; // frame layout when ecg off
    private FrameLayout flEcgOn; // frame layout when ecg on

    private final HrTimeFragment seqFragment = new HrTimeFragment(); // heart rate timing-sequence Fragment
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
        List<Fragment> fragmentList = new ArrayList<Fragment>(Arrays.asList(debugFragment, seqFragment));
        List<String> titleList = new ArrayList<>(Arrays.asList(HrDebugFragment.TITLE, HrTimeFragment.TITLE));
        EcgCtrlPanelAdapter fragAdapter = new EcgCtrlPanelAdapter(getChildFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(2);
        layout.setupWithViewPager(pager);

        device.setListener(this);
        ecgView.setListener(this);

        // 打开设备
        MainActivity activity = (MainActivity) getActivity();
        device.open(activity.getNotifyService());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: // cfg return
                if(resultCode == RESULT_FIRST_USER) {
                    boolean ecgLock = data.getBooleanExtra("ecg_lock", true);
                    device.lockEcg(ecgLock);
                } else if(resultCode == RESULT_OK) {
                    HrConfiguration cfg = new HrConfiguration();
                    cfg.setHrLow(data.getIntExtra("hr_low", 50));
                    cfg.setHrHigh(data.getIntExtra("hr_high", 180));
                    cfg.setWarn(data.getBooleanExtra("is_warn", true));
                    device.updateConfig(cfg);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void openConfigureActivity() {
        HrConfiguration cfg = device.getConfig();

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

                    if(hrInfo.process((short) bpm, hrData.getTime())) {
                        seqFragment.updateHrInfo(hrInfo);
                    }

                    HrConfiguration cfg = device.getConfig();
                    if(cfg.isWarn() && (bpm > cfg.getHrHigh() || bpm < cfg.getHrLow())) {
                        warn();
                    }
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

        if(hrInfo.getHrAveList().size() < 10) {
            Toast.makeText(getContext(), "由于时间太短，心率记录不被保存。", Toast.LENGTH_SHORT).show();
        } else {
            BleHrRecord10 hrRecord10 = BleHrRecord10.create(new byte[]{0x01,0x00}, device.getAddress(), AccountManager.getInstance().getAccount());
            if(hrRecord10 != null) {
                hrRecord10.setHrList(hrInfo.getHrAveList());
                hrRecord10.save();
                ViseLog.e(hrRecord10.toString());
            }
        }
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
