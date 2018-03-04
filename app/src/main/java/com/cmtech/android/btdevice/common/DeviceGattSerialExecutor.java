package com.cmtech.android.btdevice.common;

import android.util.Log;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.utils.HexUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by bme on 2018/3/2.
 * Gatt数据单元串行执行器
 * 用于实现Gatt设备上的数据单元element的串行执行
 */

public class DeviceGattSerialExecutor {
    // 指定的设备镜像
    private final DeviceMirror deviceMirror;

    // 要执行的命令队列
    private final Queue<BluetoothGattCommand> commandList = new LinkedList<>();

    // 标记上一个命令是否执行完毕
    private volatile boolean done = true;

    // 执行命令的线程
    private Thread executeThread;

    // 当前在执行的命令
    private BluetoothGattCommand currentCommand;

    // IBleCallback的装饰器类，在一般的回调完成后，执行串行命令所需动作
    private class BleSerialCommandCallback implements IBleCallback {
        IBleCallback bleCallback;

        public BleSerialCommandCallback(IBleCallback bleCallback) {
            this.bleCallback = bleCallback;
        }

        @Override
        public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
            synchronized(DeviceGattSerialExecutor.this) {
                // 先做一般操作
                bleCallback.onSuccess(data, bluetoothGattChannel, bluetoothLeDevice);

                // 标记命令执行完毕
                done = true;
                // 通知执行线程执行下一条
                DeviceGattSerialExecutor.this.notifyAll();
                Log.d("SerialExecutor", "The returned data is " + HexUtil.encodeHexStr(data));
            }
        }

        @Override
        public void onFailure(BleException exception) {
            synchronized(DeviceGattSerialExecutor.this) {
                bleCallback.onFailure(exception);

                // 有错误，直接终止线程
                executeThread.interrupt();
                Log.d("SerialExecutor", "Something is wrong! The execution is interrupted!");
            }
        }
    }

    // 构造器：指定设备镜像
    public DeviceGattSerialExecutor(DeviceMirror deviceMirror) {
        this.deviceMirror = deviceMirror;
    }

    // 在设备上查找一个数据单元
    public Object findElement(BluetoothGattElement element) {
        if(deviceMirror == null || element == null) return null;
        return element.retrieve(deviceMirror);
    }

    /**
     * 将"读数据单元"操作加入串行执行器
     * @param element 数据单元
     * @param dataOpCallback 读回调
     * @return 是否添加成功
     */
    public boolean readElement(BluetoothGattElement element, IBleCallback dataOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_READ)
                .setDataOpCallback(new BleSerialCommandCallback(dataOpCallback)).build();
        if(command == null) return false;
        return addCommandToList(command);
    }

    /**
     * 将"写数据单元"操作加入串行执行器
     * @param element 数据单元
     * @param data 数据
     * @param dataOpCallback 写回调
     * @return 是否添加成功
     */
    public boolean writeElement(BluetoothGattElement element, byte[] data, IBleCallback dataOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setData(data)
                .setDataOpCallback(new BleSerialCommandCallback(dataOpCallback)).build();
        if(command == null) return false;
        return addCommandToList(command);
    }

    /**
     * 将"数据单元Notify"操作加入串行执行器
     * @param element 数据单元
     * @param enable 使能或失能
     * @param dataOpCallback 写回调
     * @param notifyOpCallback Notify数据回调
     * @return 是否添加成功
     */
    public boolean notifyElement(BluetoothGattElement element, boolean enable
                            , IBleCallback dataOpCallback, IBleCallback notifyOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_NOTIFY)
                .setData((enable) ? new byte[]{0x01} : new byte[]{0x00})
                .setDataOpCallback(new BleSerialCommandCallback(dataOpCallback))
                .setNotifyOpCallback(notifyOpCallback).build();
        if(command == null) return false;
        return addCommandToList(command);
    }

    /**
     * 将"数据单元Indicate"操作加入串行执行器
     * @param element 数据单元
     * @param enable 使能或失能
     * @param dataOpCallback 写回调
     * @param indicateOpCallback Notify数据回调
     * @return 是否添加成功
     */
    public boolean indicateElement(BluetoothGattElement element, boolean enable
            , IBleCallback dataOpCallback, IBleCallback indicateOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_INDICATE)
                .setData((enable) ? new byte[]{0x01} : new byte[]{0x00})
                .setDataOpCallback(new BleSerialCommandCallback(dataOpCallback))
                .setNotifyOpCallback(indicateOpCallback).build();
        if(command == null) return false;
        return addCommandToList(command);
    }

    private synchronized boolean addCommandToList(BluetoothGattCommand command) {
        boolean flag = commandList.offer(command);

        // 添加成功，通知执行线程
        if(flag) notifyAll();

        return flag;
    }

    public void startExecuteCommand() {

        executeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Thread", "Execution Thread: "+Thread.currentThread().getId());
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        executeNextCommand();
                    }
                }catch (InterruptedException e) {
                        Log.d("SerialExecutor", "The execution is interrupted!!!!!!");
                } finally {
                    commandList.clear();
                    executeThread = null;
                }
            }
        });

        executeThread.start();
    }

    private synchronized void executeNextCommand() throws InterruptedException{
        // 如果上一条命令还没有执行完毕，或者命令队列为空，就等待
        while(!done || commandList.isEmpty()) {
            wait();
        }
        // 清除上次执行的命令的数据操作IBleCallback，否则会出现多次异常触发.
        // 有可能是ViseBle内部问题，也有可能本身蓝牙就会这样
        if(currentCommand != null) {
            deviceMirror.removeBleCallback(currentCommand.getChannel().getGattInfoKey());
        }

        // 取出一条命令执行
        currentCommand = commandList.poll();
        currentCommand.execute();

        Log.d("SerialExecutor", "execute command: " + currentCommand.toString());

        // 设置未完成标记
        done = false;
    }

    public void stopExecuteCommand() {
        if(executeThread == null) return;
        executeThread.interrupt();
    }


}
