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
    private static final int DESCRIPTION_CHAR_LEN = 50;

    private int id; // id
    private String phone = ""; // 手机号
    private String name = ""; // 名称
    private String description = ""; // 个人描述信息

    public User() {

    }

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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean readFromStream(DataInput in) throws IOException{
        phone = DataIOUtil.readFixedString(in, PHONE_CHAR_LEN);
        name = DataIOUtil.readFixedString(in, NAME_CHAR_LEN);
        description = DataIOUtil.readFixedString(in, DESCRIPTION_CHAR_LEN);
        return true;
    }

    public boolean writeToStream(DataOutput out) throws IOException{
        DataIOUtil.writeFixedString(out, phone, PHONE_CHAR_LEN);
        DataIOUtil.writeFixedString(out, name, NAME_CHAR_LEN);
        DataIOUtil.writeFixedString(out, description, DESCRIPTION_CHAR_LEN);
        return true;
    }

    public int length() {
        return (PHONE_CHAR_LEN + NAME_CHAR_LEN + DESCRIPTION_CHAR_LEN)*2;
    }

    @Override
    public String toString() {
        return "Phone: " + phone + " Name：" + name + ' ' + " Personal Info：" + description;
    }

    @Override
    public int hashCode() {
        return phone.hashCode();
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(!(otherObject instanceof User)) return false;
        User other = (User) otherObject;
        return phone.equals(other.phone);
    }

    @Override
    public Object clone() {
        User account = new User();
        account.phone = phone;
        account.name = name;
        account.description = description;
        return account;
    }

}
