package com.cmtech.android.btdeviceapp.model;

import android.bluetooth.BluetoothGattCharacteristic;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.core.DeviceMirror;
import com.vise.log.ViseLog;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static com.cmtech.android.btdeviceapp.model.DeviceConnectState.CONNECT_SUCCESS;

/**
 * Created by bme on 2018/3/2.
 * Gatt命令的串行执行器
 */

public class GattCommandSerialExecutor extends Thread{
    private DeviceMirror deviceMirror;

    // 要执行的GATT命令队列
    private final Queue<BluetoothGattCommand> commandList = new LinkedList<>();

    // 当前在执行的GATT命令
    private BluetoothGattCommand currentCommand = null;

    // 标记当前命令是否执行完毕
    private boolean isCurrentCmdDone = true;

    // 构造器
    public GattCommandSerialExecutor(DeviceMirror deviceMirror) {
        this.deviceMirror = deviceMirror;
    }

    // 通知当前命令已经执行完毕：成功或失败
    public synchronized void notifyCurrentCommandExecuted(boolean isSuccess)
    {
        if(isSuccess) {
            this.isCurrentCmdDone = true;
            notifyAll();
        } else {
            interrupt();
        }
    }

    // 当前命令中的channel是否与指定的channel相同：比较其UUID和PropertyType
    public synchronized boolean isChannelSameAsCurrentCommand(BluetoothGattChannel channel) {
        if(currentCommand == null) return false;
        String uuid = channel.getCharacteristic().getUuid().toString();
        String curUuid = currentCommand.getChannel().getCharacteristicUUID().toString();
        return ( uuid.equalsIgnoreCase(curUuid) && (channel.getPropertyType() == currentCommand.getChannel().getPropertyType()));
    }

    // 添加一条GATT命令
    public synchronized boolean addOneGattCommand(BluetoothGattCommand command) {
        boolean flag = commandList.offer(command);

        // 添加成功，通知执行线程
        if(flag) notifyAll();

        return flag;
    }

    public synchronized void reExecuteCurrentCommand() {
        if(currentCommand != null) currentCommand.execute();
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
            //currentCommand = null;
            //isCurrentCmdDone = true;
        }
    }

    // 执行下一条命令
    private synchronized void executeNextCommand() throws InterruptedException{
        // 如果上一条命令还没有执行完毕，或者命令队列为空，就等待
        while(!isCurrentCmdDone || commandList.isEmpty()) {
            wait();
        }
        // 清除上次执行命令的数据操作IBleCallback，否则会出现多次执行该回调.
        // 有可能是ViseBle内部问题，也有可能本身蓝牙就会这样
        if(currentCommand != null) {
            if(deviceMirror != null) deviceMirror.removeBleCallback(currentCommand.getGattInfoKey());
        }

        // 取出一条命令执行
        currentCommand = commandList.poll();
        currentCommand.execute();

        ViseLog.i("Do Command: " + currentCommand.toString() + " , " + Arrays.toString(currentCommand.getChannel().getCharacteristic().getValue()));

        // 设置未完成标记
        isCurrentCmdDone = false;
    }

    /**
     * 执行"写数据单元"操作
     * @param element 数据单元
     * @param data 数据
     * @param dataOpCallback 写回调
     * @return 是否添加成功
     */
    protected synchronized boolean addWriteCommand(BluetoothGattElement element, byte[] data, IBleCallback dataOpCallback) {
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setData(data)
                .setDataOpCallback(dataOpCallback).build();
        if(command == null) return false;
        return addOneGattCommand(command);
    }
}
