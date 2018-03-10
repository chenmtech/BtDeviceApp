package com.cmtech.android.btdevice.thermo;

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

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDevice;
import com.cmtech.android.btdeviceapp.util.ByteUtil;

import static com.cmtech.android.btdevice.thermo.ThermoGattSerialExecutor.*;

/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends DeviceFragment {
    private static final int MSG_THERMODATA = 0;

    private TextView tvThermoData;

    private ThermoGattSerialExecutor serialExecutor;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_THERMODATA) {
                if (msg.obj != null) {
                    byte[] data = (byte[]) msg.obj;
                    double temp = ByteUtil.getInt(new byte[]{data[0], data[1], 0x00, 0x00})/100.0;
                    String str = String.format("%.2f", temp);
                    tvThermoData.setText(str);
                }
            }
        }
    };

    public ThermoFragment() {

    }

    public static ThermoFragment newInstance() {
        return new ThermoFragment();
    }

    @Override
    public void updateDeviceInfo(final MyBluetoothDevice device, int type) {
        if(this.device == device) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    updateConnectState();
                }
            });
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

        tvThermoData = (TextView)view.findViewById(R.id.tv_thermo_data);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d("Main Thread", ""+Thread.currentThread().getId());

        DeviceMirror deviceMirror = device.getDeviceMirror();
        serialExecutor = new ThermoGattSerialExecutor(deviceMirror);

        Object thermoData = serialExecutor.getGattObject(THERMODATA);
        Object thermoControl = serialExecutor.getGattObject(THERMOCONTROL);
        Object thermoPeriod = serialExecutor.getGattObject(THERMOPERIOD);
        if(thermoData == null || thermoControl == null || thermoPeriod == null) {
            Log.d("ThermoFragment", "can't find Gatt object of this element on the device.");
            return;
        }



        serialExecutor.addReadCommand(THERMODATA, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "first write period: " + HexUtil.encodeHexStr(data));
                Message msg = new Message();
                msg.what = MSG_THERMODATA;
                msg.obj = data;
                handler.sendMessage(msg);
                Log.d("Thread", "Read Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });

        IBleCallback notifyCallback = new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Message msg = new Message();
                msg.what = MSG_THERMODATA;
                msg.obj = data;
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };

        serialExecutor.addNotifyCommand(THERMODATACCC, true, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("Thread", "Notify Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {

            }
        }, notifyCallback);



        serialExecutor.addWriteCommand(THERMOCONTROL, new byte[]{0x03}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "second write period: " + HexUtil.encodeHexStr(data));

                Log.d("Thread", "Control Write Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });

        serialExecutor.addWriteCommand(THERMOPERIOD, new byte[]{0x01}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "second write period: " + HexUtil.encodeHexStr(data));

                Log.d("Thread", "Period Write Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });

        serialExecutor.startExecuteCommand();


    }


    @Override
    public void onDestroy() {
        // 停止命令执行
        serialExecutor.stopExecuteCommand();

        super.onDestroy();
    }

}
