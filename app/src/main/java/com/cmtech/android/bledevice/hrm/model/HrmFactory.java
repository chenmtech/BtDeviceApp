package com.cmtech.android.bledevice.hrm.model;

import static com.cmtech.android.bledevice.hrm.model.HrmDevice.hrMonitorServiceUuid;

import android.content.Context;

import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.hrm.activityfragment.HrmFragment;
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
public class HrmFactory extends DeviceFactory {
    private static final String HRM_DEFAULT_NAME = MyApplication.getStr(R.string.hr_monitor_name); // default standard heart rate monitor name
    private static final int HRM_DEFAULT_ICON = R.drawable.ic_hrm_default_icon;
    private static final String HRM_FACTORY = HrmFactory.class.getName();

    public static final DeviceType HRM_DEVICE_TYPE = new DeviceType(hrMonitorServiceUuid, HRM_DEFAULT_ICON, HRM_DEFAULT_NAME, HRM_FACTORY);

    private HrmFactory(DeviceCommonInfo info) {
        super(info);
    }

    @Override
    public IDevice createDevice(Context context) {
        return new HrmDevice(context, info);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(info.getAddress(), HrmFragment.class);
    }
}
