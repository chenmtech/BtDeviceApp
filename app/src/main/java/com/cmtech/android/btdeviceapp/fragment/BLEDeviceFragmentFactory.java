package com.cmtech.android.btdeviceapp.fragment;

import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;
import com.cmtech.android.btdeviceapp.model.BLEDeviceType;

import static java.lang.Class.forName;

/**
 * Created by bme on 2018/2/28.
 */

public class BLEDeviceFragmentFactory {

    private BLEDeviceFragmentFactory() {

    }

    public static BLEDeviceFragment build(BLEDeviceModel device) {
        BLEDeviceType deviceType = BLEDeviceType.fromUuid(device.getUuidString());
        try {
            BLEDeviceFragment fragment = (BLEDeviceFragment) Class.forName(deviceType.getFragName()).newInstance();
            return fragment;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }
}
