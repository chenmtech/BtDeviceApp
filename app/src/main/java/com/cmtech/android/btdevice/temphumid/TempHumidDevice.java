package com.cmtech.android.btdevice.temphumid;

import android.os.Message;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.btdeviceapp.model.BLEDevice;
import com.cmtech.android.btdeviceapp.model.BLEDeviceBasicInfo;
import com.cmtech.android.btdeviceapp.model.BluetoothGattElement;
import com.cmtech.android.btdeviceapp.util.Uuid;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class TempHumidDevice extends BLEDevice {
    private static final String TAG = "TempHumidDevice";

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

    private static final int DEFAULT_TEMPHUMID_PERIOD  = 5000; // 默认温湿度采样周期，单位：毫秒
    ////////////////////////////////////////////////////////


    //////////////// 定时服务相关常量 /////////////////////////
    private static final String timerServiceUuid            = "aa70";
    private static final String timerValueUuid              = "aa71";

    public static final BluetoothGattElement TIMERVALUE =
            new BluetoothGattElement(timerServiceUuid, timerValueUuid, null);

    private static final byte DEVICE_DEFAULT_TIMER_PERIOD  = 30; // 设备默认定时周期，单位：分钟
    ////////////////////////////////////////////////////////


    ////////////////////////////// 变量 ////////////////////////////////////////////////
    // 当前温湿度
    private TempHumidData curTempHumid;

    // 历史温湿度数据
    private List<TempHumidData> historyDataList = new ArrayList<>();

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

    // 准备读取的历史数据对应的时间备份
    private Calendar backuptime = null;

    // 当前温湿度数据观察者列表
    private final List<ITempHumidDataObserver> tempHumidDataObserverList = new LinkedList<>();
    ////////////////////////////////////////////////////////////////////////////////////


    // 构造器
    public TempHumidDevice(BLEDeviceBasicInfo basicInfo) {
        super(basicInfo);
        initialize();
    }

    @Override
    public void initialize() {
        curTempHumid = null;
        historyDataList.clear();
        hasTimerService = false;
        hasStartTimerService = false;
        timeLastUpdated = null;
        backuptime = null;

        //tempHumidDataObserverList.clear();
    }

    // 获取当前温湿度值
    public TempHumidData getCurTempHumid() {
        return curTempHumid;
    }

    // 获取历史数据
    public List<TempHumidData> getHistoryDataList() {
        return historyDataList;
    }


    @Override
    public synchronized void processGattMessage(Message msg)
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
                TempHumidData data = new TempHumidData(backuptime, (byte[]) msg.obj);
                historyDataList.add(data);
                saveDataToDb(data);
                notifyObserverHistoryTempHumidDataChanged();
                break;

                default:
                    break;

        }
    }

    private void saveDataToDb(TempHumidData data) {
        TempHumidHistoryData historyData = new TempHumidHistoryData();
        historyData.setMacAddress(getMacAddress());
        historyData.setTime(data.getTime());
        historyData.setTemp(data.getTemp());
        historyData.setHumid(data.getHumid());
        historyData.save();
    }

    @Override
    public synchronized void executeAfterConnectSuccess() {
        // 检查所需的服务和特征值
        if(!checkServiceAndCharacteristic()) return;

        // 创建Gatt串行命令执行器
        createGattCommandExecutor();

        // 先读取一次当前温湿度值
        commandExecutor.addReadCommand(TEMPHUMIDDATA, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendMessage(MSG_TEMPHUMIDDATA, new TempHumidData(Calendar.getInstance(), data));
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });

        // 设置温湿度采样周期
        int period = DEFAULT_TEMPHUMID_PERIOD;
        commandExecutor.addWriteCommand(TEMPHUMIDPERIOD, (byte) (period / 100), null);

        // 启动温湿度采集
        commandExecutor.addWriteCommand(TEMPHUMIDCTRL, (byte)0x01, null);

        // enable 温湿度采集的notification
        IBleCallback notifyCallback = new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendMessage(MSG_TEMPHUMIDDATA, new TempHumidData(Calendar.getInstance(), data));
            }

            @Override
            public void onFailure(BleException exception) {
                ViseLog.i("onFailure");
            }
        };
        commandExecutor.addNotifyCommand(TEMPHUMIDDATACCC, true, null, notifyCallback);


        if(hasTimerService) {
            readTimerServiceValue();
        }
    }

    private boolean checkServiceAndCharacteristic() {
        boolean hasBasicTempHumidService = true;

        Object tempHumidData = getGattObject(TEMPHUMIDDATA);
        Object tempHumidControl = getGattObject(TEMPHUMIDCTRL);
        Object tempHumidPeriod = getGattObject(TEMPHUMIDPERIOD);
        Object tempHumidDataCCC = getGattObject(TEMPHUMIDDATACCC);

        if (tempHumidData == null || tempHumidControl == null || tempHumidPeriod == null || tempHumidDataCCC == null) {
            ViseLog.i("The basic service and characteristic is bad.");
            hasBasicTempHumidService = false;
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
        return hasBasicTempHumidService;
    }

    private void readTimerServiceValue() {
        commandExecutor.addReadCommand(TIMERVALUE, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendMessage(MSG_TIMERVALUE, data);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    private synchronized boolean processTimerServiceValue(byte[] data) {
        if(data.length != 4) return false;

        hasStartTimerService = (data[3] == 1) ? true : false;

        if (hasStartTimerService) {
            deviceTimerPeriod = data[2];                // 保存设备的定时周期值
            deviceCurTime = guessDeviceCurTime(data[0], data[1]);                     // 通过小时和分钟来猜测设备的当前时间
            readHistoryDataFromDevice();      // 读取设备的历史数据
        } else {
            startTimerService();                // 没有开启定时服务（比如刚换电池），就先开启定时服务
        }
        return true;
    }

    private void readHistoryDataFromDevice() {
        if(!hasStartTimerService) return;

        Calendar updateFrom;
        if(timeLastUpdated != null) {
            updateFrom = (Calendar) timeLastUpdated.clone();
            updateFrom.add(Calendar.MINUTE, deviceTimerPeriod);
            if(updateFrom.after(deviceCurTime)) return;    // 上次更新到现在还不到一个deviceTimerPeriod,不需要更新
        } else {
            updateFrom = (Calendar) deviceCurTime.clone();
            updateFrom.add(Calendar.DAY_OF_MONTH, -1);
            updateFrom.add(Calendar.MINUTE, deviceTimerPeriod); // 从前一天的一个deviceTimerPeriod周期时间开始更新
        }

        // 这里有问题
        readOneHistoryDataAtTime(updateFrom);

        readOneHistoryDataAtTime(updateFrom);
    }

    private void readOneHistoryDataAtTime(Calendar time) {
        backuptime = (Calendar)time.clone();
        byte[] hourminute = {(byte)backuptime.get(Calendar.HOUR_OF_DAY), (byte)backuptime.get(Calendar.MINUTE)};

        // 写历史数据时间
        commandExecutor.addWriteCommand(TEMPHUMIDHISTORYTIME, hourminute, null);

        // 读取历史数据
        commandExecutor.addReadCommand(TEMPHUMIDHISTORYDATA, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendMessage(MSG_TEMPHUMIDHISTORYDATA, data);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    private void startTimerService() {
        Calendar time = Calendar.getInstance();
        byte[] value = {(byte)time.get(Calendar.HOUR_OF_DAY), (byte)time.get(Calendar.MINUTE), deviceTimerPeriod, 0x01};

        commandExecutor.addWriteCommand(TIMERVALUE, value, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                ViseLog.i("Success to start timer service.");
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    private Calendar guessDeviceCurTime(byte hour, byte minute) {
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

    @Override
    public void executeAfterDisconnect(boolean isActive) {
        stopCommandExecutor();
    }

    // 关闭设备
    @Override
    public synchronized void close() {
        super.close();

        // 初始化所有数据
        initialize();
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