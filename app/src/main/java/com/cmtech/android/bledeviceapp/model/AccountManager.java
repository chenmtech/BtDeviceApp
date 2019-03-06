package com.cmtech.android.bledeviceapp.model;


import org.litepal.LitePal;

import java.util.List;

/**
 *  AccountManager: 账户管理器
 *  Created by bme on 2018/10/27.
 */

public class AccountManager {
    private static AccountManager instance;     //入口操作管理

    private User account;

    public User getAccount() {
        return account;
    }

    public void setAccount(User account) {
        this.account = account;
    }

    public static AccountManager getInstance() {
        if (instance == null) {
            synchronized (AccountManager.class) {
                if (instance == null) {
                    instance = new AccountManager();
                }
            }
        }
        return instance;
    }

    private AccountManager() {
    }

    // 是否已经登录
    public boolean isSignIn() {
        return account != null;
    }

    // 退出账号
    public void signOut() {
        account = null;
    }

    // 注册新账户
    public boolean signUp(String phone) {
        List<User> find = LitePal.where("phone = ?", phone).find(User.class);
        if(find != null && find.size() > 0) {
            return false;
        } else {
            account = new User();
            account.setPhone(phone);
            account.save();
            return true;
        }
    }

    // 登录
    public boolean signIn(String phone) {
        List<User> find = LitePal.where("phone = ?", phone).find(User.class);
        if(find != null && find.size() == 1) {
            account = find.get(0);
            return true;
        } else {
            return false;
        }
    }

}
