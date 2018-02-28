package com.cmtech.android.btdevice.common;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.cmtech.android.btdeviceapp.R;

/**
 * Created by bme on 2018/2/28.
 */

public class DeviceFragTitleLayout extends RelativeLayout {

    public DeviceFragTitleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.device_frag_title, this);


    }




}
