package com.cmtech.android.bledevice.thermo.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledevice.thermo.model.IThermoDataObserver;
import com.cmtech.android.bledevice.thermo.model.ThermoDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevice.core.BleDeviceFragment;

import java.util.Locale;


/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends BleDeviceFragment implements IThermoDataObserver {
    private static final String LESSTHAN34 = "低于34.0";

    private TextView tvThermoCurrentTemp;
    private TextView tvThermoHightestTemp;
    private TextView tvThermoStatus;
    private ImageButton ibThermoResetHighestTemp;

    private ThermoDevice device;

    public ThermoFragment() {

    }

    public static BleDeviceFragment newInstance(String macAddress) {
        BleDeviceFragment fragment = new ThermoFragment();
        return BleDeviceFragment.pushMacAddressIntoFragmentArgument(macAddress, fragment);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (ThermoDevice)getDevice();
        device.registerThermoDataObserver(this);

        return inflater.inflate(R.layout.fragment_thermometer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvThermoCurrentTemp = view.findViewById(R.id.tv_thermo_currenttempvalue);
        tvThermoHightestTemp = view.findViewById(R.id.tv_thermo_highesttempvalue);
        tvThermoStatus = view.findViewById(R.id.tv_thermo_status);
        ibThermoResetHighestTemp = view.findViewById(R.id.ib_thermo_resethighesttemp);

        ibThermoResetHighestTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetHighestTemp();
            }
        });
    }

    @Override
    public void openConfigActivity() {

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
