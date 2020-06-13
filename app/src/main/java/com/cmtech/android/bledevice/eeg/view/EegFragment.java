package com.cmtech.android.bledevice.eeg.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.bledevice.eeg.model.EegDevice;
import com.cmtech.android.bledevice.eeg.model.OnEegListener;
import com.cmtech.android.bledevice.view.OnWaveViewListener;
import com.cmtech.android.bledevice.view.ScanEegView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.cmtech.android.bledeviceapp.adapter.CtrlPanelAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cmtech.android.bledevice.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.view
 * ClassName:      EegFragment
 * Description:    EEG monitor fragment
 * Author:         chenm
 * CreateDate:     2020-06-11 06:06
 * UpdateUser:     chenm
 * UpdateDate:     2020-06-11 06:06
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class EegFragment extends DeviceFragment implements OnEegListener, OnWaveViewListener {
    private EegDevice device; // device

    private ScanEegView eegView; // EegView
    private TextView tvMessage; // message

    private ViewPager pager;
    private CtrlPanelAdapter fragAdapter;
    private final EegRecordFragment eegRecFrag = new EegRecordFragment(); // eeg record fragment

    public EegFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (EegDevice) getDevice();
        device.setContext(getContext());
        return inflater.inflate(R.layout.fragment_device_eeg, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvMessage = view.findViewById(R.id.tv_message);

        eegView = view.findViewById(R.id.eeg_view);
        eegView.setup(device.getSampleRate(), device.getCaliValue(), DEFAULT_ZERO_LOCATION);

        pager = view.findViewById(R.id.eeg_control_panel_viewpager);
        TabLayout layout = view.findViewById(R.id.eeg_control_panel_tab);
        List<Fragment> fragmentList = new ArrayList<Fragment>(Arrays.asList(eegRecFrag));
        String title = getResources().getString(EegRecordFragment.TITLE_ID);
        List<String> titleList = new ArrayList<>(Arrays.asList(title));
        fragAdapter = new CtrlPanelAdapter(getChildFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(1);
        layout.setupWithViewPager(pager);

        device.setListener(this);
        eegView.setListener(this);

        // 打开设备
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null)
            device.open(activity.getNotiService());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void openConfigureActivity() {

    }

    @Override
    public void onFragmentUpdated(final int sampleRate, final int value1mV, final double zeroLocation) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    eegView.setup(sampleRate, value1mV, zeroLocation);
                }
            });
        }
    }

    @Override
    public void onEegSignalShowed(final int eegSignal) {
        eegView.showData(eegSignal);
    }

    @Override
    public void onEegSignalRecordStatusChanged(boolean isRecord) {
        eegRecFrag.updateRecordStatus(isRecord);
    }

    @Override
    public void onEegSignalRecordTimeUpdated(int second) {
        eegRecFrag.setEcgRecordTime(second);
    }

    @Override
    public void onEegSignalShowStatusUpdated(boolean isShow) {
        if(isShow) {
            eegView.start();
            //eegView.initialize();
        } else {
            eegView.stop();
        }
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

        eegView.stop();
    }

    public void setEegRecord(boolean isRecord) {
        if(device != null) {
            device.setEegRecord(isRecord);
        }
    }
}
