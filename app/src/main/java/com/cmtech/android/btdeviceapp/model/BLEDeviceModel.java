package com.cmtech.android.btdeviceapp.model;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.common.PropertyType;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.DeviceMirrorPool;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.TimeoutException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceConnectStateObserver;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceModelInterface;
import com.vise.log.ViseLog;


import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.btdeviceapp.model.DeviceConnectState.*;

/**
 * Created by bme on 2018/2/19.
 */

public abstract class BLEDeviceModel implements IBLEDeviceModelInterface{

    private static final int MSG_CONNECTCALLBACK       =  0;         // 连接相关回调消息
    private static final int MSG_NORMALGATTCALLBACK = 1;             // Gatt相关回调消息

    // 设备基本信息
    private final BLEDeviceBasicInfo basicInfo;

    // 设备镜像，连接成功后才会赋值。连接断开后，其Gatt将close
    protected DeviceMirror deviceMirror = null;

    // 设备连接状态
    private DeviceConnectState state = DeviceConnectState.CONNECT_WAITING;

    // GATT命令串行执行器
    protected GattCommandSerialExecutor commandExecutor;

    // 连接状态观察者列表
    private final List<IBLEDeviceConnectStateObserver> connectStateObserverList = new LinkedList<>();

    // 构造器
    public BLEDeviceModel(BLEDeviceBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    @Override
    public String getMacAddress() {
        return basicInfo.getMacAddress();
    }

    @Override
    public void setMacAddress(String macAddress) {
        basicInfo.setMacAddress(macAddress);
    }

    @Override
    public String getNickName() {
        return basicInfo.getNickName();
    }

    @Override
    public void setNickName(String nickName) {
        basicInfo.setNickName(nickName);
    }

    @Override
    public String getUuidString() {
        return basicInfo.getUuidString();
    }

    @Override
    public void setUuidString(String uuidString) {
        basicInfo.setUuidString(uuidString);
    }

    @Override
    public boolean isAutoConnected() {
        return basicInfo.isAutoConnected();
    }

    @Override
    public void setAutoConnected(boolean autoConnected) {
        basicInfo.setAutoConnected(autoConnected);
    }

    @Override
    public String getImagePath() {
        return basicInfo.getImagePath();
    }

    @Override
    public void setImagePath(String imagePath) {
        basicInfo.setImagePath(imagePath);
    }

    @Override
    public BLEDeviceBasicInfo getBasicInfo() {
        return basicInfo;
    }

    @Override
    public DeviceConnectState getDeviceConnectState() {
        return state;
    }

    @Override
    public void setDeviceConnectState(DeviceConnectState state) {
        this.state = state;
    }

    public boolean isCommandExecutorAlive() {
        if((commandExecutor != null) && commandExecutor.isAlive())
            return true;
        else {
            return false;
        }
    }

    public void createCommandExecutor() {
        ViseLog.i("create new command executor.");
        if(isCommandExecutorAlive()) return;

        commandExecutor = new GattCommandSerialExecutor(deviceMirror);
        commandExecutor.start();
    }

    // 连接结果类
    static class ConnectResultObject {
        DeviceConnectState state;
        Object obj;

        ConnectResultObject(DeviceConnectState state, Object obj) {
            this.state = state;
            this.obj = obj;
        }
    }

    // 连接回调
    final IConnectCallback connectCallback = new IConnectCallback() {
        @Override
        public void onConnectSuccess(DeviceMirror deviceMirror) {
            Log.d("CONNECTCALLBACK", "onConnectSuccess");
            Message msg = new Message();
            msg.what = MSG_CONNECTCALLBACK;
            msg.obj = new ConnectResultObject(DeviceConnectState.CONNECT_SUCCESS, deviceMirror);
            handler.removeMessages(MSG_CONNECTCALLBACK);
            handler.sendMessage(msg);
        }

        @Override
        public void onConnectFailure(BleException exception) {
            DeviceConnectState state;
            if(exception instanceof TimeoutException)
                state = CONNECT_SCANTIMEOUT;
            else
                state = CONNECT_FAILURE;

            Log.d("CONNECTCALLBACK", "onConnectFailure with state = " + state);
            Message msg = new Message();
            msg.what = MSG_CONNECTCALLBACK;
            msg.obj = new ConnectResultObject(state, exception);
            handler.removeMessages(MSG_CONNECTCALLBACK);
            handler.sendMessage(msg);
        }

        @Override
        public void onDisconnect(boolean isActive) {
            Log.d("CONNECTCALLBACK", "onDisconnect");
            Message msg = new Message();
            msg.what = MSG_CONNECTCALLBACK;
            msg.obj = new ConnectResultObject(DeviceConnectState.CONNECT_DISCONNECT, isActive);
            handler.removeMessages(MSG_CONNECTCALLBACK);
            handler.sendMessage(msg);
        }
    };

    // 一般的Gatt回调，会产生一般的Gatt消息
    final protected IBleCallback commonGattCallback = new IBleCallback() {
        @Override
        public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
            //ViseLog.i("onSuccess : characteristic = " + bluetoothGattChannel.getCharacteristic().getUuid() +
            //        ", value = " + Arrays.toString(bluetoothGattChannel.getCharacteristic().getValue()));

            Message msg = new Message();
            msg.what = MSG_NORMALGATTCALLBACK;
            msg.obj = bluetoothGattChannel;
            handler.sendMessage(msg);
        }

        @Override
        public void onFailure(BleException exception) {
            ViseLog.i("onFailure" );
            commandExecutor.reExecuteCurrentCommand();
        }
    };

