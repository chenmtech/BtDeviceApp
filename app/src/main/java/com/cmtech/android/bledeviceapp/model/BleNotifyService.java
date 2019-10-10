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

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 *
 * ClassName:      BleNotifyService
 * Description:    通知服务
 * Author:         chenm
 * CreateDate:     2018-12-09 07:02
 * UpdateUser:     chenm
 * UpdateDate:     2018-12-09 07:02
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public class BleNotifyService extends Service implements BleDevice.OnBleDeviceUpdatedListener {
    private final static String TAG = "BleNotifyService";
    private final static int NOTIFY_INTERVAL_BECAUSE_BLE_ERROR = 5000; // 单位：ms
    private final static int NOTIFY_TIMES_BECAUSE_BLE_ERROR = 5;
    private final static String NOTIFY_STRING_WHEN_NO_DEVICE_OPEN = "无设备打开。";
    private final static int NOTIFY_ID = 0x0001; // id不可设置为0,否则不能设置为前台service
    private final static String NOTIFY_TITLE = "欢迎使用" + MyApplication.getContext().getString(R.string.app_name); // 通知栏标题
    private final static Ringtone WARN_RINGTONE = RingtoneManager.getRingtone(MyApplication.getContext(), Settings.System.DEFAULT_NOTIFICATION_URI); // 报警铃声
    private Timer bleErrorWarnTimer; // BLE Error报警定时器
    private NotificationCompat.Builder notifyBuilder;
    private final BleNotifyServiceBinder binder = new BleNotifyServiceBinder();

    public class BleNotifyServiceBinder extends Binder {
        public BleNotifyService getService() {
            return BleNotifyService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initDeviceManager(PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()));
        for(BleDevice device : BleDeviceManager.getDeviceList()) {
            device.addListener(this);
        }

        initNotificationBuilder();
        sendNotification();
    }

    // 初始化BleDeviceManager: 从Preference获取所有设备注册信息，并构造相应的设备
    private void initDeviceManager(SharedPreferences pref) {
        List<BleDeviceRegisterInfo> registerInfoList = BleDeviceRegisterInfo.createFromPref(pref);
        if(registerInfoList == null || registerInfoList.isEmpty()) return;
        for(BleDeviceRegisterInfo registerInfo : registerInfoList) {
           BleDeviceManager.createDeviceIfNotExist(this, registerInfo);
        }
    }

    private void initNotificationBuilder() {
        notifyBuilder = new NotificationCompat.Builder(this, "default");
        notifyBuilder.setSmallIcon(R.mipmap.ic_kang); //设置状态栏的通知图标
        notifyBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_kang)); //设置通知栏横条的图标
        notifyBuilder.setAutoCancel(false); //禁止用户点击删除按钮删除
        notifyBuilder.setOngoing(true); //禁止滑动删除
        notifyBuilder.setShowWhen(true); //右上角的时间显示
        notifyBuilder.setContentTitle(NOTIFY_TITLE); //设置通知栏的标题与内容

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        notifyBuilder.setContentIntent(pi);
    }

    private void sendNotification() {
        List<String> notifyContents = new ArrayList<>();
        for(BleDevice dev : BleDeviceManager.getDeviceList()) {
            if(!dev.isClosed()) {
                notifyContents.add(dev.getMacAddress() + ": " + dev.getStateDescription());
            }
        }
        Notification notification = createNotification(notifyContents);
        if(notification != null) {
            startForeground(NOTIFY_ID, notification);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        ViseLog.e("BleNotifyService.onDestroy()");
        super.onDestroy();

        for(final BleDevice device : BleDeviceManager.getDeviceList()) {
            if(device.getBleGatt() != null) {
                device.getBleGatt().clear();
            }
            //device.close();
            //device.removeListener(BleNotifyService.this);
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
        sendNotification();
    }

    @Override
    public void onBleErrorNotified(final BleDevice device) {
        playWarnRingtone();
    }

    // 播放报警声音
    private void playWarnRingtone() {
        if(bleErrorWarnTimer == null) {
            bleErrorWarnTimer = new Timer();
            bleErrorWarnTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(!WARN_RINGTONE.isPlaying()) {
                        WARN_RINGTONE.play();
                    }
                }
            }, 0, NOTIFY_INTERVAL_BECAUSE_BLE_ERROR);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    stopWarnRingtone();
                }
            }, NOTIFY_INTERVAL_BECAUSE_BLE_ERROR * NOTIFY_TIMES_BECAUSE_BLE_ERROR);
        }
    }

    private void stopWarnRingtone() {
        if(bleErrorWarnTimer != null) {
            bleErrorWarnTimer.cancel();
            bleErrorWarnTimer = null;
            if(WARN_RINGTONE.isPlaying()) {
                WARN_RINGTONE.stop();
            }
        }
    }

    @Override
    public void onBatteryUpdated(BleDevice device) {

    }

    private Notification createNotification(List<String> notifyContents){
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(NOTIFY_TITLE);
        if(notifyContents == null || notifyContents.isEmpty()) {
            notifyBuilder.setContentText(NOTIFY_STRING_WHEN_NO_DEVICE_OPEN);
            inboxStyle.addLine(NOTIFY_STRING_WHEN_NO_DEVICE_OPEN);
        } else {
            notifyBuilder.setContentText(String.format("有%s个设备打开", notifyContents.size()));
            for (String content : notifyContents) {
                inboxStyle.addLine(content);
            }
        }
        notifyBuilder.setStyle(inboxStyle);
        Notification notification = notifyBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        //创建通知
        return notification;
    }
}
