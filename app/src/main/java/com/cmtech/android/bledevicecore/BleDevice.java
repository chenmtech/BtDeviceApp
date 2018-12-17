package com.cmtech.android.bledevicecore;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.callback.scan.IScanCallback;
import com.cmtech.android.ble.callback.scan.ScanCallback;
import com.cmtech.android.ble.callback.scan.SingleFilterScanCallback;
import com.cmtech.android.ble.common.ConnectState;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.IDeviceMirrorStateObserver;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.TimeoutException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.BluetoothLeDeviceStore;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.vise.log.ViseLog;

import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.bledevicecore.BleDeviceConstant.RECONNECT_INTERVAL;

/**
 * BleDevice: 低功耗蓝牙设备类
 * Created by bme on 2018/2/19.
 */

public abstract class BleDevice implements IDeviceMirrorStateObserver {
    // 设备基本信息对象
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
    private boolean autoConnect() {
        return basicInfo.autoConnect();
    }
    public String getImagePath() {
        return basicInfo.getImagePath();
    }
    private int getReconnectTimes() { return basicInfo.getReconnectTimes(); }


    // 设备信息，当扫描到后会赋值，并一直保留，直到程序关闭
    private BluetoothLeDevice bluetoothLeDevice = null;
    public BluetoothLeDevice getBluetoothLeDevice() {
        return bluetoothLeDevice;
    }

    // GATT命令串行执行器
    private BleGattCommandExecutor commandExecutor;

    // 设备状态观察者列表
    private final List<IBleDeviceStateObserver> stateObserverList = new LinkedList<>();

    // 标记是否正在关闭设备，如果是，则所有回调都不会响应
    private boolean isClosing = false;

    // 当前的重连次数
    private int curReconnectTimes = 0;

    // 标记是否刚刚已经执行了onConnectSuccess，为了防止有时候出现连续两次执行的异常情况
    // 这个异常是BLE包的问题
    private boolean hasExecuteConnectSuccess = false;

    // 设备连接状态，初始化为关闭状态
    private BleDeviceConnectState connectState = BleDeviceConnectState.CONNECT_CLOSED;

    public BleDeviceConnectState getConnectState() {
        return connectState;
    }

    // 设置设备连接状态，并通知状态观察者
    public void setConnectState(BleDeviceConnectState connectState) {
        if(this.connectState == connectState) {
            return;
        }
        this.connectState = connectState;
        notifyDeviceStateObservers();
    }

    // 获取设备状态描述信息
    public synchronized String getStateDescription() {
        return connectState.getDescription();
    }

    // 获取设备状态图标
    public synchronized int getStateIcon() {
        return connectState.getIcon();
    }


