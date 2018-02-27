package com.cmtech.android.btdeviceapp.thermo.frag;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends DeviceFragment {

    TextView tvConnectState;

    public ThermoFragment() {

    }

    public static ThermoFragment newInstance(ConfiguredDevice device) {
        Bundle args = new Bundle();
        args.putSerializable("device", device);
        ThermoFragment fragment = new ThermoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void updateDeviceInfo(final ConfiguredDevice device, int type) {
        if(tvConnectState != null && ThermoFragment.this.device == device) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String connectState = device.getConnectStateString();
                    tvConnectState.setText(connectState);
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_thermometer, container, false);
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


}
