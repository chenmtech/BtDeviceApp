package com.cmtech.android.bledevice.unknowndevice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;

/**
 * Created by bme on 2018/2/28.
 */

public class UnknownDeviceFragment extends DeviceFragment {
    TextView tvServices;
    TextView tvCharacteristic;

    public UnknownDeviceFragment() {
        super();
    }

    public static UnknownDeviceFragment newInstance() {
        return new UnknownDeviceFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_unknown, container, false);

        tvServices = view.findViewById(R.id.tv_device_services);
        tvCharacteristic = view.findViewById(R.id.tv_device_characteristics);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void openConfigureActivity() {
    }
}
