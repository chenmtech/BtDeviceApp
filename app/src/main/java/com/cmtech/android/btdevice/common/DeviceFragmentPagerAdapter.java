package com.cmtech.android.btdevice.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import java.util.List;
import java.util.Map;

/**
 * Created by bme on 2018/2/27.
 */

public class DeviceFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<ConfiguredDevice> list;

    public DeviceFragmentPagerAdapter(FragmentManager fm, List<ConfiguredDevice> list) {
        super(fm);
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        // 创建fragment
        DeviceFragment fragment = DeviceFragmentFactory.build(list.get(position));
        // 获取对应的device
        ConfiguredDevice device = list.get(position);
        // 将fragment设置到device
        device.setFragment(fragment);
        // 将fragment注册为device的观察者
        device.registerDeviceObserver(fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return list.get(position).getNickName();
    }
}
