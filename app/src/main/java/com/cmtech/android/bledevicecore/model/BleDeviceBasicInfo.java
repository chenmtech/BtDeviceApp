package com.cmtech.android.bledevicecore.model;


import org.litepal.crud.LitePalSupport;

/**
 *  BleDeviceBasicInfo: 设备基本信息，字段信息将保存在数据库中
 *  Created by bme on 2018/6/27.
 */

public class BleDeviceBasicInfo extends LitePalSupport {
    // 数据库保存的字段
    // id
    private int id;

    // mac地址
    private String macAddress;

    // 设备昵称
    private String nickName;

    // 设备广播Uuid Short String
    private String uuidString;

    // 是否自动连接
    private boolean autoConnect;

    // 图标
    private String imagePath;

    public BleDeviceBasicInfo() {

    }

    public BleDeviceBasicInfo(BleDeviceBasicInfo basicInfo) {
        id = basicInfo.id;
        macAddress = basicInfo.macAddress;
        nickName = basicInfo.nickName;
        uuidString = basicInfo.uuidString;
        autoConnect = basicInfo.autoConnect;
        imagePath = basicInfo.imagePath;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUuidString() {
        return uuidString;
    }

    public void setUuidString(String uuidString) {
        this.uuidString = uuidString;
    }

    public boolean autoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
