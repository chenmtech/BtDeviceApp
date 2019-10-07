package com.cmtech.android.bledeviceapp.model;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.bledeviceapp.activity.BleFragment;

import java.util.List;

/**
 * BleFragTabManager: BleFragment管理器
 * Created by bme on 2018/12/08.
 */

public class BleFragTabManager extends FragTabManager {

    public BleFragTabManager(FragmentManager fragmentManager, TabLayout tabLayout, int containerId) {
        this(fragmentManager, tabLayout, containerId, false);
    }

    public BleFragTabManager(FragmentManager fragmentManager, TabLayout tabLayout, int containerId, boolean isShowTabText) {
        super(fragmentManager, tabLayout, containerId, isShowTabText);
    }

    // 寻找设备对应的Fragment
    public BleFragment findFragment(BleDevice device) {
        if(device == null) return null;

        List<Fragment> fragments = getFragmentList();
        for(Fragment fragment : fragments) {
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
