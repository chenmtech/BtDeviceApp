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

    // 是否登录
    public boolean isSignIn() {
        return userAccount != null;
    }

    // 登出
    public void signOut() {
        userAccount = null;
    }

    // 注册
    public boolean signUp(String account, String password) {
        List<UserAccount> find = LitePal.where("name = ?", account).find(UserAccount.class);
        if(find != null && find.size() > 0) {
            return false;
        } else {
            UserAccount user = new UserAccount();
            user.setName(account);
            user.setPassword(password);
            user.save();
            return true;
        }
    }

    // 登录
    public boolean signIn(String account, String password) {
        List<UserAccount> find = LitePal.where("name = ? and password = ?", account, password).find(UserAccount.class);
        if(find != null && find.size() == 1) {
            UserAccount user = new UserAccount();
            user.setName(account);
            user.setPassword(password);
            setUserAccount(user);
            return true;
        } else {
            return false;
        }
    }

}
