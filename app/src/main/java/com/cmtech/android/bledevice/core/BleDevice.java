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
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.OnDeviceMirrorStateChangedListener;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.bledevice.SupportedDeviceType;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.vise.log.ViseLog;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BleDevice: 低功耗蓝牙设备类
 * 作为DeviceMirror的状态的观察者，需要实现IDeviceMirrorStateObserver
 * Created by bme on 2018/2/19.
 */

@ThreadSafe
public abstract class BleDevice implements OnDeviceMirrorStateChangedListener {
    private final static String TAG = "BleDevice";

    private BleDeviceBasicInfo basicInfo; // 设备基本信息对象

    private int battery = -1;

    // 设备BluetoothLeDevice，当扫描到后会赋值，并一直保留，直到程序关闭
    private volatile BluetoothLeDevice bluetoothLeDevice = null;

    private AtomicInteger curReconnectTimes = new AtomicInteger(0); // 当前已重连次数

    // 标记设备是否正在关闭，如果是，则对设备的所有回调都不会响应
    private boolean closing = false;

    private boolean connectSuccess = false;

    // 扫描回调适配器，将IScanCallback适配为BluetoothAdapter.LeScanCallback
    // 每次新的扫描必须创建新的实例
    private ScanCallback scanCallback;

    private final Handler workHandler; // 工作Handler，包括连接相关的处理和Gatt命令和数据的处理，都在Handler线程中执行

    // 需要同步
    @GuardedBy("this")
    private BleDeviceConnectState connectState = BleDeviceConnectState.CONNECT_CLOSED; // 设备连接状态，初始化为关闭状态

    private final List<OnBleDeviceStateListener> deviceStateListeners = new LinkedList<>(); // 设备状态观察者列表

    protected final BleDeviceGattOperator gattOperator = new BleDeviceGattOperator(this); // Gatt命令执行器




    public BleDevice(BleDeviceBasicInfo basicInfo) {
        this.basicInfo = basicInfo;

        workHandler = createWorkHandler("Device:" + basicInfo.getMacAddress());
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

    private int getReconnectTimes() { return basicInfo.getReconnectTimes(); }

    public BluetoothLeDevice getBluetoothLeDevice() { return bluetoothLeDevice; }

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

            ViseLog.e(connectState + " in " + Thread.currentThread());

            this.connectState = connectState;

            updateDeviceConnectState();
        }
    }

    // 获取设备状态描述信息
    public synchronized String getStateDescription() {
        return connectState.getDescription();
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

        if(autoConnect()) {
            startScan();
        }
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
                curReconnectTimes.set(0);
                startScan();
                break;
        }
    }

    // 发送Gatt消息给工作线程
    public final void sendGattMessage(int what, Object obj) {
        Message.obtain(workHandler, what, obj).sendToTarget();
    }

    // 延时后开始连接，有些资料说最好放到UI线程中执行连接
    private void startConnect(final int millisecond) {
        workHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViseLog.e("startConnect in " + Thread.currentThread());

                MyConnectCallback connectCallback = new MyConnectCallback(BleDevice.this);

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
        ViseLog.i("register device listener:" + listener);
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

    // 处理扫描结果
    synchronized void processScanResult(boolean canConnect, BluetoothLeDevice bluetoothLeDevice) {
        ViseLog.e("ProcessScanResult: " + canConnect + " in " + Thread.currentThread());

        if(closing || isClosed())
            return;

        if(canConnect) {
            this.bluetoothLeDevice = bluetoothLeDevice;

            setConnectState(BleDeviceConnectState.CONNECT_PROCESS);

            startConnect(0); // 扫描成功，启动连接
        } else {
            removeCallbacksAndMessages();

            setConnectState(BleDeviceConnectState.CONNECT_DISCONNECT);

            reconnect();
        }
    }

    // 处理连接成功回调
    synchronized void processConnectSuccess(DeviceMirror mirror) {
        ViseLog.e("processConnectSuccess in " + Thread.currentThread());

        if (closing || isClosed()) {
            BleDeviceUtil.disconnect(this);
            return;
        }

        if(connectSuccess) {
            return;
        }

        removeCallbacksAndMessages();

        mirror.registerStateListener(this);

        setConnectState(BleDeviceConnectState.getFromCode(mirror.getConnectState().getCode()));

        // 设备执行连接后处理，如果出错则断开
        if (!executeAfterConnectSuccess()) {
            disconnect();
        }

        connectSuccess = true;
    }

    // 处理连接错误
    synchronized void processConnectFailure(final BleException bleException) {
        ViseLog.e("processConnectFailure in " + Thread.currentThread() + " with " +bleException );

        if (closing || isClosed()) {
            setConnectState(BleDeviceConnectState.CONNECT_CLOSED);
        } else {
            // 仍然有可能会连续执行两次下面语句
            executeAfterConnectFailure();

            removeCallbacksAndMessages();

            reconnect();
        }

        connectSuccess = false;
    }

    // 处理连接断开
    synchronized void processDisconnect(boolean isActive) {
        ViseLog.e("processDisconnect: " + isActive+ " in " + Thread.currentThread() );

        if (closing || isClosed()) {
            setConnectState(BleDeviceConnectState.CONNECT_CLOSED);

        } else if (!isActive) {
            executeAfterDisconnect();

            removeCallbacksAndMessages();
        }

        connectSuccess = false;
    }

    // 重新连接
    private void reconnect() {
        int canReconnectTimes = getReconnectTimes();

        if(curReconnectTimes.get() < canReconnectTimes || canReconnectTimes == -1) {
            /*new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //setConnectState(BleDeviceConnectState.CONNECT_PROCESS);

                    startScan();
                }
            });*/

            if(curReconnectTimes.get() < canReconnectTimes)
                curReconnectTimes.incrementAndGet();

            startScan();
        } else if(basicInfo.isWarnAfterReconnectFailure()) {
            notifyReconnectFailureObservers(true); // 重连失败后通知报警

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

    // 开始扫描
    private void startScan() {
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                ViseLog.e("startScan in " + Thread.currentThread());
                scanCallback = new SingleFilterScanCallback(new MyScanCallback(BleDevice.this)).setDeviceMac(basicInfo.getMacAddress());

                scanCallback.setScan(true).scan();
            }
        });

        setConnectState(BleDeviceConnectState.CONNECT_SCAN);
    }

    // 停止扫描
    private void stopScan() {
        removeCallbacksAndMessages();

        workHandler.post(new Runnable() {
            @Override
            public void run() {
                ViseLog.i("stopScan in " + Thread.currentThread());

                if(scanCallback != null)
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
