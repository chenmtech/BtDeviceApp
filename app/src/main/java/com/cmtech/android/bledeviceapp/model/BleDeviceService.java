package com.cmtech.android.bledeviceapp.model;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.BleDevice;
import com.cmtech.android.bledevicecore.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.BleDeviceConnectState;
import com.cmtech.android.bledevicecore.BleDeviceManager;
import com.cmtech.android.bledevicecore.IBleDeviceStateObserver;
import com.vise.log.ViseLog;

import java.util.List;

/**
 *  BleDeviceService: BleDevice服务
 *  Created by bme on 2018/12/09.
 */

public class BleDeviceService extends Service implements IBleDeviceStateObserver {
    private final static String TAG = "BleDeviceService";

    /**
     * id不可设置为0,否则不能设置为前台service
     */
    private static final int DEVICE_SERVICE_ID = 0x0001;

    private BleDeviceManager deviceManager;

    private DeviceServiceBinder binder = new DeviceServiceBinder();

    private Notification notification;


    @Override
    public void onCreate() {
        super.onCreate();

        deviceManager = new BleDeviceManager();

        notification = createNotification("");

        startForeground(DEVICE_SERVICE_ID, notification);
    }

    public class DeviceServiceBinder extends Binder {
        public BleDeviceService getService() {
            return BleDeviceService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void updateDeviceState(BleDevice device) {
        StringBuilder builder = new StringBuilder();
        for(BleDevice dev : deviceManager.getDeviceList()) {
            if(dev.getConnectState() != BleDeviceConnectState.CONNECT_CLOSED) {
                builder.append(dev.getMacAddress()).append(": ").append(dev.getConnectState().getDescription()).append('\n');
            }
        }
        notification = createNotification(builder.toString());
        startForeground(DEVICE_SERVICE_ID, notification);
        ViseLog.e(TAG + device.getConnectState().getDescription());
    }

    // 创建并添加一个设备
    public BleDevice addDevice(BleDeviceBasicInfo basicInfo) {
        BleDevice device = deviceManager.addDevice(basicInfo);
        if(device != null) {
            device.registerDeviceStateObserver(this);
        }
        return device;
    }

    // 添加多个设备
    public void addDevice(List<BleDeviceBasicInfo> basicInfoList) {
        if(basicInfoList == null) return;
        for(BleDeviceBasicInfo basicInfo : basicInfoList) {
            addDevice(basicInfo);
        }
    }

    // 删除一个设备
    public void deleteDevice(BleDevice device) {
        deviceManager.deleteDevice(device);
    }

    // 获取设备清单
    public List<BleDevice> getDeviceList() {
        return deviceManager.getDeviceList();
    }

    // 获取设备的Mac列表
    public List<String> getDeviceMacList() {
        return deviceManager.getDeviceMacList();
    }

    // 获取设备
    public BleDevice findDevice(BleDeviceBasicInfo basicInfo) {
        return deviceManager.findDevice(basicInfo);
    }

    // 获取设备
    public BleDevice findDevice(String macAddress) {
        return deviceManager.findDevice(macAddress);
    }



    /**
     * Notification
     */
    public Notification createNotification(String content){
        //使用兼容版本
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this, "default");
        //设置状态栏的通知图标
        builder.setSmallIcon(R.mipmap.ic_kang);
        //设置通知栏横条的图标
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_kang));
        //禁止用户点击删除按钮删除
        builder.setAutoCancel(false);
        //禁止滑动删除
        builder.setOngoing(true);
        //右上角的时间显示
        builder.setShowWhen(true);
        //设置通知栏的标题内容
        builder.setContentTitle("欢迎使用" + getResources().getString(R.string.app_name));
        builder.setContentText(content);
        //创建通知
        return builder.build();
    }
}
