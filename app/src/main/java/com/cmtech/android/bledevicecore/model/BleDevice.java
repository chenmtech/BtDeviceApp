package com.cmtech.android.bledevicecore.model;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.callback.scan.IScanCallback;
import com.cmtech.android.ble.callback.scan.ScanCallback;
import com.cmtech.android.ble.callback.scan.SingleFilterScanCallback;
import com.cmtech.android.ble.common.ConnectState;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.TimeoutException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.BluetoothLeDeviceStore;
import com.vise.log.ViseLog;

import java.util.LinkedList;
import java.util.List;

/**
 * BleDevice: 低功耗蓝牙设备类
 * Created by bme on 2018/2/19.
 */

public abstract class BleDevice{
    // 设备基本信息
    private BleDeviceBasicInfo basicInfo;
    public BleDeviceBasicInfo getBasicInfo() {
        return basicInfo;
    }
    public void setBasicInfo(BleDeviceBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }
    public String getMacAddress() {
        return basicInfo.getMacAddress();
    }
    public String getNickName() {
        return basicInfo.getNickName();
    }
    public String getUuidString() {
        return basicInfo.getUuidString();
    }
    public boolean autoConnect() {
        return basicInfo.autoConnect();
    }
    public String getImagePath() {
        return basicInfo.getImagePath();
    }
    public int getReconnectTimes() { return basicInfo.getReconnectTimes(); }

    // 设备信息，当扫描到后会赋值
    private BluetoothLeDevice bluetoothLeDevice = null;
    public BluetoothLeDevice getBluetoothLeDevice() {
        return bluetoothLeDevice;
    }

    // GATT命令串行执行器
    private BleGattCommandExecutor commandExecutor;

    // 设备状态观察者列表
    private final List<IBleDeviceStateObserver> stateObserverList = new LinkedList<>();

    // 当前已重连次数
    private int curReconnectTimes = 0;

    // 是否正在关闭。当断开连接时，根据这个标志判断是否应该退回到close状态
    private boolean isClosing = false;


    // 设备连接状态
    private BleDeviceConnectState connectState = BleDeviceConnectState.STOP;
    // 设置设备连接状态
    private void setConnectState(BleDeviceConnectState connectState) {
        this.connectState = connectState;
        notifyDeviceStateObservers();
    }
    // 获取设备状态描述信息
    public synchronized String getStateDescription() {
        return connectState.getDescription();
    }
    // 设备当前状态是否可做连接操作
    public synchronized boolean isEnableConnect() {
        return connectState.isEnableConnect();
    }
    // 设备当前状态是否可做关闭操作
    public synchronized boolean isEnableClose() {
        return connectState.isEnableClose();
    }
    // 获取设备状态图标
    public synchronized int getStateIcon() {
        return connectState.getIcon();
    }



