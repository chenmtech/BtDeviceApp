package com.cmtech.android.btdevice.thermo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.btdevice.temphumid.TempHumidDevice;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;


/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends BleDeviceFragment implements IThermoDataObserver{


    private TextView tvThermoData;
    private TextView tvThermoStatus;


    public ThermoFragment() {

    }

    public static ThermoFragment newInstance() {
        return new ThermoFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((ThermoDevice)device).registerThermoDataObserver(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_thermometer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvThermoData = (TextView)view.findViewById(R.id.tv_thermo_data);
        tvThermoStatus = view.findViewById(R.id.tv_thermo_status);
    }


    @Override
    public void updateThermoData() {
        double temp = ((ThermoDevice)device).getCurTemp();

        if(temp < 34.00) {
            tvThermoData.setText("<34.0");
        }
        else {
            String str = String.format("%.2f", temp);
            tvThermoData.setText(str);
        }
        if(temp < 37.0) {
            tvThermoStatus.setText("正常");
        } else if(temp < 38.0) {
            tvThermoStatus.setText("低烧，请注意休息！");
        } else if(temp < 38.5) {
            tvThermoStatus.setText("体温异常，请注意降温！");
        } else {
            tvThermoStatus.setText("高烧，请及时就医！");
        }
    }
}
