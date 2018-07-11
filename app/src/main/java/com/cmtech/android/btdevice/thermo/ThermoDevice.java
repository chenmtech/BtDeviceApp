package com.cmtech.android.btdevice.thermo;

import android.os.Message;

import com.cmtech.android.btdeviceapp.model.BLEDevice;
import com.cmtech.android.btdeviceapp.model.BLEDeviceBasicInfo;
import com.cmtech.android.btdeviceapp.model.BluetoothGattElement;
import com.cmtech.android.btdeviceapp.util.ByteUtil;
import com.cmtech.android.btdeviceapp.util.Uuid;

public class ThermoDevice extends BLEDevice {
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

    public ThermoDevice(BLEDeviceBasicInfo persistantInfo) {
        super(persistantInfo);
    }


    @Override
    public void processGattMessage(Message msg)
    {
        if (msg.what == MSG_THERMODATA) {
            if (msg.obj != null) {
                byte[] data = (byte[]) msg.obj;
                double temp = ByteUtil.getShort(data)/100.0;

                if(temp < 34.00) {
                    //tvThermoData.setText("<34.0");
                }
                else {
                    String str = String.format("%.2f", temp);
                    //tvThermoData.setText(str);
                }
                if(temp < 37.0) {
                    //tvThermoStatus.setText("正常");
                } else if(temp < 38.0) {
                    //tvThermoStatus.setText("低烧，请注意休息！");
                } else if(temp < 38.5) {
                    //tvThermoStatus.setText("体温异常，请注意降温！");
                } else {
                    //tvThermoStatus.setText("高烧，请及时就医！");
                }
            }
        }
    }


    @Override
    public void initialize() {
    }

    @Override
    public void executeAfterConnectSuccess() {
        return;
        /*
        Object thermoData = getGattObject(THERMODATA);
        Object thermoControl = getGattObject(THERMOCONTROL);
        Object thermoPeriod = getGattObject(THERMOPERIOD);
        if(thermoData == null || thermoControl == null || thermoPeriod == null) {
            Log.d("ThermoFragment", "Can't find the Gatt object on the device.");
            return;
        }

        // 读温度数据
        addReadCommand(THERMODATA, new IBleCallback() {
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
        addWriteCommand(THERMOPERIOD, new byte[]{0x01}, new IBleCallback() {
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
        addNotifyCommand(THERMODATACCC, true, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("Thread", "Notify Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {

            }
        }, notifyCallback);


        // 启动温度采集
        addWriteCommand(THERMOCONTROL, new byte[]{0x03}, new IBleCallback() {
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
*/

    }

    @Override
    public void executeAfterDisconnect(boolean isActive) {

    }

}