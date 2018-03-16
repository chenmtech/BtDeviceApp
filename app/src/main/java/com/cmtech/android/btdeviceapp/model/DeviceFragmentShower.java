package com.cmtech.android.btdeviceapp.model;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenm on 2018/3/16.
 */

public class DeviceFragmentShower {
    FragOperManager fragManager;
    TabLayout tabLayout;
    int curPos = -1;

    public DeviceFragmentShower(FragmentActivity activity, TabLayout tabLayout, int containerId) {
        fragManager = new FragOperManager(activity, containerId);
        this.tabLayout = tabLayout;
        init();
    }

    private void init() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if(pos < 0) return;

                List<Fragment> fragments = fragManager.getFragList();
                if(curPos >= 0 && curPos != pos) fragManager.chHideFrag(fragments.get(curPos));
                fragManager.chShowFrag(fragments.get(pos));
                curPos = pos;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    public int size() {
        return fragManager.getFragList().size();
    }

    public void addFragment(MyBluetoothDevice device) {
        DeviceFragment fragment = device.getFragment();
        List<Fragment> fragments = fragManager.getFragList();
        if(fragment == null || fragments.contains(fragment)) return;

        fragManager.chAddFrag(fragment, "", false);
        tabLayout.addTab(tabLayout.newTab().setText(device.getNickName()).setIcon(device.getIcon()));

        //if(curPos >= 0) fragManager.chHideFrag(fragments.get(curPos));
        //fragManager.chShowFrag(fragment);

        tabLayout.getTabAt(tabLayout.getTabCount()-1).select();
    }


    public void updateTabInfo(MyBluetoothDevice device) {
        DeviceFragment fragment = device.getFragment();
        List<Fragment> fragments = fragManager.getFragList();
        if(fragment == null || !fragments.contains(fragment)) return;
        tabLayout.getTabAt(fragments.indexOf(fragment)).setText(device.getNickName());
        tabLayout.getTabAt(fragments.indexOf(fragment)).setIcon(device.getIcon());
    }

    public void deleteFragment(DeviceFragment fragment) {
        List<Fragment> fragments = fragManager.getFragList();
        if(fragment == null || !fragments.contains(fragment)) return;

        int index = fragments.indexOf(fragment);
        tabLayout.removeTab(tabLayout.getTabAt(index));
        fragManager.chRemoveFrag(fragment);


        if(tabLayout.getTabCount() > 0) {
            //tabLayout.getTabAt(tabLayout.getTabCount()-1).select();
            fragManager.chShowFrag(fragments.get(tabLayout.getTabCount()-1));
        }
    }

    public void selectFragment(MyBluetoothDevice device) {

    }

}
