package com.cmtech.android.btdevice.ecgmonitor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;

/**
 * Created by bme on 2018/3/13.
 */

public class EcgMonitorFragment extends BleDeviceFragment {
    private TextView tvEcgSampleRate;
    private TextView tvEcgLeadType;
    private TextView tvEcg1mV;
    private WaveView ecgView;
    private ImageButton btnEcgStartandStop;
    private CheckBox cbEcgRecord;
    private CheckBox cbEcgFilter;


    public EcgMonitorFragment() {

    }

    public static EcgMonitorFragment newInstance() {
        return new EcgMonitorFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ecgmonitor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEcgSampleRate = (TextView)view.findViewById(R.id.tv_ecg_samplerate);
        tvEcgLeadType = (TextView)view.findViewById(R.id.tv_ecg_leadtype);
        tvEcg1mV = (TextView)view.findViewById(R.id.tv_ecg_1mv);
        ecgView = (WaveView)view.findViewById(R.id.ecg_view);
        btnEcgStartandStop = view.findViewById(R.id.btn_ecg_startandstop);
        cbEcgRecord = view.findViewById(R.id.cb_ecg_record);
        cbEcgFilter = view.findViewById(R.id.cb_ecg_filter);


        btnEcgStartandStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EcgMonitorDeviceController)controller).toggleSampleEcg();
            }
        });

        cbEcgRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((EcgMonitorDeviceController)controller).setEcgRecord(b);
            }
        });

        cbEcgFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((EcgMonitorDeviceController)controller).setEcgFilter(b);
            }
        });
    }





}
