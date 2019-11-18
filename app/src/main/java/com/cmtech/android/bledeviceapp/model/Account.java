package com.cmtech.android.bledeviceapp.model;

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

    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
