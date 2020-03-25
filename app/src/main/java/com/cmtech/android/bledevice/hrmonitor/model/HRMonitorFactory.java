package com.cmtech.android.bledevice.hrmonitor.model;

import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.hrmonitor.view.HRMonitorFragment;
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
    private static final String HRMONITOR_UUID = "180D"; // standard heart rate uuid
    private static final String DEFAULT_HRMONITOR_NAME = "标准心率计"; // default standard heart rate monitor name
    private static final int DEFAULT_HRMONITOR_IMAGE_ID = R.drawable.ic_ecgmonitor_default_image;
    private static final String HRMONITOR_FACTORY = HRMonitorFactory.class.getName();

    public static final DeviceType HRMONITOR_DEVICE_TYPE = new DeviceType(HRMONITOR_UUID, DEFAULT_HRMONITOR_IMAGE_ID, DEFAULT_HRMONITOR_NAME, HRMONITOR_FACTORY);


    private HRMonitorFactory(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public IDevice createDevice() {
        return new HRMonitorDevice(registerInfo);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(registerInfo.getAddress(), HRMonitorFragment.class);
    }
}
