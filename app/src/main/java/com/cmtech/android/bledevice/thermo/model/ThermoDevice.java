package com.cmtech.android.bledevice.thermo.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cmtech.android.bledevice.temphumid.model.TempHumidGattOperator;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledevice.core.BleDataOpException;
import com.cmtech.android.bledevice.core.BleDevice;
import com.cmtech.android.bledevice.core.BleDeviceBasicInfo;
import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledevice.core.BleGattElement;
import com.cmtech.android.bledevice.core.IBleDataOpCallback;

import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.bledevice.core.BleDeviceConstant.CCCUUID;
import static com.cmtech.android.bledevice.core.BleDeviceConstant.MY_BASE_UUID;

public class ThermoDevice extends BleDevice {
    public static final int MSG_THERMODATA = 1;



    private static final byte DEFAULT_SAMPLE_PERIOD = (byte)0x01;
    ///////////////////////////////////////////////////////

    // 当前体温数据观察者列表
    private final List<IThermoDataObserver> thermoDataObserverList = new LinkedList<>();

    private final ThermoGattOperator gattOperator; // Gatt操作者


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
        updateThermoData();
    }

    public ThermoDevice(BleDeviceBasicInfo basicInfo, ThermoGattOperator gattOperator) {
        super(basicInfo, gattOperator);
        initializeAfterConstruction();
        this.gattOperator = gattOperator;
    }

    private void initializeAfterConstruction() {
    }

    @Override
    public boolean executeAfterConnectSuccess() {
        gattOperator.start();

        // 检查是否有正常的温湿度服务和特征值
        if(!gattOperator.checkBasicService()) return false;

        resetHighestTemp();

        // 读温度数据
        gattOperator.readThermoData();

        gattOperator.startThermometer(DEFAULT_SAMPLE_PERIOD);
        return true;
    }

    @Override
    public void executeAfterDisconnect() {
        gattOperator.stop();
    }

    @Override
    public void executeAfterConnectFailure() {
        gattOperator.stop();
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

                updateThermoData();
            }
        }
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
    private void updateThermoData() {
        for(final IThermoDataObserver observer : thermoDataObserverList) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(observer != null)
                        observer.updateThermoData();
                }
            });
        }
    }

}
