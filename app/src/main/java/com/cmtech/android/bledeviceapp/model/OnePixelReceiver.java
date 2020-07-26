package com.cmtech.android.bledeviceapp.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cmtech.android.bledeviceapp.activity.OnePixelActivity;
import com.vise.log.ViseLog;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      OnePixelReceiver
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/7/26 上午7:40
 * UpdateUser:     更新者
 * UpdateDate:     2020/7/26 上午7:40
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class OnePixelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {    //屏幕关闭启动1像素Activity
            ViseLog.e("SCREEN OFF");
            Intent it = new Intent(context, OnePixelActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) || intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {   //屏幕打开 结束1像素
            ViseLog.e("SCREEN ON or USER PRESENT");
            context.sendBroadcast(new Intent("finish"));
        }
    }
}
