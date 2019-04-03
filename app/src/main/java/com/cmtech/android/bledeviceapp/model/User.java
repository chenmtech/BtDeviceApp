package com.cmtech.android.bledeviceapp.model;

import com.cmtech.android.bledeviceapp.util.DataIOUtil;

import org.litepal.crud.LitePalSupport;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 *  User: 用户类
 *  Created by bme on 2018/10/27.
 */

public class User extends LitePalSupport implements Serializable, Cloneable{
    private static final int PHONE_CHAR_LEN = 15; // 手机号字符数
    private static final int NAME_CHAR_LEN = 10; // 人名字符数
    private static final int REMARK_CHAR_LEN = 50; // 备注字符数

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
        // 读手机号
        phone = DataIOUtil.readFixedString(in, PHONE_CHAR_LEN);
        // 读人名
        userName = DataIOUtil.readFixedString(in, NAME_CHAR_LEN);
        // 读备注信息
        remark = DataIOUtil.readFixedString(in, REMARK_CHAR_LEN);

        return true;
    }

    public boolean writeToStream(DataOutput out) throws IOException{
        // 写手机号
        DataIOUtil.writeFixedString(out, phone, PHONE_CHAR_LEN);
        // 写人名
        DataIOUtil.writeFixedString(out, userName, NAME_CHAR_LEN);
        // 写备注
        DataIOUtil.writeFixedString(out, remark, REMARK_CHAR_LEN);
        return true;
    }

    /**
     * 获取用户对象占用的字符长度
     * @return 字符长度
     */
    public int length() {
        return (PHONE_CHAR_LEN + NAME_CHAR_LEN + REMARK_CHAR_LEN)*2;
    }

    @Override
    public String toString() {
        return "用户名：" + userName + ' ' + "备注：" + remark;
    }

    @Override
    public int hashCode() {
        return phone.hashCode();
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;

        User other = (User) otherObject;
        // 只要电话相同，就是同一个用户
        return  (phone.equals(other.phone));
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        User account = (User) super.clone();
        account.phone = phone;
        account.userName = userName;
        account.portraitFilePath = portraitFilePath;
        account.remark = remark;
        return account;
    }

}
