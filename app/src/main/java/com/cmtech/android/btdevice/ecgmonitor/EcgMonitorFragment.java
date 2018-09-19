package com.cmtech.android.btdevice.ecgmonitor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate.IEcgMonitorState;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;
import com.vise.log.ViseLog;

/**
 * Created by bme on 2018/3/13.
 */

public class EcgMonitorFragment extends BleDeviceFragment implements IEcgMonitorObserver{
    private TextView tvEcgSampleRate;
    private TextView tvEcgLeadType;
    private TextView tvEcg1mV;
    private WaveView ecgView;
    private ImageButton btnSwitchSampleEcg;
    private CheckBox cbEcgRecord;
    private CheckBox cbEcgFilter;


    public EcgMonitorFragment() {

    }

    public static EcgMonitorFragment newInstance() {
        return new EcgMonitorFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((EcgMonitorDevice)device).registerEcgMonitorObserver(this);
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
        btnSwitchSampleEcg = view.findViewById(R.id.btn_ecg_startandstop);
        cbEcgRecord = view.findViewById(R.id.cb_ecg_record);
        cbEcgFilter = view.findViewById(R.id.cb_ecg_filter);


        btnSwitchSampleEcg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EcgMonitorDeviceController)controller).switchSampleState();
            }
        });

        cbEcgRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((EcgMonitorDeviceController)controller).isRecordEcg(b);
            }
        });

        cbEcgFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((EcgMonitorDeviceController)controller).isFilterEcg(b);
            }
        });
    }

    @Override
    public void updateState(IEcgMonitorState state) {
        if(state.canStart()) {
            btnSwitchSampleEcg.setImageDrawable(getResources().getDrawable(R.mipmap.ic_ecg_play_48px));
            btnSwitchSampleEcg.setClickable(true);
        } else if(state.canStop()) {
            btnSwitchSampleEcg.setImageDrawable(getResources().getDrawable(R.mipmap.ic_ecg_pause_48px));
            btnSwitchSampleEcg.setClickable(true);
        } else {
            btnSwitchSampleEcg.setClickable(false);
        }
    }

    @Override
    public void updateSampleRate(int sampleRate) {
        tvEcgSampleRate.setText(""+sampleRate);
    }

    @Override
    public void updateLeadType(EcgLeadType leadType) {
        tvEcgLeadType.setText(leadType.getDescription());
    }

    @Override
    public void updateCalibrationValue(int calibrationValue) {
        tvEcg1mV.setText("" + calibrationValue);
    }

    @Override
    public void updateEcgView(int xRes, float yRes, int viewGridWidth) {
        ecgView.setRes(xRes, yRes);
        ecgView.setGridWidth(viewGridWidth);
        ecgView.setZeroLocation(0.5);
        ecgView.startShow();
    }

    @Override
    public void updateEcgData(int ecgData) {
        ecgView.addData(ecgData);
    }
}
