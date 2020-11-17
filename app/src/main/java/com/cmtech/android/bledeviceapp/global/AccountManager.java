package com.cmtech.android.bledeviceapp.global;


import android.content.Context;

import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.vise.log.ViseLog;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.model.Account.LOGIN_WAY_PASSWORD;
import static com.cmtech.android.bledeviceapp.model.Account.LOGIN_WAY_QR_CODE;

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
    private Account account; // account

    AccountManager() {
        account = Account.readFromSharedPreference();
    }

    public Account getAccount() {
        return account;
    }

    /**
     * 本地账户登录
     * 当应用程序启动时，会先检查本地账户登录是否成功
     * 如果可以，则允许用户继续使用app功能，保证在没有网络的情况下继续使用app一些功能
     * 如果不可以，则要求用户必须重新进行网络登录
     * @return true: 本地登录成功； false：本地登录失败
     */
    public boolean localLogin() {
        ViseLog.e(account);
        return isValid() && !account.isNeedWebLogin();
    }

    /**
     * 用密码进行网络登录
     * @param userName：用户名
     * @param password：密码
     * @param context：上下文
     * @param showString：登录时显示的进度提示字符串，如果为null或""，则不显示进度条，在后台登录；否则在前台登录
     * @param callback：登录后的回调
     */
    public void login(String userName, String password, final Context context, String showString, ICodeCallback callback) {
        account.setUserName(userName);
        account.setPassword(password);
        account.setLoginWay(LOGIN_WAY_PASSWORD);
        account.login(context, showString, callback);
    }

    /**
     * 用手机验证码进行网络登录
     * @param userName：用户名
     * @param context：上下文
     * @param showString：登录时显示的进度提示字符串，如果为null或""，则不显示进度条，在后台登录；否则在前台登录
     * @param callback：登录后的回调
     */
    public void login(String userName, final Context context, String showString, ICodeCallback callback) {
        account.setUserName(userName);
        account.setLoginWay(LOGIN_WAY_QR_CODE);
        account.login(context, showString, callback);
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
     * 本地登出当前账号
     * 把账号信息从本地删除，网络端不会删除
     * 仅在切换账户时使用
     */
    public void localLogout() {
        if(account != null) {
            account.remove();
            account = null;
        }
    }

    /**
     * 当前账户是否为有效账户
     * @return
     */
    public boolean isValid() {
        return account != null && (account.getAccountId() != INVALID_ID);
    }
}
