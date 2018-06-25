package com.cmtech.android.btdeviceapp.model;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理MainActivity中的TabLayout和Fragment
 * Created by chenm on 2018/3/16.
 */

public class MainTabFragmentManager {
    private MainActivity activity;

    // Fragment管理器实例
    private final MyFragmentManager fragManager;

    // TabLayout实例
    private final TabLayout tabLayout;

    // 当前显示的Fragment和Tab的位置
    private int curPos = -1;

    public MainTabFragmentManager(FragmentActivity activity, TabLayout tabLayout, int containerId) {
        this.activity = (MainActivity) activity;
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

                // 隐藏当前的Fragment
                if(curPos >= 0 && curPos != pos) fragManager.hideFragment(fragManager.fragments.get(curPos));

                // 显示选中的Fragment
                fragManager.showFragment(fragManager.fragments.get(pos));
                activity.setTitle(tab.getText());

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

    // Fragment数量
    public int size() {
        return fragManager.fragments.size();
    }

    // 添加设备的Fragment，并显示
    public void addFragment(BLEDeviceModel device, BLEDeviceFragment fragment) {
        if(fragment == null || fragManager.fragments.contains(fragment)) return;

        fragManager.addFragment(fragment, "");
        Drawable drawable = null;

        String imagePath = device.getImagePath();
        if(imagePath != null && !"".equals(imagePath)) {
            drawable = new BitmapDrawable(MyApplication.getContext().getResources(), device.getImagePath());
        } else {
            drawable = MyApplication.getContext().getResources().getDrawable(BLEDeviceType.fromUuid(device.getUuidString()).getImage());
        }
        tabLayout.addTab(tabLayout.newTab().setText(device.getNickName()).setIcon(drawable), true);
    }

    // 更新设备的Tab信息
    public void updateTabInfo(BLEDeviceFragment fragment) {
        if(fragment == null || !fragManager.fragments.contains(fragment)) return;

        BLEDeviceModel device = fragment.getDevice();

        TabLayout.Tab tab = tabLayout.getTabAt(fragManager.fragments.indexOf(fragment));
        if(tab != null) {
            tab.setText(device.getNickName());
            String imagePath = device.getImagePath();
            if(imagePath != null && !"".equals(imagePath)) {
                Drawable drawable = new BitmapDrawable(MyApplication.getContext().getResources(), imagePath);
                tab.setIcon(drawable);
            }
        }
    }

    // 删除Fragment
    public void deleteFragment(BLEDeviceFragment fragment) {
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

    // 显示设备的Fragment
    public void showDeviceFragment(BLEDeviceFragment fragment) {
        if(fragment == null || !fragManager.fragments.contains(fragment)) return;

        int index = fragManager.fragments.indexOf(fragment);
        tabLayout.getTabAt(index).select();
    }

    private static class MyFragmentManager {

        //private FragmentActivity context;

        private FragmentManager manager;

        private int containerId;

        private List<Fragment> fragments;

        public MyFragmentManager(FragmentActivity context, int containerId) {
            super();
            //this.context = context;
            this.containerId = containerId;
            manager = context.getSupportFragmentManager();
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

        /*public List<Fragment> getFragments() {
            return fragments;
        }*/

    }

}
