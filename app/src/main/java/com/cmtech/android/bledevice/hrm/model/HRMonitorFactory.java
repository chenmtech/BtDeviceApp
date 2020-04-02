package com.cmtech.android.bledevice.hrm.model;

import com.cmtech.android.ble.core.DeviceInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.hrm.view.HRMonitorFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
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
public class HRMonitorFactory extends DeviceFactory {
    private static final String HRM_UUID = "180D"; // standard heart rate uuid
    private static final String HRM_DEFAULT_NAME = "心率计"; // default standard heart rate monitor name
    private static final int HRM_DEFAULT_ICON = R.drawable.ic_hrm_default_icon;
    private static final String HRM_FACTORY = HRMonitorFactory.class.getName();

    public static final DeviceType HRM_DEVICE_TYPE = new DeviceType(HRM_UUID, HRM_DEFAULT_ICON, HRM_DEFAULT_NAME, HRM_FACTORY);


    private HRMonitorFactory(DeviceInfo info) {
        super(info);
    }

    @Override
    public IDevice createDevice() {
        return new HRMonitorDevice(info);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(info.getAddress(), HRMonitorFragment.class);
    }
}
