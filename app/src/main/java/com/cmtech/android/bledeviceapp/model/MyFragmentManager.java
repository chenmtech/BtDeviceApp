package com.cmtech.android.bledeviceapp.model;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.BleDeviceType;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment和TabLayout管理器
 * Created by chenm on 2018/3/16.
 */

public class MyFragmentManager {

    // fragment改变监听器接口
    public interface OnFragmentChangedListener {
        void onFragmentchanged(Fragment fragment);
    }
    // fragment改变监听器
    private OnFragmentChangedListener listener = null;
    // 设置监听器
    public void setOnFragmentChangedListener(OnFragmentChangedListener listener) {
        this.listener = listener;
    }

    // Fragment内部管理器实例
    private final InnerFragmentManager fragManager;

    // TabLayout实例
    private final TabLayout tabLayout;

    // 当前显示的Fragment和Tab的位置
    private int curPos = -1;



    // 构造器
    public MyFragmentManager(android.support.v4.app.FragmentManager fragmentManager, TabLayout tabLayout, int containerId) {
        fragManager = new InnerFragmentManager(fragmentManager, containerId);
        this.tabLayout = tabLayout;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if(pos < 0) return;

                // 隐藏当前的Fragment
                if(curPos >= 0 && curPos < size() && curPos != pos) fragManager.hideFragment(fragManager.getFragment(curPos));

                // 显示选中的Fragment
                fragManager.showFragment(fragManager.getFragment(pos));

                curPos = pos;

                if(listener != null) {
                    listener.onFragmentchanged(fragManager.getFragment(pos));
                }
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

    // 添加Fragment，并显示
    public void addFragment(Fragment fragment, String tabText) {
        if(fragment == null || fragManager.fragments.contains(fragment)) return;

        fragManager.addFragment(fragment, "");

        TabLayout.Tab tab = tabLayout.newTab();

        tabLayout.addTab(tab.setText(tabText), true);
    }

    // 添加Fragment，并显示
    public void addFragment(Fragment fragment, String tabImagePath, String tabText) {
        if(fragment == null || fragManager.fragments.contains(fragment)) return;

        fragManager.addFragment(fragment, "");

        TabLayout.Tab tab = tabLayout.newTab();

        Drawable drawable = null;
        if(tabImagePath != null && !"".equals(tabImagePath)) {
            drawable = new BitmapDrawable(MyApplication.getContext().getResources(), tabImagePath);
        } else {
            drawable = MyApplication.getContext().getResources().getDrawable(R.mipmap.ic_device_default_icon);
        }
        tabLayout.addTab(tab.setText(tabText).setIcon(drawable), true);

        tabLayout.addTab(tab.setText(tabText), true);
    }

    // 获取当前fragment
    public Fragment getCurrentFragment() {
        return fragManager.getFragment(curPos);
    }

    // 获取当前tab
    public TabLayout.Tab getCurrentTab() {
        return tabLayout.getTabAt(curPos);
    }

    // 更新tab
    public void updateTabInfo(Fragment fragment, String tabImagePath, String tabText) {
        if(fragment == null || !fragManager.fragments.contains(fragment)) return;

        TabLayout.Tab tab = tabLayout.getTabAt(fragManager.fragments.indexOf(fragment));
        if(tab != null) {
            tab.setText(tabText);

            if(tabImagePath != null && !"".equals(tabImagePath)) {
                Drawable drawable = new BitmapDrawable(MyApplication.getContext().getResources(), tabImagePath);
                tab.setIcon(drawable);
            }
        }
    }

    // 删除Fragment
    public void deleteFragment(Fragment fragment) {
        if(fragment == null || !fragManager.fragments.contains(fragment)) return;

        fragManager.removeFragment(fragment);
        int index = fragManager.fragments.indexOf(fragment);
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        if(tab != null)
            tabLayout.removeTab(tab);
    }

    // 显示Fragment
    public void showFragment(Fragment fragment) {
        if(fragment == null || !fragManager.fragments.contains(fragment)) return;

        int index = fragManager.fragments.indexOf(fragment);
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        if(tab != null)
            tab.select();
    }

    public List<Fragment> getFragmentList() {
        return fragManager.fragments;
    }

    private static class InnerFragmentManager {

        private android.support.v4.app.FragmentManager fragmentManager;

        private int containerId;

        private List<Fragment> fragments = new ArrayList<>();

        InnerFragmentManager(android.support.v4.app.FragmentManager fragmentManager, int containerId) {
            this.fragmentManager = fragmentManager;
            this.containerId = containerId;
        }

        public int size() { return fragments.size(); }

        void addFragment(Fragment fragment, String tag) {
            if(fragment != null) {
                fragments.add(fragment);

                FragmentTransaction fTransaction = fragmentManager.beginTransaction();
                fTransaction.add(containerId, fragment, tag);
                fTransaction.commit();
            }
        }

        void removeFragment(Fragment fragment) {
            if (fragment != null) {
                fragments.remove(fragment);

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.remove(fragment);
                transaction.commit();
            }
        }

        void hideFragment(Fragment fragment) {
            if (fragment != null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(fragment);
                transaction.commit();
            }
        }

        void showFragment(Fragment fragment) {
            if (fragment != null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.show(fragment);
                transaction.commit();
            }
        }

        Fragment getFragment(int pos) {
            return (pos >= 0 && pos < fragments.size()) ? fragments.get(pos) : null;
        }
    }
}
