package com.cmtech.android.btdevice.common;

import android.util.Log;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.utils.HexUtil;
import com.cmtech.android.btdevice.thermo.ThermoManager;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.cmtech.android.btdevice.thermo.ThermoManager.THERMOPERIOD;

/**
 * Created by bme on 2018/3/2.
 */

public class DeviceManager {
    private final DeviceMirror deviceMirror;
    private Queue<BluetoothGattCommand> commandList = new LinkedList<>();
    private volatile boolean done = true;
    private Thread executeThread;

    public DeviceManager(DeviceMirror deviceMirror) {
        this.deviceMirror = deviceMirror;
    }

    public DeviceMirror getDeviceMirror() {return deviceMirror;}

    public Object findElement(BluetoothGattElement element) {
        if(deviceMirror == null || element == null) return null;
        return element.retrieve(deviceMirror);
    }

    public synchronized boolean readElement(BluetoothGattElement element, IBleCallback dataOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_READ)
                .setDataOpCallback(dataOpCallback).build();
        if(command == null) return false;
        return addCommandToList(command);
    }

    public boolean writeElement(BluetoothGattElement element, byte[] data, IBleCallback dataOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setData(data)
                .setDataOpCallback(dataOpCallback).build();
        if(command == null) return false;
        return addCommandToList(command);
    }

    private synchronized boolean addCommandToList(BluetoothGattCommand command) {
        boolean flag = commandList.offer(command);
        if(!flag) DeviceManager.this.notifyAll();
        return flag;
    }

    public synchronized void startExecuteCommand() {
        Log.d("DeviceManager", commandList.size()+"");

        if(executeThread != null) return;

        executeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        executeNextCommand();
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
            }
        });
        executeThread.start();
    }

    public synchronized void stopExecuteCommand() {
        if(executeThread == null) return;
        executeThread.interrupt();
        executeThread = null;
        commandList.clear();
    }

    private synchronized void executeNextCommand() throws InterruptedException{
        while(!done || commandList.isEmpty()) {
            wait();
        }

        commandList.poll().execute(this);
        done = false;

        Log.d("DeviceManager", "execute one command");
    }

    public class BleSerialCommandCallback implements IBleCallback {
        IBleCallback bleCallback;

        public BleSerialCommandCallback(IBleCallback bleCallback) {
            this.bleCallback = bleCallback;
        }

        @Override
        public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
            synchronized(DeviceManager.this) {
                bleCallback.onSuccess(data, bluetoothGattChannel, bluetoothLeDevice);
                done = true;
                DeviceManager.this.notifyAll();
            }
        }

        @Override
        public synchronized void onFailure(BleException exception) {
            synchronized(DeviceManager.this) {
                bleCallback.onFailure(exception);
                executeThread.interrupt();
            }
        }
    }

}
