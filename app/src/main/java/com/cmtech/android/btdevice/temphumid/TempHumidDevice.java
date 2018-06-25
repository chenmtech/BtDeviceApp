package com.cmtech.android.btdevice.temphumid;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;
import com.cmtech.android.btdeviceapp.model.BLEDevicePersistantInfo;
import com.cmtech.android.btdeviceapp.model.BluetoothGattElement;
import com.cmtech.android.btdeviceapp.util.Uuid;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TempHumidDevice extends BLEDeviceModel {
    private static final String TAG = "TempHumidFragment";

    private static final int MSG_TEMPHUMIDDATA = 1;
    private static final int MSG_TEMPHUMIDCTRL = 2;
    private static final int MSG_TEMPHUMIDPERIOD = 3;
    private static final int MSG_TEMPHUMIDHISTORYDATA = 4;
    private static final int MSG_TIMERVALUE = 5;

    ///////////////// 温湿度计Service相关的常量////////////////
    private static final String tempHumidServiceUuid    = "aa60";           // 温湿度计服务UUID:aa60
    private static final String tempHumidDataUuid       = "aa61";           // 温湿度数据特征UUID:aa61
    private static final String tempHumidCtrlUuid       = "aa62";           // 测量控制UUID:aa62
    private static final String tempHumidPeriodUuid     = "aa63";           // 采样周期UUID:aa63
    private static final String tempHumidHistoryTimeUuid  = "aa64";         // 历史数据采集的时间UUID
    private static final String tempHumidHistoryDataUuid = "aa65";          // 历史数据UUID


    public static final BluetoothGattElement TEMPHUMIDDATA =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidDataUuid, null);

    public static final BluetoothGattElement TEMPHUMIDCTRL =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidCtrlUuid, null);

    public static final BluetoothGattElement TEMPHUMIDPERIOD =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidPeriodUuid, null);

    public static final BluetoothGattElement TEMPHUMIDDATACCC =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidDataUuid, Uuid.CCCUUID);

    public static final BluetoothGattElement TEMPHUMIDHISTORYTIME =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidHistoryTimeUuid, null);

    public static final BluetoothGattElement TEMPHUMIDHISTORYDATA =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidHistoryDataUuid, null);
    ////////////////////////////////////////////////////////


    //////////////// 定时服务相关常量 /////////////////////////
    private static final String timerServiceUuid          = "AA70";
    private static final String timerValueUuid              ="aa71";

    public static final BluetoothGattElement TIMERVALUE =
            new BluetoothGattElement(timerServiceUuid, timerValueUuid, null);

    private static final byte DEVICE_DEFAULT_TIMER_PERIOD  = 30; // 设备默认定时周期，单位：分钟
    ////////////////////////////////////////////////////////

    // 设备是否启动定时服务
    private boolean isDeviceStartTimerService = false;
    // 设备的当前时间
    private Calendar deviceCurTime;
    // 设备的定时周期
    private byte deviceTimerPeriod = DEVICE_DEFAULT_TIMER_PERIOD;   // 设定的设备定时更新周期，单位：分钟
    // 上次更新设备历史数据的时间
    private Calendar timeLastUpdated = null;

    public List<TempHumidData> getDataList() {
        return dataList;
    }

    private List<TempHumidData> dataList = new ArrayList<>();

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_TEMPHUMIDDATA) {
                if(msg.obj != null) {
                    TempHumidData data = (TempHumidData) msg.obj;
                    //tvHumidData.setText( ""+data.getHumid() );
                    //tvTempData.setText(String.format("%.1f", data.getTemp()));
                    float heatindex = computeHeatIndex(data.getTemp(), data.getHumid());
                    //tvHeadIndex.setText(String.format("%.1f", heatindex));
                }
            } else if(msg.what == MSG_TEMPHUMIDHISTORYDATA) {
                if(msg.obj != null) {
                    //dataList.add((TempHumidData) msg.obj);
                    //historyDataAdapter.notifyItemInserted(dataList.size()-1);
                    //rvHistoryData.scrollToPosition(dataList.size()-1);
                }
            } else if(msg.what == MSG_TIMERVALUE) {
                if (isDeviceStartTimerService) {
                    //updateHistoryDataFromDevice(device.getSerialExecutor());      // 读取设备的历史数据
                } else {
                    //startTimerService(device.getSerialExecutor());          // 没有开启定时服务（比如刚换电池），就先开启定时服务
                }
            }
        }
    };

    public TempHumidDevice(BLEDevicePersistantInfo persistantInfo) {
        super(persistantInfo);
    }

    @Override
    public void executeAfterConnectSuccess() {
        Object thermoData = getGattObject(TEMPHUMIDDATA);
        Object thermoControl = getGattObject(TEMPHUMIDCTRL);
        Object thermoPeriod = getGattObject(TEMPHUMIDPERIOD);
        if (thermoData == null || thermoControl == null || thermoPeriod == null) {
            Log.d("TempHumidFragment", "can't find Gatt object of this element on the device.");
            return;
        }

        Log.d(TAG, "begin to start temphumid sampling");

        // 设置采样周期: 设置的值以100ms为单位
        int period = 5000;
        executeWriteCommand(TEMPHUMIDPERIOD, new byte[]{(byte) (period / 100)}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice
                    bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "second write period: " + HexUtil.encodeHexStr(data));
                handler.sendEmptyMessage(MSG_TEMPHUMIDPERIOD);
                Log.d("Thread", "Period Write Callback Thread: " + Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });


        // 温湿度数据Notify回调
        IBleCallback notifyCallback = new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Message msg = new Message();
                msg.what = MSG_TEMPHUMIDDATA;
                msg.obj = new TempHumidData(Calendar.getInstance(), data);
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };

        // enable温湿度notify
        executeNotifyCommand(TEMPHUMIDDATACCC, true, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("Thread", "Notify Callback Thread: " + Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {

            }
        }, notifyCallback);


        // 启动温湿度采集
        executeWriteCommand(TEMPHUMIDCTRL, new byte[]{0x01}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "second write period: " + HexUtil.encodeHexStr(data));
                //handler.sendEmptyMessage(MSG_TEMPHUMIDCTRL);
                Log.d("Thread", "Control Write Callback Thread: " + Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });

        initDeviceTimerService();
    }


    private void initDeviceTimerService() {
        if(getGattObject(TIMERVALUE) != null) {
            Log.d(TAG, "serialExecutor has GATT TIMERVALUE");

            executeReadCommand(TIMERVALUE, new IBleCallback() {
                @Override
                public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                    isDeviceStartTimerService = (data[3] == 1) ? true : false;

                    if (isDeviceStartTimerService) {
                        deviceTimerPeriod = data[2];                // 保存设备的定时周期值
                        deviceCurTime = guessDeviceCurTime(data);                     // 通过小时和分钟来猜测设备的当前时间
                    }
                    handler.sendEmptyMessage(MSG_TIMERVALUE);
                }

                @Override
                public void onFailure(BleException exception) {

                }
            });
        }
    }

    private Calendar guessDeviceCurTime(byte[] data) {
        Calendar now = Calendar.getInstance();
        Calendar devicenow;

        Calendar devicetoday = (Calendar) now.clone();
        devicetoday.set(Calendar.HOUR_OF_DAY, data[0]);
        devicetoday.set(Calendar.MINUTE, data[1]);
        devicetoday.set(Calendar.SECOND, 0);
        devicetoday.set(Calendar.MILLISECOND, 0);

        Calendar deviceyest = (Calendar) devicetoday.clone();
        deviceyest.add(Calendar.DAY_OF_MONTH, -1);

        Calendar devicetomm = (Calendar) devicetoday.clone();
        devicetomm.add(Calendar.DAY_OF_MONTH, 1);

        long difftoday = Math.abs(now.getTimeInMillis()-devicetoday.getTimeInMillis());
        long diffyest = Math.abs(now.getTimeInMillis()-deviceyest.getTimeInMillis());
        long difftomm = Math.abs(now.getTimeInMillis()-devicetomm.getTimeInMillis());

        if(difftoday < Math.min(diffyest, difftomm))
            devicenow = devicetoday;
        else if(diffyest < Math.min(difftoday, difftomm))
            devicenow = deviceyest;
        else
            devicenow = devicetomm;

        Log.d("TempHumidFragment", "now=" + DateFormat.getDateTimeInstance().format(now.getTime()) );
        Log.d("TempHumidFragment", "devicenow=" + DateFormat.getDateTimeInstance().format(devicenow.getTime()));

        return devicenow;
    };

    private void updateHistoryDataFromDevice() {
        if(!isDeviceStartTimerService) return;

        Calendar updateFrom;
        if(timeLastUpdated != null) {
            updateFrom = (Calendar) timeLastUpdated.clone();
            updateFrom.add(Calendar.MINUTE, deviceTimerPeriod);
            if(updateFrom.after(deviceCurTime)) return;    // 上次更新到现在还不到一个deviceTimerPeriod,不需要更新
        } else {
            updateFrom = (Calendar) deviceCurTime.clone();
            updateFrom.add(Calendar.DAY_OF_MONTH, -1);
            updateFrom.add(Calendar.MINUTE, deviceTimerPeriod); // 从前一天的时间开始更新
        }

        for (int i = 0; i < 48; i++) {
            readHistoryDataAtTime(updateFrom);
            updateFrom.add(Calendar.MINUTE, deviceTimerPeriod);
            if(updateFrom.after(deviceCurTime)) break;
        }

        timeLastUpdated = (Calendar)deviceCurTime.clone();
    }

    private void readHistoryDataAtTime(Calendar time) {
        final Calendar backuptime = (Calendar)time.clone();
        final byte[] hourminute = {(byte)backuptime.get(Calendar.HOUR_OF_DAY), (byte)backuptime.get(Calendar.MINUTE)};
        // 写历史数据时间
        executeWriteCommand(TEMPHUMIDHISTORYTIME, hourminute, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });

        // 读取历史数据
        executeReadCommand(TEMPHUMIDHISTORYDATA, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Message msg = new Message();
                msg.what = MSG_TEMPHUMIDHISTORYDATA;
                msg.obj = new TempHumidData(backuptime, data);
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    private void startTimerService() {
        Log.d("TempHumidFragment", "start timer service");

        Calendar time = Calendar.getInstance();
        byte[] value = {(byte)time.get(Calendar.HOUR_OF_DAY), (byte)time.get(Calendar.MINUTE), deviceTimerPeriod, 0x01};
        executeWriteCommand(TIMERVALUE, value, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("TempHumidFragment", "TIMERVALUE data:" + Arrays.toString(data));
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });
    }

    private float computeHeatIndex(float t, float rh) {
        t = t*1.8f+32.0f;
        float index = (float)((16.923 + (0.185212 * t) + (5.37941 * rh) - (0.100254 * t * rh) +
                (0.00941695 * (t * t)) + (0.00728898 * (rh * rh)) +
                (0.000345372 * (t * t * rh)) - (0.000814971 * (t * rh * rh)) +
                (0.0000102102 * (t * t * rh * rh)) - (0.000038646 * (t * t * t)) + (0.0000291583 *
                (rh * rh * rh)) + (0.00000142721 * (t * t * t * rh)) +
                (0.000000197483 * (t * rh * rh * rh)) - (0.0000000218429 * (t * t * t * rh * rh)) +
                0.000000000843296 * (t * t * rh * rh * rh)) -
                (0.0000000000481975 * (t * t * t * rh * rh * rh)));
        index = (index-32)/1.8f;
        return index;
    }

    @Override
    public void executeAfterDisconnect(boolean isActive) {

    }
}
