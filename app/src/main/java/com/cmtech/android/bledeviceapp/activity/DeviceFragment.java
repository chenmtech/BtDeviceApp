package com.cmtech.android.bledeviceapp.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledeviceapp.model.DeviceManager;

/**
 * DeviceFragment：设备Fragment
 * Created by bme on 2018/2/27.
 */

public abstract class DeviceFragment extends Fragment{
    private static final String TAG = "DeviceFragment";
    private static final String ARG_DEVICE_MAC = "device_mac";

    private IDevice device; // 设备

    protected DeviceFragment() {
    }

    public static DeviceFragment create(String macAddress, Class<? extends DeviceFragment> fragClass) {
        try {
            DeviceFragment fragment = fragClass.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(ARG_DEVICE_MAC, macAddress);
            fragment.setArguments(bundle);
            return fragment;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public IDevice getDevice() {
        return device;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof MainActivity)) {
            throw new IllegalArgumentException("The context is not a instance of MainActivity.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 用macAddress获取BleDevice
        Bundle bundle = getArguments();
        if(bundle == null) throw new IllegalStateException();
        String deviceMac = bundle.getString(ARG_DEVICE_MAC);
        device = DeviceManager.findDevice(deviceMac);
        if(device == null) throw new IllegalStateException("The device of the fragment doesn't exist.");

        // 更新状态
        updateState();

        // 注册设备状态观察者
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null) {
            device.addListener(activity);
            device.updateState();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // 切换状态
    public void switchState() {
        device.switchState();
    }

    // 更新状态
    public void updateState() {
        // isAdded()用来判断Fragment是否与Activity关联，如果关联了，才能更新状态信息
        if(device != null && isAdded()) {

        }
    }

    // 关闭
    public void close() {
        if(device != null && device.isDisconnectedForever()) {
            device.close();

            if(getActivity() != null) {
                ((MainActivity) getActivity()).removeFragment(this);
            }
        }
    }

    // 打开配置Activity
    public abstract void openConfigureActivity();

}