    // 扫描回调
    private final IScanCallback scanCallback = new IScanCallback() {
        @Override
        public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
            synchronized (BleDevice.this) {
                BleDevice.this.bluetoothLeDevice = bluetoothLeDevice;
                BluetoothDevice device = bluetoothLeDevice.getDevice();
                if(device.getBondState() == BluetoothDevice.BOND_NONE) {            // 还没有绑定，则启动绑定
                    device.createBond();
                } else if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    BleDevice.this.onScanFinish(true);
                }
            }
        }

        @Override
        public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {

        }

        @Override
        public void onScanTimeout() {
            synchronized (BleDevice.this) {
                BleDevice.this.onScanFinish(false);
            }
        }
    };

    // 连接回调
    private final IConnectCallback connectCallback = new IConnectCallback() {
        // 连接成功回调
        @Override
        public void onConnectSuccess(DeviceMirror mirror) {
            synchronized (BleDevice.this) {
                BleDevice.this.onConnectSuccess(mirror);
            }
        }

        // 连接失败回调
        @Override
        public void onConnectFailure(final BleException exception) {
            synchronized (BleDevice.this) {
                BleDevice.this.onConnectFailure(exception);
            }
        }

        // 连接断开回调
        @Override
        public void onDisconnect(final boolean isActive) {
            synchronized (BleDevice.this) {
                BleDevice.this.onDisconnect(isActive);
            }
        }
    };


    // 创建HandlerThread及其消息Handler
    private final Handler handler = createMessageHandler();
    private Handler createMessageHandler() {
        HandlerThread thread = new HandlerThread("BleDevice Work Thread");
        thread.start();
        return new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                processGattMessage(msg);
            }
        };
    }
    public Handler getHandler() {
        return handler;
    }

    private ScanCallback filterScanCallback = null;

    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    // 构造器
    public BleDevice(BleDeviceBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    // 打开设备
    public synchronized void open() {
        handler.removeCallbacksAndMessages(null);
        curReconnectTimes = 0;
        isClosing = false;

        if(autoConnect())
            scanOrConnect();
    }

    // 关闭设备
    public synchronized void close() {
        handler.removeCallbacksAndMessages(null);
        bluetoothLeDevice = null;
        isClosing = true;

        if(connectState == BleDeviceConnectState.SCAN_PROCESS)
            stopScan();
        else
            disconnect();
    }

    // 转换设备状态
    public synchronized void switchState() {
        handler.removeCallbacksAndMessages(null);
        switch (connectState) {
            case CONNECT_SUCCESS:
                disconnect();
                break;

            default:
                curReconnectTimes = 0;
                scanOrConnect();
                break;
        }
    }

    // 扫描或连接
    private void scanOrConnect() {
        ViseLog.e("scanOrConnect");
        handler.removeCallbacksAndMessages(null);
        if(bluetoothLeDevice == null) {         // 没有扫描成功，则启动扫描
            startScan();
        } else {        // 否则直接扫描成功，准备连接
            startConnect();
        }
    }

    // 开始扫描
    private void startScan() {
        filterScanCallback = new SingleFilterScanCallback(scanCallback).setDeviceMac(getMacAddress()).setScan(true);
        filterScanCallback.scan();
        setConnectState(BleDeviceConnectState.SCAN_PROCESS);
    }

    // 停止扫描
    private void stopScan() {
        ViseLog.i("stopScan");
        handler.removeCallbacksAndMessages(null);
        if(filterScanCallback != null) {
            filterScanCallback.setScan(false).scan();
        }
    }

    // 开始连接
    private void startConnect() {
        ViseLog.e("startConnect");
        BleDeviceUtil.connect(bluetoothLeDevice, connectCallback);
        DeviceMirror deviceMirror = BleDeviceUtil.getDeviceMirror(this);
        if(deviceMirror != null)
            setConnectState(BleDeviceConnectState.getFromCode(deviceMirror.getConnectState().getCode()));
    }

    // 断开连接
    private void disconnect() {
        ViseLog.i("disconnect");
        handler.removeCallbacksAndMessages(null);
        stopCommandExecutor();
        executeAfterDisconnect();

        if(BleDeviceUtil.getDeviceMirror(this) != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onDisconnect(true);
                }
            }, 1000);
            BleDeviceUtil.disconnect(this);
            setConnectState(BleDeviceConnectState.getFromCode(ConnectState.CONNECT_INIT.getCode()));
        } else {
            setConnectState(BleDeviceConnectState.getFromCode(ConnectState.CONNECT_DISCONNECT.getCode()));
        }
    }

    private void reconnect(final int delay) {
        int canReconnectTimes = getReconnectTimes();
        if(curReconnectTimes < canReconnectTimes || canReconnectTimes == -1) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    startConnect();
                }
            }, delay);
            if(curReconnectTimes < canReconnectTimes)
                curReconnectTimes++;
        }
    }


    // 设备是否已连接
    protected synchronized boolean isConnected() {
        DeviceMirror deviceMirror = BleDeviceUtil.getDeviceMirror(this);
        return (deviceMirror != null && deviceMirror.isConnected());
    }

    // 添加Gatt操作命令
    // 添加读取命令
    protected synchronized boolean addReadCommand(BleGattElement element, IBleDataOpCallback dataOpCallback) {
        IBleCallback callback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
        return ((commandExecutor != null) && commandExecutor.addReadCommand(element, callback));
    }

    // 添加写入多字节命令
    protected synchronized boolean addWriteCommand(BleGattElement element, byte[] data, IBleDataOpCallback dataOpCallback) {
        IBleCallback callback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
        return ((commandExecutor != null) && commandExecutor.addWriteCommand(element, data, callback));
    }

    // 添加写入单字节命令
    protected synchronized boolean addWriteCommand(BleGattElement element, byte data, IBleDataOpCallback dataOpCallback) {
        IBleCallback callback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
        return ((commandExecutor != null) && commandExecutor.addWriteCommand(element, data, callback));
    }

    // 添加Notify命令
    protected synchronized boolean addNotifyCommand(BleGattElement element, boolean enable
            , IBleDataOpCallback dataOpCallback, IBleDataOpCallback notifyOpCallback) {
        IBleCallback dataCallback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
        IBleCallback notifyCallback = (notifyOpCallback == null) ? null : new BleDataOpCallbackAdapter(notifyOpCallback);
        return ((commandExecutor != null) && commandExecutor.addNotifyCommand(element, enable, dataCallback, notifyCallback));
    }

    // 添加Indicate命令
    protected synchronized boolean addIndicateCommand(BleGattElement element, boolean enable
            , IBleDataOpCallback dataOpCallback, IBleDataOpCallback indicateOpCallback) {
        IBleCallback dataCallback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
        IBleCallback indicateCallback = (indicateOpCallback == null) ? null : new BleDataOpCallbackAdapter(indicateOpCallback);
        return ((commandExecutor != null) && commandExecutor.addIndicateCommand(element, enable, dataCallback, indicateCallback));
    }

    // 添加Instant命令
    protected synchronized boolean addInstantCommand(IBleDataOpCallback dataOpCallback) {
        IBleCallback dataCallback = (dataOpCallback == null) ? null : new BleDataOpCallbackAdapter(dataOpCallback);
        return ((commandExecutor != null) && commandExecutor.addInstantCommand(dataCallback));
    }

    // 登记设备状态观察者
    public void registerDeviceStateObserver(IBleDeviceStateObserver observer) {
        if(!stateObserverList.contains(observer)) {
            stateObserverList.add(observer);
        }
    }

    // 删除设备状态观察者
    public void removeDeviceStateObserver(IBleDeviceStateObserver observer) {
        int index = stateObserverList.indexOf(observer);
        if(index >= 0) {
            stateObserverList.remove(index);
        }
    }

    // 通知设备状态观察者
    public void notifyDeviceStateObservers() {
        for(final IBleDeviceStateObserver observer : stateObserverList) {
            if(observer != null) {
                observer.updateDeviceState(BleDevice.this);
            }
        }
    }

    /*
     * 子类需要提供的抽象方法
     */
    // 连接成功后执行的操作
    public abstract boolean executeAfterConnectSuccess();
    // 连接错误后执行的操作
    public abstract void executeAfterConnectFailure();
    // 断开连接后执行的操作
    public abstract void executeAfterDisconnect();
    // 处理Gatt命令回调消息函数
    public abstract void processGattMessage(Message msg);


    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    // 创建Gatt命令执行器，并启动执行器
    private boolean createGattCommandExecutor() {
        ViseLog.i("create new command executor.");
        if(isCommandExecutorAlive()) return false;
        DeviceMirror deviceMirror = BleDeviceUtil.getDeviceMirror(this);
        if(deviceMirror == null) return false;

        commandExecutor = new BleGattCommandExecutor(deviceMirror);
        commandExecutor.start();
        return true;
    }

    // 停止Gatt命令执行器
    private void stopCommandExecutor() {
        if(isCommandExecutorAlive()) commandExecutor.stop();
    }

    // Gatt命令执行器是否Alive
    private boolean isCommandExecutorAlive() {
        return ((commandExecutor != null) && commandExecutor.isAlive());
    }

    // 获取设备上element对应的Gatt Object
    protected Object getGattObject(BleGattElement element) {
        return (element == null) ? null : element.retrieveGattObject(this);
    }

    // 发送Gatt命令执行后回调处理消息
    protected void sendGattMessage(int what, Object obj) {
        Message msg = Message.obtain(handler, what, obj);
        msg.sendToTarget();
    }


    // 扫描结束回调
    private void onScanFinish(boolean result) {
        if(result) {
            startConnect();
        } else {
            handler.removeCallbacksAndMessages(null);
            setConnectState(BleDeviceConnectState.STOP);
        }
    }

    // 连接成功回调
    private void onConnectSuccess(DeviceMirror mirror) {
        ViseLog.i("onConnectSuccess");
        handler.removeCallbacksAndMessages(null);
        curReconnectTimes = 0;
        setConnectState(BleDeviceConnectState.CONNECT_SUCCESS);

        // 创建Gatt串行命令执行器
        if(!createGattCommandExecutor() || !executeAfterConnectSuccess()) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            }, 500);
        }
    }

    // 连接错误回调
    private void onConnectFailure(final BleException bleException) {
        ViseLog.i("onConnectFailure");
        handler.removeCallbacksAndMessages(null);
        stopCommandExecutor();
        executeAfterConnectFailure();

        if (bleException instanceof TimeoutException) {
            setConnectState(BleDeviceConnectState.getFromCode(ConnectState.CONNECT_TIMEOUT.getCode()));
        } else {
            setConnectState(BleDeviceConnectState.getFromCode(ConnectState.CONNECT_FAILURE.getCode()));
        }
    }

    // 连接断开回调
    private void onDisconnect(boolean isActive) {
        ViseLog.i("onDisconnect");
        handler.removeCallbacksAndMessages(null);
        setConnectState(BleDeviceConnectState.getFromCode(ConnectState.CONNECT_DISCONNECT.getCode()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleDevice that = (BleDevice) o;
        String thisAddress = getMacAddress();
        String thatAddress = that.getMacAddress();

        return thisAddress != null ? thisAddress.equals(thatAddress) : thatAddress == null;
    }

    @Override
    public int hashCode() {
        return getMacAddress() != null ? getMacAddress().hashCode() : 0;
    }

}
