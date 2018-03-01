package com.cmtech.android.btdevice.thermo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdevice.common.DeviceFragment;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;
import static com.cmtech.android.btdevice.thermo.ThermoManager.*;

import java.util.List;

/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends DeviceFragment {
    private TextView tvServices;
    private TextView tvCharacteristics;
    private TextView tvDescriptors;

    private ThermoManager manager = new ThermoManager();

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
        View view = inflater.inflate(R.layout.fragment_thermometer, container, false);

        initComponentInParentView(view);

        tvServices = (TextView)view.findViewById(R.id.tv_device_services);
        tvCharacteristics = (TextView)view.findViewById(R.id.tv_device_characteristics);
        tvDescriptors = (TextView)view.findViewById(R.id.tv_device_descriptors);

        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BluetoothGattCharacteristic thermoData = (BluetoothGattCharacteristic)manager.find(device, THERMODATA);
        if(thermoData == null) {
            Toast.makeText(getContext(), "wrong", Toast.LENGTH_SHORT);
        }

        List<BluetoothGattService> services = device.getDeviceMirror().getBluetoothGatt().getServices();
        StringBuilder serviceStr = new StringBuilder();
        StringBuilder charaStr = new StringBuilder();
        StringBuilder descStr = new StringBuilder();
        for(BluetoothGattService service : services) {
            serviceStr.append(service.getUuid().toString() + '\n');
            List<BluetoothGattCharacteristic> charas = service.getCharacteristics();
            for(BluetoothGattCharacteristic chara : charas) {
                charaStr.append(chara.getUuid().toString() + '\n');
                List<BluetoothGattDescriptor> descriptors = chara.getDescriptors();
                for(BluetoothGattDescriptor descriptor : descriptors) {
                    descStr.append(chara.getUuid().toString() + descriptor.getUuid().toString() + '\n');
                }

            }
        }

        tvServices.setText(serviceStr.toString());

        tvCharacteristics.setText(charaStr.toString());

        tvDescriptors.setText(descStr.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ThermoFragment", "onDestroy");
    }
}
