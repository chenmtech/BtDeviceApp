package com.cmtech.android.bledeviceapp.model;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.bledeviceapp.activity.BleDeviceFragment;

import java.util.List;



public class BleDeviceFragmentManager extends MyFragmentManager {

    public BleDeviceFragmentManager(android.support.v4.app.FragmentManager fragmentManager, TabLayout tabLayout, int containerId) {
        super(fragmentManager, tabLayout, containerId);
    }

    // 寻找设备对应的Fragment
    public BleDeviceFragment findFragment(BleDevice device) {
        if(device == null) return null;

        List<Fragment> fragmentList = getFragmentList();
        for(Fragment fragment : fragmentList) {
            if(device.equals(((BleDeviceFragment)fragment).getDevice())) {
                return (BleDeviceFragment)fragment;
            }
        }
        return null;
    }

    // 设备的Fragment是否打开
    public boolean isDeviceFragmentOpened(BleDevice device) {
        return (findFragment(device) != null);
    }

    // 设备的Fragment是否被选中
    public boolean isDeviceFragmentSelected(BleDevice device) {
        Fragment fragment = findFragment(device);

        if(fragment == null) return false;

        return (fragment == getCurrentFragment());
    }
}
