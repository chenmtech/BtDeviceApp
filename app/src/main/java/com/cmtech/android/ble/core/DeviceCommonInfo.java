package com.cmtech.android.ble.core;


import android.support.annotation.NonNull;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;


/**
 * ClassName:      DeviceInfo
 * Description:    设备信息
 * Author:         chenm
 * CreateDate:     2018-06-27 08:56
 * UpdateUser:     chenm
 * UpdateDate:     2019-06-28 08:56
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public abstract class DeviceCommonInfo extends LitePalSupport implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_NAME = ""; // 缺省设备名
    public static final String DEFAULT_ICON = ""; // 缺省设备图标路径名
    public static final boolean DEFAULT_AUTO_CONNECT = true; // 设备打开时是否自动连接

    private int id;
    private final String address; // mac address
    private final String uuid; // uuid string
    private final boolean local; // is local device or web device
    private String name = DEFAULT_NAME; // device name
    private String icon = DEFAULT_ICON; // 设备图标完整路径
    private boolean autoConnect = DEFAULT_AUTO_CONNECT; // 设备打开后是否自动连接

    protected DeviceCommonInfo(String address, String uuid, boolean local) {
        this.address = address;
        this.uuid = uuid;
        this.local = local;
    }

    protected DeviceCommonInfo(String address, String uuid, boolean local, String name, String icon,
                               boolean autoConnect) {
        this.address = address;
        this.uuid = uuid;
        this.local = local;
        this.name = name;
        this.icon = icon;
        this.autoConnect = autoConnect;
    }

    public int getId() {
        return id;
    }
    public String getAddress() {
        return address;
    }
    public String getUuid() {
        return uuid;
    }
    public boolean isLocal() {
        return local;
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

    public void update(DeviceCommonInfo info) {
        if (address.equalsIgnoreCase(info.address) && uuid.equalsIgnoreCase(info.uuid)) {
            name = info.name;
            icon = info.icon;
            autoConnect = info.autoConnect;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "name" + name + "address" + address + "icon" + icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceCommonInfo that = (DeviceCommonInfo) o;
        return address.equalsIgnoreCase(that.address) && this.local == that.local;
    }

    @Override
    public int hashCode() {
        return address.hashCode() + (local ? 0 : 1);
    }
}
