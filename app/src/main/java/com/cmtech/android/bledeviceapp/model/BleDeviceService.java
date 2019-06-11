package com.cmtech.android.bledeviceapp.model;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.ble.extend.BleDeviceBasicInfo;
import com.cmtech.android.ble.extend.OnBleDeviceListener;
import com.cmtech.android.ble.utils.BleUtil;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 *  BleDeviceService: BleDevice服务
 *  Created by bme on 2018/12/09.
 */

public class BleDeviceService extends Service implements OnBleDeviceListener {
    private final static String TAG = "BleDeviceService";

    private final int WARN_TIME_INTERVAL = 5000;

    private String notiTitle;
    private final static String NOTIFICATION_CONTENT_TEXT = "当前设备状态";

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

    private NotificationCompat.Builder notificationBuilder;

    private Ringtone warnRingtone;

    private class DisconnectWarnTask extends TimerTask {
        @Override
        public void run() {
            if(!warnRingtone.isPlaying()) {
                warnRingtone.play();
            }
        }
    }
    private Timer disconnectWarnTimer;

    @Override
    public void onCreate() {
        super.onCreate();

        deviceManager = new BleDeviceManager();
        initDeviceFromPref(PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()));

        notiTitle = "欢迎使用" + getResources().getString(R.string.app_name);

        warnRingtone = RingtoneManager.getRingtone(this, Settings.System.DEFAULT_NOTIFICATION_URI);

        initNotificationBuilder();

        startForeground(SERVICE_NOTIFICATION_ID, createNotification(Arrays.asList(new String[]{NOTIFICATION_CONTENT_TEXT})));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        ViseLog.e(TAG, " onDestroy");
        super.onDestroy();

        for(final BleDevice device : getDeviceList()) {
            device.close();

            device.removeConnectStateListener(BleDeviceService.this);
        }

        stopForeground(true);

        stopWarnRingtone();

        UserManager.getInstance().signOut();

        BleUtil.disconnectAllDevice();
        BleUtil.clearAllDevice();

        ViseLog.e("killProcess");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onConnectStateUpdated(final BleDevice device) {
        List<String> info = new ArrayList<>();
        for(BleDevice dev : deviceManager.getDeviceList()) {
            if(!dev.isClosed()) {
                info.add(dev.getMacAddress() + ": " + dev.getConnectStateDescription());
            }
        }

        if(info.size() == 0) {
            info.add("无设备打开");
        }

        sendNotification(info);

        //ViseLog.e(TAG + device.getConnectState().getDescription() + Arrays.toStringWithSampleRate(info.toArray()));
    }

    @Override
    public void onReconnectFailureNotified(BleDevice device, boolean warn) {
        if(warn) {
            playWarnRingtone();
        } else {
            stopWarnRingtone();
        }
    }

    @Override
    public void onBatteryUpdated(BleDevice device) {

    }

    // 创建并添加一个设备
    public BleDevice createAndAddDevice(BleDeviceBasicInfo basicInfo) {
        BleDevice device = deviceManager.createAndAddDevice(basicInfo);
        if(device != null) {
            device.addConnectStateListener(this);
        }
        return device;
    }

    // 创建添加多个设备
    public void createAndAddDevice(List<BleDeviceBasicInfo> basicInfoList) {
        if(basicInfoList == null) return;
        for(BleDeviceBasicInfo basicInfo : basicInfoList) {
            createAndAddDevice(basicInfo);
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
            //device.setConnectState(BleDeviceConnectState.CONNECT_CLOSED);
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

    // 播放报警声音
    private void playWarnRingtone() {
        if(disconnectWarnTimer == null) {
            disconnectWarnTimer = new Timer();
            disconnectWarnTimer.scheduleAtFixedRate(new BleDeviceService.DisconnectWarnTask(), 0, WARN_TIME_INTERVAL);
        }
    }

    public void stopWarnRingtone() {
        if(disconnectWarnTimer != null) {
            disconnectWarnTimer.cancel();
            disconnectWarnTimer = null;
            if(warnRingtone.isPlaying()) {
                warnRingtone.stop();
            }
        }
    }

    // 从Preference获取所有设备信息，并构造相应的BLEDevice
    private void initDeviceFromPref(SharedPreferences pref) {
        List<BleDeviceBasicInfo> basicInfoList = BleDeviceBasicInfo.createAllFromPref(pref);
        createAndAddDevice(basicInfoList);
    }

    private void initNotificationBuilder() {
        notificationBuilder = new NotificationCompat.Builder(this, "default");
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
        notificationBuilder.setContentTitle(notiTitle);
        notificationBuilder.setContentText(NOTIFICATION_CONTENT_TEXT);

        Intent startMainActivity = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, startMainActivity, 0);
        notificationBuilder.setContentIntent(pi);
    }

    private void sendNotification(List<String> contents) {
        Notification notification = createNotification(contents);
        if(notification != null) {
            startForeground(SERVICE_NOTIFICATION_ID, notification);
        }
    }

    private Notification createNotification(List<String> contents){
        if(contents == null || contents.size() <= 0) return null;

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(notiTitle);
        for(String content : contents) {
            inboxStyle.addLine(content);
        }
        notificationBuilder.setStyle(inboxStyle);

        Notification notification = notificationBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        //创建通知
        return notification;
    }
}
