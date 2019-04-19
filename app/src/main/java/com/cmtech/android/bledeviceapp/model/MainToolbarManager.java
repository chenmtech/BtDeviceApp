package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.cmtech.android.bledeviceapp.R;
import com.vise.utils.view.BitmapUtil;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      MainToolbarManager
 * Description:    管理主界面的工具条
 * Author:         作者名
 * CreateDate:     2019/4/20 上午5:47
 * UpdateUser:     更新者
 * UpdateDate:     2019/4/20 上午5:47
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public class MainToolbarManager {
    public final static int NAVI_ICON_FLAG = 0x01;
    public final static int TITLE_FLAG = 0x02;
    public final static int BATTERY_FLAG = 0x04;
    public final static int MENU_FLAG = 0x08;

    private final Context context;

    private final Toolbar toolbar;

    private final TextView tvDeviceBattery;

    private MenuItem menuConfig;

    private MenuItem menuClose;

    public MainToolbarManager(Context context, Toolbar toolbar, TextView tvDeviceBattery) {
        this.context = context;
        this.toolbar = toolbar;
        this.tvDeviceBattery = tvDeviceBattery;
    }

    public void setMenuItems(MenuItem menuConfig, MenuItem menuClose) {
        this.menuConfig = menuConfig;
        this.menuClose = menuClose;
    }

    public void set(int flag, Object... objects) {
        int i = 0;
        if((flag & NAVI_ICON_FLAG) != 0) {
            setNavigationIcon((String)objects[i++]);
        }

        if((flag & TITLE_FLAG) != 0) {
            String[] titles = (String[]) objects[i++];
            setTitle(titles[0], titles[1]);
        }

        if((flag & BATTERY_FLAG) != 0) {
            int battery = (int) objects[i++];
            setBattery(battery);
        }

        if((flag & MENU_FLAG) != 0) {
            int fragSize = (int) objects[i];
            updateMenuItem(fragSize);
        }
    }

    public void setTitle(String title, String subtitle) {
        toolbar.setTitle(title);

        toolbar.setSubtitle(subtitle);
    }

    public void setBattery(int battery) {
        if(battery < 0) {
            tvDeviceBattery.setVisibility(View.GONE);
        } else {
            tvDeviceBattery.setVisibility(View.VISIBLE);

            tvDeviceBattery.setText(String.valueOf(battery));

            Drawable drawable = context.getResources().getDrawable(R.drawable.battery_list_drawable);

            drawable.setLevel(battery % 4);

            drawable.setBounds(0,0, drawable.getMinimumWidth(), drawable.getMinimumHeight());

            tvDeviceBattery.setCompoundDrawables(drawable, null, null, null);
        }
    }

    public void setNavigationIcon(String imagePath) {
        Drawable drawable;
        if(imagePath == null || "".equals(imagePath.trim())) {
            drawable = context.getResources().getDrawable(R.mipmap.ic_menu);
        } else {
            Bitmap bitmap = BitmapUtil.getSmallBitmap(imagePath, 64, 64);

            drawable = new BitmapDrawable(context.getResources(), bitmap);
        }

        toolbar.setNavigationIcon(drawable);
    }

    public void updateMenuItem(int fragmentSize) {
        if(menuConfig == null || menuClose == null) return;

        if(fragmentSize == 0) {
            menuConfig.setVisible(false);

            menuClose.setVisible(true);
        } else {
            menuConfig.setVisible(true);

            menuClose.setVisible(false);
        }
    }
}
