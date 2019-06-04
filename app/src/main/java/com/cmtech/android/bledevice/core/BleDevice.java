package com.cmtech.android.bledevice.core;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.v4.content.ContextCompat;

import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.callback.scan.ScanCallback;
import com.cmtech.android.ble.callback.scan.SingleFilterScanCallback;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.bledevice.SupportedDeviceType;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.vise.log.ViseLog;

import net.jcip.annotations.ThreadSafe;

import java.io.ObjectInput;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


/**
  *
  * ClassName:      BleDevice
  * Description:    表示低功耗蓝牙设备
  * Author:         chenm
  * CreateDate:     2018-02-19 07:02
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-05 07:02
  * UpdateRemark:   更新说明
  * Version:        1.0
 */


@ThreadSafe
public abstract class BleDevice implements Handler.Callback {
    private final static BleDeviceConnectState DEVICE_INIT_STATE = BleDeviceConnectState.CONNECT_CLOSED;

    private class MyConnectCallback implements IConnectCallback {
        MyConnectCallback() {
        }

        @Override
        public void onConnectSuccess(final DeviceMirror deviceMirror) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    processConnectSuccess(deviceMirror);
                }
            });
        }
        @Override
        public void onConnectFailure(final BleException exception) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    processConnectFailure(exception);
                }
            });
        }
        @Override
        public void onDisconnect(final boolean isActive) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    processDisconnect(isActive);
                }
            });
        }
    }


    private BleDeviceBasicInfo basicInfo; // 设备基本信息对象

    private BluetoothLeDevice bluetoothLeDevice = null; // 设备BluetoothLeDevice，当扫描到设备后会赋值

    private int battery = -1; // 设备电池电量

    private int curReconnectTimes = 0; // 当前重连次数

    // 标记设备是否正在关闭，如果是，则对设备的所有回调都不会响应
    private boolean closing = false;

    private boolean connectSuccess = false;

    // 扫描回调适配器，将IScanCallback适配为BluetoothAdapter.LeScanCallback，每次新的扫描必须创建新的实例
    private ScanCallback scanCallback;

    private BleDeviceConnectState connectState = DEVICE_INIT_STATE; // 设备连接状态，初始化为关闭状态

    private final List<OnBleDeviceStateListener> deviceStateListeners = new LinkedList<>(); // 设备状态观察者列表

    protected final BleDeviceGattOperator gattOperator = new BleDeviceGattOperator(this); // Gatt命令执行者

    // 主线程Handler，包括连接相关的处理和Gatt消息的处理，都在主线程中执行
    private final Handler mainHandler = new Handler(Looper.getMainLooper(), this);


    public BleDevice(BleDeviceBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
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

    public String getImagePath() {
        return basicInfo.getImagePath();
    }

    public Drawable getImageDrawable() {
        if(getImagePath().equals("")) {
            int imageId = SupportedDeviceType.getDeviceTypeFromUuid(getUuidString()).getDefaultImage();

            return ContextCompat.getDrawable(MyApplication.getContext(), imageId);
        } else {
            return new BitmapDrawable(MyApplication.getContext().getResources(), getImagePath());
        }
    }

    BluetoothLeDevice getBluetoothLeDevice() { return bluetoothLeDevice; }

    public boolean isClosed() {
        return connectState == BleDeviceConnectState.CONNECT_CLOSED;
    }

    protected boolean isConnected() {
        return connectState == BleDeviceConnectState.CONNECT_SUCCESS;
    }

    public BleDeviceConnectState getConnectState() {
        return connectState;
    }
    public String getStateDescription() {
        return connectState.getDescription();
    } // 获取设备状态描述信息

    public void setConnectState(BleDeviceConnectState connectState) {
        if(this.connectState != connectState) {
            ViseLog.e(connectState + " in " + Thread.currentThread());

            this.connectState = connectState;

            updateDeviceConnectState();
        }
    }

    public int getBattery() {
        return battery;
    }

    protected void setBattery(final int battery) {
        this.battery = battery;

        for(final OnBleDeviceStateListener listener : deviceStateListeners) {
            if(listener != null) {
                listener.onDeviceBatteryUpdated(BleDevice.this);
            }
        }
    }

    // 打开设备
    public void open() {
        ViseLog.i("open");

        if(!isClosed())
            return;

        closing = false;

        if(basicInfo.autoConnect()) {
            startScan();
        }
    }

    // 关闭设备
    public void close() {
        ViseLog.i(getClass().getSimpleName() + " " + getMacAddress() + ": close()");
        removeCallbacksAndMessages();

        closing = true;

        if(connectState == BleDeviceConnectState.CONNECT_SCAN)
            stopScan();
        else
            disconnect();
    }

    // 销毁设备
    public final void destroy() {
        ViseLog.i("destroy");
        //mainHandler.getLooper().quit();
    }

    // 切换设备状态
    final void switchState() {
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
                curReconnectTimes = 0;
                startScan();
                break;
        }
    }

    // 发送Gatt消息给工作线程
    public final void sendGattMessage(int what, Object obj) {
        Message.obtain(mainHandler, what, obj).sendToTarget();
    }

    protected void removeCallbacksAndMessages() {
        mainHandler.removeCallbacksAndMessages(null);
    }

    protected void postDelayed(Runnable runnable, long delay) {
        mainHandler.postDelayed(runnable, delay);
    }

    // 开始扫描
    private void startScan() {
        if(connectState != BleDeviceConnectState.CONNECT_CLOSED && connectState != BleDeviceConnectState.CONNECT_DISCONNECT) {
            return;
        }

        ViseLog.e("startScan in " + Thread.currentThread());

        scanCallback = new SingleFilterScanCallback(new MyScanCallback(BleDevice.this)).setDeviceMac(basicInfo.getMacAddress());

        scanCallback.setScan(true).scan();

        setConnectState(BleDeviceConnectState.CONNECT_SCAN);
    }


    // 延时后开始连接，有些资料说最好放到UI线程中执行连接
    private void startConnect() {
        //removeCallbacksAndMessages();
        setConnectState(BleDeviceConnectState.CONNECT_PROCESS);

        ViseLog.e("startConnect in " + Thread.currentThread());

        MyConnectCallback connectCallback = new MyConnectCallback();

        BleDeviceUtil.connect(bluetoothLeDevice, connectCallback);
    }

    // 断开连接
    protected void disconnect() {
        executeAfterDisconnect();

        removeCallbacksAndMessages();

        ViseLog.e("disconnect in " + Thread.currentThread());

        BleDeviceUtil.disconnect(BleDevice.this);
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
    final void updateDeviceConnectState() {
        for(OnBleDeviceStateListener listener : deviceStateListeners) {
            if(listener != null) {
                listener.onDeviceConnectStateUpdated(this);
            }
        }
    }

    // 通知设备状态观察者重连失败，是否报警
    public final void notifyReconnectFailureObservers(final boolean isWarn) {
        for(final OnBleDeviceStateListener observer : deviceStateListeners) {
            if(observer != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        observer.onReconnectFailureNotify(BleDevice.this, isWarn);
                    }
                });
            }
        }
    }

    // 处理扫描结果
    @MainThread
    void processScanResult(boolean canConnect, BluetoothLeDevice bluetoothLeDevice) {
        ViseLog.e("ProcessScanResult: " + canConnect + " in " + Thread.currentThread());

        if(closing || isClosed())
            return;

        if(canConnect) {
            this.bluetoothLeDevice = bluetoothLeDevice;

            startConnect(); // 扫描成功，启动连接
        } else {
            removeCallbacksAndMessages();

            setConnectState(BleDeviceConnectState.CONNECT_DISCONNECT);

            reconnect();
        }
    }

    // 处理连接成功回调
    @MainThread
    private void processConnectSuccess(DeviceMirror mirror) {
        ViseLog.e("processConnectSuccess in " + Thread.currentThread());

        if (closing || isClosed()) {
            BleDeviceUtil.disconnect(this);
            return;
        }

        if(connectSuccess) {
            return;
        }

        removeCallbacksAndMessages();

        setConnectState(BleDeviceConnectState.CONNECT_SUCCESS);

        // 设备执行连接后处理，如果出错则断开
        if (!executeAfterConnectSuccess()) {
            disconnect();
        }

        connectSuccess = true;
    }

    // 处理连接错误
    @MainThread
    private void processConnectFailure(final BleException bleException) {
        ViseLog.e("processConnectFailure in " + Thread.currentThread() + " with " +bleException );

        if (closing || isClosed()) {
            setConnectState(BleDeviceConnectState.CONNECT_CLOSED);
        } else {
            // 仍然有可能会连续执行两次下面语句
            executeAfterConnectFailure();

            removeCallbacksAndMessages();

            setConnectState(BleDeviceConnectState.CONNECT_DISCONNECT);

            bluetoothLeDevice = null;

            reconnect();
        }

        //disconnect();

        connectSuccess = false;
    }

    // 处理连接断开
    @MainThread
    private void processDisconnect(boolean isActive) {
        ViseLog.e("processDisconnect: " + isActive+ " in " + Thread.currentThread() );

        if (closing || isClosed()) {
            setConnectState(BleDeviceConnectState.CONNECT_CLOSED);

        } else {
            if (!isActive) {
                executeAfterDisconnect();

                removeCallbacksAndMessages();
            }

            setConnectState(BleDeviceConnectState.CONNECT_DISCONNECT);

        }

        connectSuccess = false;
    }

    // 重新连接
    private void reconnect() {
        int canReconnectTimes = basicInfo.getReconnectTimes();

        if(curReconnectTimes < canReconnectTimes) {
            curReconnectTimes++;
            startScan();
        } else if(canReconnectTimes == -1) {
            startScan();
        } else if(basicInfo.isWarnAfterReconnectFailure()) {
            notifyReconnectFailureObservers(true); // 重连失败后通知报警
        }
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
    /*private Handler createWorkHandler(String threadName) {
        HandlerThread thread = new HandlerThread(threadName);

        thread.start();

        return new Handler(thread.getLooper(), this);
    }*/

    @Override
    public boolean handleMessage(Message message) {
        processGattMessage(message);

        return false;
    }

    // 停止扫描
    private void stopScan() {
        removeCallbacksAndMessages();

        ViseLog.i("stopScan in " + Thread.currentThread());

        if(scanCallback != null)
            scanCallback.setScan(false).scan();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BleDevice that = (BleDevice) o;

        BleDeviceBasicInfo thisInfo = getBasicInfo();

        BleDeviceBasicInfo thatInfo = that.getBasicInfo();

        return Objects.equals(thisInfo, thatInfo);
    }

    @Override
    public int hashCode() {
        return (getBasicInfo() != null) ? getBasicInfo().hashCode() : 0;
    }

}
