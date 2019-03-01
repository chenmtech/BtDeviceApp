package com.cmtech.android.bledeviceapp.model;


import org.litepal.LitePal;

import java.util.List;

/**
 *  UserAccountManager: 用户账户管理器
 *  Created by bme on 2018/10/27.
 */

public class UserAccountManager {
    private static UserAccountManager instance;     //入口操作管理

    private UserAccount userAccount;

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public static UserAccountManager getInstance() {
        if (instance == null) {
            synchronized (UserAccountManager.class) {
                if (instance == null) {
                    instance = new UserAccountManager();
                }
            }
        }
        return instance;
    }

    private UserAccountManager() {
    }

    // 是否已经登录
    public boolean isSignIn() {
        return userAccount != null;
    }

    // 退出账号
    public void signOut() {
        userAccount = null;
    }

    // 注册新账户
    public boolean signUp(String phone) {
        List<UserAccount> find = LitePal.where("phone = ?", phone).find(UserAccount.class);
        if(find != null && find.size() > 0) {
            return false;
        } else {
            UserAccount user = new UserAccount();
            user.setPhone(phone);
            user.save();
            setUserAccount(user);
            return true;
        }
    }

    // 登录
    public boolean signIn(String phone) {
        List<UserAccount> find = LitePal.where("phone = ?", phone).find(UserAccount.class);
        if(find != null && find.size() == 1) {
            setUserAccount(find.get(0));
            return true;
        } else {
            return false;
        }
    }

}
