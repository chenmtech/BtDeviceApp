package com.cmtech.android.bledeviceapp.model;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.bledeviceapp.activity.BleFragment;

import java.util.List;

/**
 *
 * ClassName:      BleFragTabManager
 * Description:    BleFragment和TabLayout管理器
 * Author:         chenm
 * CreateDate:     2018-12-08 07:02
 * UpdateUser:     chenm
 * UpdateDate:     2018-12-08 07:02
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public class BleFragTabManager extends FragTabManager {

    public BleFragTabManager(FragmentManager fragmentManager, TabLayout tabLayout, int containerId) {
        this(fragmentManager, tabLayout, containerId, false);
    }

    public BleFragTabManager(FragmentManager fragmentManager, TabLayout tabLayout, int containerId, boolean isShowTabText) {
        super(fragmentManager, tabLayout, containerId, isShowTabText);
    }

    // 寻找设备的Fragment
    public BleFragment findFragment(AbstractDevice device) {
        if(device != null) {
            List<Fragment> fragments = getFragmentList();
            for (Fragment fragment : fragments) {
                if (device.equals(((BleFragment) fragment).getDevice())) {
                    return (BleFragment) fragment;
                }
            }
        }
        return null;
    }

    // 设备的Fragment是否打开
    public boolean isFragmentOpened(AbstractDevice device) {
        return (findFragment(device) != null);
    }

    // 设备的Fragment是否被选中
    public boolean isFragmentSelected(AbstractDevice device) {
        Fragment fragment = findFragment(device);
        return (fragment != null && fragment == getCurrentFragment());
    }
}
