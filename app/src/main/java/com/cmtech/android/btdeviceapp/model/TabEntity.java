package com.cmtech.android.btdeviceapp.model;

import com.flyco.tablayout.listener.CustomTabEntity;

/**
 * Created by bme on 2018/3/4.
 */

public class TabEntity implements CustomTabEntity {
    public String title;            // Tab的标题文字
    public int selectedIcon;        // Tab的选中图标
    public int unSelectedIcon;      // Tab的未选中图标

    public TabEntity(String title, int selectedIcon, int unSelectedIcon) {
        this.title = title;
        this.selectedIcon = selectedIcon;
        this.unSelectedIcon = unSelectedIcon;
    }

    @Override
    public String getTabTitle() {
        return title;
    }

    @Override
    public int getTabSelectedIcon() {
        return selectedIcon;
    }

    @Override
    public int getTabUnselectedIcon() {
        return unSelectedIcon;
    }
}
