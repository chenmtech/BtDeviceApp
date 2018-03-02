package com.cmtech.android.btdevice.thermo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.utils.HexUtil;
import com.cmtech.android.btdevice.common.BluetoothGattCommand;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdevice.common.DeviceFragment;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import java.util.Arrays;

import static com.cmtech.android.btdevice.thermo.ThermoManager.*;

/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends DeviceFragment {
    private TextView tvServices;
    private TextView tvCharacteristics;
    private TextView tvDescriptors;

    private ThermoManager manager;

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
        manager = new ThermoManager(device.getDeviceMirror());

        Object thermoData = manager.findElement(THERMODATA);
        Object thermoPeriod = manager.findElement(THERMOPERIOD);
        if(thermoData == null || thermoPeriod == null) {
            Log.d("ThermoFragment", "can't find element");
            return;
        }


        manager.readElement(THERMODATA, new IBleCallback() {
            @Override
            public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvServices.setText(HexUtil.encodeHexStr(data));
                        Log.d("THERMODATA", Arrays.toString(data));
                    }
                });
            }

            @Override
            public void onFailure(BleException exception) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvServices.setText("数据操作错误");
                        Log.d("THERMODATA", "wrong");
                    }
                });
            }
        });

        manager.readElement(THERMOCONTROL, new IBleCallback() {
            @Override
            public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCharacteristics.setText(HexUtil.encodeHexStr(data));
                        Log.d("THERMOCONTROL", Arrays.toString(data));
                    }
                });
            }

            @Override
            public void onFailure(BleException exception) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCharacteristics.setText("数据操作错误");
                        Log.d("THERMOCONTROL", "wrong");
                    }
                });
            }
        });

        manager.readElement(THERMOPERIOD, new IBleCallback() {
            @Override
            public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                MainActivity.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvDescriptors.setText(HexUtil.encodeHexStr(data));
                        Log.d("THERMOPERIOD", Arrays.toString(data));
                    }
                });
            }

            @Override
            public void onFailure(BleException exception) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvDescriptors.setText("数据操作错误");
                        Log.d("THERMOPERIOD", "wrong");
                    }
                });
            }
        });

        manager.startExecuteCommand();

        //tvServices.setText(serviceStr.toString());

        //tvCharacteristics.setText(charaStr.toString());

        //tvDescriptors.setText(descStr.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(manager != null) manager.stopExecuteCommand();
    }
}
