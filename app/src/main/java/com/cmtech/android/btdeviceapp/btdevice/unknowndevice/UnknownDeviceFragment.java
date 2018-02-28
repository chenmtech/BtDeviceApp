package com.cmtech.android.btdeviceapp.btdevice.unknowndevice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.btdevice.common.OpenedDeviceFragment;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

/**
 * Created by bme on 2018/2/28.
 */

public class UnknownDeviceFragment extends OpenedDeviceFragment {
    TextView tvConnectState;

    public UnknownDeviceFragment() {

    }

    public static UnknownDeviceFragment newInstance(ConfiguredDevice device) {
        Bundle args = new Bundle();
        args.putSerializable("device", device);
        UnknownDeviceFragment fragment = new UnknownDeviceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void updateDeviceInfo(final ConfiguredDevice device, int type) {
        if(tvConnectState != null && UnknownDeviceFragment.this.device == device) {
            String connectState = device.getConnectStateString();
            tvConnectState.setText(connectState);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_unknowndevice, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ConfiguredDevice device = (ConfiguredDevice) getArguments().getSerializable("device");
        if(device != null)
            setDevice(device);
        String connectState = device.getConnectStateString();
        tvConnectState = view.findViewById(R.id.device_connect_state_tv);
        tvConnectState.setText(connectState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("UnknownDeviceFragment", "onDestroy");
    }
}
