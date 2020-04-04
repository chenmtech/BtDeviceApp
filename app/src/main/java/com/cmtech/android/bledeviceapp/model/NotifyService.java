package com.cmtech.android.bledeviceapp.model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.cmtech.android.ble.core.BleDeviceInfo;
import com.cmtech.android.ble.core.DeviceInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.ScanException;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;


/**
 * ClassName:      NotifyService
 * Description:    通知服务
 * Author:         chenm
 * CreateDate:     2018-12-09 07:02
 * UpdateUser:     chenm
 * UpdateDate:     2018-12-09 07:02
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public class NotifyService extends Service implements IDevice.OnDeviceListener {
    private static final String TAG = "NotifyService";
    private static final int NOTIFY_ID = 0x0001; // id不可设置为0,否则不能设置为前台service
    private final BleNotifyServiceBinder binder = new BleNotifyServiceBinder();
    private String notifyTitle; // 通知栏标题
    private String noDevice; // 无设备打开时的通知串
    private NotificationCompat.Builder notifyBuilder;

    @Override
    public void onCreate() {
        super.onCreate();

        notifyTitle = getString(R.string.welcome_text_format, getString(R.string.app_name));
        noDevice = getString(R.string.no_device_opened);

        initDeviceManager();

        initNotificationBuilder();

        sendNotification();
    }

    // 初始化BleDeviceManager: 从Preference获取所有设备注册信息，并构造相应的设备
    private void initDeviceManager() {
        List<BleDeviceInfo> infos = LitePal.findAll(BleDeviceInfo.class);
        if (infos == null || infos.isEmpty()) return;
        for (DeviceInfo info : infos) {
            DeviceManager.createNewDevice(info);
        }
        DeviceManager.addListener(this);
    }

    private void initNotificationBuilder() {
        notifyBuilder = new NotificationCompat.Builder(this, "default");
        notifyBuilder.setSmallIcon(R.mipmap.ic_kang); //设置状态栏的通知图标
        notifyBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_kang)); //设置通知栏横条的图标
        notifyBuilder.setAutoCancel(false); //禁止用户点击删除按钮删除
        notifyBuilder.setOngoing(true); //禁止滑动删除
        notifyBuilder.setShowWhen(true); //右上角的时间显示
        notifyBuilder.setContentTitle(notifyTitle); //设置通知栏的标题与内容

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        notifyBuilder.setContentIntent(pi);

        //适配8.0service
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel("default", "default_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    private void sendNotification() {
        List<String> notifyContents = new ArrayList<>();
        List<IDevice> openedDevices = DeviceManager.getOpenedDevice();
        for (IDevice device : openedDevices) {
            notifyContents.add(device.getAddress() + ": " + device.getState().getDescription());
        }
        Notification notification = createNotification(notifyContents);
        if (notification != null) {
            startForeground(NOTIFY_ID, notification);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        ViseLog.e("NotifyService.onDestroy()");
        super.onDestroy();

        DeviceManager.removeListener(this);
        DeviceManager.clear();

        stopForeground(true);
        AccountManager.signOut();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViseLog.e("killProcess");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onStateUpdated(final IDevice device) {
        sendNotification();
    }

    @Override
    public void onExceptionNotified(IDevice device, BleException ex) {
        if (ex instanceof ScanException) {
            if (((ScanException) ex).getScanError() == ScanException.SCAN_ERR_BT_CLOSED) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onBatteryUpdated(IDevice device) {
    }

    private Notification createNotification(List<String> notifyContents) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(notifyTitle);
        if (notifyContents == null || notifyContents.isEmpty()) {
            notifyBuilder.setContentText(noDevice);
            inboxStyle.addLine(noDevice);
        } else {
            notifyBuilder.setContentText(getString(R.string.some_devices_opened, notifyContents.size()));
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

    public class BleNotifyServiceBinder extends Binder {
        public NotifyService getService() {
            return NotifyService.this;
        }
    }

}
