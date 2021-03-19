package com.cmtech.android.bledevice.ppg.model;

import android.content.Context;

import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.ppg.activityfragment.PpgFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.fragment.DeviceFragment;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      HRMonitorFactory
 * Description:    Heart Rate Monitor Factory
 * Author:         chenm
 * CreateDate:     2020-02-04 06:10
 * UpdateUser:     chenm
 * UpdateDate:     2020-02-04 06:10
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class PpgFactory extends DeviceFactory {
    private static final String PPG_UUID = "AAB0"; // PPG uuid
    private static final String PPG_DEFAULT_NAME = "脉搏仪"; // default ppg name
    private static final int PPG_DEFAULT_ICON = R.drawable.ic_ppg_default_icon;
    private static final String PPG_FACTORY = PpgFactory.class.getName();

    public static final DeviceType PPG_DEVICE_TYPE = new DeviceType(PPG_UUID, PPG_DEFAULT_ICON, PPG_DEFAULT_NAME, PPG_FACTORY);


    private PpgFactory(DeviceCommonInfo info) {
        super(info);
    }

    @Override
    public IDevice createDevice(Context context) {
        return new PpgDevice(context, info);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(info.getAddress(), PpgFragment.class);
    }
}
