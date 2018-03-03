package com.cmtech.android.btdevice.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.cmtech.android.btdeviceapp.R;

/**
 * Created by bme on 2018/2/28.
 */

public class DeviceFragTitleLayout extends RelativeLayout {

    public DeviceFragTitleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.include_device_fragment_title, this);


    }




}
