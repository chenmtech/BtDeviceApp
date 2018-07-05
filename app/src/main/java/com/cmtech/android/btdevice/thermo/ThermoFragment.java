package com.cmtech.android.btdevice.thermo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.model.BLEDeviceFragment;


/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends BLEDeviceFragment {


    private TextView tvThermoData;
    private TextView tvThermoStatus;


    public ThermoFragment() {

    }

    public static ThermoFragment newInstance() {
        return new ThermoFragment();
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



}
