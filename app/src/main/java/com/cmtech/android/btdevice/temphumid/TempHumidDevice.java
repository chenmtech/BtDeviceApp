package com.cmtech.android.btdevice.temphumid;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Message;
import android.util.Log;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;
import com.cmtech.android.btdeviceapp.model.BLEDeviceBasicInfo;
import com.cmtech.android.btdeviceapp.model.BluetoothGattElement;
import com.cmtech.android.btdeviceapp.util.Uuid;
import com.vise.log.ViseLog;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class TempHumidDevice extends BLEDeviceModel {
    private static final String TAG = "TempHumidDevice";

    private static final int MSG_TEMPHUMIDDATA = 2;
    private static final int MSG_TEMPHUMIDCTRL = 3;
    private static final int MSG_TEMPHUMIDPERIOD = 4;
    private static final int MSG_TEMPHUMIDHISTORYDATA = 5;
    private static final int MSG_TIMERVALUE = 6;


    ///////////////// 温湿度计Service相关的常量和变量////////////////
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

    private TempHumidData curTempHumid;

    // 当前温湿度数据观察者列表
    private final List<ITempHumidDataObserver> tempHumidDataObserverList = new LinkedList<>();
    ////////////////////////////////////////////////////////


    //////////////// 定时服务相关常量和变量 /////////////////////////
    private static final String timerServiceUuid            = "aa70";
    private static final String timerValueUuid              = "aa71";

    public static final BluetoothGattElement TIMERVALUE =
            new BluetoothGattElement(timerServiceUuid, timerValueUuid, null);

    private static final byte DEVICE_DEFAULT_TIMER_PERIOD  = 30; // 设备默认定时周期，单位：分钟

    // 设备是否具有定时服务
    private boolean hasTimerService = false;
    // 设备是否已经启动定时服务
    private boolean hasStartTimerService = false;
    // 设备的定时周期
    private byte deviceTimerPeriod = DEVICE_DEFAULT_TIMER_PERIOD;   // 设定的设备定时更新周期，单位：分钟
    // 设备的当前时间
    private Calendar deviceCurTime;
    // 上次更新设备历史数据的时间
    private Calendar timeLastUpdated = null;
    // 准备读取的历史数据时间备份
    private Calendar backuptime;
    // 设备的历史数据
    private List<TempHumidData> historyDataList = new ArrayList<>();
    ////////////////////////////////////////////////////////

    private int times = 0;

    public TempHumidDevice(BLEDeviceBasicInfo basicInfo) {
        super(basicInfo);
    }

    public TempHumidData getCurTempHumid() {
        return curTempHumid;
    }

    public List<TempHumidData> getHistoryDataList() {
        return historyDataList;
    }

    // 处理一般Gatt消息
    @Override
    public synchronized void processCommonGattMessage(BluetoothGattChannel channel) {
        if(!isCommandExecutorAlive() || channel == null) return;
        BluetoothGattCharacteristic characteristic = channel.getCharacteristic();
        String shortUuid = Uuid.longToShortString(characteristic.getUuid().toString());

        ViseLog.i("Characteristic uuid: " + shortUuid + ", Property: " + channel.getPropertyType() + ", Value: " + Arrays.toString(characteristic.getValue()));

        if(commandExecutor.isChannelSameAsCurrentCommand(channel)) {

            if(shortUuid.equalsIgnoreCase(tempHumidDataUuid)) {
                if(channel.getPropertyType() == PropertyType.PROPERTY_READ) {
                    curTempHumid = new TempHumidData(Calendar.getInstance(), characteristic.getValue());
                    notifyObserverCurrentTempHumidDataChanged();
                }
            } else if(shortUuid.equalsIgnoreCase(timerValueUuid)) {
                if(channel.getPropertyType() == PropertyType.PROPERTY_READ) {
                    onReadTimerValue(characteristic.getValue());
                    ViseLog.i("The timer value is " + Arrays.toString(characteristic.getValue()));
                } else {
                    ViseLog.i("The timer service has been started.");
                }
            } else if(shortUuid.equalsIgnoreCase(tempHumidHistoryDataUuid)) {
                TempHumidData data = new TempHumidData(backuptime, characteristic.getValue());
                historyDataList.add(data);
                notifyObserverHistoryTempHumidDataChanged();
                times++;
                ViseLog.i("the history data was read " + times + " times.");
                /*timeLastUpdated = (Calendar)backuptime.clone();
                backuptime.add(Calendar.MINUTE, deviceTimerPeriod);
                if(!backuptime.after(deviceCurTime)) {
                    readHistoryDataAtTime(backuptime);
                }*/
            }
            commandExecutor.notifyCurrentCommandExecuted(true);
        }
    }

    @Override
    public synchronized void processSpecialGattMessage(Message msg)
    {
        if (msg.what == MSG_TEMPHUMIDDATA) {
            if(msg.obj != null) {
                curTempHumid = (TempHumidData) msg.obj;
                notifyObserverCurrentTempHumidDataChanged();
            }
        }
    }

    @Override
    public synchronized void executeAfterConnectSuccess() {
        if(!checkServiceAndCharacteristic()) return;

        if(!isCommandExecutorAlive()) createCommandExecutor();

        addReadCommand(TEMPHUMIDDATA, commonGattCallback);

        // 设置采集周期
        int period = 5000;
        addWriteCommand(TEMPHUMIDPERIOD, new byte[]{(byte)(period / 100)}, commonGattCallback);


        // enable 温湿度采集的notification
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
                ViseLog.i("onFailure");

            }
        };
        addNotifyCommand(TEMPHUMIDDATACCC, true, commonGattCallback, notifyCallback);


        // 启动温湿度采集
        addWriteCommand(TEMPHUMIDCTRL, new byte[]{0x01}, commonGattCallback);

        if(hasTimerService)
            startTimerService();
    }

    private boolean checkServiceAndCharacteristic() {
        Object tempHumidData = getGattObject(TEMPHUMIDDATA);
        Object tempHumidControl = getGattObject(TEMPHUMIDCTRL);
        Object tempHumidPeriod = getGattObject(TEMPHUMIDPERIOD);
        Object tempHumidDataCCC = getGattObject(TEMPHUMIDDATACCC);

        if (tempHumidData == null || tempHumidControl == null || tempHumidPeriod == null || tempHumidDataCCC == null) {
            ViseLog.i("The basic service and characteristic is bad.");
            return false;
        }

        Object timerValue = getGattObject(TIMERVALUE);
        Object historyTime = getGattObject(TEMPHUMIDHISTORYTIME);
        Object historyData = getGattObject(TEMPHUMIDHISTORYDATA);
        if (timerValue == null || historyTime == null || historyData == null) {
            ViseLog.i("The device don't provide history data sampling service.");
            hasTimerService = false;
        } else {
            hasTimerService = true;
        }
        return true;
    }


    private void startTimerService() {
        addReadCommand(TIMERVALUE, commonGattCallback);
    }

    private void onReadTimerValue(byte[] data) {
        if(data.length != 4) return;

        hasStartTimerService = (data[3] == 1) ? true : false;

        if (hasStartTimerService) {
            deviceTimerPeriod = data[2];                // 保存设备的定时周期值
            deviceCurTime = guessDeviceCurTime(data);                     // 通过小时和分钟来猜测设备的当前时间
            onReadHistoryDataFromDevice();      // 读取设备的历史数据
        } else {
            onStartTimerService();                // 没有开启定时服务（比如刚换电池），就先开启定时服务
        }
    }



    private void onReadHistoryDataFromDevice() {
        if(!hasStartTimerService) return;

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

        // 这里有问题
        readHistoryDataAtTime(updateFrom);
        //readHistoryDataAtTime(updateFrom);
    }

    private void readHistoryDataAtTime(Calendar time) {
        backuptime = (Calendar)time.clone();
        final byte[] hourminute = {(byte)backuptime.get(Calendar.HOUR_OF_DAY), (byte)backuptime.get(Calendar.MINUTE)};

        // 写历史数据时间
        //addWriteCommand(TEMPHUMIDHISTORYTIME, hourminute, commonGattCallback);

        // 读取历史数据
        addReadCommand(TEMPHUMIDHISTORYDATA, commonGattCallback);
    }

    private void onStartTimerService() {
        Calendar time = Calendar.getInstance();
        byte[] value = {(byte)time.get(Calendar.HOUR_OF_DAY), (byte)time.get(Calendar.MINUTE), deviceTimerPeriod, 0x01};

        addWriteCommand(TIMERVALUE, value, commonGattCallback);
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



    @Override
    public void executeAfterDisconnect(boolean isActive) {
        stopCommandExecutor();
    }

    @Override
    public void executeAfterConnectFailure() {
        stopCommandExecutor();
        //disconnect();
    }

    // 关闭设备
    @Override
    public synchronized void close() {
        super.close();

        // 清空连接状态观察者列表
        tempHumidDataObserverList.clear();
    }

    // 登记温湿度数据观察者
    public void registerTempHumidDataObserver(ITempHumidDataObserver observer) {
        if(!tempHumidDataObserverList.contains(observer)) {
            tempHumidDataObserverList.add(observer);
        }
    }

    // 删除连接状态观察者
    public void removeTempHumidDataObserver(ITempHumidDataObserver observer) {
        int index = tempHumidDataObserverList.indexOf(observer);
        if(index >= 0) {
            tempHumidDataObserverList.remove(index);
        }
    }

    // 通知连接状态观察者
    public void notifyObserverCurrentTempHumidDataChanged() {
        for(final ITempHumidDataObserver observer : tempHumidDataObserverList) {
            if(observer != null) {
                observer.updateCurrentTempHumidData();
            }
        }
    }

    // 通知连接状态观察者
    public void notifyObserverHistoryTempHumidDataChanged() {
        for(final ITempHumidDataObserver observer : tempHumidDataObserverList) {
            if(observer != null) {
                observer.updateHistoryTempHumidData();
            }
        }
    }
}
