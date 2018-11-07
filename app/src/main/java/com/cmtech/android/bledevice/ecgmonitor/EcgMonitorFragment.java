package com.cmtech.android.bledevice.ecgmonitor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate.IEcgMonitorState;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;

/**
 * Created by bme on 2018/3/13.
 */

public class EcgMonitorFragment extends BleDeviceFragment implements IEcgMonitorObserver{
    private TextView tvEcgSampleRate;
    private TextView tvEcgLeadType;
    private TextView tvEcg1mV;
    private TextView tvEcgHr;
    private ScanWaveView ecgView;
    private ImageButton btnSwitchSampleEcg;

    private ToggleButton tbEcgRecord;
    private CheckBox cbEcgFilter;

    private Button btnReplay;


    public EcgMonitorFragment() {

    }

    public static EcgMonitorFragment newInstance() {
        return new EcgMonitorFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((EcgMonitorDevice)getDevice()).registerEcgMonitorObserver(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ecgmonitor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEcgSampleRate = view.findViewById(R.id.tv_ecg_samplerate);
        tvEcgLeadType = view.findViewById(R.id.tv_ecg_leadtype);
        tvEcg1mV = view.findViewById(R.id.tv_ecg_1mv);
        tvEcgHr = view.findViewById(R.id.tv_ecg_hr);
        ecgView = view.findViewById(R.id.ecg_view);

        btnSwitchSampleEcg = view.findViewById(R.id.btn_ecg_startandstop);
        btnSwitchSampleEcg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EcgMonitorController)getController()).switchSampleState();
            }
        });

        tbEcgRecord = view.findViewById(R.id.tb_ecg_record);
        tbEcgRecord.setChecked(((EcgMonitorDevice)getDevice()).isRecord());
        tbEcgRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((EcgMonitorController)getController()).setEcgRecord(b);
                btnReplay.setClickable(!b);
            }
        });

        cbEcgFilter = view.findViewById(R.id.cb_ecg_filter);
        cbEcgFilter.setChecked(((EcgMonitorDevice)getDevice()).isEcgFilter());
        cbEcgFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((EcgMonitorController)getController()).setEcgFilter(b);
            }
        });

        btnReplay = view.findViewById(R.id.btn_ecg_replay);
        btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EcgMonitorController)getController()).replay();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(getDevice() != null)
            ((EcgMonitorDevice)getDevice()).removeEcgMonitorObserver();
    }

    @Override
    public void updateState(final IEcgMonitorState state) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    @Override
    public void updateSampleRate(final int sampleRate) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvEcgSampleRate.setText(""+sampleRate);
            }
        });
    }

    @Override
    public void updateLeadType(final EcgLeadType leadType) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvEcgLeadType.setText(leadType.getDescription());
            }
        });
    }

    @Override
    public void updateCalibrationValue(final int calibrationValue) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvEcg1mV.setText("" + calibrationValue);
            }
        });
    }

    @Override
    public void updateEcgView(final int xRes, final float yRes, final int viewGridWidth) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ecgView.setRes(xRes, yRes);
                ecgView.setGridWidth(viewGridWidth);
                ecgView.setZeroLocation(0.5);
                ecgView.initView();
            }
        });
    }

    @Override
    public void updateRecordStatus(final boolean clickable) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //tbEcgRecord.setChecked(isChecked);
                tbEcgRecord.setClickable(clickable);
            }
        });
    }

    @Override
    public void updateFilterStatus(final boolean clickable) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //cbEcgFilter.setChecked(isChecked);
                cbEcgFilter.setClickable(clickable);
            }
        });
    }

    @Override
    public void updateEcgData(int ecgData) {
        ecgView.showData(ecgData);
    }

    @Override
    public void updateEcgHr(final int hr) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvEcgHr.setText("" + hr);
            }
        });
    }
}
