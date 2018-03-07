package com.cmtech.android.btdeviceapp.model;

import com.cmtech.android.btdevice.common.DeviceFragment;
import com.cmtech.android.btdevice.common.TabEntity;

/**
 * Created by bme on 2018/3/7.
 */

public class OpenedDevice {
    private ConfiguredDevice configuredDevice;
    private DeviceFragment fragment;
    private int icon;

    public OpenedDevice(ConfiguredDevice configuredDevice, int icon) {
        this.configuredDevice = configuredDevice;
        this.icon = icon;
    }

    public OpenedDevice(ConfiguredDevice configuredDevice, int icon, DeviceFragment fragment) {
        this(configuredDevice, icon);
        setFragment(fragment);
    }

    public ConfiguredDevice getConfiguredDevice() {
        return configuredDevice;
    }

    public void setConfiguredDevice(ConfiguredDevice configuredDevice) {
        this.configuredDevice = configuredDevice;
    }

    public DeviceFragment getFragment() {
        return fragment;
    }

    public void setFragment(DeviceFragment fragment) {
        this.fragment = fragment;
        configuredDevice.registerDeviceObserver(fragment);
    }

    public TabEntity getTabEntity() {
        return new TabEntity(configuredDevice.getNickName(), icon, icon);
    }

    public void setTabEntity(TabEntity tabEntity) {
        configuredDevice.setNickName(tabEntity.getTabTitle());
        icon = tabEntity.selectedIcon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
