package com.cmtech.android.bledeviceapp.model;

import org.litepal.annotation.Column;

/**
 *
 * ClassName:      Account
 * Description:    账户类
 * Author:         chenm
 * CreateDate:     2019/11/06 上午3:57
 * UpdateUser:     chenm
 * UpdateDate:     2019/11/06 上午3:57
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public class Account extends User {
    private String imagePath = ""; // 头像文件路径
    @Column(ignore = true)
    private String huaweiId = "";

    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public String getHuaweiId() {
        return huaweiId;
    }
    public void setHuaweiId(String huaweiId) {
        this.huaweiId = huaweiId;
    }

    @Override
    public String toString() {
        return super.toString() +
                "Account{" +
                "imagePath='" + imagePath + '\'' +
                ", huaweiId='" + huaweiId + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return huaweiId.hashCode();
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(!(otherObject instanceof Account)) return false;
        Account other = (Account) otherObject;
        return huaweiId.equals(other.huaweiId);
    }
}
