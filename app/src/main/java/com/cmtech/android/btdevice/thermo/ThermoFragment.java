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
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.utils.HexUtil;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdevice.common.DeviceFragment;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import static com.cmtech.android.btdevice.thermo.ThermoGattSerialExecutor.*;

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

    private ThermoGattSerialExecutor serialExecutor;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_THERMODATA) {
                if(msg.obj != null) {
                    byte[] data = (byte[]) msg.obj;
                    tvServices.setText(""+bytes2int(new byte[]{0x00,0x00,data[1],data[0]}));
                }
            } else if (msg.what == MSG_THERMOCONTROL) {
                tvCharacteristics.setText("characteristic");
            } else if (msg.what == MSG_THERMOPERIOD) {
                tvDescriptors.setText("descriptor");
            }
        }
    };

    public ThermoFragment() {

    }

    public static ThermoFragment newInstance() {
        return new ThermoFragment();
    }

    @Override
    public void updateDeviceInfo(final ConfiguredDevice device, int type) {
        if(ThermoFragment.this.device == device) {
            MainActivity.getActivity().runOnUiThread(new Runnable() {
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

        tvServices = (TextView)view.findViewById(R.id.tv_device_services);
        tvCharacteristics = (TextView)view.findViewById(R.id.tv_device_characteristics);
        tvDescriptors = (TextView)view.findViewById(R.id.tv_device_descriptors);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d("Main Thread", ""+Thread.currentThread().getId());

        serialExecutor = new ThermoGattSerialExecutor(device.getDeviceMirror());

        Object thermoData = serialExecutor.findElement(THERMODATA);
        Object thermoControl = serialExecutor.findElement(THERMOCONTROL);
        Object thermoPeriod = serialExecutor.findElement(THERMOPERIOD);
        if(thermoData == null || thermoControl == null || thermoPeriod == null) {
            Log.d("ThermoFragment", "can't find element");
            return;
        }



        serialExecutor.readElement(THERMODATA, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "first write period: " + HexUtil.encodeHexStr(data));
                handler.sendEmptyMessage(MSG_THERMODATA);
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

        serialExecutor.notifyElement(THERMODATACCC, true, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("Thread", "Notify Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {

            }
        }, notifyCallback);

        serialExecutor.startExecuteCommand();

        serialExecutor.writeElement(THERMOCONTROL, new byte[]{0x03}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "second write period: " + HexUtil.encodeHexStr(data));
                handler.sendEmptyMessage(MSG_THERMOCONTROL);
                Log.d("Thread", "Control Write Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });

        serialExecutor.writeElement(THERMOPERIOD, new byte[]{0x02}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "second write period: " + HexUtil.encodeHexStr(data));
                handler.sendEmptyMessage(MSG_THERMOPERIOD);
                Log.d("Thread", "Period Write Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 停止命令执行
        serialExecutor.stopExecuteCommand();
    }

    //高位在前，低位在后
    public static byte[] int2bytes(int num){
        byte[] result = new byte[4];
        result[0] = (byte)((num >>> 24) & 0xff);//说明一
        result[1] = (byte)((num >>> 16)& 0xff );
        result[2] = (byte)((num >>> 8) & 0xff );
        result[3] = (byte)((num >>> 0) & 0xff );
        return result;
    }

    //高位在前，低位在后
    public static int bytes2int(byte[] bytes){
        int result = 0;
        if(bytes.length == 4){
            int a = (bytes[0] & 0xff) << 24;//说明二
            int b = (bytes[1] & 0xff) << 16;
            int c = (bytes[2] & 0xff) << 8;
            int d = (bytes[3] & 0xff);
            result = a | b | c | d;
        }
        return result;
    }
}
