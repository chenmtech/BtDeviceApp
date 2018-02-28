package com.cmtech.android.btdevice.thermo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdevice.common.DeviceFragment;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import java.util.Arrays;
import java.util.List;

/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends DeviceFragment {
    TextView tvServices;
    TextView tvCharacteristic;

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
        if(ThermoFragment.this.device == device) {
            updateConnectState();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_thermometer, container, false);

        initComponentInView(view);

        tvServices = (TextView)view.findViewById(R.id.device_services);
        tvCharacteristic = (TextView)view.findViewById(R.id.device_characteristic);

        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<BluetoothGattService> services = device.getDeviceMirror().getBluetoothGatt().getServices();
        StringBuilder serviceStr = new StringBuilder();
        StringBuilder charaStr = new StringBuilder();
        for(BluetoothGattService service : services) {
            serviceStr.append(service.getUuid().toString() + '\n');
            List<BluetoothGattCharacteristic> charas = service.getCharacteristics();
            for(BluetoothGattCharacteristic chara : charas) {
                charaStr.append(chara.getUuid().toString() + '\n');
            }
        }

        tvServices.setText(serviceStr.toString());
        tvCharacteristic.setText(charaStr.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ThermoFragment", "onDestroy");
    }
}
