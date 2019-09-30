package com.cmtech.android.bledeviceapp.model;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.bledeviceapp.activity.BleFragment;

import java.util.List;

/**
 * BleFragAndTabManager: Fragment管理器
 * Created by bme on 2018/12/08.
 */

public class BleFragAndTabManager extends FragAndTabManager {

    public BleFragAndTabManager(FragmentManager fragmentManager, TabLayout tabLayout, int containerId) {
        super(fragmentManager, tabLayout, containerId);
    }

    // 寻找设备对应的Fragment
    public BleFragment findFragment(BleDevice device) {
        if(device == null) return null;

        List<Fragment> fragmentList = getFragmentList();
        for(Fragment fragment : fragmentList) {
            if(device.equals(((BleFragment)fragment).getDevice())) {
                return (BleFragment)fragment;
            }
        }
        return null;
    }

    // 设备的Fragment是否打开
    public boolean isFragmentOpened(BleDevice device) {
        return (findFragment(device) != null);
    }

    // 设备的Fragment是否被选中
    public boolean isFragmentSelected(BleDevice device) {
        Fragment fragment = findFragment(device);
        if(fragment == null) return false;
        return (fragment == getCurrentFragment());
    }
}