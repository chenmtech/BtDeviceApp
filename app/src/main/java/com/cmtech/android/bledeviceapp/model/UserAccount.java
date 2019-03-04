package com.cmtech.android.bledeviceapp.model;

import com.cmtech.android.bledeviceapp.util.DataIOUtil;

import org.litepal.crud.LitePalSupport;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 *  UserAccount: 用户账户类
 *  Created by bme on 2018/10/27.
 */

public class UserAccount  extends LitePalSupport implements Serializable, Cloneable{
    private static final int PHONE_LEN = 15; // 手机号字符数
    private static final int NAME_LEN = 10; // 人名字符数
    private static final int REMARK_LEN = 50; // 备注字符数

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

    public boolean readFromStream(DataInput in) throws IOException{
        // 读创建人手机号
        phone = DataIOUtil.readFixedString(PHONE_LEN, in);
        // 读创建人
        userName = DataIOUtil.readFixedString(NAME_LEN, in);
        // 读创建人备注
        remark = DataIOUtil.readFixedString(REMARK_LEN, in);

        return true;
    }

    public boolean writeToStream(DataOutput out) throws IOException{
        // 写创建人手机号
        DataIOUtil.writeFixedString(phone, PHONE_LEN, out);
        // 写创建人
        DataIOUtil.writeFixedString(userName, NAME_LEN, out);
        // 写创建人备注
        DataIOUtil.writeFixedString(remark, REMARK_LEN, out);
        return true;
    }

    public int length() {
        return (PHONE_LEN + NAME_LEN + REMARK_LEN)*2;
    }

    @Override
    public String toString() {
        return "用户名：" + userName + ";"
                + "备注：" + remark;
    }

    @Override
    public int hashCode() {
        return userName.hashCode();
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;

        UserAccount other = (UserAccount) otherObject;

        return  (phone.equals(other.phone));
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        UserAccount account = (UserAccount) super.clone();
        account.phone = phone;
        account.userName = userName;
        account.portraitFilePath = portraitFilePath;
        account.remark = remark;
        return account;
    }

}
