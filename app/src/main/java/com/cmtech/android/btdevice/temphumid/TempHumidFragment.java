package com.cmtech.android.btdevice.temphumid;

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

import java.util.Arrays;



/**
 * Created by bme on 2018/2/27.
 */

public class TempHumidFragment extends DeviceFragment {
    private static final int MSG_TEMPHUMIDDATA = 0;
    private static final int MSG_TEMPHUMIDCTRL = 1;
    private static final int MSG_TEMPHUMIDPERIOD = 2;

    ///////////////// 温湿度计Service相关的常量////////////////
    private static final String tempHumidServiceUuid    = "aa60";           // 温湿度计服务UUID:aa60
    private static final String tempHumidDataUuid       = "aa61";           // 温湿度数据特征UUID:aa61
    private static final String tempHumidCtrlUuid       = "aa62";           // 测量控制UUID:aa62
    private static final String tempHumidPeriodUuid     = "aa63";           // 采样周期UUID:aa63

    public static final BluetoothGattElement TEMPHUMIDDATA =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidDataUuid, null);

    public static final BluetoothGattElement TEMPHUMIDCTRL =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidCtrlUuid, null);

    public static final BluetoothGattElement TEMPHUMIDPERIOD =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidPeriodUuid, null);

    public static final BluetoothGattElement TEMPHUMIDDATACCC =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidDataUuid, Uuid.CCCUUID);
    ////////////////////////////////////////////////////////


    private TextView tvTempData;
    private TextView tvHumidData;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_TEMPHUMIDDATA) {
                if(msg.obj != null) {
                    byte[] data = (byte[]) msg.obj;
                    byte[] buf = Arrays.copyOfRange(data, 0, 4);
                    int humid = (int)ByteUtil.getFloat(buf);
                    tvHumidData.setText( ""+humid );
                    buf = Arrays.copyOfRange(data, 4, 8);
                    float temp = ByteUtil.getFloat(buf);
                    tvTempData.setText(String.format("%.1f", temp));
                }
            }
        }
    };

    public TempHumidFragment() {

    }

    public static TempHumidFragment newInstance() {
        return new TempHumidFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_temphumid, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTempData = (TextView)view.findViewById(R.id.tv_temp_data);
        tvHumidData = (TextView)view.findViewById(R.id.tv_humid_data);
    }


    @Override
    public synchronized void executeGattInitOperation() {
        GattSerialExecutor serialExecutor = device.getSerialExecutor();

        if(serialExecutor == null) return;

        Log.d("FragmentThread", ""+Thread.currentThread().getId());

        Object thermoData = serialExecutor.getGattObject(TEMPHUMIDDATA);
        Object thermoControl = serialExecutor.getGattObject(TEMPHUMIDCTRL);
        Object thermoPeriod = serialExecutor.getGattObject(TEMPHUMIDPERIOD);
        if(thermoData == null || thermoControl == null || thermoPeriod == null) {
            Log.d("TempHumidFragment", "can't find Gatt object of this element on the device.");
            return;
        }

        // 读温湿度
        serialExecutor.addReadCommand(TEMPHUMIDDATA, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "first write period: " + HexUtil.encodeHexStr(data));
                Message msg = new Message();
                msg.what = MSG_TEMPHUMIDDATA;
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
        serialExecutor.addWriteCommand(TEMPHUMIDPERIOD, new byte[]{0x0A}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "second write period: " + HexUtil.encodeHexStr(data));
                handler.sendEmptyMessage(MSG_TEMPHUMIDPERIOD);
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
                msg.what = MSG_TEMPHUMIDDATA;
                msg.obj = data;
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };

        // enable温湿度notify
        serialExecutor.addNotifyCommand(TEMPHUMIDDATACCC, true, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("Thread", "Notify Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {

            }
        }, notifyCallback);


        // 启动温湿度采集
        serialExecutor.addWriteCommand(TEMPHUMIDCTRL, new byte[]{0x01}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "second write period: " + HexUtil.encodeHexStr(data));
                //handler.sendEmptyMessage(MSG_TEMPHUMIDCTRL);
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
