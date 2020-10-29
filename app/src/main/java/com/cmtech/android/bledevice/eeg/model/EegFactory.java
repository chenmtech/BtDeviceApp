package com.cmtech.android.bledevice.eeg.model;

import android.content.Context;

import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.eeg.activityfragment.EegFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.fragment.DeviceFragment;
import com.cmtech.android.bledeviceapp.global.MyApplication;
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
public class EegFactory extends DeviceFactory {
    private static final String EEG_UUID = "AAA0"; // EEG uuid
    private static final String EEG_DEFAULT_NAME = MyApplication.getStr(R.string.km_eeg_monitor_name); // default eeg name
    private static final int EEG_DEFAULT_ICON = R.drawable.ic_eeg_default_icon;
    private static final String EEG_FACTORY = EegFactory.class.getName();

    public static final DeviceType EEG_DEVICE_TYPE = new DeviceType(EEG_UUID, EEG_DEFAULT_ICON, EEG_DEFAULT_NAME, EEG_FACTORY);


    private EegFactory(DeviceCommonInfo info) {
        super(info);
    }

    @Override
    public IDevice createDevice(Context context) {
        return new EegDevice(context, info);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(info.getAddress(), EegFragment.class);
    }
}
