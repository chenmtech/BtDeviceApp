package com.cmtech.android.bledevice.thermo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledevicecore.model.Uuid;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.model.BleGattElement;

import java.util.LinkedList;
import java.util.List;

public class ThermoDevice extends BleDevice {
    private static final int MSG_THERMODATA = 1;

    ///////////////// 体温计Service相关的常量////////////////
    private static final String thermoServiceUuid       = "aa30";           // 体温计服务UUID:aa30
    private static final String thermoDataUuid          = "aa31";           // 体温数据特征UUID:aa31
    private static final String thermoControlUuid       = "aa32";           // 体温测量控制UUID:aa32
    private static final String thermoPeriodUuid        = "aa33";           // 体温采样周期UUID:aa33

    public static final BleGattElement THERMODATA =
            new BleGattElement(thermoServiceUuid, thermoDataUuid, null);

    public static final BleGattElement THERMOCONTROL =
            new BleGattElement(thermoServiceUuid, thermoControlUuid, null);

    public static final BleGattElement THERMOPERIOD =
            new BleGattElement(thermoServiceUuid, thermoPeriodUuid, null);

    public static final BleGattElement THERMODATACCC =
            new BleGattElement(thermoServiceUuid, thermoDataUuid, Uuid.CCCUUID);
    ///////////////////////////////////////////////////////

    // 当前体温数据观察者列表
    private final List<IThermoDataObserver> thermoDataObserverList = new LinkedList<>();


    private double curTemp = 0.0;

    public double getCurTemp() {
        return curTemp;
    }

    public void setCurTemp(double curTemp) {
        this.curTemp = curTemp;
    }

    private double highestTemp = 0.0;

    public double getHighestTemp() { return highestTemp; }

    public void setHighestTemp(double highestTemp) {
        this.highestTemp = highestTemp;
    }

    public void resetHighestTemp() {
        highestTemp = curTemp;
    }

    public ThermoDevice(BleDeviceBasicInfo basicInfo) {
        super(basicInfo);
        initializeAfterConstruction();
    }

    private void initializeAfterConstruction() {
    }

    @Override
    public boolean executeAfterConnectSuccess() {

        // 检查是否有正常的温湿度服务和特征值
        if(!checkBasicThermoService()) return false;

        resetHighestTemp();

        // 读温度数据
        addReadCommand(THERMODATA, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "first write period: " + HexUtil.encodeHexStr(data));
                sendGattMessage(MSG_THERMODATA, data);
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });

        startThermometer((byte)0x01);
        return true;
    }

    @Override
    public void executeAfterDisconnect() {

    }

    @Override
    public void executeAfterConnectFailure() {

    }


    @Override
    public synchronized void processGattMessage(Message msg)
    {
        if (msg.what == MSG_THERMODATA) {
            if (msg.obj != null) {
                byte[] data = (byte[]) msg.obj;
                double temp = ByteUtil.getShort(data)/100.0;
                setCurTemp(temp);

                if(temp > highestTemp) {
                    setHighestTemp(temp);
                }

                notifyObserverThermoDataChanged();
            }
        }
    }

    // 检测基本温湿度服务是否正常
    private boolean checkBasicThermoService() {
        Object thermoData = getGattObject(THERMODATA);
        Object thermoControl = getGattObject(THERMOCONTROL);
        Object thermoPeriod = getGattObject(THERMOPERIOD);
        Object thermoDataCCC = getGattObject(THERMODATACCC);

        if(thermoData == null || thermoControl == null || thermoPeriod == null || thermoDataCCC == null) {
            Log.d("ThermoFragment", "Can't find the Gatt object on the device.");
            return false;
        }

        return true;
    }

    /*
    启动体温计，设置采样周期
    period: 采样周期，单位：秒
     */
    private void startThermometer(byte period) {
        // 设置采样周期
        addWriteCommand(THERMOPERIOD, period, null);

        IBleCallback notifyCallback = new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                sendGattMessage(MSG_THERMODATA, data);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };

        // enable温度数据notify
        addNotifyCommand(THERMODATACCC, true, null, notifyCallback);


        // 启动温度采集
        addWriteCommand(THERMOCONTROL, (byte)0x03, null);
    }

    // 登记体温数据观察者
    public void registerThermoDataObserver(IThermoDataObserver observer) {
        if(!thermoDataObserverList.contains(observer)) {
            thermoDataObserverList.add(observer);
        }
    }

    // 删除体温数据观察者
    public void removeThermoDataObserver(IThermoDataObserver observer) {
        int index = thermoDataObserverList.indexOf(observer);
        if(index >= 0) {
            thermoDataObserverList.remove(index);
        }
    }

    // 通知体温数据观察者
    public void notifyObserverThermoDataChanged() {
        for(final IThermoDataObserver observer : thermoDataObserverList) {
            if(observer != null) {
                observer.updateThermoData();
            }
        }
    }

}
