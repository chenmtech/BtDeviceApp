package com.cmtech.android.btdevice.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cmtech.android.ble.common.ConnectState;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

/**
 * Created by bme on 2018/2/27.
 */

public abstract class DeviceFragment extends Fragment implements ConfiguredDevice.IConfiguredDeviceObersver {
    protected ConfiguredDevice device;

    protected TextView tvConnectState;
    protected Button btnDisconnect;
    protected Button btnClose;

    public DeviceFragment() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        device = (ConfiguredDevice) getArguments().getSerializable("device");
        if(device != null) setDevice(device);

        updateConnectState();
    }

    protected void setDevice(ConfiguredDevice device) {
        this.device = device;
        device.registerDeviceObserver(this);
    }

    protected void updateConnectState() {
        if(device != null) {
            tvConnectState.setText(device.getConnectStateString());
            if(device.getConnectState() == ConnectState.CONNECT_SUCCESS) {
                btnDisconnect.setEnabled(true);
            } else {
                btnDisconnect.setEnabled(false);
            }
        }
    }

    protected void initComponentInView(View view) {
        tvConnectState = view.findViewById(R.id.device_connect_state_tv);
        btnDisconnect = view.findViewById(R.id.device_disconnect_btn);

        btnClose = view.findViewById(R.id.device_close_btn);
        btnDisconnect = view.findViewById(R.id.device_disconnect_btn);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DeviceFragment", DeviceFragment.this.getClass().getSimpleName());
                device.removerDeviceObserver(DeviceFragment.this);
                MainActivity.getActivity().closeConnectedDevice(device);
            }
        });
    }

}
