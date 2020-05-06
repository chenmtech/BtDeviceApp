package com.cmtech.android.bledeviceapp.model;

import com.cmtech.android.bledeviceapp.util.DataIOUtil;

import org.litepal.crud.LitePalSupport;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
  *
  * ClassName:      Account
  * Description:    用户类
  * Author:         chenm
  * CreateDate:     2018/10/27 上午3:57
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/20 上午3:57
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class Account extends LitePalSupport implements Serializable, Cloneable{
    public static final int PLAT_NAME_CHAR_LEN = 10;
    public static final int PLAT_ID_CHAR_LEN = 255;
    public static final int NAME_CHAR_LEN = 10;
    private static final int NOTE_CHAR_LEN = 50;

    private int id; // id
    private String platName = ""; // registration platform name
    private String platId = ""; // platform ID
    private String name = ""; // user name
    private String icon = ""; // user icon
    private String note = ""; // user note
    private String localIcon = "";

    public Account() {
    }

    public Account(Account account) {
        this.platName = account.platName;
        this.platId = account.platId;
        this.name = account.name;
        this.icon = account.icon;
        this.note = account.note;
        this.localIcon = account.localIcon;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getPlatName() { return platName;}
    public void setPlatName(String platName) { this.platName = platName;}
    public String getPlatId() {
        return platId;
    }
    public void setPlatId(String platId) {
        this.platId = platId;
    }
    public String getShortPlatId() {
        if(platId.length() > 3) {
            return String.format("%s****%s", platId.substring(0, 3), platId.substring(platId.length() - 3));
        } else
            return platId;
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
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public String getLocalIcon() {
        return localIcon;
    }
    public void setLocalIcon(String localIcon) {
        this.localIcon = localIcon;
    }

    public boolean readFromStream(DataInput in) throws IOException{
        platName = DataIOUtil.readFixedString(in, PLAT_NAME_CHAR_LEN);
        platId = DataIOUtil.readFixedString(in, PLAT_ID_CHAR_LEN);
        name = DataIOUtil.readFixedString(in, NAME_CHAR_LEN);
        note = DataIOUtil.readFixedString(in, NOTE_CHAR_LEN);
        return true;
    }

    public boolean writeToStream(DataOutput out) throws IOException{
        DataIOUtil.writeFixedString(out, platName, PLAT_NAME_CHAR_LEN);
        DataIOUtil.writeFixedString(out, platId, PLAT_ID_CHAR_LEN);
        DataIOUtil.writeFixedString(out, name, NAME_CHAR_LEN);
        DataIOUtil.writeFixedString(out, note, NOTE_CHAR_LEN);
        return true;
    }

    public int length() {
        return (PLAT_NAME_CHAR_LEN + PLAT_ID_CHAR_LEN + NAME_CHAR_LEN + NOTE_CHAR_LEN)*2;
    }

    @Override
    public String toString() {
        return "Plat: " + getPlatName() + " Id: " + getShortPlatId() + " Name：" + name + ' ' + " Icon：" + icon + " Note：" + note;
    }

    @Override
    public int hashCode() {
        return (platName + platId).hashCode();
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(!(otherObject instanceof Account)) return false;
        Account other = (Account) otherObject;
        return (platName + platId).equals(other.platName+other.platId);
    }

    @Override
    public Object clone() {
        return new Account(this);
    }

}
