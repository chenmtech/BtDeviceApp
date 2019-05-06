package com.cmtech.android.bledevice.core;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.ble.callback.scan.ScanCallback;
import com.cmtech.android.ble.callback.scan.SingleFilterScanCallback;
import com.cmtech.android.ble.common.ConnectState;
import com.cmtech.android.ble.core.OnDeviceMirrorStateChangedListener;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.bledevice.SupportedDeviceType;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.vise.log.ViseLog;

import java.util.LinkedList;
import java.util.List;

/**
 * BleDevice: 低功耗蓝牙设备类
 * 作为DeviceMirror的状态的观察者，需要实现IDeviceMirrorStateObserver
 * Created by bme on 2018/2/19.
 */

public abstract class BleDevice implements OnDeviceMirrorStateChangedListener {
    private final static String TAG = "BleDevice";

    private BleDeviceBasicInfo basicInfo; // 设备基本信息对象

    private int battery = -1;

    private BluetoothLeDevice bluetoothLeDevice = null; // 设备BluetoothLeDevice，当扫描到后会赋值，并一直保留，直到程序关闭

    private boolean closing = false; // 标记设备是否正在关闭，如果是，则对设备的所有回调都不会响应

    private final ScanCallback scanCallback; // 扫描回调适配器，将IScanCallback适配为BluetoothAdapter.LeScanCallback

    private final MyConnectCallback connectCallback = new MyConnectCallback(this); // 连接回调

    private final Handler workHandler; // 工作Handler，包括连接相关的处理和Gatt命令和数据的处理，都在Handler线程中执行

    // 需要同步
    private BleDeviceConnectState connectState = BleDeviceConnectState.CONNECT_CLOSED; // 设备连接状态，初始化为关闭状态

    private final List<OnBleDeviceStateListener> deviceStateListeners = new LinkedList<>(); // 设备状态观察者列表

    protected final BleDeviceGattOperator gattOperator = new BleDeviceGattOperator(this); // Gatt命令执行器

    public BleDevice(BleDeviceBasicInfo basicInfo) {
        this.basicInfo = basicInfo;

        workHandler = createWorkHandler("Device:" + basicInfo.getMacAddress());

        scanCallback = new SingleFilterScanCallback(new MyScanCallback(this)).setDeviceMac(basicInfo.getMacAddress());
    }

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

    public Drawable getImageDrawable() {
        if(getImagePath().equals("")) {
            int imageId = SupportedDeviceType.getDeviceTypeFromUuid(getUuidString()).getDefaultImage();

            return MyApplication.getContext().getResources().getDrawable(imageId);
        } else {
            return new BitmapDrawable(MyApplication.getContext().getResources(), getImagePath());
        }
    }

    public int getReconnectTimes() { return basicInfo.getReconnectTimes(); }

    public BluetoothLeDevice getBluetoothLeDevice() { return bluetoothLeDevice; }

    public void setBluetoothLeDevice(BluetoothLeDevice bluetoothLeDevice) { this.bluetoothLeDevice = bluetoothLeDevice; }

    public synchronized String getStateDescription() {
        return connectState.getDescription();
    } // 获取设备状态描述信息

    public boolean isClosing() {
        return closing;
    } // 是否关闭中

    public synchronized boolean isClosed() {
        return connectState == BleDeviceConnectState.CONNECT_CLOSED;
    }

    public synchronized boolean isConnected() {
        return connectState == BleDeviceConnectState.CONNECT_SUCCESS;
    }

    public synchronized BleDeviceConnectState getConnectState() {
        return connectState;
    }

