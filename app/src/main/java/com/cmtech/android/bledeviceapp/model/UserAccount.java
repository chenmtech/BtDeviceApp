package com.cmtech.android.bledeviceapp.model;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 *  UserAccount: 用户账户类
 *  Created by bme on 2018/10/27.
 */

public class UserAccount  extends LitePalSupport implements Serializable{
    private int id; // id
    private String phone = ""; // 手机号
    private String userName = "未设置"; // 网络名称
    private String portraitFilePath = ""; // 头像文件路径
    private String remark = ""; // 备注

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPortraitFilePath() {
        return portraitFilePath;
    }

    public void setPortraitFilePath(String portraitFilePath) {
        this.portraitFilePath = portraitFilePath;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
