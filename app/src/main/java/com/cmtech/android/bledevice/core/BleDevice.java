package com.cmtech.android.bledevice.core;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.cmtech.android.ble.callback.scan.IScanCallback;
import com.cmtech.android.ble.callback.scan.ScanCallback;
import com.cmtech.android.ble.callback.scan.SingleFilterScanCallback;
import com.cmtech.android.ble.common.ConnectState;
import com.cmtech.android.ble.core.IDeviceMirrorStateObserver;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.BluetoothLeDeviceStore;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.vise.log.ViseLog;

import java.util.LinkedList;
import java.util.List;

/**
 * BleDevice: 低功耗蓝牙设备类
 * 作为DeviceMirror的状态的观察者，需要实现IDeviceMirrorStateObserver
 * Created by bme on 2018/2/19.
 */

public abstract class BleDevice implements IDeviceMirrorStateObserver {
    private final static String TAG = "BleDevice";

    private BleDeviceBasicInfo basicInfo; // 设备基本信息对象
    private BluetoothLeDevice bluetoothLeDevice = null; // 设备BluetoothLeDevice，当扫描到后会赋值，并一直保留，直到程序关闭
    private final List<IBleDeviceStateObserver> stateObserverList = new LinkedList<>(); // 设备状态观察者列表
    private boolean closing = false; // 标记设备是否正在关闭，如果是，则对设备的所有回调都不会响应
    private BleDeviceConnectState connectState = BleDeviceConnectState.CONNECT_CLOSED; // 设备连接状态，初始化为关闭状态
    private ScanCallback filterScanCallback = null; // 扫描回调适配器，将IScanCallback适配为BluetoothAdapter.LeScanCallback
    // 扫描回调，注意与上述的ScanCallback相区别
    private final IScanCallback scanCallback = new IScanCallback() {
        @Override
        public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
            BluetoothDevice bluetoothDevice = bluetoothLeDevice.getDevice();
            if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                Toast.makeText(MyApplication.getContext(), "该设备未绑定，无法使用。", Toast.LENGTH_SHORT).show();
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
    private final MyConnectCallback connectCallback = new MyConnectCallback(this);
    protected final Handler workHandler = createWorkHandler(); // 工作Handler
    protected final BleDeviceGattOperator gattOperator = new BleDeviceGattOperator(this); // Gatt执行器

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
    public int getReconnectTimes() { return basicInfo.getReconnectTimes(); }
    public BluetoothLeDevice getBluetoothLeDevice() {
        return bluetoothLeDevice;
    }
    public BleDeviceConnectState getConnectState() {
        return connectState;
    }
    // 设置设备连接状态，并通知状态观察者
    public void setConnectState(BleDeviceConnectState connectState) {
        if(this.connectState != connectState) {
            this.connectState = connectState;
            notifyDeviceStateObservers();
        }
    }
    // 获取设备状态描述信息
    public String getStateDescription() {
        return connectState.getDescription();
    }
    // 获取设备状态图标
    public int getStateIcon() {
        return connectState.getIcon();
    }

    public boolean isClosing() {
        return closing;
    }

    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    // 构造器
    public BleDevice(BleDeviceBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    // 打开设备
    public final synchronized void open() {
        ViseLog.i("open");
        if(!isClosed())
            return;

        closing = false;

        if(autoConnect())
            scanOrConnect();
    }

    // 切换设备状态
    public final synchronized void switchState() {
        ViseLog.i("switchDeviceState");
        workHandler.removeCallbacksAndMessages(null);
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

    // 关闭设备
    public synchronized void close() {
        ViseLog.e(getClass().getSimpleName() + " " + getMacAddress() + ": close()");

        workHandler.removeCallbacksAndMessages(null);

        closing = true;

        if(connectState == BleDeviceConnectState.CONNECT_SCAN)
            stopScan();
        else
            disconnect();
    }

    // 停止设备工作线程
    public final synchronized void quit() {
        ViseLog.i("quit");
        workHandler.getLooper().quit();
    }

    // 发送Gatt消息给工作线程
    public final void sendGattMessage(int what, Object obj) {
        Message.obtain(workHandler, what, obj).sendToTarget();
    }

    // 开始连接，有些资料说最好放到UI线程中执行连接
    public synchronized void startConnect() {
        workHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViseLog.i("startConnect in " + Thread.currentThread());
                BleDeviceUtil.connect(bluetoothLeDevice, connectCallback);
            }
        }, 1000);
    }

