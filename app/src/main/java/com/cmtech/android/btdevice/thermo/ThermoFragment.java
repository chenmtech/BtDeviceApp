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
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.model.BluetoothGattElement;
import com.cmtech.android.btdeviceapp.model.GattSerialExecutor;
import com.cmtech.android.btdeviceapp.util.ByteUtil;
import com.cmtech.android.btdeviceapp.util.Uuid;


/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends DeviceFragment {
    private static final int MSG_THERMODATA = 0;

    ///////////////// 体温计Service相关的常量////////////////
    private static final String thermoServiceUuid       = "aa30";           // 体温计服务UUID:aa30
    private static final String thermoDataUuid          = "aa31";           // 体温数据特征UUID:aa31
    private static final String thermoControlUuid       = "aa32";           // 体温测量控制UUID:aa32
    private static final String thermoPeriodUuid        = "aa33";           // 体温采样周期UUID:aa33

    public static final BluetoothGattElement THERMODATA =
            new BluetoothGattElement(thermoServiceUuid, thermoDataUuid, null);

    public static final BluetoothGattElement THERMOCONTROL =
            new BluetoothGattElement(thermoServiceUuid, thermoControlUuid, null);

    public static final BluetoothGattElement THERMOPERIOD =
            new BluetoothGattElement(thermoServiceUuid, thermoPeriodUuid, null);

    public static final BluetoothGattElement THERMODATACCC =
            new BluetoothGattElement(thermoServiceUuid, thermoDataUuid, Uuid.CCCUUID);
    ///////////////////////////////////////////////////////

    private TextView tvThermoData;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_THERMODATA) {
                if (msg.obj != null) {
                    byte[] data = (byte[]) msg.obj;
                    double temp = ByteUtil.getShort(data)/100.0;
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
    public void executeGattInitOperation() {
        GattSerialExecutor serialExecutor = device.getSerialExecutor();

        if(serialExecutor == null) return;

        Log.d("FragmentThread", ""+Thread.currentThread().getId());

        Object thermoData = serialExecutor.getGattObject(THERMODATA);
        Object thermoControl = serialExecutor.getGattObject(THERMOCONTROL);
        Object thermoPeriod = serialExecutor.getGattObject(THERMOPERIOD);
        if(thermoData == null || thermoControl == null || thermoPeriod == null) {
            Log.d("ThermoFragment", "Can't find the Gatt object on the device.");
            return;
        }

        // 读温度数据
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

        // 设置采样周期为1s
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

        // enable温度数据notify
        serialExecutor.addNotifyCommand(THERMODATACCC, true, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("Thread", "Notify Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {

            }
        }, notifyCallback);


        // 启动温度采集
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

        serialExecutor.start();
    }



}
