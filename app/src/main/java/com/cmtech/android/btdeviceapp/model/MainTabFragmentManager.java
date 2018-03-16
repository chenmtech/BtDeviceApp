package com.cmtech.android.btdeviceapp.model;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenm on 2018/3/16.
 */

public class MainTabFragmentManager {

    private final MyFragmentManager fragManager;

    private final TabLayout tabLayout;

    private int curPos = -1;

    public MainTabFragmentManager(FragmentActivity activity, TabLayout tabLayout, int containerId) {
        fragManager = new MyFragmentManager(activity, containerId);
        this.tabLayout = tabLayout;
        init();
    }

    private void init() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if(pos < 0) return;

                if(curPos >= 0 && curPos != pos) fragManager.hideFragment(fragManager.fragments.get(curPos));
                fragManager.showFragment(fragManager.fragments.get(pos));
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
        return fragManager.fragments.size();
    }

    public void addDeviceFragment(MyBluetoothDevice device) {
        DeviceFragment fragment = device.getFragment();
        if(fragment == null || fragManager.fragments.contains(fragment)) return;

        fragManager.addFragment(fragment, "");
        tabLayout.addTab(tabLayout.newTab().setText(device.getNickName()).setIcon(device.getIcon()), true);
    }


    public void updateTabInfo(MyBluetoothDevice device) {
        DeviceFragment fragment = device.getFragment();
        if(fragment == null || !fragManager.fragments.contains(fragment)) return;
        TabLayout.Tab tab = tabLayout.getTabAt(fragManager.fragments.indexOf(fragment));
        if(tab != null) tab.setText(device.getNickName()).setIcon(device.getIcon());
    }


    public void deleteFragment(DeviceFragment fragment) {
        if(fragment == null || !fragManager.fragments.contains(fragment)) return;

        int index = fragManager.fragments.indexOf(fragment);
        tabLayout.removeTab(tabLayout.getTabAt(index));
        fragManager.removeFragment(fragment);

        int size = fragManager.size();
        if(size > 0) {
            fragManager.showFragment(fragManager.fragments.get(size-1));
            curPos = size-1;
        }
    }

    public void showDeviceFragment(MyBluetoothDevice device) {
        DeviceFragment fragment = device.getFragment();
        if(fragment == null || !fragManager.fragments.contains(fragment)) return;

        int index = fragManager.fragments.indexOf(fragment);
        tabLayout.getTabAt(index).select();
    }

    private static class MyFragmentManager {

        private FragmentActivity context;

        private FragmentManager manager;

        private int containerId;

        private List<Fragment> fragments;

        public MyFragmentManager(FragmentActivity context, int containerId) {
            super();
            this.context = context;
            this.containerId = containerId;
            manager = this.context.getSupportFragmentManager();
            fragments = new ArrayList<>();
        }

        public int size() { return fragments.size(); }

        public void addFragment(Fragment fragment, String tag) {
            fragments.add(fragment);
            FragmentTransaction fTransaction = manager.beginTransaction();
            fTransaction.add(containerId, fragment, tag);
            fTransaction.commit();
        }

        public void removeFragment(Fragment fragment) {
            if (fragment != null) {
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.remove(fragment);
                transaction.commit();
                fragments.remove(fragment);
            }
        }

        public void hideFragment(Fragment fragment) {
            if (fragment != null) {
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.hide(fragment);
                transaction.commit();
            }
        }

        public void showFragment(Fragment fragment) {
            if (fragment != null) {
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.show(fragment);
                transaction.commit();
            }
        }

        public List<Fragment> getFragments() {
            return fragments;
        }

    }

}
