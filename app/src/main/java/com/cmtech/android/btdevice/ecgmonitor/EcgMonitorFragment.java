package com.cmtech.android.btdevice.ecgmonitor;

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
import com.cmtech.android.btdeviceapp.util.Uuid;

import java.util.Arrays;

/**
 * Created by bme on 2018/3/13.
 */

public class EcgMonitorFragment extends DeviceFragment {
    private static final int MSG_ECGDDATA = 0;

    ///////////////// 温湿度计Service相关的常量////////////////
    private static final String ecgMonitorServiceUuid    = "aa40";           // 温湿度计服务UUID:aa60
    private static final String ecgMonitorDataUuid       = "aa41";           // 温湿度数据特征UUID:aa61
    private static final String ecgMonitorCtrlUuid       = "aa42";           // 测量控制UUID:aa62

    public static final BluetoothGattElement ECGMONITORDATA =
            new BluetoothGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, null);

    public static final BluetoothGattElement ECGMONITORCTRL =
            new BluetoothGattElement(ecgMonitorServiceUuid, ecgMonitorCtrlUuid, null);

    public static final BluetoothGattElement ECGMONITORDATACCC =
            new BluetoothGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, Uuid.CCCUUID);
    ////////////////////////////////////////////////////////


    private TextView tvEcgData;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ECGDDATA) {
                if(msg.obj != null) {
                    byte[] data = (byte[]) msg.obj;
                    tvEcgData.setText( Arrays.toString(data) );
                }
            }
        }
    };

    public EcgMonitorFragment() {

    }

    public static EcgMonitorFragment newInstance() {
        return new EcgMonitorFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ecgmonitor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEcgData = (TextView)view.findViewById(R.id.tv_ecg_data);
    }


    @Override
    public synchronized void executeGattInitOperation() {
        GattSerialExecutor serialExecutor = device.getSerialExecutor();

        if(serialExecutor == null) return;

        Log.d("FragmentThread", ""+Thread.currentThread().getId());

        Object ecgData = serialExecutor.getGattObject(ECGMONITORDATA);
        Object ecgControl = serialExecutor.getGattObject(ECGMONITORCTRL);
        if(ecgData == null || ecgControl == null) {
            Log.d("EcgMonitorFragment", "can't find Gatt object of this element on the device.");
            return;
        }


        IBleCallback notifyCallback = new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Message msg = new Message();
                msg.what = MSG_ECGDDATA;
                msg.obj = data;
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };

        // enable ECG 数据notify
        serialExecutor.addNotifyCommand(ECGMONITORDATACCC, true, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("Thread", "Notify Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {

            }
        }, notifyCallback);


        // 启动ECG数据采集
        serialExecutor.addWriteCommand(ECGMONITORCTRL, new byte[]{0x01}, new IBleCallback() {
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
