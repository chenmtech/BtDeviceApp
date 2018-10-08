package com.cmtech.android.bledevice.temphumid;

import android.os.Bundle;
import android.os.Message;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.model.BleGattElement;
import com.cmtech.android.bledeviceapp.util.Uuid;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class TempHumidDevice extends BleDevice {
    private static final String TAG = "TempHumidDevice";

    private static final int MSG_TEMPHUMIDDATA = 1;             // 当前温湿度数据消息
    private static final int MSG_TEMPHUMIDCTRL = 2;             //
    private static final int MSG_TEMPHUMIDPERIOD = 3;
    private static final int MSG_TEMPHUMIDHISTORYDATA = 4;      // 历史数据消息
    private static final int MSG_TIMERVALUE = 5;                // 定时器服务特征值消息


    ///////////////// 温湿度计Service相关的常量////////////////
    private static final String tempHumidServiceUuid    = "aa60";           // 温湿度计服务UUID:aa60
    private static final String tempHumidDataUuid       = "aa61";           // 温湿度数据特征UUID:aa61
    private static final String tempHumidCtrlUuid       = "aa62";           // 测量控制UUID:aa62
    private static final String tempHumidPeriodUuid     = "aa63";           // 采样周期UUID:aa63
    private static final String tempHumidHistoryTimeUuid  = "aa64";         // 历史数据采集的时间UUID
    private static final String tempHumidHistoryDataUuid = "aa65";          // 历史数据UUID


    public static final BleGattElement TEMPHUMIDDATA =
            new BleGattElement(tempHumidServiceUuid, tempHumidDataUuid, null);

    public static final BleGattElement TEMPHUMIDCTRL =
            new BleGattElement(tempHumidServiceUuid, tempHumidCtrlUuid, null);

    public static final BleGattElement TEMPHUMIDPERIOD =
            new BleGattElement(tempHumidServiceUuid, tempHumidPeriodUuid, null);

    public static final BleGattElement TEMPHUMIDDATACCC =
            new BleGattElement(tempHumidServiceUuid, tempHumidDataUuid, Uuid.CCCUUID);

    public static final BleGattElement TEMPHUMIDHISTORYTIME =
            new BleGattElement(tempHumidServiceUuid, tempHumidHistoryTimeUuid, null);

    public static final BleGattElement TEMPHUMIDHISTORYDATA =
            new BleGattElement(tempHumidServiceUuid, tempHumidHistoryDataUuid, null);

    private static final int DEFAULT_TEMPHUMID_PERIOD  = 5000; // 默认温湿度采样周期，单位：毫秒
    ////////////////////////////////////////////////////////


    //////////////// 定时服务相关常量 /////////////////////////
    private static final String timerServiceUuid            = "aa70";
    private static final String timerValueUuid              = "aa71";

    public static final BleGattElement TIMERVALUE =
            new BleGattElement(timerServiceUuid, timerValueUuid, null);

    private static final byte DEVICE_DEFAULT_TIMER_PERIOD  = 30; // 设备默认定时周期，单位：分钟
    ////////////////////////////////////////////////////////


    ////////////////////////////// 变量 ////////////////////////////////////////////////
    // 当前温湿度
    private TempHumidData curTempHumid;

    // 历史温湿度数据列表
    private List<TempHumidData> historyDataList = new ArrayList<>();

    // 设备是否具有定时服务
    private boolean hasTimerService = false;

    // 设备是否已经启动定时服务
    private boolean timerServiceStarted = false;

    // 设备的定时周期
    private byte deviceTimerPeriod = DEVICE_DEFAULT_TIMER_PERIOD;   // 设定的设备定时更新周期，单位：分钟

    // 设备的当前时间
    private Calendar deviceCurTime;

    // 上次更新设备历史数据的时间
    private Calendar timeLastUpdated = null;

    // 当前温湿度数据观察者列表
    private final List<ITempHumidDataObserver> tempHumidDataObserverList = new LinkedList<>();

    // 是否正在更新历史数据，防止多次更新数据导致数据重复
    private boolean isUpdatingHistoryData = false;


    // 获取当前温湿度值
    public TempHumidData getCurTempHumid() {
        return curTempHumid;
    }

    // 获取历史数据列表
    public List<TempHumidData> getHistoryDataList() {
        return historyDataList;
    }
    ////////////////////////////////////////////////////////////////////////////////////


    // 构造器
    public TempHumidDevice(BleDeviceBasicInfo basicInfo) {
        super(basicInfo);
        initializeAfterConstruction();
    }

    @Override
    public void initializeAfterConstruction() {
        curTempHumid = null;

        hasTimerService = false;
        timerServiceStarted = false;

        historyDataList.clear();
        timeLastUpdated = null;

        isUpdatingHistoryData = false;

        // 从数据库中读取设备历史数据
        readHistoryDataFromDb();
    }

    @Override
    public void executeAfterConnectSuccess() {

        // 检查是否有正常的温湿度服务和特征值
        if(!checkBasicTempHumidService()) return;

        hasTimerService = existTimerService();

        // 先读取一次当前温湿度值
        readCurrentTempHumid();

        // 启动温湿度采集服务
        int period = DEFAULT_TEMPHUMID_PERIOD;
        startTempHumidService(period);

        // 更新历史数据
        isUpdatingHistoryData = false;
        updateHistoryData();
    }

    @Override
    public void executeAfterDisconnect() {

    }

    @Override
    public void executeAfterConnectFailure() {

    }

    @Override
    public synchronized void processGattCallbackMessage(Message msg)
    {
        switch (msg.what) {
            // 获取到当前温湿度值
            case MSG_TEMPHUMIDDATA:
                if(msg.obj != null) {
                    curTempHumid = (TempHumidData) msg.obj;
                    notifyObserverCurrentTempHumidDataChanged();
                }
                break;

            // 获取到设备上的定时器设定数据值
            case MSG_TIMERVALUE:
                processTimerServiceValue((byte[])msg.obj);
                break;

            // 获取到设备上指定时间的一个历史数据值
            case MSG_TEMPHUMIDHISTORYDATA:
                TempHumidData data =  (TempHumidData) msg.obj;
                historyDataList.add(data);
                saveDataToDb(data);
                timeLastUpdated = (Calendar) data.getTime().clone();
                notifyObserverHistoryTempHumidDataChanged();
                break;

                default:
                    break;

        }
    }

    @Override
    public Bundle bundleConfigure() {
        return null;
    }

    // 更新历史数据
    public synchronized void updateHistoryData() {
        if(!isConnected() || isUpdatingHistoryData) return;
        ViseLog.i("正在更新历史数据");

        isUpdatingHistoryData = true;

        if(hasTimerService)
            readTimerServiceValue();

    }

    // 读取当前温湿度值
    private void readCurrentTempHumid() {
        addReadCommand(TEMPHUMIDDATA, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendGattCallbackMessage(MSG_TEMPHUMIDDATA, new TempHumidData(Calendar.getInstance(), data));
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    // 启动温湿度采集服务
    private void startTempHumidService(int period) {
        addWriteCommand(TEMPHUMIDPERIOD, (byte) (period / 100), null);

        // 启动温湿度采集
        addWriteCommand(TEMPHUMIDCTRL, (byte)0x01, null);

        // enable 温湿度采集的notification
        IBleCallback notifyCallback = new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendGattCallbackMessage(MSG_TEMPHUMIDDATA, new TempHumidData(Calendar.getInstance(), data));
            }

            @Override
            public void onFailure(BleException exception) {
                ViseLog.i("onFailure");
            }
        };
        addNotifyCommand(TEMPHUMIDDATACCC, true, null, notifyCallback);
    }

    // 将一个数据保存到数据库中
    private void saveDataToDb(TempHumidData data) {
        TempHumidHistoryData historyData = new TempHumidHistoryData();
        historyData.setMacAddress(getMacAddress());
        historyData.setTimeInMillis(data.getTime().getTimeInMillis());
        historyData.setTemp(data.getTemp());
        historyData.setHumid(data.getHumid());
        historyData.save();
    }

    // 从数据库中读取设备历史数据
    private void readHistoryDataFromDb() {
        Calendar time = Calendar.getInstance();
        time.add(Calendar.DAY_OF_MONTH, -1);
        long timeInMillis = time.getTimeInMillis();

        List<TempHumidHistoryData> historyDataList = LitePal.where("macAddress = ? and timeInMillis > ?", getMacAddress(), String.valueOf(timeInMillis)).
                order("timeInMillis desc").find(TempHumidHistoryData.class);
        if(historyDataList.isEmpty()) {
            timeLastUpdated = null;
        } else {
            for(int i = historyDataList.size()-1; i >= 0; i--) {
                this.historyDataList.add(new TempHumidData(historyDataList.get(i)));
            }
            timeLastUpdated = this.historyDataList.get(this.historyDataList.size()-1).getTime();
        }

        //ViseLog.i("historyDataList = " + historyDataList);
        ViseLog.i("timeLastUpdated = " + timeLastUpdated);

    }

    // 检测基本温湿度服务是否正常
    private boolean checkBasicTempHumidService() {
        boolean hasBasicTempHumidService = true;

        Object tempHumidData = getGattObject(TEMPHUMIDDATA);
        Object tempHumidControl = getGattObject(TEMPHUMIDCTRL);
        Object tempHumidPeriod = getGattObject(TEMPHUMIDPERIOD);
        Object tempHumidDataCCC = getGattObject(TEMPHUMIDDATACCC);

        if (tempHumidData == null || tempHumidControl == null || tempHumidPeriod == null || tempHumidDataCCC == null) {
            ViseLog.i("The basic service and characteristic is bad.");
            hasBasicTempHumidService = false;
        }

        return hasBasicTempHumidService;
    }

    // 检测是否存在定时器服务
    private boolean existTimerService() {
        Object timerValue = getGattObject(TIMERVALUE);
        Object historyTime = getGattObject(TEMPHUMIDHISTORYTIME);
        Object historyData = getGattObject(TEMPHUMIDHISTORYDATA);
        if (timerValue == null || historyTime == null || historyData == null) {
            ViseLog.i("The device don't provide history data sampling service.");
            return false;
        } else {
            return true;
        }
    }

    // 读取定时器服务特征值
    private void readTimerServiceValue() {
        addReadCommand(TIMERVALUE, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendGattCallbackMessage(MSG_TIMERVALUE, data);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    // 处理定时器服务特征值
    private synchronized boolean processTimerServiceValue(byte[] data) {
        if(data.length != 4) return false;

        timerServiceStarted = (data[3] == 1) ? true : false;

        if (timerServiceStarted) {
            deviceTimerPeriod = data[2];                // 保存设备的定时周期值
            deviceCurTime = getDeviceCurrentTime(data[0], data[1]);                     // 通过小时和分钟来猜测设备的当前时间
            readHistoryDataFromDevice();      // 读取设备的历史数据
        } else {
            startTimerService();                // 没有开启定时服务（比如刚换电池），就先开启定时服务
        }

        // 添加更新历史数据完毕的命令
        addInstantCommand(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                isUpdatingHistoryData = false;
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
        return true;
    }

    // 从设备读取历史数据
    private void readHistoryDataFromDevice() {
        Calendar updateFrom = (Calendar) deviceCurTime.clone();
        updateFrom.add(Calendar.DAY_OF_MONTH, -1);
        if(timeLastUpdated != null && updateFrom.before(timeLastUpdated))
            updateFrom = (Calendar) timeLastUpdated.clone();
        updateFrom.add(Calendar.MINUTE, deviceTimerPeriod); // 从前一天的一个deviceTimerPeriod周期时间之后开始更新
        if(updateFrom.after(deviceCurTime)) return;    // 上次更新到现在还不到一个deviceTimerPeriod,不需要更新

        do {
            readOneHistoryDataAtTime(updateFrom);
            updateFrom.add(Calendar.MINUTE, deviceTimerPeriod);
            if(updateFrom.after(deviceCurTime)) break;
        }while(true);
    }

    // 读取一个历史数据
    private void readOneHistoryDataAtTime(Calendar time) {
        final Calendar backuptime = (Calendar)time.clone();
        byte[] hourminute = {(byte)backuptime.get(Calendar.HOUR_OF_DAY), (byte)backuptime.get(Calendar.MINUTE)};

        // 写历史数据时间
        addWriteCommand(TEMPHUMIDHISTORYTIME, hourminute, null);

        // 读取历史数据
        addReadCommand(TEMPHUMIDHISTORYDATA, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendGattCallbackMessage(MSG_TEMPHUMIDHISTORYDATA, new TempHumidData(backuptime, data));
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    // 启动定时器服务
    private void startTimerService() {
        Calendar time = Calendar.getInstance();
        byte[] value = {(byte)time.get(Calendar.HOUR_OF_DAY), (byte)time.get(Calendar.MINUTE), deviceTimerPeriod, 0x01};

        addWriteCommand(TIMERVALUE, value, null);
    }

    // 获取设备当前时间
    private Calendar getDeviceCurrentTime(byte hour, byte minute) {
        Calendar now = Calendar.getInstance();
        Calendar devicenow;

        Calendar today = (Calendar) now.clone();
        today.set(Calendar.HOUR_OF_DAY, hour);
        today.set(Calendar.MINUTE, minute);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        Calendar tommorow = (Calendar) today.clone();
        tommorow.add(Calendar.DAY_OF_MONTH, 1);

        long difftoday = Math.abs(now.getTimeInMillis()-today.getTimeInMillis());
        long diffyest = Math.abs(now.getTimeInMillis()-yesterday.getTimeInMillis());
        long difftomm = Math.abs(now.getTimeInMillis()-tommorow.getTimeInMillis());

        if(difftoday < Math.min(diffyest, difftomm))
            devicenow = today;
        else if(diffyest < Math.min(difftoday, difftomm))
            devicenow = yesterday;
        else
            devicenow = tommorow;

        ViseLog.i("now = " + DateFormat.getDateTimeInstance().format(now.getTime()) );
        ViseLog.i("devicenow = " + DateFormat.getDateTimeInstance().format(devicenow.getTime()));

        return devicenow;
    };


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