    // 断开连接
    protected synchronized void disconnect() {
        executeAfterDisconnect();
        workHandler.removeCallbacksAndMessages(null);

        workHandler.post(new Runnable() {
            @Override
            public void run() {
                ViseLog.i("disconnect in " + Thread.currentThread());
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

    // 设备是否已关闭
    public final boolean isClosed() {
        return connectState == BleDeviceConnectState.CONNECT_CLOSED;
    }

    // 设备是否已连接
    public final boolean isConnected() {
        return connectState == BleDeviceConnectState.CONNECT_SUCCESS;
    }

    // 登记设备状态观察者
    public final void registerDeviceStateObserver(IBleDeviceStateObserver observer) {
        ViseLog.e("register device observer:" + observer);
        if(!stateObserverList.contains(observer)) {
            stateObserverList.add(observer);
        }
    }

    // 删除设备状态观察者
    public final void removeDeviceStateObserver(IBleDeviceStateObserver observer) {
        int index = stateObserverList.indexOf(observer);
        if(index >= 0) {
            stateObserverList.remove(index);
        }
    }

    // 通知设备状态观察者
    public final void notifyDeviceStateObservers() {
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

    // 通知设备状态观察者重连失败，是否报警
    public final void notifyReconnectFailureObservers(final boolean isWarn) {
        for(final IBleDeviceStateObserver observer : stateObserverList) {
            if(observer != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        observer.notifyReconnectFailure(BleDevice.this, isWarn);
                    }
                });
            }
        }
    }

    // IDeviceMirrorStateObserver接口函数
    @Override
    public void updateStateAccordingMirror(ConnectState mirrorState) {
        setConnectState(BleDeviceConnectState.getFromCode(mirrorState.getCode()));
    }

    /*
     * 抽象方法
     */
    public abstract boolean executeAfterConnectSuccess(); // 连接成功后执行的操作
    public abstract void executeAfterConnectFailure(); // 连接错误后执行的操作
    public abstract void executeAfterDisconnect(); // 断开连接后执行的操作
    public abstract void processGattMessage(Message msg); // 处理Gatt消息函数

    ///////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////私有方法/////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    // 创建工作线程，输出它的Handler
    private Handler createWorkHandler() {
        HandlerThread thread = new HandlerThread("Device:"+getMacAddress());
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
        } else {        // 否则直接扫描成功，准备连接
            connectCallback.clearReconnectTimes();
            startConnect();
        }
    }

    // 开始扫描
    private synchronized void startScan() {
        filterScanCallback = new SingleFilterScanCallback(scanCallback).setDeviceMac(getMacAddress()).setScan(true);
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                ViseLog.i("startScan in " + Thread.currentThread());
                filterScanCallback.scan();
            }
        });
        setConnectState(BleDeviceConnectState.CONNECT_SCAN);
    }


    // 停止扫描
    private synchronized void stopScan() {
        removeCallbacksAndMessages();
        if(filterScanCallback != null) {
            workHandler.post(new Runnable() {
                @Override
                public void run() {
                    ViseLog.i("stopScan in " + Thread.currentThread());
                    filterScanCallback.setScan(false).scan();
                }
            });
        }
    }



    // 扫描结束回调处理
    private void onScanFinish(boolean result) {
        ViseLog.i("onScanFinish " + result);
        if(closing)
            return;

        if(result) {
            setConnectState(BleDeviceConnectState.CONNECT_PROCESS);
            startConnect();
        } else {
            workHandler.removeCallbacksAndMessages(null);
            setConnectState(BleDeviceConnectState.CONNECT_DISCONNECT);
        }
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
