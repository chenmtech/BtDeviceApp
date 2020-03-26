package com.cmtech.android.ble.core;


import java.io.Serializable;


/**
 * ClassName:      DeviceInfo
 * Description:    设备信息，字段信息将保存在Preference中
 * Author:         chenm
 * CreateDate:     2018-06-27 08:56
 * UpdateUser:     chenm
 * UpdateDate:     2019-06-28 08:56
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public abstract class DeviceInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_DEVICE_NAME = ""; // 缺省设备名
    public static final String DEFAULT_DEVICE_ICON = ""; // 缺省设备图标路径名
    public static final boolean DEFAULT_DEVICE_AUTO_CONNECT = true; // 设备打开时是否自动连接

    protected final String address; // mac address
    protected final String uuid; // uuid string
    protected String name = DEFAULT_DEVICE_NAME; // device name
    protected String icon = DEFAULT_DEVICE_ICON; // 设备图标完整路径
    protected boolean autoConnect = DEFAULT_DEVICE_AUTO_CONNECT; // 设备打开后是否自动连接

    public DeviceInfo(String address, String uuid) {
        this.address = address;
        this.uuid = uuid;
    }

    protected DeviceInfo(String address, String uuid, String name, String icon,
                         boolean autoConnect) {
        this.address = address;
        this.uuid = uuid;
        this.name = name;
        this.icon = icon;
        this.autoConnect = autoConnect;
    }

    public abstract boolean isLocal();
    public String getAddress() {
        return address;
    }
    public String getUuid() {
        return uuid;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public boolean isAutoConnect() {
        return autoConnect;
    }
    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    public void update(DeviceInfo info) {
        if (address.equalsIgnoreCase(info.address) && uuid.equalsIgnoreCase(info.uuid)) {
            name = info.name;
            icon = info.icon;
            autoConnect = info.autoConnect;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfo that = (DeviceInfo) o;
        return address.equalsIgnoreCase(that.address) && isLocal() == that.isLocal();
    }

    @Override
    public int hashCode() {
        return address.hashCode() + (isLocal() ? 0 : 1);
    }
}