    // 扫描回调
    private final IScanCallback scanCallback = new IScanCallback() {
        @Override
        public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
            BluetoothDevice bluetoothDevice = bluetoothLeDevice.getDevice();
            if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                Toast.makeText(MyApplication.getContext(), "未绑定设备，无法使用。", Toast.LENGTH_SHORT).show();
                //bluetoothDevice.createBond();   // 还没有绑定，则启动绑定
                BleDevice.this.onScanFinish(false);
            } else if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                BleDevice.this.bluetoothLeDevice = bluetoothLeDevice;
                BleDevice.this.onScanFinish(true);
            }
        }

        @Override
        public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {

        }

        @Override
        public void onScanTimeout() {
            BleDevice.this.onScanFinish(false);
        }
    };

    // 连接回调
    private final IConnectCallback connectCallback = new IConnectCallback() {
        // 连接成功回调
        @Override
        public void onConnectSuccess(DeviceMirror mirror) {
            BleDevice.this.onConnectSuccess(mirror);
        }

        // 连接失败回调
        @Override
        public void onConnectFailure(final BleException exception) {
            BleDevice.this.onConnectFailure(exception);
        }

        // 连接断开回调
        @Override
        public void onDisconnect(final boolean isActive) {
            BleDevice.this.onDisconnect(isActive);
        }
    };

    private final Handler handler = createMessageHandler();

    // 创建HandlerThread及其消息Handler
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

    protected Handler getHandler() {
        return handler;
    }

    // 扫描回调
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
        ViseLog.i("open");
        if(!isClosed())
            return;

        handler.removeCallbacksAndMessages(null);
        isClosing = false;
        curReconnectTimes = 0;

        //notifyDeviceStateObservers();

        if(autoConnect())
            scanOrConnect();
    }

    // 关闭设备
    public synchronized void close() {
        ViseLog.e(getClass().getSimpleName() + " " + getMacAddress() + ": close()");

        handler.removeCallbacksAndMessages(null);

        isClosing = true;

        if(connectState == BleDeviceConnectState.CONNECT_SCAN)
            stopScan();
        else
            disconnect();
    }

    // 切换设备状态
    public synchronized void switchState() {
        ViseLog.i("switchState");
        handler.removeCallbacksAndMessages(null);
        switch (connectState) {
            case CONNECT_SUCCESS:
                disconnect();
                break;

            case CONNECT_SCAN:
            case CONNECT_PROCESS:
                break;

            default:
                curReconnectTimes = 0;
                scanOrConnect();
                break;
        }
    }

    // 设备是否已关闭
    public boolean isClosed() {
        return connectState == BleDeviceConnectState.CONNECT_CLOSED;
    }

    // 设备是否已连接
    public boolean isConnected() {
        //DeviceMirror deviceMirror = BleDeviceUtil.getDeviceMirror(this);
        //return (deviceMirror != null && deviceMirror.isConnected());
        return connectState == BleDeviceConnectState.CONNECT_SUCCESS;
    }


    // 扫描或连接
    private synchronized void scanOrConnect() {
        ViseLog.i("scanOrConnect");
        handler.removeCallbacksAndMessages(null);
        if(bluetoothLeDevice == null) {         // 没有扫描成功，则启动扫描
            startScan();
        } else {        // 否则直接扫描成功，准备连接
            startConnect();
        }
    }

    // 开始扫描
    private synchronized void startScan() {
        ViseLog.i("startScan");
        filterScanCallback = new SingleFilterScanCallback(scanCallback).setDeviceMac(getMacAddress()).setScan(true);
        filterScanCallback.scan();
        setConnectState(BleDeviceConnectState.CONNECT_SCAN);
    }

    // 停止扫描
    private synchronized void stopScan() {
        ViseLog.i("stopScan");
        handler.removeCallbacksAndMessages(null);
        if(filterScanCallback != null) {
            filterScanCallback.setScan(false).scan();
        }
    }

    // 开始连接，有些资料说最好放到UI线程中执行连接
    private synchronized void startConnect() {
        ViseLog.i("startConnect");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BleDeviceUtil.connect(bluetoothLeDevice, connectCallback);
            }
        }, 1000);
    }

    // 断开连接
    private synchronized void disconnect() {
        ViseLog.i("disconnect");
        stopCommandExecutor();
        executeAfterDisconnect();
        handler.removeCallbacksAndMessages(null);

        BleDeviceUtil.disconnect(this);
        hasExecuteConnectSuccess = false;
    }

    // 重新连接
    private synchronized void reconnect() {
        int canReconnectTimes = getReconnectTimes();
        if(curReconnectTimes < canReconnectTimes || canReconnectTimes == -1) {
            startConnect();
            if(curReconnectTimes < canReconnectTimes)
                curReconnectTimes++;
        } else if(basicInfo.isWarnAfterReconnectFailure()) {
            notifyReconnectFailureObservers(true);
        }
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
        ViseLog.e("register device observer:" + observer);
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
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        observer.updateDeviceState(BleDevice.this);
                    }
                });
            }
        }
    }

    // 通知观察者设备重连失败
    public void notifyReconnectFailureObservers(final boolean isWarn) {
        for(final IBleDeviceStateObserver observer : stateObserverList) {
            if(observer != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        observer.warnDeviceReconnectFailure(BleDevice.this, isWarn);
                    }
                });
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
        ViseLog.i("stop command executor.");
        if(isCommandExecutorAlive()) commandExecutor.stop();
    }

    // Gatt命令执行器是否Alive
    private boolean isCommandExecutorAlive() {
        return ((commandExecutor != null) && commandExecutor.isAlive());
    }

    // 发送Gatt命令执行后回调处理消息
    protected void sendGattMessage(int what, Object obj) {
        Message msg = Message.obtain(handler, what, obj);
        msg.sendToTarget();
    }


    // 扫描结束回调
    private void onScanFinish(boolean result) {
        ViseLog.i("onScanFinish " + result);
        if(isClosing)
            return;

        if(result) {
            setConnectState(BleDeviceConnectState.CONNECT_PROCESS);
            startConnect();
        } else {
            handler.removeCallbacksAndMessages(null);
            setConnectState(BleDeviceConnectState.CONNECT_DISCONNECT);
        }
    }

    // 连接成功回调
    private void onConnectSuccess(DeviceMirror mirror) {
        ViseLog.i("onConnectSuccess");
        if(hasExecuteConnectSuccess) {
            return;
        }

        if(isClosing) {
            BleDeviceUtil.disconnect(this);
            return;
        }

        handler.removeCallbacksAndMessages(null);
        mirror.registerStateObserver(this);
        setConnectState(BleDeviceConnectState.getFromCode(mirror.getConnectState().getCode()));

        // 创建Gatt串行命令执行器
        createGattCommandExecutor();
        if(!executeAfterConnectSuccess()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            });
        }

        curReconnectTimes = 0;
        hasExecuteConnectSuccess = true;
    }

    // 连接错误回调
    private void onConnectFailure(final BleException bleException) {
        ViseLog.i("onConnectFailure " + bleException);

        if(isClosing)
            return;

        if(bleException instanceof TimeoutException) {
            return;
        }
        // 仍然有可能会连续执行两次下面语句
        stopCommandExecutor();
        executeAfterConnectFailure();
        handler.removeCallbacksAndMessages(null);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reconnect();
            }
        }, RECONNECT_INTERVAL);

        hasExecuteConnectSuccess = false;
    }

    // 连接断开回调
    private void onDisconnect(boolean isActive) {
        ViseLog.i("onDisconnect " + isActive);
        if(isClosing) {
            setConnectState(BleDeviceConnectState.CONNECT_CLOSED);
        } else if(!isActive) {
            hasExecuteConnectSuccess = false;
            stopCommandExecutor();
            executeAfterConnectFailure();
            handler.removeCallbacksAndMessages(null);
        }
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

    @Override
    public void updateDeviceStateAccordingMirror(ConnectState mirrorState) {
        setConnectState(BleDeviceConnectState.getFromCode(mirrorState.getCode()));
    }
}
