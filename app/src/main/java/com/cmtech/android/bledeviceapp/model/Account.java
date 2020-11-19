package com.cmtech.android.bledeviceapp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.vise.utils.cipher.BASE64;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import org.json.JSONException;
import org.json.JSONObject;

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

public class Account implements Serializable, IJsonable, IWebOperation {
    public static final int MALE = 1;
    public static final int FEMALE = 2;

    private int accountId = INVALID_ID;
    private String userName = ""; // user name, like phone number
    private String password = ""; // password
    private String nickName = ""; // nick name
    private String note = ""; // note
    private String icon = ""; // icon file path in local disk
    private int gender = 0;
    private long birthday = 0;
    private int weight = 0;
    private int height = 0;
    private boolean needWebLogin = true;

    private Account() {
    }

    public int getAccountId() {
        return accountId;
    }
    public void setAccountId(int accountId) {
        this.accountId = accountId;
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("accountId", accountId);
        editor.commit();
    }
    public String getUserName() { return userName;}
    public void setUserName(String userName) {
        this.userName = userName;
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("userName", userName);
        editor.commit();
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("password", this.password);
        editor.commit();
    }
    public String getNickName() {
        return nickName;
    }
    public String getNickNameOrUserName() {
        if("".equals(nickName)) {
            return userName;
        }
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("nickName", nickName);
        editor.commit();
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("note", note);
        editor.commit();
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("icon", icon);
        editor.commit();
    }
    public boolean isNeedWebLogin() {
        return needWebLogin;
    }
    public void setNeedWebLogin(boolean needWebLogin) {
        this.needWebLogin = needWebLogin;
    }

    public int getGender() {
        return gender;
    }

    public long getBirthday() {
        return birthday;
    }

    public int getWeight() {
        return weight;
    }

    public int getHeight() {
        return height;
    }

    public void setPersonInfo(int gender, long birthday, int weight, int height) {
        this.gender = gender;
        this.birthday = birthday;
        this.weight = weight;
        this.height = height;
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("gender", gender);
        editor.putLong("birthday", birthday);
        editor.putInt("weight", weight);
        editor.putInt("height", height);
        editor.commit();
    }

    public boolean notSetPersonInfo() {
        return gender == 0;
    }

    @Override
    public void fromJson(JSONObject json) {
        try {
            nickName = json.getString("nickName");
            note = json.getString("note");
            String iconStr = json.getString("iconStr");
            if (!TextUtils.isEmpty(iconStr)) {
                Bitmap bitmap = BitmapUtil.byteToBitmap(BASE64.decode(iconStr));
                if (bitmap != null) {
                    try {
                        assert DIR_IMAGE != null;
                        File iconFile = FileUtil.getFile(DIR_IMAGE, userName + ".jpg");
                        BitmapUtil.saveBitmap(bitmap, iconFile);
                        icon = iconFile.getCanonicalPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            gender = json.getInt("gender");
            birthday = json.getLong("birthday");
            weight = json.getInt("weight");
            height = json.getInt("height");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("ver", "1.0");
            //json.put("userName", userName);
            json.put("password", password);
            json.put("nickName", nickName);
            json.put("note", note);

            String iconStr = "";
            if(!TextUtils.isEmpty(icon)) {
                Bitmap bitmap = BitmapFactory.decodeFile(icon);
                iconStr = BitmapUtil.bitmapToString(bitmap);
            }
            json.put("iconStr", iconStr);
            json.put("gender", gender);
            json.put("birthday", birthday);
            json.put("weight", weight);
            json.put("height", height);
            return json;
        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void signUp(Context context, ICodeCallback callback) {
        //ViseLog.e("account signup");
        new AccountAsyncTask(context, "正在注册，请稍等...", CMD_SIGNUP, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                callback.onFinish(response.getCode());
            }
        }).execute(this);
    }

    public void login(Context context, String showString, ICodeCallback callback) {
        new AccountAsyncTask(context, showString, CMD_LOGIN, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                JSONObject content = (JSONObject) response.getContent();
                if(code == RETURN_CODE_SUCCESS) {
                    if(content == null)
                        code = RETURN_CODE_DATA_ERR;
                    else {
                        try {
                            setAccountId(content.getInt("id"));
                            //ViseLog.e("accountId=" + accountId);
                            if (accountId != INVALID_ID) {
                                setNeedWebLogin(false);
                                //ViseLog.e(Account.this);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = RETURN_CODE_DATA_ERR;
                        }
                    }
                }
                callback.onFinish(code);
            }
        }).execute(this);
    }

    public void remove() {
        SharedPreferences preferences = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();

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
                //ViseLog.e(response);
                int code = response.getCode();
                JSONObject content = (JSONObject) response.getContent();
                if (code == RETURN_CODE_SUCCESS) {
                    fromJson(content);
                    writeToSharedPreference();
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
        return "AccountId: " + accountId + ",UserName: " + userName + ",Password: " + password + ",NickName：" + nickName + ' '
                + ",gender:" + gender + ",birthday:" + birthday + ",weight:" + weight + ",height:" + height
                + ",Note：" + note + ",icon: " + icon +",needWebLogin:" + needWebLogin;
    }

    @Override
    public int hashCode() {
        return userName.hashCode();
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(!(otherObject instanceof Account)) return false;
        Account other = (Account) otherObject;
        return userName.equals(other.userName);
    }

    public static Account readFromSharedPreference() {
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", 0);
        Account account = new Account();
        account.accountId = settings.getInt("accountId", INVALID_ID);
        account.userName = settings.getString("userName", "");
        account.password = settings.getString("password", "");
        account.nickName = settings.getString("nickName", "");
        account.note = settings.getString("note", "");
        account.icon = settings.getString("icon", "");
        account.gender = settings.getInt("gender", 0);
        account.birthday = settings.getLong("birthday", 0);
        account.weight = settings.getInt("weight", 0);
        account.height = settings.getInt("height", 0);
        return account;
    }

    public void writeToSharedPreference() {
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("accountId", accountId);
        editor.putString("userName", userName);
        editor.putString("password", password);
        editor.putString("nickName", nickName);
        editor.putString("note", note);
        editor.putString("icon", icon);
        editor.putInt("gender", gender);
        editor.putLong("birthday", birthday);
        editor.putInt("weight", weight);
        editor.putInt("height", height);
        editor.commit();
    }
}
