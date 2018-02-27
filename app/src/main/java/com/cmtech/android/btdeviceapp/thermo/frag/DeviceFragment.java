package com.cmtech.android.btdeviceapp.thermo.frag;

import android.support.v4.app.Fragment;

import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

/**
 * Created by bme on 2018/2/27.
 */

public abstract class DeviceFragment extends Fragment implements ConfiguredDevice.IConfiguredDeviceObersver {
    ConfiguredDevice device;

    public DeviceFragment() {

    }

    void setDevice(ConfiguredDevice device) {
        this.device = device;
        device.registerDeviceObserver(this);
    }

}
