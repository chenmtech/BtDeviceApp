package com.cmtech.android.bledevice.ptt.model;

import android.content.Context;

import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.ptt.activityfragment.PttFragment;
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
public class PttFactory extends DeviceFactory {
    private static final String PTT_UUID = "AAC0"; // PTT uuid
    private static final String PTT_DEFAULT_NAME = "心电脉搏"; // default PTT name
    private static final int PTT_DEFAULT_ICON = R.drawable.ic_eeg_default_icon;
    private static final String PTT_FACTORY = PttFactory.class.getName();

    public static final DeviceType PTT_DEVICE_TYPE = new DeviceType(PTT_UUID, PTT_DEFAULT_ICON, PTT_DEFAULT_NAME, PTT_FACTORY);


    private PttFactory(DeviceCommonInfo info) {
        super(info);
    }

    @Override
    public IDevice createDevice(Context context) {
        return new PttDevice(context, info);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(info.getAddress(), PttFragment.class);
    }
}
