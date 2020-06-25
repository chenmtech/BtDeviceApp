package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.vise.utils.view.BitmapUtil;

import static com.cmtech.android.ble.core.IDevice.INVALID_BATTERY;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      MainToolbarManager
 * Description:    主界面工具条管理器
 * Author:         chenm
 * CreateDate:     2019/4/20 上午5:47
 * UpdateUser:     更新者
 * UpdateDate:     2019/10/17 下午4:47
 * UpdateRemark:   更新了菜单项的操作
 * Version:        1.0
 */

public class MainToolbarManager {
    private final static int FLAG_NAVI_ICON = 0x01; // 导航图标
    private final static int FLAG_TITLE = 0x02; // 标题
    private final static int FLAG_BATTERY = 0x04; // 电池电量
    private final static int FLAG_MENU = 0x08; // 菜单

    private final Context context;
    private final Toolbar toolbar;
    private final TextView tvBattery;
    private MenuItem[] menuItems;

    public MainToolbarManager(Context context, Toolbar toolbar, TextView tvBattery) {
        this.context = context;
        this.toolbar = toolbar;
        this.tvBattery = tvBattery;
    }

    public void setMenuItems(MenuItem[] menuItems) {
        this.menuItems = menuItems;
    }

    public void set(int flag, Object... objects) {
        int i = 0;
        if((flag & FLAG_NAVI_ICON) != 0) {
            setNavIcon((String)objects[i++]);
        }
        if((flag & FLAG_TITLE) != 0) {
            String[] titles = (String[]) objects[i++];
            setTitle(titles[0], titles[1]);
        }
        if((flag & FLAG_BATTERY) != 0) {
            int battery = (int) objects[i++];
            setBattery(battery);
        }
        if((flag & FLAG_MENU) != 0) {
            boolean[] visible = (boolean[]) objects[i];
            updateMenuVisible(visible);
        }
    }

    public void setTitle(String title, String subtitle) {
        toolbar.setTitle(title);
        toolbar.setSubtitle(subtitle);
    }

    public void setBattery(int battery) {
        if(battery == INVALID_BATTERY) {
            tvBattery.setVisibility(View.GONE);
            return;
        }

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.battery_list_drawable);
        if(drawable == null) {
            tvBattery.setVisibility(View.GONE);
        } else {
            tvBattery.setVisibility(View.VISIBLE);
            tvBattery.setText(String.valueOf(battery));
            int level = (int)(battery/25.0);
            if(level > 3) level = 3;
            drawable.setLevel(level);
            drawable.setBounds(0,0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tvBattery.setCompoundDrawables(null, drawable, null, null);
        }
    }

    public void setNavIcon(String navIcon) {
        Drawable drawable;
        if(navIcon == null || "".equals(navIcon.trim())) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.ic_menu);
        } else {
            Bitmap bitmap = BitmapUtil.getSmallBitmap(navIcon, 64, 64);
            drawable = new BitmapDrawable(context.getResources(), bitmap);
        }
        toolbar.setNavigationIcon(drawable);
    }

    public void updateMenuVisible(boolean[] visible) {
        if(menuItems == null || menuItems.length == 0 || visible == null || visible.length == 0 || visible.length != menuItems.length) return;

        int i = 0;
        for(MenuItem item : menuItems) {
            item.setVisible(visible[i++]);
        }
    }

    public void updateMenuVisible(int itemIndex, boolean visible) {
        if(itemIndex >= 0 && itemIndex < menuItems.length && visible != menuItems[itemIndex].isVisible())
            menuItems[itemIndex].setVisible(visible);
    }
}
