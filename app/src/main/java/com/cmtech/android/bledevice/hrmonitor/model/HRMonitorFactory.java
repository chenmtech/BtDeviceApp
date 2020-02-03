package com.cmtech.android.bledevice.hrmonitor.model;

import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledevice.hrmonitor.view.HRMonitorFragment;
import com.cmtech.android.bledevice.temphumid.model.TempHumidFactory;
import com.cmtech.android.bledevice.temphumid.view.TempHumidFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      HRMonitorFactory
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020-02-04 06:10
 * UpdateUser:     更新者
 * UpdateDate:     2020-02-04 06:10
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HRMonitorFactory extends DeviceFactory {
    private static final String HRMONITOR_UUID = "180D"; // 标准心率计
    private static final String HRMONITOR_DEFAULT_NAME = "标准心率计";
    private static final int HRMONITOR_DEFAULT_IMAGE_ID = R.drawable.ic_ecgmonitor_default_image;
    private static final String HRMONITOR_FACTORY = HRMonitorFactory.class.getName();

    public static final DeviceType HRMONITOR_DEVICE_TYPE = new DeviceType(HRMONITOR_UUID, HRMONITOR_DEFAULT_IMAGE_ID, HRMONITOR_DEFAULT_NAME, HRMONITOR_FACTORY);


    private HRMonitorFactory(DeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public IDevice createDevice() {
        return new HRMonitorDevice(registerInfo);
    }

    @Override
    public DeviceFragment createFragment() {
        return DeviceFragment.create(registerInfo.getMacAddress(), HRMonitorFragment.class);
    }
}