    public synchronized void setConnectState(BleDeviceConnectState connectState) {
        if(this.connectState != connectState) {

            ViseLog.e("ConnectState changed thread: " + Thread.currentThread() + "State is : " + connectState);

            this.connectState = connectState;
            updateDeviceConnectState();
        }
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(final int battery) {
        this.battery = battery;

        for(final OnBleDeviceStateListener listener : deviceStateListeners) {
            if(listener != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onUpdateDeviceBattery(BleDevice.this);
                    }
                });
            }
        }
    }

    // 打开设备
    public synchronized void open() {
        ViseLog.i("open");
        if(!isClosed())
            return;
        closing = false;
        if(autoConnect())
            scanOrConnect();
    }

    // 关闭设备
    public synchronized void close() {
        ViseLog.i(getClass().getSimpleName() + " " + getMacAddress() + ": close()");
        removeCallbacksAndMessages();
        closing = true;
        if(connectState == BleDeviceConnectState.CONNECT_SCAN)
            stopScan();
        else
            disconnect();
    }

    // 销毁设备
    public final synchronized void destroy() {
        ViseLog.i("destroy");
        workHandler.getLooper().quit();
    }

    // 切换设备状态
    public final synchronized void switchState() {
        ViseLog.i("switchDeviceState");
        removeCallbacksAndMessages();
        switch (connectState) {
            case CONNECT_SUCCESS:
                disconnect();
                break;
            case CONNECT_SCAN:
            case CONNECT_PROCESS:
                break;
            default:
                scanOrConnect();
                break;
        }
    }

    // 发送Gatt消息给工作线程
    public final void sendGattMessage(int what, Object obj) {
        Message.obtain(workHandler, what, obj).sendToTarget();
    }

    // 延时后开始连接，有些资料说最好放到UI线程中执行连接
    public synchronized void startConnect(final int millisecond) {
        workHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViseLog.i("startConnect in " + Thread.currentThread());
                BleDeviceUtil.connect(bluetoothLeDevice, connectCallback);
            }
        }, millisecond);
    }

    // 断开连接
    protected synchronized void disconnect() {
        executeAfterDisconnect();

        removeCallbacksAndMessages();

        workHandler.post(new Runnable() {
            @Override
            public void run() {
                ViseLog.e("disconnect in " + Thread.currentThread());
                BleDeviceUtil.disconnect(BleDevice.this);
            }
        });
    }

    public void removeCallbacksAndMessages() {
        workHandler.removeCallbacksAndMessages(null);
    }

    public void postDelayed(Runnable runnable, long delay) {
        workHandler.postDelayed(runnable, delay);
    }

    // 登记设备状态观察者
    public final void registerDeviceStateListener(OnBleDeviceStateListener listener) {
        ViseLog.e("register device listener:" + listener);
        if(!deviceStateListeners.contains(listener)) {
            deviceStateListeners.add(listener);
        }
    }

    // 删除设备状态观察者
    public final void removeDeviceStateListener(OnBleDeviceStateListener listener) {
        deviceStateListeners.remove(listener);
    }

    // 通知设备状态观察者
    public final void updateDeviceConnectState() {
        for(final OnBleDeviceStateListener listener : deviceStateListeners) {
            if(listener != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onUpdateDeviceConnectState(BleDevice.this);
                    }
                });
            }
        }
    }

    // 通知设备状态观察者重连失败，是否报警
    public final void notifyReconnectFailureObservers(final boolean isWarn) {
        for(final OnBleDeviceStateListener observer : deviceStateListeners) {
            if(observer != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        observer.onNotifyReconnectFailure(BleDevice.this, isWarn);
                    }
                });
            }
        }
    }


    @Override
    public void onUpdateDeviceStateAccordingMirrorState(ConnectState mirrorState) {
        setConnectState(BleDeviceConnectState.getFromCode(mirrorState.getCode()));
    }

    /*
     * 抽象方法
     */
    public abstract boolean executeAfterConnectSuccess(); // 连接成功后执行的操作
    public abstract void executeAfterConnectFailure(); // 连接错误后执行的操作
    public abstract void executeAfterDisconnect(); // 断开连接后执行的操作
    public abstract void processGattMessage(Message msg); // 处理Gatt消息函数

    /**
     * 私有方法
     */
    // 创建工作线程，输出Handler
    private Handler createWorkHandler(String threadName) {
        HandlerThread thread = new HandlerThread(threadName);

        thread.start();

        return new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                processGattMessage(msg);
            }
        };
    }

    // 扫描或连接
    private synchronized void scanOrConnect() {
        removeCallbacksAndMessages();
        if(bluetoothLeDevice == null) {         // 没有扫描成功，则启动扫描
            startScan();
        } else {        // 否则直接连接
            connectCallback.clearReconnectTimes();
            startConnect(1000);
        }
    }

    // 开始扫描
    private synchronized void startScan() {
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                ViseLog.i("startScan in " + Thread.currentThread());
                scanCallback.setScan(true).scan();
            }
        });
        setConnectState(BleDeviceConnectState.CONNECT_SCAN);
    }

    // 停止扫描
    private synchronized void stopScan() {
        removeCallbacksAndMessages();
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                ViseLog.i("stopScan in " + Thread.currentThread());
                scanCallback.setScan(false).scan();
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleDevice that = (BleDevice) o;
        BleDeviceBasicInfo thisInfo = getBasicInfo();
        BleDeviceBasicInfo thatInfo = that.getBasicInfo();

        return (thisInfo != null) ? thisInfo.equals(thatInfo) : (thatInfo == null);
    }

    @Override
    public int hashCode() {
        return (getBasicInfo() != null) ? getBasicInfo().hashCode() : 0;
    }

}
