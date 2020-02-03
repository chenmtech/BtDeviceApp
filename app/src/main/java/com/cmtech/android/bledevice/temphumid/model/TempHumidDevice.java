package com.cmtech.android.bledevice.temphumid.model;

import android.os.Handler;
import android.os.Looper;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleDeviceConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.bmefile.ByteUtil;
import com.vise.log.ViseLog;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.cmtech.android.bledeviceapp.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.AppConstant.MY_BASE_UUID;
import static com.cmtech.android.bledeviceapp.AppConstant.STANDARD_BLE_UUID;

/**
 * TempHumidDevice: 温湿度计设备类
 * Created by bme on 2018/9/20.
 */

public class TempHumidDevice extends AbstractDevice {
    private static final String TAG = "TempHumidDevice";

    private static final short DEFAULT_TEMPHUMID_INTERVAL  = 15; // 默认温湿度测量间隔，单位：秒

    ///////////////// 温湿度计Service相关的常量////////////////
    private static final String tempHumidServiceUuid    = "aa60";           // 温湿度计服务UUID:aa60
    private static final String tempHumidDataUuid       = "aa61";           // 温湿度数据特征UUID:aa61
    private static final String tempHumidIntervalUuid   = "2a21";           // 测量间隔UUID:aa63
    private static final String tempHumidIRangeUuid     = "2906";           // 测量间隔范围UUID

    private static final UUID tempHumidServiceUUID    = UuidUtil.stringToUuid(tempHumidServiceUuid, MY_BASE_UUID);
    private static final UUID tempHumidDataUUID       = UuidUtil.stringToUuid(tempHumidDataUuid, MY_BASE_UUID);
    private static final UUID tempHumidIntervalUUID   = UuidUtil.stringToUuid(tempHumidIntervalUuid, STANDARD_BLE_UUID);
    private static final UUID tempHumidIRangeUUID     = UuidUtil.stringToUuid(tempHumidIRangeUuid, STANDARD_BLE_UUID);

    private static final BleGattElement TEMPHUMIDDATA =
            new BleGattElement(tempHumidServiceUUID, tempHumidDataUUID, null, "温湿度数据");
    private static final BleGattElement TEMPHUMIDINTERVAL =
            new BleGattElement(tempHumidServiceUUID, tempHumidIntervalUUID, null, "测量间隔(s)");
    private static final BleGattElement TEMPHUMIDDATACCC =
            new BleGattElement(tempHumidServiceUUID, tempHumidDataUUID, CCC_UUID, "温湿度CCC");
    private static final BleGattElement TEMPHUMIDIRANGE =
            new BleGattElement(tempHumidServiceUUID, tempHumidIntervalUUID, tempHumidIRangeUUID, "温湿度CCC");


    // 当前温湿度
    private TempHumidData curTempHumid;

    // 当前温湿度数据观察者列表
    private final List<ITempHumidDataObserver> tempHumidDataObserverList = new LinkedList<>();


    // 获取当前温湿度值
    public TempHumidData getCurTempHumid() {
        return curTempHumid;
    }


    // 构造器
    public TempHumidDevice(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
        curTempHumid = null;
    }

    @Override
    public boolean onConnectSuccess() {
        // 检查是否有正常的温湿度服务和特征值
        BleGattElement[] elements = new BleGattElement[]{TEMPHUMIDDATA, TEMPHUMIDINTERVAL, TEMPHUMIDDATACCC, TEMPHUMIDIRANGE};

        if(!((BleDeviceConnector)connector).containGattElements(elements)) {
            ViseLog.e("temphumid element wrong.");

            return false;
        }

        // 启动温湿度采集服务
        startTempHumidService(DEFAULT_TEMPHUMID_INTERVAL);

        return true;
    }

    @Override
    public  void onDisconnect() {

    }

    @Override
    public  void onConnectFailure() {

    }


    // 启动温湿度采集服务
    private void startTempHumidService(short period) {
        ((BleDeviceConnector)connector).write(TEMPHUMIDINTERVAL, ByteUtil.getBytes(period), null);

        ((BleDeviceConnector)connector).read(TEMPHUMIDINTERVAL, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                ViseLog.e(data);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });

        // enable温湿度测量Indication
        IBleDataCallback indicateCallback = new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                ViseLog.e("hi");
                curTempHumid = new TempHumidData(Calendar.getInstance(), data);

                updateCurrentData();
            }

            @Override
            public void onFailure(BleException exception) {
                ViseLog.i("onFailure");
            }
        };
        ((BleDeviceConnector)connector).indicate(TEMPHUMIDDATACCC, true, indicateCallback);
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
    private void updateCurrentData() {
        for(final ITempHumidDataObserver observer : tempHumidDataObserverList) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(observer != null)
                        observer.updateCurrentData();
                }
            });
        }
    }
}
