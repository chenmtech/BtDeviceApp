package com.cmtech.android.bledeviceapp.model;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class UserAccount  extends LitePalSupport implements Serializable{
    // 数据库保存的字段
    // id
    private int id;

    private String phoneNum = "";

    private String userName = "未设置";

    private String imagePath = "";

    private long lastLoginTime = -1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}