    // 消息分发：用来处理连接和通信回调后产生的消息，由于有些要修改GUI，所以使用主线程
    final protected Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 连接消息
                case MSG_CONNECTCALLBACK:
                    processConnectMessage((ConnectResultObject)msg.obj);
                    break;

                // 一般Gatt消息
                case MSG_NORMALGATTCALLBACK:
                    processCommonGattMessage((BluetoothGattChannel) msg.obj);
                    break;

                // 主要用来处理Notify和Indicate之类的消息
                default:
                    processSpecialGattMessage(msg);
                    break;

            }

        }
    };

    // 连接结果处理函数
    private void processConnectMessage(ConnectResultObject result) {
        //if(state == result.state) return;   // 有时候会有连续多次回调，忽略后面的回调处理

        setDeviceConnectState(result.state);
        notifyConnectStateObservers();

        switch (result.state) {
            case CONNECT_SUCCESS:
                onConnectSuccess((DeviceMirror)result.obj);
                break;
            case CONNECT_SCANTIMEOUT:
            case CONNECT_FAILURE:
                onConnectFailure((BleException)result.obj);
                break;
            case CONNECT_DISCONNECT:
                onDisconnect((Boolean) result.obj);
                break;

            default:
                break;
        }
    }

    // 连接成功处理
    private void onConnectSuccess(DeviceMirror mirror) {


        DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();

        if (deviceMirrorPool.isContainDevice(mirror)) {
            this.deviceMirror = mirror;
            executeAfterConnectSuccess();
            if(handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
    }

    // 连接失败处理
    private void onConnectFailure(BleException exception) {
        clearDeviceMirror();

        executeAfterConnectFailure();

        if(handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    // 断开连接处理
    private void onDisconnect(Boolean isActive) {
        clearDeviceMirror();

        executeAfterDisconnect(isActive);

        if(handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void stopCommandExecutor() {
        if(isCommandExecutorAlive()) {
            commandExecutor.interrupt();
            try {
                commandExecutor.join();
                commandExecutor = null;
            } catch (InterruptedException ex) {
                ViseLog.i("The Gatt Command serial executor is interrupted!!!!!!" + commandExecutor.getName());
            }
        }
    }

    // 发起连接
    @Override
    public synchronized void connect() {
        if(state == CONNECT_SUCCESS || state == CONNECT_CONNECTING || state == CONNECT_DISCONNECTING) return;

        setDeviceConnectState(CONNECT_CONNECTING);
        notifyConnectStateObservers();

        MyApplication.getViseBle().connectByMac(getMacAddress(), connectCallback);
    }

    // 断开连接
    @Override
    public synchronized void disconnect() {
        if(state == CONNECT_SUCCESS) {
            stopCommandExecutor();

            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }

            setDeviceConnectState(CONNECT_DISCONNECTING);
            notifyConnectStateObservers();

            if (deviceMirror != null) {
                MyApplication.getViseBle().disconnect(deviceMirror.getBluetoothLeDevice());
                deviceMirror.removeAllCallback();
                MyApplication.getViseBle().getDeviceMirrorPool().removeDeviceMirror(deviceMirror);
            } else {
                setDeviceConnectState(CONNECT_DISCONNECT);
                notifyConnectStateObservers();
            }
        }
    }

    // 关闭设备
    @Override
    public synchronized void close() {
        stopCommandExecutor();

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        // 断开连接
        clearDeviceMirror();

        state = CONNECT_WAITING;

        notifyConnectStateObservers();

        // 清空连接状态观察者列表
        connectStateObserverList.clear();
    }

    private void clearDeviceMirror() {
        if (deviceMirror != null) {
            MyApplication.getViseBle().disconnect(deviceMirror.getBluetoothLeDevice());
            MyApplication.getViseBle().getDeviceMirrorPool().removeDeviceMirror(deviceMirror);
            //deviceMirror.clear();     // 不能clear，否则下次连接出错
        }
    }

    // 获取设备的服务列表
    @Override
    public List<BluetoothGattService> getServices() {
        if(deviceMirror != null && deviceMirror.getBluetoothGatt() != null) {
            return deviceMirror.getBluetoothGatt().getServices();
        }
        return null;
    }

    // 获取设备上element对应的Gatt Object
    protected Object getGattObject(BluetoothGattElement element) {
        if(deviceMirror == null || element == null) return null;
        return element.retrieveGattObject(deviceMirror);
    }

    /**
     * 执行"读数据单元"操作
     * @param element 数据单元
     * @param dataOpCallback 读回调
     * @return 是否添加成功
     */
    protected synchronized boolean addReadCommand(BluetoothGattElement element, IBleCallback dataOpCallback) {
        if(!isCommandExecutorAlive() || state != CONNECT_SUCCESS) return false;
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_READ)
                .setDataOpCallback(dataOpCallback).build();
        if(command == null) return false;
        return commandExecutor.addOneGattCommand(command);
    }

    /**
     * 执行"写数据单元"操作
     * @param element 数据单元
     * @param data 数据
     * @param dataOpCallback 写回调
     * @return 是否添加成功
     */
    protected synchronized boolean addWriteCommand(BluetoothGattElement element, byte[] data, IBleCallback dataOpCallback) {
        if(!isCommandExecutorAlive() || state != CONNECT_SUCCESS) return false;
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setData(data)
                .setDataOpCallback(dataOpCallback).build();
        if(command == null) return false;
        return commandExecutor.addOneGattCommand(command);
    }

    /**
     * 执行"数据单元Notify"操作
     * @param element 数据单元
     * @param enable 使能或失能
     * @param dataOpCallback 写回调
     * @param notifyOpCallback Notify数据回调
     * @return 是否添加成功
     */
    protected synchronized boolean addNotifyCommand(BluetoothGattElement element, boolean enable
            , IBleCallback dataOpCallback, IBleCallback notifyOpCallback) {
        if(!isCommandExecutorAlive() || state != CONNECT_SUCCESS) return false;
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_NOTIFY)
                .setData((enable) ? new byte[]{0x01} : new byte[]{0x00})
                .setDataOpCallback(dataOpCallback)
                .setNotifyOpCallback(notifyOpCallback).build();
        if(command == null) return false;
        return commandExecutor.addOneGattCommand(command);
    }

    /**
     * 执行"数据单元Indicate"操作
     * @param element 数据单元
     * @param enable 使能或失能
     * @param dataOpCallback 写回调
     * @param indicateOpCallback Notify数据回调
     * @return 是否添加成功
     */
    protected synchronized boolean addIndicateCommand(BluetoothGattElement element, boolean enable
            , IBleCallback dataOpCallback, IBleCallback indicateOpCallback) {
        if(!isCommandExecutorAlive() || state != CONNECT_SUCCESS) return false;
        BluetoothGattCommand.Builder builder = new BluetoothGattCommand.Builder();
        BluetoothGattCommand command = builder.setDeviceMirror(deviceMirror)
                .setBluetoothElement(element)
                .setPropertyType(PropertyType.PROPERTY_INDICATE)
                .setData((enable) ? new byte[]{0x01} : new byte[]{0x00})
                .setDataOpCallback(dataOpCallback)
                .setNotifyOpCallback(indicateOpCallback).build();
        if(command == null) return false;
        return commandExecutor.addOneGattCommand(command);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BLEDeviceModel that = (BLEDeviceModel) o;
        String thisAddress = getMacAddress();
        String thatAddress = that.getMacAddress();

        return thisAddress != null ? thisAddress.equals(thatAddress) : thatAddress == null;
    }

    @Override
    public int hashCode() {
        return getMacAddress() != null ? getMacAddress().hashCode() : 0;
    }



    // 登记连接状态观察者
    @Override
    public void registerConnectStateObserver(IBLEDeviceConnectStateObserver observer) {
        if(!connectStateObserverList.contains(observer)) {
            connectStateObserverList.add(observer);
        }
    }

    // 删除连接状态观察者
    @Override
    public void removeConnectStateObserver(IBLEDeviceConnectStateObserver observer) {
        int index = connectStateObserverList.indexOf(observer);
        if(index >= 0) {
            connectStateObserverList.remove(index);
        }
    }

    // 通知连接状态观察者
    @Override
    public void notifyConnectStateObservers() {
        for(final IBLEDeviceConnectStateObserver observer : connectStateObserverList) {
            if(observer != null) {
                observer.updateConnectState(this);
            }
        }
    }

}
