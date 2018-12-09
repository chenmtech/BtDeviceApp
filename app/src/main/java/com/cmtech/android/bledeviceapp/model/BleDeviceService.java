package com.cmtech.android.bledeviceapp.model;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.cmtech.android.bledevicecore.BleDeviceManager;

/**
 *  BleDeviceService: BleDevice服务
 *  Created by bme on 2018/12/09.
 */

public class BleDeviceService extends Service {
    private BleDeviceManager deviceManager;

    private DeviceServiceBinder binder = new DeviceServiceBinder();


    @Override
    public void onCreate() {
        super.onCreate();

        deviceManager = new BleDeviceManager();
    }

    public class DeviceServiceBinder extends Binder {
        public BleDeviceManager getDeviceManager() {
            return deviceManager;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
