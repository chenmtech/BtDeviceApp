package com.cmtech.android.bledeviceapp.model;

import org.litepal.LitePal;

import static com.cmtech.android.bledeviceapp.AppConstant.PHONE_PLAT_NAME;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      PhoneAccount
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/7/1 上午9:47
 * UpdateUser:     更新者
 * UpdateDate:     2020/7/1 上午9:47
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class PhoneAccount{
    public static boolean isAuthValid() {
        Account account = LitePal.where("platName = ?", PHONE_PLAT_NAME).findFirst(Account.class);
        return (account != null);
    }

    public static void removeAccount() {
        LitePal.deleteAll("Account", "platName = ?", PHONE_PLAT_NAME);
    }

    public static Account getAccount() {
        return LitePal.where("platName = ?", PHONE_PLAT_NAME).findFirst(Account.class);
    }

}
