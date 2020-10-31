package com.cmtech.android.bledevice.thm.model;

import android.content.Context;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.data.record.BleTempHumidRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordFactory;
import com.cmtech.android.bledeviceapp.data.record.RecordType;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;
import com.cmtech.bmefile.ByteUtil;
import com.vise.log.ViseLog;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.cmtech.android.bledeviceapp.data.record.BasicRecord.DEFAULT_RECORD_VER;
import static com.cmtech.android.bledeviceapp.global.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.MY_BASE_UUID;
import static com.cmtech.android.bledeviceapp.global.AppConstant.STANDARD_BLE_UUID;

/**
 * TempHumidDevice
 * Created by bme on 2018/9/20.
 */

public class ThmDevice extends AbstractDevice {
    private static final String TAG = "TempHumidDevice";

    private static final short DEFAULT_TEMPHUMID_INTERVAL  = 15; // default measurement interval, unit: second

    private static final String tempHumidServiceUuid    = "aa60";           // 温湿度计服务UUID:aa60
    private static final String tempHumidDataUuid       = "aa61";           // 温湿度数据特征UUID:aa61
    private static final String tempHumidIntervalUuid   = "2a21";           // 测量间隔UUID:aa63
    private static final String tempHumidIRangeUuid     = "2906";           // 测量间隔范围UUID

    private static final UUID tempHumidServiceUUID    = UuidUtil.stringToUUID(tempHumidServiceUuid, MY_BASE_UUID);
    private static final UUID tempHumidDataUUID       = UuidUtil.stringToUUID(tempHumidDataUuid, MY_BASE_UUID);
    private static final UUID tempHumidIntervalUUID   = UuidUtil.stringToUUID(tempHumidIntervalUuid, STANDARD_BLE_UUID);
    private static final UUID tempHumidIRangeUUID     = UuidUtil.stringToUUID(tempHumidIRangeUuid, STANDARD_BLE_UUID);

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
    private final List<OnThmListener> listeners = new LinkedList<>();

    // get T&H data
    public BleTempHumidData getTempHumidData() {
        return tempHumidData;
    }

    // 构造器
    public ThmDevice(Context context, DeviceCommonInfo registerInfo) {
        super(context, registerInfo);
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

    public void save(String loc) {
        BleTempHumidRecord record = (BleTempHumidRecord) RecordFactory.create(RecordType.TH, DEFAULT_RECORD_VER, new Date().getTime(), getAddress(), MyApplication.getAccount());
        record.setTemperature(tempHumidData.getTemp()/100.0f);
        record.setHumid(tempHumidData.getHumid()/100.0f);
        record.setHeatIndex(tempHumidData.calculateHeatIndex());
        record.setLocation(loc);
        record.save();
        Toast.makeText(MyApplication.getContext(), MyApplication.getStr(R.string.save_record_success), Toast.LENGTH_SHORT).show();
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
    public void registerListener(OnThmListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    // 删除连接状态观察者
    public void removeListener(OnThmListener listener) {
        int index = listeners.indexOf(listener);
        if(index >= 0) {
            listeners.remove(index);
        }
    }

    //
    private void notifyTempHumidData() {
        for(final OnThmListener listener : listeners) {
            if(listener != null)
                listener.onTempHumidDataUpdated(tempHumidData);
        }
        setNotificationInfo("温度" + tempHumidData.getTemp()/100.0f + " 湿度" + tempHumidData.getHumid()/100.0f);
    }
}
