package com.cmtech.android.bledeviceapp.model;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.ble.core.OnBleDeviceStateListener;
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

public class BleDeviceService extends Service implements OnBleDeviceStateListener {
    private final static String TAG = "BleDeviceService";
    private final static int WARN_TIME_INTERVAL = 15000;
    private final static String NOTIFICATION_CONTENT_TEXT = "当前设备状态";

    private static final int SERVICE_NOTIFICATION_ID = 0x0001; // id不可设置为0,否则不能设置为前台service

    private String notifyTitle;
    private NotificationCompat.Builder notificationBuilder;
    private Ringtone warnRingtone;

    public class DeviceServiceBinder extends Binder {
        public BleDeviceService getService() {
            return BleDeviceService.this;
        }
    }
    private final DeviceServiceBinder binder = new DeviceServiceBinder();



    private class RingTonePlayTask extends TimerTask {
        @Override
        public void run() {
            if(!warnRingtone.isPlaying()) {
                warnRingtone.play();
            }
        }
    }
    private Timer bleErrorWarnTimer;

    @Override
    public void onCreate() {
        super.onCreate();

        initDeviceFromPref(PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()));

        notifyTitle = "欢迎使用" + getResources().getString(R.string.app_name);
        warnRingtone = RingtoneManager.getRingtone(this, Settings.System.DEFAULT_NOTIFICATION_URI);
        initNotificationBuilder();
        startForeground(SERVICE_NOTIFICATION_ID, createNotification(Arrays.asList(new String[]{NOTIFICATION_CONTENT_TEXT})));
    }

    // 从Preference获取所有设备注册信息，并构造相应的设备
    private void initDeviceFromPref(SharedPreferences pref) {
        List<BleDeviceRegisterInfo> registerInfoList = BleDeviceRegisterInfo.createAllFromPref(pref);
        if(registerInfoList == null) return;
        for(BleDeviceRegisterInfo registerInfo : registerInfoList) {
            createDeviceThenListen(registerInfo);
        }
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
        notificationBuilder.setContentTitle(notifyTitle);
        notificationBuilder.setContentText(NOTIFICATION_CONTENT_TEXT);

        Intent startMainActivity = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, startMainActivity, 0);
        notificationBuilder.setContentIntent(pi);
    }

    private Notification createNotification(List<String> contents){
        if(contents == null || contents.size() <= 0) return null;

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(notifyTitle);
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

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        ViseLog.e("BleDeviceService.onDestroy()");
        super.onDestroy();

        for(final BleDevice device : getDeviceList()) {
            if(device.getBleGatt() != null) {
                device.getBleGatt().clear();
            }
            //device.close();
            //device.removeDeviceStateListener(BleDeviceService.this);
        }

        stopForeground(true);
        stopWarnRingtone();
        UserManager.getInstance().signOut();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViseLog.e("killProcess");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onConnectStateUpdated(final BleDevice device) {
        List<String> info = new ArrayList<>();
        for(BleDevice dev : BleDeviceManager.getDeviceList()) {
            if(!dev.isClosed()) {
                info.add(dev.getMacAddress() + ": " + dev.getStateDescription());
            }
        }

        if(info.size() == 0) {
            info.add("无设备打开");
        }

        sendNotification(info);
    }

    private void sendNotification(List<String> contents) {
        Notification notification = createNotification(contents);
        if(notification != null) {
            startForeground(SERVICE_NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onBleErrorNotified(final BleDevice device, boolean warn) {
        if(warn) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(BleDeviceService.this);
            builder.setTitle("蓝牙错误").setMessage("蓝牙错误导致设备无法连接，需要重启蓝牙。");
            builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    device.cancelNotifyBleError();
                }
            }).setCancelable(false).show();

            playWarnRingtone();
        } else {
            stopWarnRingtone();
        }
    }

    @Override
    public void onBatteryUpdated(BleDevice device) {

    }

    // 创建一个设备,并监听它
    public BleDevice createDeviceThenListen(BleDeviceRegisterInfo registerInfo) {
        BleDevice device = BleDeviceManager.createDeviceIfNotExist(this, registerInfo);
        if(device != null) {
            device.addDeviceStateListener(this);
        }
        return device;
    }

    // 关闭设备
    public void closeDevice(final BleDevice device) {
        if(device != null) {
            device.close();
        }
    }

    // 删除一个设备
    public void deleteDevice(BleDevice device) {
        BleDeviceManager.deleteDevice(device);
    }

    // 获取设备清单
    public List<BleDevice> getDeviceList() {
        return BleDeviceManager.getDeviceList();
    }

    // 获取设备的Mac列表
    public List<String> getDeviceMacList() {
        return BleDeviceManager.getDeviceMacList();
    }

    // 获取设备
    public BleDevice findDevice(BleDeviceRegisterInfo basicInfo) {
        return BleDeviceManager.findDevice(basicInfo);
    }

    // 获取设备
    public BleDevice findDevice(String macAddress) {
        return BleDeviceManager.findDevice(macAddress);
    }

    // 是否有设备打开
    public boolean hasDeviceOpened() {
        return BleDeviceManager.hasDeviceOpened();
    }

    // 播放报警声音
    private void playWarnRingtone() {
        if(bleErrorWarnTimer == null) {
            bleErrorWarnTimer = new Timer();
            bleErrorWarnTimer.scheduleAtFixedRate(new RingTonePlayTask(), 0, WARN_TIME_INTERVAL);
        }
    }

    public void stopWarnRingtone() {
        if(bleErrorWarnTimer != null) {
            bleErrorWarnTimer.cancel();
            bleErrorWarnTimer = null;
            if(warnRingtone.isPlaying()) {
                warnRingtone.stop();
            }
        }
    }
}
