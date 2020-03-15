package com.cmtech.android.bledevice.temphumid.model;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;
import com.cmtech.bmefile.ByteUtil;
import com.vise.log.ViseLog;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.cmtech.android.bledeviceapp.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.AppConstant.MY_BASE_UUID;
import static com.cmtech.android.bledeviceapp.AppConstant.STANDARD_BLE_UUID;

/**
 * TempHumidDevice
 * Created by bme on 2018/9/20.
 */

public class TempHumidDevice extends AbstractDevice {
    private static final String TAG = "TempHumidDevice";

    private static final short DEFAULT_TEMPHUMID_INTERVAL  = 15; // default measurement interval, unit: second

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
            new BleGattElement(tempHumidServiceUUID, tempHumidIntervalUUID, tempHumidIRangeUUID, "测量间隔范围");

    // current Temp&Humid data
    private BleTempHumidData tempHumidData;

    private short interval = DEFAULT_TEMPHUMID_INTERVAL;

    private short intMin;
    private short intMax;

    // device listeners
    private final List<OnTempHumidDeviceListener> listeners = new LinkedList<>();

    // get T&H data
    public BleTempHumidData getTempHumidData() {
        return tempHumidData;
    }

    // 构造器
    public TempHumidDevice(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public boolean onConnectSuccess() {
        BleGattElement[] elements = new BleGattElement[]{TEMPHUMIDDATA, TEMPHUMIDINTERVAL, TEMPHUMIDDATACCC, TEMPHUMIDIRANGE};

        if(!((BleConnector)connector).containGattElements(elements)) {
            ViseLog.e("temphumid element wrong.");
            return false;
        }

        // read interval range
        readIRange();

        // set measurement interval
        ((BleConnector)connector).write(TEMPHUMIDINTERVAL, ByteUtil.getBytes(interval), null);

        // start measurement
        startTempHumidMeasure();

        return true;
    }

    @Override
    public  void onDisconnect() {

    }

    @Override
    public  void onConnectFailure() {

    }

    // start measurement
    private void startTempHumidMeasure() {
        // enable temphumid indication
        IBleDataCallback indicateCallback = new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                int temp = ByteUtil.getShort(new byte[]{data[0], data[1]});
                int humid = UnsignedUtil.getUnsignedShort(ByteUtil.getShort(new byte[]{data[2], data[3]}));
                tempHumidData = new BleTempHumidData(new Date().getTime(), temp, humid);

                notifyTempHumidData();
            }

            @Override
            public void onFailure(BleException exception) {
                ViseLog.i("onFailure");
            }
        };
        ((BleConnector)connector).indicate(TEMPHUMIDDATACCC, false, null);

        ((BleConnector)connector).indicate(TEMPHUMIDDATACCC, true, indicateCallback);
    }

    private void readIRange() {
        ((BleConnector)connector).read(TEMPHUMIDIRANGE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                intMin = ByteUtil.getShort(new byte[]{data[0], data[1]});
                intMax = ByteUtil.getShort(new byte[]{data[2], data[3]});
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    public void setInterval(short interval) {
        if(this.interval != interval) {
            this.interval = interval;
            //((BleConnector)connector).indicate(TEMPHUMIDDATACCC, false, null);
            ((BleConnector)connector).write(TEMPHUMIDINTERVAL, ByteUtil.getBytes(interval), null);
        }
    }

    public short getInterval() {
        return interval;
    }

    // 登记温湿度数据观察者
    public void registerListener(OnTempHumidDeviceListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    // 删除连接状态观察者
    public void removeListener(OnTempHumidDeviceListener listener) {
        int index = listeners.indexOf(listener);
        if(index >= 0) {
            listeners.remove(index);
        }
    }

    //
    private void notifyTempHumidData() {
        for(final OnTempHumidDeviceListener listener : listeners) {
            if(listener != null)
                listener.onTempHumidDataUpdated(tempHumidData);
        }
    }
}
