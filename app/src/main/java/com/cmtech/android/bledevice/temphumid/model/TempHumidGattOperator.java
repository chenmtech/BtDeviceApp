package com.cmtech.android.bledevice.temphumid.model;

import com.cmtech.android.bledevice.core.BleDataOpException;
import com.cmtech.android.bledevice.core.BleDeviceGattOperator;
import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledevice.core.BleGattElement;
import com.cmtech.android.bledevice.core.IBleDataOpCallback;
import com.vise.log.ViseLog;

import java.util.Calendar;

import static com.cmtech.android.bledevice.core.BleDeviceConstant.CCCUUID;
import static com.cmtech.android.bledevice.core.BleDeviceConstant.MY_BASE_UUID;
import static com.cmtech.android.bledevice.temphumid.model.TempHumidDevice.MSG_TEMPHUMIDDATA;
import static com.cmtech.android.bledevice.temphumid.model.TempHumidDevice.MSG_TEMPHUMIDHISTORYDATA;
import static com.cmtech.android.bledevice.temphumid.model.TempHumidDevice.MSG_TIMERVALUE;

public class TempHumidGattOperator extends BleDeviceGattOperator {

    ///////////////// 温湿度计Service相关的常量////////////////
    private static final String tempHumidServiceUuid    = "aa60";           // 温湿度计服务UUID:aa60
    private static final String tempHumidDataUuid       = "aa61";           // 温湿度数据特征UUID:aa61
    private static final String tempHumidCtrlUuid       = "aa62";           // 测量控制UUID:aa62
    private static final String tempHumidPeriodUuid     = "aa63";           // 采样周期UUID:aa63
    private static final String tempHumidHistoryTimeUuid  = "aa64";         // 历史数据采集的时间UUID
    private static final String tempHumidHistoryDataUuid = "aa65";          // 历史数据UUID


    private static final BleGattElement TEMPHUMIDDATA =
            new BleGattElement(tempHumidServiceUuid, tempHumidDataUuid, null, MY_BASE_UUID, "温湿度数据");

    private static final BleGattElement TEMPHUMIDCTRL =
            new BleGattElement(tempHumidServiceUuid, tempHumidCtrlUuid, null, MY_BASE_UUID, "温湿度Ctrl");

    private static final BleGattElement TEMPHUMIDPERIOD =
            new BleGattElement(tempHumidServiceUuid, tempHumidPeriodUuid, null, MY_BASE_UUID, "采集周期(ms)");

    private static final BleGattElement TEMPHUMIDDATACCC =
            new BleGattElement(tempHumidServiceUuid, tempHumidDataUuid, CCCUUID, MY_BASE_UUID, "温湿度CCC");

    private static final BleGattElement TEMPHUMIDHISTORYTIME =
            new BleGattElement(tempHumidServiceUuid, tempHumidHistoryTimeUuid, null, MY_BASE_UUID, "历史数据采集时间");

    private static final BleGattElement TEMPHUMIDHISTORYDATA =
            new BleGattElement(tempHumidServiceUuid, tempHumidHistoryDataUuid, null, MY_BASE_UUID, "温湿度历史数据");


    ////////////////////////////////////////////////////////


    //////////////// 定时服务相关常量 /////////////////////////
    private static final String timerServiceUuid            = "aa70";
    private static final String timerValueUuid              = "aa71";

    private static final BleGattElement TIMERVALUE =
            new BleGattElement(timerServiceUuid, timerValueUuid, null, MY_BASE_UUID, "定时周期(min)");




    public TempHumidGattOperator() {
        super();
    }


    // 读取当前温湿度值
    public void readCurrentTempHumid() {
        addReadCommand(TEMPHUMIDDATA, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                device.sendGattMessage(MSG_TEMPHUMIDDATA, new TempHumidData(Calendar.getInstance(), data));
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });
    }

    // 启动温湿度采集服务
    public void startTempHumidService(int period) {
        addWriteCommand(TEMPHUMIDPERIOD, (byte) (period / 100), null);

        // 启动温湿度采集
        addWriteCommand(TEMPHUMIDCTRL, (byte)0x01, null);

        // enable 温湿度采集的notification
        IBleDataOpCallback notifyCallback = new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                device.sendGattMessage(MSG_TEMPHUMIDDATA, new TempHumidData(Calendar.getInstance(), data));
            }

            @Override
            public void onFailure(BleDataOpException exception) {
                ViseLog.i("onFailure");
            }
        };
        addNotifyCommand(TEMPHUMIDDATACCC, true, null, notifyCallback);
    }

    // 检测是否存在定时器服务
    public boolean existTimerService() {
        Object timerValue = BleDeviceUtil.getGattObject(device, TIMERVALUE);
        Object historyTime = BleDeviceUtil.getGattObject(device, TEMPHUMIDHISTORYTIME);
        Object historyData = BleDeviceUtil.getGattObject(device, TEMPHUMIDHISTORYDATA);
        if (timerValue == null || historyTime == null || historyData == null) {
            ViseLog.i("The device don't provide history data sampling service.");
            return false;
        } else {
            return true;
        }
    }

    // 读取定时器服务特征值
    public void readTimerServiceValue() {
        addReadCommand(TIMERVALUE, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                device.sendGattMessage(MSG_TIMERVALUE, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });
    }

    // 读取一个历史数据
    public void readOneHistoryDataAtTime(Calendar time) {
        final Calendar backuptime = (Calendar)time.clone();
        byte[] hourminute = {(byte)backuptime.get(Calendar.HOUR_OF_DAY), (byte)backuptime.get(Calendar.MINUTE)};

        // 写历史数据时间
        addWriteCommand(TEMPHUMIDHISTORYTIME, hourminute, null);

        // 读取历史数据
        addReadCommand(TEMPHUMIDHISTORYDATA, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                device.sendGattMessage(MSG_TEMPHUMIDHISTORYDATA, new TempHumidData(backuptime, data));
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });
    }

    // 启动定时器服务
    public void startTimerService() {
        Calendar time = Calendar.getInstance();
        byte[] value = {(byte)time.get(Calendar.HOUR_OF_DAY), (byte)time.get(Calendar.MINUTE), ((TempHumidDevice)device).getDeviceTimerPeriod(), 0x01};

        addWriteCommand(TIMERVALUE, value, null);
    }

    // 检测基本温湿度服务是否正常
    @Override
    public boolean checkBasicService() {
        boolean hasBasicTempHumidService = true;

        Object tempHumidData = BleDeviceUtil.getGattObject(device, TEMPHUMIDDATA);
        Object tempHumidControl = BleDeviceUtil.getGattObject(device, TEMPHUMIDCTRL);
        Object tempHumidPeriod = BleDeviceUtil.getGattObject(device, TEMPHUMIDPERIOD);
        Object tempHumidDataCCC = BleDeviceUtil.getGattObject(device, TEMPHUMIDDATACCC);

        if (tempHumidData == null || tempHumidControl == null || tempHumidPeriod == null || tempHumidDataCCC == null) {
            ViseLog.i("The basic service and characteristic is bad.");
            hasBasicTempHumidService = false;
        }

        return hasBasicTempHumidService;
    }
}
