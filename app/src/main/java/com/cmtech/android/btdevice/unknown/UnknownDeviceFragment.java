package com.cmtech.android.btdevice.unknown;

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
import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import java.util.List;

/**
 * Created by bme on 2018/2/28.
 */

public class UnknownDeviceFragment extends DeviceFragment {
    TextView tvServices;
    TextView tvCharacteristic;

    public UnknownDeviceFragment() {

    }

    public static UnknownDeviceFragment newInstance() {
        //Bundle args = new Bundle();
        //args.putSerializable("device", device);
        UnknownDeviceFragment fragment = new UnknownDeviceFragment();
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void updateDeviceInfo(final ConfiguredDevice device, int type) {
        if(UnknownDeviceFragment.this.device.getConfiguredDevice() == device) {
            updateConnectState();
        }

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

        List<BluetoothGattService> services = device.getConfiguredDevice().getDeviceMirror().getBluetoothGatt().getServices();
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
        Log.d("UnknownDeviceFragment", "onDestroy");
    }
}
