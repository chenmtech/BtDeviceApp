package com.cmtech.android.bledevice.thermo.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cmtech.android.bledevice.thermo.model.IThermoDataObserver;
import com.cmtech.android.bledevice.thermo.model.ThermoDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;

import java.util.Locale;


/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends BleDeviceFragment implements IThermoDataObserver {
    private static final String LESSTHAN34 = "< 34.0";

    private TextView tvThermoCurrentTemp;
    private TextView tvThermoHightestTemp;
    private TextView tvThermoStatus;
    private Button btnThermoResetHighestTemp;

    private ThermoDevice device;

    public ThermoFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        device = (ThermoDevice)getDevice();
        if(device == null)
            throw new IllegalArgumentException();

        device.registerThermoDataObserver(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thermometer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvThermoCurrentTemp = view.findViewById(R.id.tv_thermo_currenttempvalue);
        tvThermoHightestTemp = view.findViewById(R.id.tv_thermo_highesttempvalue);
        tvThermoStatus = view.findViewById(R.id.tv_thermo_status);
        btnThermoResetHighestTemp = view.findViewById(R.id.btn_thermo_resethighesttemp);

        btnThermoResetHighestTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetHighestTemp();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(device != null) {
            device.removeThermoDataObserver(this);
        }
    }

    private synchronized void resetHighestTemp() {
        device.resetHighestTemp();
    }


    @Override
    public void updateThermoData() {
        final double curTemp = ((ThermoDevice)getDevice()).getCurTemp();
        final double highestTemp = ((ThermoDevice)getDevice()).getHighestTemp();

        if(curTemp < 34.00) {
            tvThermoCurrentTemp.setText(LESSTHAN34);
        }
        else {
            String str = String.format(Locale.getDefault(), "%.2f", curTemp);
            tvThermoCurrentTemp.setText(str);
        }

        if(highestTemp < 34.00) {
            tvThermoHightestTemp.setText(LESSTHAN34);
        }
        else {
            String str = String.format(Locale.getDefault(),"%.2f", highestTemp);
            tvThermoHightestTemp.setText(str);
        }

        if(highestTemp < 37.0) {
            tvThermoStatus.setText("正常");
        } else if(highestTemp < 38.0) {
            tvThermoStatus.setText("低烧，请注意休息！");
        } else if(highestTemp < 38.5) {
            tvThermoStatus.setText("体温异常，请注意降温！");
        } else {
            tvThermoStatus.setText("高烧，请及时就医！");
        }
    }
}
