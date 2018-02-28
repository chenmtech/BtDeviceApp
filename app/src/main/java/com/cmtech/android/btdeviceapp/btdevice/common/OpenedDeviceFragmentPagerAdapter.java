package com.cmtech.android.btdeviceapp.btdevice.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;
import com.cmtech.android.btdeviceapp.btdevice.common.OpenedDeviceFragmentFactory;

import java.util.List;

/**
 * Created by bme on 2018/2/27.
 */

public class OpenedDeviceFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<ConfiguredDevice> list;

    public OpenedDeviceFragmentPagerAdapter(FragmentManager fm, List<ConfiguredDevice> list) {
        super(fm);
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        return OpenedDeviceFragmentFactory.build(list.get(position));
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
