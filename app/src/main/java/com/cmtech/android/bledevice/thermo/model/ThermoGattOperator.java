package com.cmtech.android.bledevice.thermo.model;

import android.util.Log;

import com.cmtech.android.bledevice.core.BleDataOpException;
import com.cmtech.android.bledevice.core.BleDevice;
import com.cmtech.android.bledevice.core.BleDeviceGattOperator;
import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledevice.core.BleGattElement;
import com.cmtech.android.bledevice.core.IBleDataOpCallback;

import static com.cmtech.android.bledevice.core.BleDeviceConstant.CCCUUID;
import static com.cmtech.android.bledevice.core.BleDeviceConstant.MY_BASE_UUID;
import static com.cmtech.android.bledevice.thermo.model.ThermoDevice.MSG_THERMODATA;

public class ThermoGattOperator extends BleDeviceGattOperator {
    ///////////////// 体温计Service相关的常量////////////////
    private static final String thermoServiceUuid       = "aa30";           // 体温计服务UUID:aa30
    private static final String thermoDataUuid          = "aa31";           // 体温数据特征UUID:aa31
    private static final String thermoControlUuid       = "aa32";           // 体温测量控制UUID:aa32
    private static final String thermoPeriodUuid        = "aa33";           // 体温采样周期UUID:aa33

    private static final BleGattElement THERMODATA =
            new BleGattElement(thermoServiceUuid, thermoDataUuid, null, MY_BASE_UUID, "体温值");
    private static final BleGattElement THERMOCONTROL =
            new BleGattElement(thermoServiceUuid, thermoControlUuid, null, MY_BASE_UUID, "体温Ctrl");
    private static final BleGattElement THERMOPERIOD =
            new BleGattElement(thermoServiceUuid, thermoPeriodUuid, null, MY_BASE_UUID, "采集周期(s)");
    private static final BleGattElement THERMODATACCC =
            new BleGattElement(thermoServiceUuid, thermoDataUuid, CCCUUID, MY_BASE_UUID, "体温CCC");

    public ThermoGattOperator(BleDevice device) {
        super(device);
    }

    public void readThermoData() {
        // 读温度数据
        addReadCommand(THERMODATA, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                device.sendGattMessage(MSG_THERMODATA, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {
            }
        });
    }

    /*
    启动体温计，设置采样周期
    period: 采样周期，单位：秒
     */
    public void startThermometer(byte period) {
        // 设置采样周期
        addWriteCommand(THERMOPERIOD, period, null);

        IBleDataOpCallback notifyCallback = new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                device.sendGattMessage(MSG_THERMODATA, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        };

        // enable温度数据notify
        addNotifyCommand(THERMODATACCC, true, null, notifyCallback);

        // 启动温度采集
        addWriteCommand(THERMOCONTROL, (byte)0x03, null);
    }

    // 检测基本温湿度服务是否正常
    @Override
    public boolean checkBasicService() {
        Object thermoData = BleDeviceUtil.getGattObject(device, THERMODATA);
        Object thermoControl = BleDeviceUtil.getGattObject(device, THERMOCONTROL);
        Object thermoPeriod = BleDeviceUtil.getGattObject(device, THERMOPERIOD);
        Object thermoDataCCC = BleDeviceUtil.getGattObject(device, THERMODATACCC);

        if(thermoData == null || thermoControl == null || thermoPeriod == null || thermoDataCCC == null) {
            Log.d("ThermoFragment", "Can't find the Gatt object on the device.");
            return false;
        }

        return true;
    }
}
