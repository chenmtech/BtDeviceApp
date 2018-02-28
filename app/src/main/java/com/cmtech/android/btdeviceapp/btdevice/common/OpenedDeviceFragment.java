package com.cmtech.android.btdeviceapp.btdevice.common;

import android.support.v4.app.Fragment;

import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

/**
 * Created by bme on 2018/2/27.
 */

public abstract class OpenedDeviceFragment extends Fragment implements ConfiguredDevice.IConfiguredDeviceObersver {
    protected ConfiguredDevice device;

    public OpenedDeviceFragment() {

    }

    protected void setDevice(ConfiguredDevice device) {
        this.device = device;
        device.registerDeviceObserver(this);
    }

}
