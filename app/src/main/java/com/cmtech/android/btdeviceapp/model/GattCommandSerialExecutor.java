package com.cmtech.android.btdeviceapp.model;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.utils.HexUtil;
import com.vise.log.ViseLog;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by bme on 2018/3/2.
 * Gatt数据操作命令的串行执行器
 */

public class GattCommandSerialExecutor extends Thread{
    // 要执行的GATT命令队列
    private final Queue<BluetoothGattCommand> commandList = new LinkedList<>();

    // 当前在执行的GATT命令
    private BluetoothGattCommand currentCommand = null;

    // 标记上一条命令是否执行完毕
    private boolean isCurrentCmdDone = true;

    // 构造器
    public GattCommandSerialExecutor() {
    }

    // 通知当前命令已经执行完毕
    public synchronized void notifyCurrentCommandExecuted(boolean isSuccess)
    {
        if(isSuccess) {
            this.isCurrentCmdDone = true;
            notifyAll();
        } else {
            interrupt();
        }
    }

    public boolean isCharacteristicSameAsCurrentCommand(BluetoothGattCharacteristic characteristic) {
        if(currentCommand == null) return false;
        String uuid = characteristic.getUuid().toString();
        String curUuid = currentCommand.getChannel().getCharacteristicUUID().toString();
        return uuid.equalsIgnoreCase(curUuid);
    }

    // 添加一条GATT命令
    public synchronized boolean addOneGattCommand(BluetoothGattCommand command) {
        boolean flag = commandList.offer(command);

        // 添加成功，通知执行线程
        if(flag) notifyAll();

        return flag;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                executeNextCommand();
            }
        }catch (InterruptedException e) {
            ViseLog.i("The Gatt Command serial executor is interrupted!!!!!!" + getName());
        } finally {
            commandList.clear();
        }
    }

    private synchronized void executeNextCommand() throws InterruptedException{
        // 如果上一条命令还没有执行完毕，或者命令队列为空，就等待
        while(!isCurrentCmdDone || commandList.isEmpty()) {
            wait();
        }
        // 清除上次执行命令的数据操作IBleCallback，否则会出现多次执行该回调.
        // 有可能是ViseBle内部问题，也有可能本身蓝牙就会这样
        //if(currentCommand != null) {
        //    if(deviceMirror != null) deviceMirror.removeBleCallback(currentCommand.getGattInfoKey());
        //}

        // 取出一条命令执行
        currentCommand = commandList.poll();
        if(currentCommand != null) currentCommand.execute();

        ViseLog.i("Now the executed command is : " + currentCommand.toString());

        // 设置未完成标记
        isCurrentCmdDone = false;
    }
}
