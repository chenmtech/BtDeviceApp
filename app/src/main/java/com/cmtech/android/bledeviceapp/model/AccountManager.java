package com.cmtech.android.bledeviceapp.model;


import org.litepal.LitePal;

/**
  *
  * ClassName:      AccountManager
  * Description:    账户管理器
  * Author:         chenm
  * CreateDate:     2018/10/27 上午4:01
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/20 上午4:01
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class AccountManager {
    private static User account; // account

    private AccountManager() {
    }

    public static User getAccount() {
        return account;
    }

    // login account
    public static void login(String platName, String platId, String name, String icon) {
        User account = LitePal.where("platName = ? and platId = ?", platName, platId).findFirst(User.class);
        if(account == null) {
            account = new User(platName, platId, name, icon);
        } else {
            account.setName(name);
            account.setIcon(icon);
        }
        account.save();
        AccountManager.account = account;
    }

    // logout account
    public static void logout() {
        account = null;
    }

    // is a valid account login
    public static boolean isLogin() {
        return account != null;
    }

    // clear account's local icon
    public static void clearAccountLocalIcon() {
        if(account != null) {
            account.setLocalIcon("");
            account.save();
        }
    }
}
