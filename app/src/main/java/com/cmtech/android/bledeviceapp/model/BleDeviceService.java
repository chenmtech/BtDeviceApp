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
import java.util.Collections;
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
    private final static List<String> NOTIFY_NO_DEVICE_OPEN = Collections.singletonList("无设备打开。");

    private final static int SERVICE_NOTIFICATION_ID = 0x0001; // id不可设置为0,否则不能设置为前台service

    private String notifyTitle; // 通知栏标题
    private NotificationCompat.Builder notifBuilder;
    private Ringtone warnRingtone;

    public class DeviceServiceBinder extends Binder {
        public BleDeviceService getService() {
            return BleDeviceService.this;
        }
    }
    private final DeviceServiceBinder binder = new DeviceServiceBinder();

    private Timer bleErrorWarnTimer;

    @Override
    public void onCreate() {
        super.onCreate();

        initDeviceFromPref(PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()));

        notifyTitle = "欢迎使用" + getResources().getString(R.string.app_name);
        warnRingtone = RingtoneManager.getRingtone(this, Settings.System.DEFAULT_NOTIFICATION_URI);
        initNotificationBuilder();
        sendNotification(NOTIFY_NO_DEVICE_OPEN);
    }

    // 从Preference获取所有设备注册信息，并构造相应的设备
    private void initDeviceFromPref(SharedPreferences pref) {
        List<BleDeviceRegisterInfo> registerInfoList = BleDeviceRegisterInfo.createFromPref(pref);
        if(registerInfoList == null) return;
        for(BleDeviceRegisterInfo registerInfo : registerInfoList) {
            BleDevice device = BleDeviceManager.createDeviceIfNotExist(this, registerInfo);
            if(device != null) {
                device.addDeviceStateListener(this);
            }
        }
    }

    private void initNotificationBuilder() {
        notifBuilder = new NotificationCompat.Builder(this, "default");
        //设置状态栏的通知图标
        notifBuilder.setSmallIcon(R.mipmap.ic_kang);
        //设置通知栏横条的图标
        notifBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_kang));
        //禁止用户点击删除按钮删除
        notifBuilder.setAutoCancel(false);
        //禁止滑动删除
        notifBuilder.setOngoing(true);
        //右上角的时间显示
        notifBuilder.setShowWhen(true);
        //设置通知栏的标题内容
        notifBuilder.setContentTitle(notifyTitle);
        notifBuilder.setContentText(NOTIFY_NO_DEVICE_OPEN.get(0));

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        notifBuilder.setContentIntent(pi);
    }

    private void sendNotification(List<String> contents) {
        Notification notification = createNotification(contents);
        if(notification != null) {
            startForeground(SERVICE_NOTIFICATION_ID, notification);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        ViseLog.e("BleDeviceService.onDestroy()");
        super.onDestroy();

        for(final BleDevice device : BleDeviceManager.getDeviceList()) {
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
        if(info.isEmpty()) {
            info = NOTIFY_NO_DEVICE_OPEN;
        }

        sendNotification(info);
    }

    @Override
    public void onBleErrorNotified(final BleDevice device, boolean warn) {
        if(warn) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(BleDeviceService.this);
            builder.setTitle("蓝牙错误").setMessage("设备无法连接，需要重启蓝牙。");
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

    // 播放报警声音
    private void playWarnRingtone() {
        if(bleErrorWarnTimer == null) {
            bleErrorWarnTimer = new Timer();
            bleErrorWarnTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(!warnRingtone.isPlaying()) {
                        warnRingtone.play();
                    }
                }
            }, 0, WARN_TIME_INTERVAL);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    stopWarnRingtone();
                }
            }, WARN_TIME_INTERVAL*5);
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

    @Override
    public void onBatteryUpdated(BleDevice device) {

    }

    private Notification createNotification(List<String> contents){
        if(contents == null || contents.size() <= 0) return null;

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(notifyTitle);
        for(String content : contents) {
            inboxStyle.addLine(content);
        }
        notifBuilder.setStyle(inboxStyle);

        Notification notification = notifBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        //创建通知
        return notification;
    }
}
