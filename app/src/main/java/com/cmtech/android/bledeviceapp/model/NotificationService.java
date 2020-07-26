package com.cmtech.android.bledeviceapp.model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.SplashActivity;
import com.vise.log.ViseLog;

import java.util.Timer;


/**
 * ClassName:      NotificationService
 * Description:    通知服务
 * Author:         chenm
 * CreateDate:     2018-12-09 07:02
 * UpdateUser:     chenm
 * UpdateDate:     2018-12-09 07:02
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public class NotificationService extends Service implements IDevice.OnCommonDeviceListener {
    private static final String TAG = "NotifyService";
    private static final int NOTIFY_ID = 1;
    private final NotificationServiceBinder binder = new NotificationServiceBinder();
    private String notifyTitle; // 通知栏标题
    private NotificationCompat.Builder notifyBuilder;

   @Override
    public void onCreate() {
        super.onCreate();
        ViseLog.e("notifyservice.onCreate");

        notifyTitle = getString(R.string.welcome_text_format, getString(R.string.app_name));

        initNotificationBuilder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ViseLog.e("notifyservice.onStartCommand");
        sendNotification("");
        return START_STICKY;
    }

    private void initNotificationBuilder() {
        notifyBuilder = new NotificationCompat.Builder(this, "default");
        notifyBuilder.setSmallIcon(R.mipmap.ic_kang); //设置状态栏的通知图标
        notifyBuilder.setAutoCancel(false); //禁止用户点击删除按钮删除
        notifyBuilder.setOngoing(true); //禁止滑动删除
        notifyBuilder.setShowWhen(false);
        notifyBuilder.setContentTitle(notifyTitle); //设置通知栏的标题与内容

        Intent intent = new Intent(this, SplashActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        notifyBuilder.setContentIntent(pi);

        //适配8.0service
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel("default", "default_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    private void sendNotification(String notifyContent) {
        ViseLog.e("receive a notification.");

        notifyBuilder.setContentText(notifyContent);
        Notification notification = notifyBuilder.build();
        startForeground(NOTIFY_ID, notification);

    }

    @Override
    public void onDestroy() {
        ViseLog.e("NotifyService.onDestroy()");
        super.onDestroy();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onConnectStateUpdated(final IDevice device) {
        sendNotification(getDeviceSimpleName(device) + device.getConnectState().getDescription());
    }

    @Override
    public void onBatteryLevelUpdated(IDevice device) {
    }

    @Override
    public void onNotificationInfoUpdated(IDevice device) {
        sendNotification(getDeviceSimpleName(device) + device.getNotificationInfo());
    }

    public class NotificationServiceBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }

    private String getDeviceSimpleName(IDevice device) {
        return device.getName() + "(" + device.getAddress().substring(device.getAddress().length()-5) + "):";
    }
}
