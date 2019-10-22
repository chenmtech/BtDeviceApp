package com.cmtech.android.bledeviceapp.model;

import com.cmtech.android.bledeviceapp.util.DataIOUtil;

import org.litepal.crud.LitePalSupport;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
  *
  * ClassName:      User
  * Description:    用户类
  * Author:         chenm
  * CreateDate:     2018/10/27 上午3:57
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/20 上午3:57
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class User extends LitePalSupport implements Serializable, Cloneable{
    private static final int PHONE_CHAR_LEN = 15;
    private static final int NAME_CHAR_LEN = 10;
    private static final int PERSONAL_INFO_CHAR_LEN = 50;

    private int id; // id
    private String phone = "";
    private String name = "";
    private String portraitPath = "";
    private String personalInfo = "";

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
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPortraitPath() {
        return portraitPath;
    }
    public void setPortraitPath(String portraitPath) {
        this.portraitPath = portraitPath;
    }
    public String getPersonalInfo() {
        return personalInfo;
    }
    public void setPersonalInfo(String personalInfo) {
        this.personalInfo = personalInfo;
    }

    public boolean readFromStream(DataInput in) throws IOException{
        phone = DataIOUtil.readFixedString(in, PHONE_CHAR_LEN);
        name = DataIOUtil.readFixedString(in, NAME_CHAR_LEN);
        personalInfo = DataIOUtil.readFixedString(in, PERSONAL_INFO_CHAR_LEN);
        return true;
    }

    public boolean writeToStream(DataOutput out) throws IOException{
        DataIOUtil.writeFixedString(out, phone, PHONE_CHAR_LEN);
        DataIOUtil.writeFixedString(out, name, NAME_CHAR_LEN);
        DataIOUtil.writeFixedString(out, personalInfo, PERSONAL_INFO_CHAR_LEN);
        return true;
    }

    public int length() {
        return (PHONE_CHAR_LEN + NAME_CHAR_LEN + PERSONAL_INFO_CHAR_LEN)*2;
    }

    @Override
    public String toString() {
        return "User Name：" + name + ' ' + "Personal Info：" + personalInfo;
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
        return phone.equals(other.phone);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        User account = (User) super.clone();
        account.phone = phone;
        account.name = name;
        account.portraitPath = portraitPath;
        account.personalInfo = personalInfo;
        return account;
    }

}
