package com.cmtech.android.bledevice.ppg.activityfragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.cmtech.android.bledevice.ppg.model.OnPpgListener;
import com.cmtech.android.bledevice.ppg.model.PpgDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.CtrlPanelAdapter;
import com.cmtech.android.bledeviceapp.fragment.DeviceFragment;
import com.cmtech.android.bledeviceapp.view.OnWaveViewListener;
import com.cmtech.android.bledeviceapp.view.ScanPpgView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class PpgFragment extends DeviceFragment implements OnPpgListener, OnWaveViewListener {
    private PpgDevice device; // device

    private ScanPpgView ppgView; // PpgView
    private TextView tvMessage; // message

    private ViewPager pager;
    private CtrlPanelAdapter fragAdapter;
    private final PpgRecordFragment ppgRecFrag = new PpgRecordFragment(); // ppg record fragment

    public PpgFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (PpgDevice) getDevice();
        return inflater.inflate(R.layout.fragment_device_ppg, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvMessage = view.findViewById(R.id.tv_message);

        ppgView = view.findViewById(R.id.ppg_view);
        ppgView.setup(device.getSampleRate(), device.getGain());

        pager = view.findViewById(R.id.ppg_control_panel_viewpager);
        TabLayout layout = view.findViewById(R.id.ppg_control_panel_tab);
        List<Fragment> fragmentList = new ArrayList<Fragment>(Arrays.asList(ppgRecFrag));
        String title = getResources().getString(PpgRecordFragment.TITLE_ID);
        List<String> titleList = new ArrayList<>(Arrays.asList(title));
        fragAdapter = new CtrlPanelAdapter(getChildFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(1);
        layout.setupWithViewPager(pager);

        device.setListener(this);
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
    public void onFragmentUpdated(final int sampleRate, final int gain) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ppgView.setup(sampleRate, gain);
                }
            });
        }
    }

    @Override
    public void onPpgSignalShowed(final int ppgSignal) {
        ppgView.addData(new int[]{ppgSignal});
    }

    @Override
    public void onPpgSignalRecordStatusChanged(boolean isRecord) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ppgRecFrag.updateRecordStatus(isRecord);
                }
            });
        }
    }

    @Override
    public void onPpgSignalRecordTimeUpdated(int second) {
        ppgRecFrag.setPpgRecordTime(second);
    }

    @Override
    public void onPpgSignalShowStatusUpdated(boolean isShow) {
        if(isShow) {
            ppgView.startShow();
            //eegView.initialize();
        } else {
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

        ppgView.stopShow();
    }

    public void setPpgRecord(boolean isRecord) {
        if(device != null) {
            device.setRecord(isRecord);
        }
    }
}
