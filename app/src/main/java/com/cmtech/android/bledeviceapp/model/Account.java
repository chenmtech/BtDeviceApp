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
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_IMAGE;
import static com.cmtech.android.bledeviceapp.global.AppConstant.PHONE_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.global.AppConstant.QQ_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.global.AppConstant.WX_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask.CMD_SIGNUP_OR_LOGIN;

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
    private String platName = ""; // platform name
    private String platId = ""; // platform ID
    private String name = ""; // name
    private String note = ""; // note
    private String icon = ""; // icon file path in local disk

    public Account() {
    }

    public Account(String platName, String platId, String name, String note, String icon) {
        this.platName = platName;
        this.platId = platId;
        this.name = name;
        this.note = note;
        this.icon = icon;
    }

    public Account(Account account) {
        this.platName = account.platName;
        this.platId = account.platId;
        this.name = account.name;
        this.note = account.note;
        this.icon = account.icon;
    }

    public int getId() {
        return id;
    }
    public String getPlatName() { return platName;}
    public String getPlatId() {
        return platId;
    }
    public String getName() {
        if("".equals(name)) {
            String tmp = platId.substring(platId.length()/4, platId.length()*3/4);
            return platId.replaceFirst(tmp, "*");
        }
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
            name = json.getString("name");
            note = json.getString("note");
            String iconStr = json.getString("iconStr");
            if (!TextUtils.isEmpty(iconStr)) {
                Bitmap bitmap = BitmapUtil.byteToBitmap(Base64.decode(iconStr, Base64.DEFAULT));
                if (bitmap != null) {
                    try {
                        assert DIR_IMAGE != null;
                        File iconFile = FileUtil.getFile(DIR_IMAGE, platName + platId + ".jpg");
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
            json.put("platName", platName);
            json.put("platId", platId);
            json.put("name", name);
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

    public void signupOrLogin(Context context, ICodeCallback callback) {
        new AccountAsyncTask(context, null, CMD_SIGNUP_OR_LOGIN, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                callback.onFinish(response.getCode());
            }
        });
    }

    public void logout() {
        if(platName.equals(QQ_PLAT_NAME)) {
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
        }
    }

    @Override
    public void upload(Context context, ICodeCallback callback) {
        new AccountAsyncTask(context, "请稍等...", AccountAsyncTask.CMD_UPLOAD, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                callback.onFinish(response.getCode());
            }
        }).execute(this);
    }

    @Override
    public void download(Context context, ICodeCallback callback) {
        download(context, "请稍等...", callback);
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

    }

    @Override
    public void retrieveList(Context context, int num, String queryStr, long fromTime, ICodeCallback callback) {

    }

    @NonNull
    @Override
    public String toString() {
        return "PlatName: " + getPlatName() + ",PlatId: " + getPlatId() + ",Name：" + name + ' '
                + ",Note：" + note + ",icon: " + icon;
    }

    @Override
    public int hashCode() {
        return (platName + platId).hashCode();
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(!(otherObject instanceof Account)) return false;
        Account other = (Account) otherObject;
        return (platName + platId).equals(other.platName+other.platId);
    }
}
