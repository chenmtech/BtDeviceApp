package com.cmtech.android.bledeviceapp.model;

import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment和TabLayout管理器
 * Created by chenm on 2018/3/16.
 */

public class FragAndTabManager {
    private static final List<Fragment> FRAGMENT_LIST = new ArrayList<>();
    private final InnerFragmentManager innerFragManager; // Fragment内部管理器实例
    private final TabLayout tabLayout; // TabLayout实例
    private final boolean isShowTabText; // 是否在Tab上显示文字
    private int curPos = -1; // 当前显示的Fragment和Tab的位置
    private OnFragmentUpdatedListener listener = null; // fragment更新监听器

    // fragment更新监听器接口
    public interface OnFragmentUpdatedListener {
        void onFragmentUpdated();
    }

    /**
     * 构造器
     * @param fragmentManager : fragment管理器
     * @param tabLayout：tabLayout
     * @param containerId: fragment容器ID
     */
    public FragAndTabManager(FragmentManager fragmentManager, TabLayout tabLayout, int containerId, boolean isShowTabText) {
        innerFragManager = new InnerFragmentManager(fragmentManager, containerId);
        this.tabLayout = tabLayout;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if(pos < 0) return;
                // 隐藏当前的Fragment
                if(curPos != pos) innerFragManager.hideFragment(getCurrentFragment());
                // 显示选中的Fragment
                innerFragManager.showFragment(getFragment(pos));
                curPos = pos;
                if(listener != null) {
                    listener.onFragmentUpdated();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        this.isShowTabText = isShowTabText;
    }

    // Fragment数量
    public static int size() {
        return FRAGMENT_LIST.size();
    }

    // 设置fragment更新监听器
    public void setOnFragmentUpdatedListener(OnFragmentUpdatedListener listener) {
        this.listener = listener;
    }

    /**
     * 添加Fragment，并显示
     * @param fragment: fragment
     * @param drawable: tablayout上的图标drawable
     * @param tabText: tablayout上的文字
     */
    public void addFragment(Fragment fragment, Drawable drawable, String tabText) {
        if(fragment == null || FRAGMENT_LIST.contains(fragment)) return;

        innerFragManager.addFragment(fragment, "");

        View view = LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.tablayout_device, null);
        ((ImageView)view.findViewById(R.id.iv_device_image)).setImageDrawable(drawable);
        ((TextView)view.findViewById(R.id.tv_device_text)).setText((isShowTabText) ? tabText : "");

        TabLayout.Tab tab = tabLayout.newTab();
        tab.setCustomView(view);
        tabLayout.addTab(tab, true);
    }

    private static Fragment getFragment(int pos) {
        return (pos >= 0 && pos < FRAGMENT_LIST.size()) ? FRAGMENT_LIST.get(pos) : null;
    }

    // 获取当前fragment
    public Fragment getCurrentFragment() {
        return getFragment(curPos);
    }

    // 获取当前tab
    public TabLayout.Tab getCurrentTab() {
        return (curPos < 0 || curPos >= size() || size() == 0) ? null : tabLayout.getTabAt(curPos);
    }

    // 更新Fragment的tab信息
    public void updateTabInfo(Fragment fragment, Drawable drawable, String tabText) {
        if(fragment == null || !FRAGMENT_LIST.contains(fragment)) return;

        TabLayout.Tab tab = tabLayout.getTabAt(FRAGMENT_LIST.indexOf(fragment));

        if(tab != null) {
            View view = tab.getCustomView();
            if(view != null) {
                if(isShowTabText) {
                    TextView tv = view.findViewById(R.id.tv_device_text);
                    tv.setText(tabText);
                }

                ImageView imageView = view.findViewById(R.id.iv_device_image);
                imageView.setImageDrawable(drawable);

                tab.setCustomView(view);
            }
        }
    }

    // 显示Fragment
    public void showFragment(Fragment fragment) {
        if(fragment == null || !FRAGMENT_LIST.contains(fragment)) return;

        int index = FRAGMENT_LIST.indexOf(fragment);
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        if(tab != null)
            tab.select();
    }

    // 删除Fragment
    public void deleteFragment(Fragment fragment) {
        if(fragment == null || !FRAGMENT_LIST.contains(fragment)) return;

        int index = FRAGMENT_LIST.indexOf(fragment);
        innerFragManager.removeFragment(fragment);
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        if(tab != null)
            tabLayout.removeTab(tab);

        if(size() == 0 && listener != null) {
            listener.onFragmentUpdated();
        }
    }

    public static List<Fragment> getFragmentList() {
        return FRAGMENT_LIST;
    }

    private static class InnerFragmentManager {
        private final FragmentManager fragmentManager;
        private final int containerId;

        InnerFragmentManager(FragmentManager fragmentManager, int containerId) {
            super();
            this.fragmentManager = fragmentManager;
            this.containerId = containerId;
        }

        void addFragment(Fragment fragment, String tag) {
            if(fragment != null) {
                FRAGMENT_LIST.add(fragment);
                FragmentTransaction fTransaction = fragmentManager.beginTransaction();
                fTransaction.add(containerId, fragment, tag);
                fTransaction.commit();
            }
        }

        void removeFragment(Fragment fragment) {
            if (fragment != null) {
                FRAGMENT_LIST.remove(fragment);
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
    }
}
