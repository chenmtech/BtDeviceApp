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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.cmtech.android.bledevice.ptt.model.OnPttListener;
import com.cmtech.android.bledevice.ptt.model.PttCfg;
import com.cmtech.android.bledevice.ptt.model.PttDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.CtrlPanelAdapter;
import com.cmtech.android.bledeviceapp.fragment.DeviceFragment;
import com.cmtech.android.bledeviceapp.view.OnWaveViewListener;
import com.cmtech.android.bledeviceapp.view.ScanEcgView;
import com.cmtech.android.bledeviceapp.view.ScanPpgView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.cmtech.android.bledevice.ptt.model.PttDevice.DEFAULT_ECG_CALI;
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
public class PttFragment extends DeviceFragment implements OnPttListener, OnWaveViewListener, AdapterView.OnItemSelectedListener {
    private static final int RC_CONFIG = 1;
    private PttDevice device; // device

    private ScanEcgView ecgView; // ECG View
    private ScanPpgView ppgView; // PPG View
    private TextView tvMessage; // message
    private EditText etPtt; // ptt
    private EditText etSbp;
    private EditText etDbp;
    private Spinner spinner;

    private ViewPager pager;
    private CtrlPanelAdapter fragAdapter;
    private final PttRecordFragment pttRecFrag = new PttRecordFragment(); // PTT record fragment
    private final PttCalibrationFragment pttCalibrationFrag = new PttCalibrationFragment(); // PTT Calibration fragment

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
        etPtt = view.findViewById(R.id.et_ptt);
        etSbp = view.findViewById(R.id.et_sbp);
        etDbp = view.findViewById(R.id.et_dbp);
        spinner = view.findViewById(R.id.spinner_ptt_type);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.ptt_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);

        ecgView = view.findViewById(R.id.ecg_view);
        ecgView.setup(device.getSampleRate(), DEFAULT_ECG_CALI, DEFAULT_ZERO_LOCATION);

        ppgView = view.findViewById(R.id.ppg_view);
        ppgView.setup(device.getSampleRate(), DEFAULT_PPG_CALI, DEFAULT_ZERO_LOCATION);

        pager = view.findViewById(R.id.ptt_control_panel_viewpager);
        TabLayout layout = view.findViewById(R.id.ptt_control_panel_tab);
        List<Fragment> fragmentList = new ArrayList<Fragment>(Arrays.asList(pttRecFrag));
        String title1 = getResources().getString(PttRecordFragment.TITLE_ID);
        String title2 = getResources().getString(PttCalibrationFragment.TITLE_ID);
        List<String> titleList = new ArrayList<>(Arrays.asList(title1));
        fragAdapter = new CtrlPanelAdapter(getChildFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(2);
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
        if(requestCode == RC_CONFIG) { // cfg return
            if(resultCode == RESULT_OK) {
                PttCfg cfg = (PttCfg) data.getSerializableExtra("ptt_cfg");
                device.updateConfig(cfg);
            }
        }
    }

    @Override
    public void openConfigureActivity() {
        PttCfg cfg = device.getConfig();

        Intent intent = new Intent(getActivity(), PttCfgActivity.class);
        intent.putExtra("ptt_cfg", cfg);
        startActivityForResult(intent, RC_CONFIG);
    }

    @Override
    public void onFragmentUpdated(final int sampleRate, final int ecgCaliValue, final int ppgCaliValue, final float zeroLocation) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ecgView.setup(sampleRate, ecgCaliValue, zeroLocation);
                    ppgView.setup(sampleRate, ppgCaliValue, zeroLocation);
                }
            });
        }
    }

    @Override
    public void onPttSignalShowed(int ecgSignal, int ppgSignal) {
        ecgView.addData(ecgSignal);
        ppgView.addData(ppgSignal);
        //ViseLog.e("show data:" + ecgSignal + "," + ppgSignal);
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
    public void onPttAndBpValueShowed(int ptt, int sbp, int dbp) {
        etPtt.setText(String.valueOf(ptt));
        etSbp.setText(String.valueOf(sbp));
        etDbp.setText(String.valueOf(dbp));
        //pttCalibrationFrag.addPttAndBpValue(ptt, sbp, dbp);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinner_ptt_type:
                device.setShowAveragePtt(position != 0);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
