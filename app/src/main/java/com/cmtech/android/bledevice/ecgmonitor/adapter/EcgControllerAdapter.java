package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.adapter
 * ClassName:      EcgControllerAdapter
 * Description:    Ecg控制Adapter
 * Author:         chenm
 * CreateDate:     2019/4/15 上午5:44
 * UpdateUser:     更新者
 * UpdateDate:     2019/4/15 上午5:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class EcgControllerAdapter extends FragmentPagerAdapter {
    private Context context;

    private List<Fragment> fragmentList;

    private List<String> titleList;

    public EcgControllerAdapter(FragmentManager fm, Context context, List<Fragment> fragmentList, List<String> titleList) {
        super(fm);
        this.context = context;
        this.fragmentList = fragmentList;
        this.titleList = titleList;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return titleList.size();
    }

    /**
     * //此方法用来显示tab上的名字
     *
     * @param position
     * @return
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }
}