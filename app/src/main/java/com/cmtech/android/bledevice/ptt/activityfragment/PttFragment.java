package com.cmtech.android.bledevice.ptt.activityfragment;

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

import com.cmtech.android.bledevice.ptt.model.OnPttListener;
import com.cmtech.android.bledevice.ptt.model.PttDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.CtrlPanelAdapter;
import com.cmtech.android.bledeviceapp.fragment.DeviceFragment;
import com.cmtech.android.bledeviceapp.view.OnWaveViewListener;
import com.cmtech.android.bledeviceapp.view.ScanEcgView;
import com.cmtech.android.bledeviceapp.view.ScanPpgView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cmtech.android.bledevice.ptt.model.PttDevice.DEFAULT_ECG_1MV_CALI;
import static com.cmtech.android.bledevice.ptt.model.PttDevice.DEFAULT_PPG_CALI;
import static com.cmtech.android.bledeviceapp.view.ScanWaveView.DEFAULT_ZERO_LOCATION;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ptt.activityfragment
 * ClassName:      PttFragment
 * Description:    PTT monitor fragment
 * Author:         chenm
 * CreateDate:     2020-12-11 06:06
 * UpdateUser:     chenm
 * UpdateDate:
 * UpdateRemark:
 * Version:        1.0
 */
public class PttFragment extends DeviceFragment implements OnPttListener, OnWaveViewListener {
    private PttDevice device; // device

    private ScanEcgView ecgView; // ECG View
    private ScanPpgView ppgView; // PPG View
    private TextView tvMessage; // message

    private ViewPager pager;
    private CtrlPanelAdapter fragAdapter;
    private final PttRecordFragment pttRecFrag = new PttRecordFragment(); // PTT record fragment

    public PttFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (PttDevice) getDevice();
        return inflater.inflate(R.layout.fragment_device_ptt, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvMessage = view.findViewById(R.id.tv_ppg_message);

        ecgView = view.findViewById(R.id.ecg_view);
        ecgView.setup(device.getSampleRate(), DEFAULT_ECG_1MV_CALI, DEFAULT_ZERO_LOCATION);

        ppgView = view.findViewById(R.id.ppg_view);
        ppgView.setup(device.getSampleRate(), DEFAULT_PPG_CALI, DEFAULT_ZERO_LOCATION);

        pager = view.findViewById(R.id.ptt_control_panel_viewpager);
        TabLayout layout = view.findViewById(R.id.ptt_control_panel_tab);
        List<Fragment> fragmentList = new ArrayList<Fragment>(Collections.singletonList(pttRecFrag));
        String title = getResources().getString(PttRecordFragment.TITLE_ID);
        List<String> titleList = new ArrayList<>(Collections.singletonList(title));
        fragAdapter = new CtrlPanelAdapter(getChildFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(1);
        layout.setupWithViewPager(pager);

        device.setListener(this);
        ecgView.setListener(this);
        ppgView.setListener(this);

        // 打开设备
        device.open();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void openConfigureActivity() {

    }

    @Override
    public void onFragmentUpdated(final int sampleRate, final int caliValue, final float zeroLocation) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ecgView.setup(sampleRate, DEFAULT_ECG_1MV_CALI, zeroLocation);
                    ppgView.setup(sampleRate, caliValue, zeroLocation);
                }
            });
        }
    }

    @Override
    public void onPttSignalShowed(int ecgSignal, int ppgSignal) {
        ecgView.addData(ecgSignal);
        ppgView.addData(ppgSignal);
    }

    @Override
    public void onPttSignalRecordStatusChanged(boolean isRecord) {
        pttRecFrag.updateRecordStatus(isRecord);
    }

    @Override
    public void onPttSignalRecordTimeUpdated(int second) {
        pttRecFrag.setPttRecordTime(second);
    }

    @Override
    public void onPttSignalShowStatusUpdated(boolean isShow) {
        if(isShow) {
            ecgView.startShow();
            ppgView.startShow();
        } else {
            ecgView.stopShow();
            ppgView.stopShow();
        }
    }

    @Override
    public void onShowStateUpdated(boolean show) {
        if(show) {
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

        ecgView.stopShow();
        ppgView.stopShow();
    }

    public void setPttRecord(boolean isRecord) {
        if(device != null) {
            device.setPttRecord(isRecord);
        }
    }
}
