package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;

import com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask.CMD_LOGIN;
import static com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask.CMD_SIGNUP;
import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_IMAGE;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;

/**
  *
  * ClassName:      Account
  * Description:    账户信息类
  * Author:         chenm
  * CreateDate:     2018/10/27 上午3:57
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/20 上午3:57
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class Account extends LitePalSupport implements Serializable, IJsonable, IWebOperation {
    private int id; // id
    private int accountId = INVALID_ID;
    private String userName = ""; // user name
    private String password = ""; // password
    private String nickName = ""; // nick name
    private String note = ""; // note
    private String icon = ""; // icon file path in local disk
    @Column(ignore = true)
    private boolean webLogin = false;

    public Account(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public Account(Account account) {
        this.userName = account.userName;
        this.password = account.password;
        this.nickName = account.nickName;
        this.note = account.note;
        this.icon = account.icon;
    }

    public int getId() {
        return id;
    }
    public int getAccountId() {
        return accountId;
    }
    public String getUserName() { return userName;}
    public String getPassword() {
        return password;
    }
    public String getNickName() {
        if("".equals(nickName)) {
            return userName;
        }
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public void fromJson(JSONObject json) {
        try {
            nickName = json.getString("nickName");
            note = json.getString("note");
            String iconStr = json.getString("iconStr");
            if (!TextUtils.isEmpty(iconStr)) {
                Bitmap bitmap = BitmapUtil.byteToBitmap(Base64.decode(iconStr, Base64.DEFAULT));
                if (bitmap != null) {
                    try {
                        assert DIR_IMAGE != null;
                        File iconFile = FileUtil.getFile(DIR_IMAGE, userName + password + ".jpg");
                        BitmapUtil.saveBitmap(bitmap, iconFile);
                        icon = iconFile.getCanonicalPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("userName", userName);
            json.put("password", password);
            json.put("nickName", nickName);
            json.put("note", note);

            String iconStr = "";
            if(!TextUtils.isEmpty(icon)) {
                Bitmap bitmap = BitmapFactory.decodeFile(icon);
                iconStr = BitmapUtil.bitmapToString(bitmap);
            }
            json.put("iconStr", iconStr);
            return json;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void signUp(Context context, ICodeCallback callback) {
        new AccountAsyncTask(context, "正在注册，请稍等...", CMD_SIGNUP, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                if(code == RETURN_CODE_SUCCESS) {
                    accountId = (Integer) response.getContent();
                    save();
                }
                callback.onFinish(code);
            }
        }).execute(new Account(userName, password));
    }

    public void login(Context context, String showString, ICodeCallback callback) {
        new AccountAsyncTask(context, showString, CMD_LOGIN, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                webLogin = (response.getCode() == RETURN_CODE_SUCCESS);
                callback.onFinish(response.getCode());
            }
        }).execute(this);
    }

    public void remove() {
        /*if(platName.equals(QQ_PLAT_NAME)) {
            Platform plat = ShareSDK.getPlatform(QQ.NAME);
            plat.removeAccount(true);
        } else if(platName.equals(WX_PLAT_NAME)) {
            Platform plat = ShareSDK.getPlatform(Wechat.NAME);
            plat.removeAccount(true);
        } else if(platName.equals(PHONE_PLAT_NAME)) {
            PhoneAccount.removeAccount();
        }

        if(!TextUtils.isEmpty(icon)) {
            try {
                FileUtil.deleteFile(new File(icon));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    @Override
    public void upload(Context context, ICodeCallback callback) {
        new AccountAsyncTask(context, "正在上传账户信息，请稍等...", AccountAsyncTask.CMD_UPLOAD, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                callback.onFinish(response.getCode());
            }
        }).execute(this);
    }

    @Override
    public void download(Context context, ICodeCallback callback) {
        download(context, "正在下载账户信息，请稍等...", callback);
    }

    public void download(Context context, String showString, ICodeCallback callback) {
        new AccountAsyncTask(context, showString, AccountAsyncTask.CMD_DOWNLOAD, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                if (code == RETURN_CODE_SUCCESS) {
                    JSONObject json = (JSONObject) response.getContent();
                    fromJson(json);
                    save();
                }
                callback.onFinish(code);
            }
        }).execute(this);
    }

    @Override
    public void delete(Context context, ICodeCallback callback) {
        throw new IllegalStateException();
    }

    @Override
    public void retrieveList(Context context, int num, String queryStr, long fromTime, ICodeCallback callback) {
        throw new IllegalStateException();
    }

    @NonNull
    @Override
    public String toString() {
        return "UserName: " + getUserName() + ",Password: " + getPassword() + ",NickName：" + nickName + ' '
                + ",Note：" + note + ",icon: " + icon;
    }

    @Override
    public int hashCode() {
        return (userName + password).hashCode();
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(!(otherObject instanceof Account)) return false;
        Account other = (Account) otherObject;
        return (userName + password).equals(other.userName+other.password);
    }
}
