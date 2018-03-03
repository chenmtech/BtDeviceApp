package com.cmtech.android.btdevice.thermo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.cmtech.android.ble.exception.TimeoutException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.utils.HexUtil;
import com.cmtech.android.btdevice.common.BluetoothGattCommand;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdevice.common.DeviceFragment;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import java.util.Arrays;

import static com.cmtech.android.ble.common.BleConstant.MSG_CONNECT_RETRY;
import static com.cmtech.android.ble.common.BleConstant.MSG_CONNECT_TIMEOUT;
import static com.cmtech.android.ble.common.BleConstant.MSG_READ_DATA_RETRY;
import static com.cmtech.android.ble.common.BleConstant.MSG_READ_DATA_TIMEOUT;
import static com.cmtech.android.ble.common.BleConstant.MSG_RECEIVE_DATA_RETRY;
import static com.cmtech.android.ble.common.BleConstant.MSG_RECEIVE_DATA_TIMEOUT;
import static com.cmtech.android.ble.common.BleConstant.MSG_WRITE_DATA_RETRY;
import static com.cmtech.android.ble.common.BleConstant.MSG_WRITE_DATA_TIMEOUT;
import static com.cmtech.android.btdevice.thermo.ThermoManager.*;

/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends DeviceFragment {
    private static final int MSG_THERMODATA = 0;
    private static final int MSG_THERMOCONTROL = 1;
    private static final int MSG_THERMOPERIOD = 2;

    private TextView tvServices;
    private TextView tvCharacteristics;
    private TextView tvDescriptors;

    private ThermoManager manager;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_THERMODATA) {
                tvServices.setText("service");
            } else if (msg.what == MSG_THERMOCONTROL) {
                tvCharacteristics.setText("characteristic");
            } else if (msg.what == MSG_THERMOPERIOD) {
                tvDescriptors.setText("descriptor");
            }
        }
    };

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
        return inflater.inflate(R.layout.fragment_thermometer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvServices = (TextView)view.findViewById(R.id.tv_device_services);
        tvCharacteristics = (TextView)view.findViewById(R.id.tv_device_characteristics);
        tvDescriptors = (TextView)view.findViewById(R.id.tv_device_descriptors);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        manager = new ThermoManager(device.getDeviceMirror());

        Object thermoData = manager.findElement(THERMODATA);
        Object thermoControl = manager.findElement(THERMOCONTROL);
        Object thermoPeriod = manager.findElement(THERMOPERIOD);
        if(thermoData == null || thermoControl == null || thermoPeriod == null) {
            Log.d("ThermoFragment", "can't find element");
            return;
        }

        //((BluetoothGattCharacteristic)thermoControl).setValue(new byte[]{0x01});
        //device.getDeviceMirror().getBluetoothGatt().writeCharacteristic((BluetoothGattCharacteristic)thermoControl);


        //device.getDeviceMirror().getBluetoothGatt().readCharacteristic((BluetoothGattCharacteristic)thermoData);
        //device.getDeviceMirror().getBluetoothGatt().readCharacteristic((BluetoothGattCharacteristic)thermoControl);

        /*manager.readElement(THERMODATA, new IBleCallback() {
            @Override
            public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                *//*ThermoFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvServices.setText(HexUtil.encodeHexStr(data));
                        Log.d("THERMODATA", Arrays.toString(data));
                    }
                });*//*
                Log.d("THERMODATA", Arrays.toString(data));
            }

            @Override
            public void onFailure(BleException exception) {
                ThermoFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvServices.setText("数据操作错误");
                        Log.d("THERMODATA", "wrong");
                    }
                });
            }
        });*/


        /*manager.writeElement(THERMOCONTROL, new byte[]{0x01}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                *//*if(data == null) return;
                final byte[] tmp = Arrays.copyOf(data, data.length);
                ThermoFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCharacteristics.setText(HexUtil.encodeHexStr(tmp));
                        Log.d("THERMOCONTROL", Arrays.toString(tmp));
                    }
                });*//*
                Log.d("THERMOCONTROL", Arrays.toString(data));
            }

            @Override
            public void onFailure(BleException exception) {
                ThermoFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCharacteristics.setText("数据操作错误");
                        Log.d("THERMOCONTROL", "wrong");
                    }
                });
            }
        });*/

        manager.writeElement(THERMOCONTROL, new byte[]{0x03}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                /*if(data == null) return;
                final byte[] tmp = Arrays.copyOf(data, data.length);
                ThermoFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCharacteristics.setText(HexUtil.encodeHexStr(tmp));
                        Log.d("THERMOCONTROL", Arrays.toString(tmp));
                    }
                });*/
                Log.d("THERMOCONTROL", HexUtil.encodeHexStr(data));
            }

            @Override
            public void onFailure(BleException exception) {
                ThermoFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCharacteristics.setText("数据操作错误");
                        Log.d("THERMOCONTROL", "wrong");
                    }
                });
            }
        });

        /*manager.readElement(THERMODATA, new IBleCallback() {
            @Override
            public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                *//*ThermoFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvDescriptors.setText(HexUtil.encodeHexStr(data));
                        Log.d("THERMODATA", Arrays.toString(data));
                    }
                });*//*
                Log.d("THERMODATA", Arrays.toString(data));
            }

            @Override
            public void onFailure(BleException exception) {
                ThermoFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvDescriptors.setText("数据操作错误");
                        Log.d("THERMODATA", "wrong");
                    }
                });
            }
        });*/

        manager.startExecuteCommand();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(manager != null) manager.stopExecuteCommand();
    }
}
