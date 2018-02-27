package com.cmtech.android.btdeviceapp.thermo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;
import com.cmtech.android.btdeviceapp.thermo.frag.ThermoFragment;

import java.util.List;

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
        return ThermoFragment.newInstance(list.get(position));
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
