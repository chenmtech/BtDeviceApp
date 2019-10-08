package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.cmtech.android.bledeviceapp.R;
import com.vise.utils.view.BitmapUtil;

import static com.cmtech.android.ble.core.BleDevice.NO_BATTERY;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      MainToolbarManager
 * Description:    主界面工具条管理器
 * Author:         chenm
 * CreateDate:     2019/4/20 上午5:47
 * UpdateUser:     更新者
 * UpdateDate:     2019/4/20 上午5:47
 * UpdateRemark:   更新说明
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
    private MenuItem menuConfig;
    private MenuItem menuClose;

    public MainToolbarManager(Context context, Toolbar toolbar, TextView tvBattery) {
        this.context = context;
        this.toolbar = toolbar;
        this.tvBattery = tvBattery;
    }

    public void setMenuItems(MenuItem menuConfig, MenuItem menuClose) {
        this.menuConfig = menuConfig;
        this.menuClose = menuClose;
    }

    public void set(int flag, Object... objects) {
        int i = 0;
        if((flag & FLAG_NAVI_ICON) != 0) {
            setNavigationIcon((String)objects[i++]);
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
            boolean[] showMenu = (boolean[]) objects[i];
            updateMenuItem(showMenu[0], showMenu[1]);
        }
    }

    public void setTitle(String title, String subtitle) {
        toolbar.setTitle(title);
        toolbar.setSubtitle(subtitle);
    }

    public void setBattery(int battery) {
        if(battery == NO_BATTERY) {
            tvBattery.setVisibility(View.GONE);
        } else {
            tvBattery.setVisibility(View.VISIBLE);
            tvBattery.setText(String.valueOf(battery));
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.battery_list_drawable);
            if(drawable == null) {
                tvBattery.setVisibility(View.GONE);
                return;
            }
            drawable.setLevel(battery % 4);
            drawable.setBounds(0,0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tvBattery.setCompoundDrawables(drawable, null, null, null);
        }
    }

    public void setNavigationIcon(String imagePath) {
        Drawable drawable;
        if(imagePath == null || "".equals(imagePath.trim())) {
            drawable = ContextCompat.getDrawable(context, R.mipmap.ic_menu);
        } else {
            Bitmap bitmap = BitmapUtil.getSmallBitmap(imagePath, 64, 64);
            drawable = new BitmapDrawable(context.getResources(), bitmap);
        }
        toolbar.setNavigationIcon(drawable);
    }

    public void updateMenuItem(boolean showMenuConfig, boolean showMenuClose) {
        if(menuConfig == null || menuClose == null) return;

        menuConfig.setVisible(showMenuConfig);
        menuClose.setVisible(showMenuClose);
    }
}
