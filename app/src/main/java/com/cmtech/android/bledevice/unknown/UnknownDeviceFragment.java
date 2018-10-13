package com.cmtech.android.bledevice.unknown;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;

/**
 * Created by bme on 2018/2/28.
 */

public class UnknownDeviceFragment extends BleDeviceFragment {
    TextView tvServices;
    TextView tvCharacteristic;

    public UnknownDeviceFragment() {

    }

    public static UnknownDeviceFragment newInstance() {
        return new UnknownDeviceFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unknowndevice, container, false);

        tvServices = (TextView)view.findViewById(R.id.tv_device_services);
        tvCharacteristic = (TextView)view.findViewById(R.id.tv_device_characteristics);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


}
