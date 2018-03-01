package com.cmtech.android.btdevice.temphumid;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdevice.common.DeviceFragment;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

/**
 * Created by bme on 2018/2/27.
 */

public class TempHumidFragment extends DeviceFragment {


    public TempHumidFragment() {

    }

    public static TempHumidFragment newInstance(ConfiguredDevice device) {
        Bundle args = new Bundle();
        args.putSerializable("device", device);
        TempHumidFragment fragment = new TempHumidFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void updateDeviceInfo(final ConfiguredDevice device, int type) {
        if(TempHumidFragment.this.device == device) {
            updateConnectState();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thermometer, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        tvConnectState = view.findViewById(R.id.device_connect_state_tv);
        btnDisconnect = view.findViewById(R.id.device_disconnect_btn);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ThermoFragment", "onDestroy");
    }
}
