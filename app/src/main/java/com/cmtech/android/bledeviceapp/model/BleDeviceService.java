package com.cmtech.android.bledeviceapp.model;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.cmtech.android.bledevicecore.BleDevice;
import com.cmtech.android.bledevicecore.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.BleDeviceConnectState;
import com.cmtech.android.bledevicecore.BleDeviceManager;
import com.cmtech.android.bledevicecore.BleDeviceUtil;
import com.cmtech.android.bledevicecore.IBleDeviceStateObserver;
import com.vise.log.ViseLog;

import java.util.List;

/**
 *  BleDeviceService: BleDevice服务
 *  Created by bme on 2018/12/09.
 */

public class BleDeviceService extends Service implements IBleDeviceStateObserver {
    private final static String TAG = "BleDeviceService";

    private final static String NODEVICE_OPENED = "无设备打开。";

    /**
     * id不可设置为0,否则不能设置为前台service
     */
    private static final int SERVICE_NOTIFICATION_ID = 0x0001;

    private BleDeviceManager deviceManager;


    public class DeviceServiceBinder extends Binder {
        public BleDeviceService getService() {
            return BleDeviceService.this;
        }
    }

    private DeviceServiceBinder binder = new DeviceServiceBinder();

    //使用兼容版本
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notifacationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        deviceManager = new BleDeviceManager();
        initDeviceFromPref();

        notificationBuilder = new NotificationCompat.Builder(this, "default");
        notifacationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notification = createNotification(NODEVICE_OPENED);

        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for(final BleDevice device : getDeviceList()) {
            device.close();
            device.setConnectState(BleDeviceConnectState.CONNECT_CLOSED);
            device.removeDeviceStateObserver(BleDeviceService.this);
        }

        // 防止设备没有彻底断开
        BleDeviceUtil.disconnectAllDevice();
        BleDeviceUtil.clearAllDevice();

        UserAccountManager.getInstance().signOut();

        //android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void updateDeviceState(final BleDevice device) {
        StringBuilder builder = new StringBuilder();
        for(BleDevice dev : deviceManager.getDeviceList()) {
            if(dev.getConnectState() != BleDeviceConnectState.CONNECT_CLOSED) {
                builder.append(dev.getMacAddress()).append(": ").append(dev.getConnectState().getDescription()).append('\n');
            }
        }
        String content = builder.toString();
        if(content.equals("")) {
            content = NODEVICE_OPENED;
        }

        sendNotification(content);

        //notification = createNotification(content);
        //startForeground(SERVICE_NOTIFICATION_ID, notification);
        ViseLog.e(TAG + device.getConnectState().getDescription());
    }

    private void sendNotification(String content) {
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(content));
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);

        Notification notification = notificationBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notifacationManager.notify(SERVICE_NOTIFICATION_ID, notification);
    }

    // 从Preference获取所有设备信息，并构造相应的BLEDevice
    private void initDeviceFromPref() {
        List<BleDeviceBasicInfo> basicInfoList = BleDeviceBasicInfo.findAllFromPreference();
        addDevice(basicInfoList);
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

    // 打开设备
    public void openDevice(final BleDevice device) {
        if(device != null) {
            device.open();
        }
    }

    // 关闭设备
    public void closeDevice(final BleDevice device) {
        if(device != null) {
            device.close();
            device.setConnectState(BleDeviceConnectState.CONNECT_CLOSED);
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

    public boolean hasDeviceOpened() {
        return deviceManager.hasDeviceOpened();
    }

    /**
     * Notification
     */
    public Notification createNotification(String content){
        //设置状态栏的通知图标
        notificationBuilder.setSmallIcon(R.mipmap.ic_kang);
        //设置通知栏横条的图标
        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_kang));
        //禁止用户点击删除按钮删除
        notificationBuilder.setAutoCancel(false);
        //禁止滑动删除
        notificationBuilder.setOngoing(true);
        //右上角的时间显示
        notificationBuilder.setShowWhen(true);
        //设置通知栏的标题内容
        notificationBuilder.setContentTitle("欢迎使用" + getResources().getString(R.string.app_name));
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content));
        Intent startMainActivity = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, startMainActivity, 0);
        notificationBuilder.setContentIntent(pi);

        Notification notification = notificationBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

        //创建通知
        return notification;
    }
}
