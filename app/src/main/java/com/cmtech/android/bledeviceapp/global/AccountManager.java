package com.cmtech.android.bledeviceapp.global;


import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.PhoneAccount;
import com.cmtech.android.bledeviceapp.model.WebResponse;
import com.vise.utils.file.FileUtil;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

import static com.cmtech.android.bledeviceapp.global.AppConstant.PHONE_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.global.AppConstant.QQ_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.global.AppConstant.WX_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;
import static com.vise.utils.handler.HandlerUtil.runOnUiThread;

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
    }

    public Account getAccount() {
        return account;
    }

    // login account
    public void login(String platName, String platId, String name, String icon) {
        Account account = LitePal.where("platName = ? and platId = ?", platName, platId).findFirst(Account.class);
        if(account == null) {
            account = new Account(platName, platId, name, "", icon);
        } else {
            account.setName(name);
            account.setIcon(icon);
        }
        account.save();
        this.account = account;
    }

    public void webLogin(final Context context) {
        if(!isLogin()) return;

        account.signupOrLogin(context, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                if(response.getCode() != RETURN_CODE_SUCCESS) {
                    runOnUiThread(() -> Toast.makeText(context, R.string.login_failure, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // logout account
    public void logout(boolean isRemoved) {
        if(!isLogin()) return;

        if(isRemoved) {
            if(account.getPlatName().equals(QQ_PLAT_NAME)) {
                Platform plat = ShareSDK.getPlatform(QQ.NAME);
                plat.removeAccount(true);
            } else if(account.getPlatName().equals(WX_PLAT_NAME)) {
                Platform plat = ShareSDK.getPlatform(Wechat.NAME);
                plat.removeAccount(true);
            } else if(account.getPlatName().equals(PHONE_PLAT_NAME)) {
                PhoneAccount.removeAccount();
            }

            if(!TextUtils.isEmpty(account.getIcon())) {
                try {
                    FileUtil.deleteFile(new File(account.getIcon()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        account = null;
    }

    // is a valid account login
    public boolean isLogin() {
        return account != null;
    }
}
