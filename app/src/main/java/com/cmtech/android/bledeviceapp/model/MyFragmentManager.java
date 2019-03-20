package com.cmtech.android.bledeviceapp.model;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmtech.android.bledevice.SupportedDeviceType;
import com.cmtech.android.bledevice.core.BleDeviceType;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment和TabLayout管理器
 * Created by chenm on 2018/3/16.
 */

public class MyFragmentManager {
    // fragment改变监听器
    private OnFragmentChangedListener listener = null;

    // Fragment内部管理器实例
    private final InnerFragmentManager fragManager;

    // TabLayout实例
    private final TabLayout tabLayout;

    // 当前显示的Fragment和Tab的位置
    private int curPos = -1;

    // fragment改变监听器接口
    public interface OnFragmentChangedListener {
        void onFragmentchanged();
    }

    // 构造器
    public MyFragmentManager(FragmentManager fragmentManager, TabLayout tabLayout, int containerId) {
        fragManager = new InnerFragmentManager(fragmentManager, containerId);
        this.tabLayout = tabLayout;
        init();
    }

    // 设置fragment改变监听器
    public void setOnFragmentChangedListener(OnFragmentChangedListener listener) {
        this.listener = listener;
    }

    private void init() {
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
                    listener.onFragmentchanged();
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
    public void addFragment(Fragment fragment, Drawable drawable, String tabText) {
        if(fragment == null || fragManager.fragments.contains(fragment)) return;

        fragManager.addFragment(fragment, "");

        TabLayout.Tab tab = tabLayout.newTab();
        View view = LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.tablayout_device, null);
        TextView tv = view.findViewById(R.id.tv_device_name);
        tv.setText(tabText);
        ImageView imageView = view.findViewById(R.id.iv_device_image);
        imageView.setImageDrawable(drawable);
        tab.setCustomView(view);

        tabLayout.addTab(tab, true);
    }

    // 获取当前fragment
    public Fragment getCurrentFragment() {
        return (curPos < 0 || curPos >= size()) ? null : fragManager.getFragment(curPos);
    }

    // 获取当前tab
    public TabLayout.Tab getCurrentTab() {
        return (curPos < 0 || curPos >= size() || size() == 0) ? null : tabLayout.getTabAt(curPos);
    }

    // 更新tab信息
    public void updateTabInfo(Fragment fragment, Drawable drawable, String tabText) {
        if(fragment == null || !fragManager.fragments.contains(fragment)) return;

        TabLayout.Tab tab = tabLayout.getTabAt(fragManager.fragments.indexOf(fragment));

        if(tab != null) {
            View view = tab.getCustomView();
            TextView tv = view.findViewById(R.id.tv_device_name);
            tv.setText(tabText);
            ImageView imageView = view.findViewById(R.id.iv_device_image);
            imageView.setImageDrawable(drawable);
            tab.setCustomView(view);
        }
    }

    // 删除Fragment
    public void deleteFragment(Fragment fragment) {
        if(fragment == null || !fragManager.fragments.contains(fragment)) return;

        int index = fragManager.fragments.indexOf(fragment);
        fragManager.removeFragment(fragment);
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

        private FragmentManager fragmentManager;

        private int containerId;

        private List<Fragment> fragments;

        InnerFragmentManager(FragmentManager fragmentManager, int containerId) {
            super();

            this.containerId = containerId;

            this.fragmentManager = fragmentManager;

            fragments = new ArrayList<>();
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
                ViseLog.i("removeFragment " + fragment);
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
            ViseLog.i("showFragment " + fragment);
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
