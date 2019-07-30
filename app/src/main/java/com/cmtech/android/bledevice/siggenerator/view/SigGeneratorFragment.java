package com.cmtech.android.bledevice.siggenerator.view;

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
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgControllerAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDeviceConfig;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorState;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.ecghrprocess.EcgHrStatisticInfoAnalyzer;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgHrStatisticsFragment;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgMonitorConfigureActivity;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgMonitorFragment;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgSignalRecordFragment;
import com.cmtech.android.bledevice.unknown.UnknownDeviceFragment;
import com.cmtech.android.bledevice.viewcomponent.ScanWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.BleDeviceFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class SigGeneratorFragment extends BleDeviceFragment {
    private static final String TAG = "SigGeneratorFragment";

    TextView tvServices;
    TextView tvCharacteristic;

    public SigGeneratorFragment() {

    }

    public static SigGeneratorFragment newInstance() {
        return new SigGeneratorFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unknowndevice, container, false);

        tvServices = view.findViewById(R.id.tv_device_services);
        tvCharacteristic = view.findViewById(R.id.tv_device_characteristics);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void openConfigActivity() {

    }
}
