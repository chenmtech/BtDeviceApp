package com.cmtech.android.bledeviceapp.global;


import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;

import android.content.Context;

import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.vise.log.ViseLog;

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
    private Account account;

    AccountManager() {
        account = Account.createFromSharedPreference();
    }

    public Account getAccount() {
        return account;
    }

    /**
     * 账户是否成功本地登录
     * 当应用程序启动时，会先检查账户本地登录是否成功
     * 如果本地登录成功，则允许用户继续使用app的本地功能，保证在没有网络的情况下继续使用
     * 如果本地登录失败，则要求用户必须重新进行网络登录
     * @return true: 本地登录成功； false：本地登录失败
     */
    public boolean isLocalLoginSuccess() {
        return isValid();
    }

    /**
     * 网络登录
     * @param context：上下文
     * @param userName：用户名
     * @param password：密码
     * @param callback：登录后的回调
     */
    public void login(final Context context, String userName, String password, ICodeCallback callback) {
        account.setUserName(userName);
        account.setPassword(password);
        account.login(context, callback);
    }

    /**
     * 网络注册新账户
     * 无论注册成功与否，在本地不会保存任何账户信息
     * @param context：上下文
     * @param userName：用户名
     * @param password：密码
     * @param callback：注册后回调
     */
    public void signUp(final Context context, String userName, String password, ICodeCallback callback) {
        account.setUserName(userName);
        account.setPassword(password);
        account.signUp(context, callback);
    }

    /**
     * 重置密码
     * @param context：上下文
     * @param userName：用户名
     * @param password：密码
     * @param callback：重置后回调
     */
    public void resetPassword(final Context context, String userName, String password, ICodeCallback callback) {
        account.setUserName(userName);
        account.setPassword(password);
        account.resetPassword(context, callback);
    }

    /**
     * 本地登出当前账号
     * 把账号信息从本地删除，网络端不会删除
     * 仅在切换账户时使用
     */
    public void localLogout() {
        if(account != null) {
            account.removeFromLocal();
            account = null;
        }
    }

    /**
     * 当前账户是否为有效账户
     * @return 是否有效
     */
    public boolean isValid() {
        return account != null && (account.getAccountId() != INVALID_ID);
    }
}
