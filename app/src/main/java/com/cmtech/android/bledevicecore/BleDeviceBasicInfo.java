package com.cmtech.android.bledevicecore;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.MyApplication;
import com.vise.log.ViseLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *  BleDeviceBasicInfo: 设备基本信息，字段信息将保存在数据库或SharePreference中
 *  Created by bme on 2018/6/27.
 */

public class BleDeviceBasicInfo implements Serializable{
    private final static long serialVersionUID = 1L;

    public static final String DEFAULT_DEVICE_NICKNAME = "";
    public static final String DEFAULT_DEVICE_IMAGEPATH = "";
    public static final boolean DEFAULT_DEVICE_AUTOCONNECT = true;
    public static final int DEFAULT_DEVICE_RECONNECTTIMES = 3;



    private final static SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

    // mac地址
    private String macAddress = "";

    // 设备昵称
    private String nickName = DEFAULT_DEVICE_NICKNAME;

    // 设备广播Uuid Short String
    private String uuidString = "";

    // 图标路径
    private String imagePath = DEFAULT_DEVICE_IMAGEPATH;

    // 是否自动连接
    private boolean autoConnect = DEFAULT_DEVICE_AUTOCONNECT;

    // 连接断开后重连次数
    private int reconnectTimes = DEFAULT_DEVICE_RECONNECTTIMES;

    public BleDeviceBasicInfo(String macAddress, String nickName, String uuidString, String imagePath, boolean autoConnect, int reconnectTimes) {
        this.macAddress = macAddress;
        this.nickName = nickName;
        this.uuidString = uuidString;
        this.imagePath = imagePath;
        this.autoConnect = autoConnect;
        this.reconnectTimes = reconnectTimes;
    }

    public BleDeviceBasicInfo(BleDeviceBasicInfo basicInfo) {
        macAddress = basicInfo.macAddress;
        nickName = basicInfo.nickName;
        uuidString = basicInfo.uuidString;
        imagePath = basicInfo.imagePath;
        autoConnect = basicInfo.autoConnect;
        reconnectTimes = basicInfo.reconnectTimes;
    }

    public BleDeviceBasicInfo(BleDevice device) {
        this(device.getBasicInfo());
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean autoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    public int getReconnectTimes() {
        return reconnectTimes;
    }

    public void setReconnectTimes(int reconnectTimes) {
        this.reconnectTimes = reconnectTimes;
    }

    public boolean saveToPref() {
        if(TextUtils.isEmpty(macAddress)) return false;

        SharedPreferences.Editor editor = pref.edit();

        Set<String> addressSet = new HashSet<>();
        addressSet = pref.getStringSet("addressSet", addressSet);
        if(addressSet.isEmpty() || !addressSet.contains(macAddress)) {
            addressSet.add(macAddress);
            editor.putStringSet("addressSet", addressSet);
        }

        editor.putString(macAddress+"_macAddress", macAddress);
        editor.putString(macAddress+"_nickName", nickName);
        editor.putString(macAddress+"_uuidString", uuidString);
        editor.putString(macAddress+"_imagePath", imagePath);
        editor.putBoolean(macAddress+"_autoConnect", autoConnect);
        editor.putInt(macAddress+"_reconnectTimes", reconnectTimes);

        ViseLog.i("saveToPref the basic info.");

        return editor.commit();
    }

    // 从Pref中删除
    public void deleteFromPref() {
        if(TextUtils.isEmpty(macAddress)) return;

        SharedPreferences.Editor editor = pref.edit();

        Set<String> addressSet = new HashSet<>();
        addressSet = pref.getStringSet("addressSet", addressSet);
        if(addressSet.contains(macAddress)) {
            addressSet.remove(macAddress);
            editor.putStringSet("addressSet", addressSet);
        }

        editor.remove(macAddress+"_macAddress");
        editor.remove(macAddress+"_nickName");
        editor.remove(macAddress+"_uuidString");
        editor.remove(macAddress+"_imagePath");
        editor.remove(macAddress+"_autoConnect");
        editor.remove(macAddress+"_reconnectTimes");

        editor.commit();
    }

    public static BleDeviceBasicInfo createFromPreference(String macAddress) {
        if(TextUtils.isEmpty(macAddress)) return null;

        String address = pref.getString(macAddress+"_macAddress", "");
        if("".equals(address)) return null;
        String nickName = pref.getString(macAddress+"_nickName", "");
        String uuidString = pref.getString(macAddress+"_uuidString", "");
        String imagePath = pref.getString(macAddress+"_imagePath", "");
        boolean autoConnect = pref.getBoolean(macAddress+"_autoConnect", false);
        int reconnectTimes = pref.getInt(macAddress+"_reconnectTimes", 3);
        return new BleDeviceBasicInfo(address, nickName, uuidString, imagePath, autoConnect, reconnectTimes);
    }

    public static List<BleDeviceBasicInfo> findAllFromPreference() {
        Set<String> addressSet = new HashSet<>();
        addressSet = pref.getStringSet("addressSet", addressSet);
        if(addressSet.isEmpty()) {
            ViseLog.i("addressSet is empty.");
            return null;
        }
        // 转为数组排序
        String[] addressArr = (String[]) addressSet.toArray(new String[addressSet.size()]);
        Arrays.sort(addressArr);

        List<BleDeviceBasicInfo> infoList = new ArrayList<>();
        for(String macAddress : addressArr) {
            BleDeviceBasicInfo basicInfo = createFromPreference(macAddress);
            if(basicInfo != null)
                infoList.add(basicInfo);
        }

        return infoList;
    }
}
