package com.cmtech.android.bledevicecore.model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by bme on 2018/2/27.
 */

public abstract class BleDeviceFragment extends Fragment{
    private static final String TAG = "BleDeviceFragment";

    // IBleDeviceFragmentActivity，包含Fragment的Activity
    private IBleDeviceFragmentActivity activity;

    // 对应的设备
    private BleDevice device;
    public BleDevice getDevice() {
        return device;
    }

    public BleDeviceFragment() {

    }

    // 将BleDevice添加到BleDeviceFragment中
    public static BleDeviceFragment addDeviceToFragment(BleDeviceFragment fragment, BleDevice device) {
        Bundle bundle = new Bundle();
        bundle.putString("device_mac", device.getMacAddress());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 获取BleDevice信息
        Bundle bundle = getArguments();
        if(bundle == null) throw new IllegalStateException();
        String deviceMac = bundle.getString("device_mac");
        device = activity.getDeviceByMac(deviceMac);
        if(device == null) throw new IllegalArgumentException();

        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof IBleDeviceFragmentActivity)) {
            throw new IllegalArgumentException();
        }

        // 获得activity
        activity = (IBleDeviceFragmentActivity) context;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 更新连接状态
        updateDeviceState();

        // 打开设备
        openDevice();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        device.close();
    }

    // 打开设备
    public void openDevice() {
        device.open();
    }

    // 关闭设备
    // 为什么这里不是调用controller.closeDevice()，而是调用activity.closeDevice(fragment)???
    // 因为关闭一个带Fragment的设备，除了要关闭设备本身以外，还要销毁它的Fragment，并将设备的控制器从控制器列表中删除
    // 这些动作需要调用activity.closeDevice才能完成
    // 关闭设备的动作会在销毁Fragment时触发onDestroy()，那里会调用controller.closeDevice()来关闭设备
    public void closeDevice() {
        activity.closeDevice(this);
    }

    // 切换设备状态，根据设备的当前状态实现状态切换
    public void switchState() {
        device.switchState();
    }

    // 更新设备连接状态
    public void updateDeviceState(final BleDevice device) {
        // isAdded()用来判断Fragment是否与Activity关联，如果关联了，才能更新状态信息
        if(device == this.device && isAdded()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    updateDeviceState();
                }
            });
        }
    }

    private void updateDeviceState() {

    }

    //////////////////////////////////////////////////////////////////////////

}
