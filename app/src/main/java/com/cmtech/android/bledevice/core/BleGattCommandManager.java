package com.cmtech.android.bledevice.core;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.utils.HexUtil;
import com.vise.log.ViseLog;

import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * BleGattCommandManager: Ble Gatt命令执行器
 * Created by bme on 2018/3/2.
 *
 */

/**
  *
  * ClassName:      BleGattCommandManager
  * Description:    Ble Gatt命令管理器
  * Author:         chenm
  * CreateDate:     2018-03-02 11:16
  * UpdateUser:     chenm
  * UpdateDate:     2019-05-02 11:16
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class BleGattCommandManager {
    private final static int CMD_ERROR_RETRY_TIMES = 3;      // Gatt命令执行错误可重复的次数

    // 需要同步
    private final DeviceMirror deviceMirror; // 执行命令的设备镜像

    // 需要同步
    private final BlockingQueue<BleGattCommand> commandList = new LinkedBlockingQueue<>(); // 要执行的命令队列

    // 需要同步
    private BleGattCommand currentCommand; // 当前在执行的命令

    // 需要同步
    private boolean currentCommandDone = true; // 标记当前命令是否执行完毕

    // 需要同步
    private int cmdErrorTimes = 0; // 命令执行错误的次数

    private Thread executeThread; // 执行命令的线程

    // IBleCallback的装饰类，在一般的回调任务完成后，执行串行命令所需动作
    private class BleSerialCommandCallback implements IBleCallback {
        private IBleCallback bleCallback;

        BleSerialCommandCallback(IBleCallback bleCallback) {
            this.bleCallback = bleCallback;
        }

        @Override
        public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
            processCommandSuccessCallback(bleCallback, data, bluetoothGattChannel, bluetoothLeDevice);

        }

        @Override
        public void onFailure(BleException exception) {
            processCommandFailureCallback(bleCallback, exception);
        }
    }

    // 构造器：指定设备镜像
    BleGattCommandManager(DeviceMirror deviceMirror) {
        this.deviceMirror = deviceMirror;
    }

    private synchronized void processCommandSuccessCallback(IBleCallback bleCallback, byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
        // 清除当前命令的数据操作IBleCallback，否则会出现多次执行该回调.
        // 有可能是ViseBle内部问题，也有可能本身蓝牙就会这样
        if(currentCommand != null && deviceMirror != null) {
            deviceMirror.removeBleCallback(currentCommand.getGattInfoKey());
        }

        if(bleCallback != null)
            bleCallback.onSuccess(data, bluetoothGattChannel, bluetoothLeDevice);

        currentCommandDone = true;

        cmdErrorTimes = 0;

        ViseLog.i("Command return " + HexUtil.encodeHexStr(data));
    }

    private synchronized void processCommandFailureCallback(IBleCallback bleCallback, BleException exception) {
        // 清除当前命令的数据操作IBleCallback，否则会出现多次执行该回调.
        // 有可能是ViseBle内部问题，也有可能本身蓝牙就会这样
        if(currentCommand != null && deviceMirror != null) {
            deviceMirror.removeBleCallback(currentCommand.getGattInfoKey());
        }

        // 有错误，且次数小于指定次数，重新执行当前命令
        if(cmdErrorTimes < CMD_ERROR_RETRY_TIMES && currentCommand != null) {
            // 再次执行当前命令
            currentCommand.execute();

            cmdErrorTimes++;
            ViseLog.i("Command Retry: " + cmdErrorTimes);
        } else {
            // 错误次数大于指定次数
            cmdErrorTimes = 0;

            stop();

            if(deviceMirror != null) deviceMirror.disconnect();

            if(bleCallback != null)
                bleCallback.onFailure(exception);
        }
        ViseLog.i(currentCommand + " is wrong: " + exception);
    }

    synchronized void addReadCommand(BleGattElement element, IBleCallback dataOpCallback) throws InterruptedException{
        BleGattCommand.Builder builder = new BleGattCommand.Builder();
        BleGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_READ)
                .setDataOpCallback(new BleSerialCommandCallback(dataOpCallback)).build();
        if(command != null)
            addCommandToList(command);
    }

    synchronized void addWriteCommand(BleGattElement element, byte[] data, IBleCallback dataOpCallback) throws InterruptedException{
        BleGattCommand.Builder builder = new BleGattCommand.Builder();
        BleGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setData(data)
                .setDataOpCallback(new BleSerialCommandCallback(dataOpCallback)).build();
        if(command != null)
            addCommandToList(command);
    }

    // 写单字节数据
    synchronized void addWriteCommand(BleGattElement element, byte data, IBleCallback dataOpCallback) throws InterruptedException{
        addWriteCommand(element, new byte[]{data}, dataOpCallback);
    }

    synchronized void addNotifyCommand(BleGattElement element, boolean enable
            , IBleCallback dataOpCallback, IBleCallback notifyOpCallback) throws InterruptedException{
        BleGattCommand.Builder builder = new BleGattCommand.Builder();
        BleGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_NOTIFY)
                .setData((enable) ? new byte[]{0x01} : new byte[]{0x00})
                .setDataOpCallback(new BleSerialCommandCallback(dataOpCallback))
                .setNotifyOpCallback(notifyOpCallback).build();
        if(command != null)
            addCommandToList(command);
    }

    synchronized void addIndicateCommand(BleGattElement element, boolean enable
            , IBleCallback dataOpCallback, IBleCallback indicateOpCallback) throws InterruptedException{
        BleGattCommand.Builder builder = new BleGattCommand.Builder();
        BleGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_INDICATE)
                .setData((enable) ? new byte[]{0x01} : new byte[]{0x00})
                .setDataOpCallback(new BleSerialCommandCallback(dataOpCallback))
                .setNotifyOpCallback(indicateOpCallback).build();
        if(command != null)
            addCommandToList(command);
    }

    // 添加Instant命令
    synchronized void addInstantCommand(IBleCallback dataOpCallback) throws InterruptedException{
        BleGattCommand command = new BleGattCommand.Builder().setDataOpCallback(dataOpCallback).setInstantCommand(true).build();
        if(command != null)
            addCommandToList(command);
    }

    private void addCommandToList(BleGattCommand command) throws InterruptedException{
        commandList.put(command);
    }

    // 开始执行命令
    public void start() {
        if(executeThread != null && executeThread.isAlive()) return;

        executeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ViseLog.i("Start Command Execution Thread: "+Thread.currentThread().getName());
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        executeNextCommand();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                } finally {
                    resetCommandList();

                    ViseLog.e("executeThread finished!!!!!!");
                }
            }
        });

        executeThread.start();
    }

    private synchronized void executeNextCommand() throws InterruptedException{
        /*// 如果当前命令还没有执行完毕，或者命令队列为空，就等待
        while(!currentCommandDone || commandList.isEmpty()) {
            wait();
        }*/

        if(currentCommandDone) {
            // 取出一条命令执行
            currentCommand = commandList.take();

            if(currentCommand != null) {
                currentCommand.execute();

                // 设置未完成标记
                if (!currentCommand.isInstantCommand())
                    currentCommandDone = false;
            }
        }
    }

    private synchronized void resetCommandList() {
        commandList.clear();
        currentCommand = null;
        currentCommandDone = false;
        cmdErrorTimes = 0;
    }

    // 停止执行命令
    public void stop() {
        if(isAlive()) {
            executeThread.interrupt();
        }
    }

    // 命令执行器是否存活
    public boolean isAlive() {
        return ((executeThread != null) && executeThread.isAlive());
    }
}
