package com.cmtech.android.bledeviceapp.model;

import static com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask.CMD_CHANGE_PASSWORD;
import static com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask.CMD_LOGIN;
import static com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask.CMD_SIGNUP;
import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_IMAGE;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.asynctask.AccountAsyncTask;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.vise.log.ViseLog;
import com.vise.utils.cipher.BASE64;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
  *
  * ClassName:      Account
  * Description:    账户类,代表当前登录app的账户
  * Author:         chenm
  * CreateDate:     2018/10/27 上午3:57
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/20 上午3:57
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class Account implements Serializable, IJsonable, IWebOperation {
    //--------------------------------------------------------常量
    public static final int MALE = 1; // 男性
    public static final int FEMALE = 2; // 女性

    //------------------------------------------------------- 实例变量
    // 账户ID号
    private int accountId = INVALID_ID;

    // 用户名，比如手机号之类的
    private String userName = "";

    // 密码
    private String password = "";

    // 昵称
    private String nickName = "";

    // 备注
    private String note = "";

    // 头像图片在本地磁盘上的文件路径名
    private String icon = "";

    // 性别
    private int gender = MALE;

    // 生日
    private long birthday = 0;

    // 体重
    private int weight = 0;

    // 身高
    private int height = 0;

    // 该账号是否成功完成网络登录操作
    private boolean isWebLoginSuccess = false;

    // 分享信息列表
    private List<ShareInfo> shareInfos = new ArrayList<>();

    // 联系人列表
    // 账户的联系人包括两类人：第一类、对方申请分享给你的人；第二类、你申请分享给对方，且对方同意了的人。
    private List<ContactPerson> contactPeople = new ArrayList<>();

    //--------------------------------------------------------------静态函数
    // 从shared preference创建账户
    public static Account createFromSharedPreference() {
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        Account account = new Account();
        account.accountId = settings.getInt("accountId", INVALID_ID);
        account.userName = settings.getString("userName", "");
        account.password = settings.getString("password", "");
        account.nickName = settings.getString("nickName", "");
        account.note = settings.getString("note", "");
        account.icon = settings.getString("icon", "");
        account.gender = settings.getInt("gender", MALE);
        account.birthday = settings.getLong("birthday", new Date(1990,0,1).getTime());
        account.weight = settings.getInt("weight", 80);
        account.height = settings.getInt("height", 180);
        return account;
    }

    //--------------------------------------------------私有构造器
    // 私有构造器，只能通过shared preference创建账户
    private Account() {
    }

    // 获取账户ID
    public int getAccountId() {
        return accountId;
    }

    // 设置账户ID，同时写入shared preference
    public void setAccountId(int accountId) {
        this.accountId = accountId;
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("accountId", this.accountId);
        editor.commit();
    }

    // 获取用户名
    public String getUserName() { return userName;}

    // 设置用户名
    public void setUserName(String userName) {
        this.userName = userName.trim();
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("userName", this.userName);
        editor.commit();
    }

    // 获取密码
    public String getPassword() {
        return password;
    }

    // 设置密码
    public void setPassword(String password) {
        this.password = password.trim();
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("password", this.password);
        editor.commit();
    }

    // 获取昵称
    public String getNickName() {
        return nickName;
    }

    // 设置昵称
    public void setNickName(String nickName) {
        this.nickName = nickName.trim();
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("nickName", this.nickName);
        editor.commit();
    }

    // 获取昵称，如果为空字符串，则返回ID号
    public String getNickNameOrUserIdIfNull() {
        if("".equals(nickName)) {
            return "ID:"+accountId;
        }
        return nickName;
    }

    // 获取备注
    public String getNote() {
        return note;
    }

    // 设置备注
    public void setNote(String note) {
        this.note = note.trim();
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("note", this.note);
        editor.commit();
    }

    // 获取头像图标文件路径名
    public String getIcon() {
        return icon;
    }

    // 设置头像图标文件路径名
    public void setIcon(String icon) {
        this.icon = icon.trim();
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("icon", this.icon);
        editor.commit();
    }

    // 获取该账户是否网络登录成功
    public boolean isWebLoginSuccess() {
        return isWebLoginSuccess;
    }

    // 设置该账户是否网络登录成功
    public void setWebLoginSuccess(boolean isWebLoginSuccess) {
        this.isWebLoginSuccess = isWebLoginSuccess;
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

    // 设置个人信息
    public void setPersonInfo(int gender, long birthday, int weight, int height) {
        if(gender != MALE && gender != FEMALE)
            throw new IllegalStateException();

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

    // 从本地shared preference删除账户信息及其附属文件
    public void removeFromLocal() {
        SharedPreferences preferences = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();

        LitePal.deleteAll(ShareInfo.class);
        LitePal.deleteAll(ContactPerson.class);

        if(!TextUtils.isEmpty(icon)) {
            try {
                FileUtil.deleteFile(new File(icon));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 获取分享信息列表
    public List<ShareInfo> getShareInfoList() {
        return shareInfos;
    }

    // 从本地数据库中读取账户的分享信息列表,并按照分享状态排序
    public void readShareInfosFromLocalDb() {
        List<ShareInfo> shareInfos = new ArrayList<>(LitePal.where("fromId = ? or toId = ?",
                "" + accountId, "" + accountId).find(ShareInfo.class));
        Collections.sort(shareInfos, new Comparator<ShareInfo>() {
            @Override
            public int compare(ShareInfo o1, ShareInfo o2) {
                int rlt = 0;
                if(o2.getStatus() > o1.getStatus()) rlt = 1;
                else if(o2.getStatus() < o1.getStatus()) rlt = -1;
                return rlt;
            }
        });
        this.shareInfos = shareInfos;
    }

    // 从服务器下载账户的分享信息。下载成功后，更新本地数据库相关信息,以及更新shareInfos字段
    public void downloadShareInfos(Context context, String showStr, ICodeCallback callback) {
        new AccountAsyncTask(context, showStr, AccountAsyncTask.CMD_DOWNLOAD_SHARE_INFO, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                if(code == RETURN_CODE_SUCCESS) {
                    JSONArray jsonArr = (JSONArray) response.getContent();
                    if(jsonArr != null) {
                        List<ShareInfo> sis = new ArrayList<>();
                        for (int i = 0; i < jsonArr.length(); i++) {
                            try {
                                JSONObject json = (JSONObject) jsonArr.get(i);
                                if (json == null) continue;
                                ShareInfo si = new ShareInfo();
                                si.fromJson(json);
                                sis.add(si);
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }
                        LitePal.deleteAll(ShareInfo.class);
                        LitePal.saveAll(sis);
                        readShareInfosFromLocalDb();
                    }
                }
                if(callback != null)
                    callback.onFinish(code);
            }
        }).execute(this);
    }

    // 获取可以分享给对方的账户ID列表
    public List<Integer> getCanShareIdList() {
        List<Integer> ids = new ArrayList<>();
        for(ShareInfo si : shareInfos) {
            if(si.getFromId() == accountId && si.getStatus() == ShareInfo.AGREE) {
                ids.add(si.getToId());
            }
        }
        return ids;
    }

    // 是否可以分享给指定ID人
    public boolean canShareTo(int shareId) {
        return getCanShareIdList().contains(shareId);
    }

    public List<ContactPerson> getContactPeople() {
        return contactPeople;
    }

    // 从本地数据库读取联系人信息
    public void readContactPeopleFromLocalDb() {
        this.contactPeople = new ArrayList<>(LitePal.findAll(ContactPerson.class));
    }

    public ContactPerson getContactPerson(int id) {
        for(ContactPerson cp : contactPeople) {
            if(cp.getAccountId() == id) return cp;
        }
        return null;
    }

    // 从分享列表中提取联系人ID列表
    public List<Integer> extractContactPeopleIdsFromShareInfos() {
        List<Integer> cps = new ArrayList<>();
        for(ShareInfo si : shareInfos) {
            if(si.getToId() == accountId) {
                cps.add(si.getFromId());
            } else if(si.getFromId() == accountId && si.getStatus() == ShareInfo.AGREE) {
                cps.add(si.getToId());
            }
        }
        return cps;
    }

    // 下载contactId指定的账户ID号的联系人信息，并保存到本地数据库中
    public void downloadContactPerson(Context context, String showStr, int contactId, ICodeCallback callback) {
        new AccountAsyncTask(context, showStr, AccountAsyncTask.CMD_DOWNLOAD_CONTACT_PERSON, new Object[]{contactId}, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                if (code == RETURN_CODE_SUCCESS) {
                    JSONObject content = (JSONObject) response.getContent();
                    try {
                        ContactPerson contactPerson = new ContactPerson();
                        contactPerson.fromJson(content);
                        contactPerson.saveOrUpdate("accountId = ?",
                                ""+contactPerson.getAccountId());
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                        code = RETURN_CODE_DATA_ERR;
                    }
                }
                if(callback != null)
                    callback.onFinish(code);
            }
        }).execute(this);
    }

    // 账户注册
    public void signUp(Context context, ICodeCallback callback) {
        new AccountAsyncTask(context, "正在注册，请稍等...", CMD_SIGNUP, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                if(callback != null)
                    callback.onFinish(response.getCode());
            }
        }).execute(this);
    }

    // 账户登录
    public void login(Context context, String showString, ICodeCallback callback) {
        new AccountAsyncTask(context, showString, CMD_LOGIN, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                if(code == RETURN_CODE_SUCCESS) {
                    JSONObject content = (JSONObject) response.getContent();
                    if(content == null)
                        code = RETURN_CODE_DATA_ERR;
                    else {
                        try {
                            setAccountId(content.getInt("id"));
                            if (accountId != INVALID_ID) {
                                setWebLoginSuccess(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = RETURN_CODE_DATA_ERR;
                        }
                    }
                }
                if(callback != null)
                    callback.onFinish(code);
            }
        }).execute(this);
    }

    // 修改密码
    public void changePassword(Context context, ICodeCallback callback) {
        new AccountAsyncTask(context, "正在修改密码，请稍等...", CMD_CHANGE_PASSWORD, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                callback.onFinish(response.getCode());
            }
        }).execute(this);
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
            json.put("nickName", nickName);
            json.put("note", note);

            String iconStr = "";
            if(!TextUtils.isEmpty(icon)) {
                Bitmap bitmap = BitmapFactory.decodeFile(icon);
                iconStr = BitmapUtil.bitmapToString(bitmap);
                ViseLog.e("icon len:" + iconStr.length());
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
    public void download(Context context, String showStr, ICodeCallback callback) {
        new AccountAsyncTask(context, showStr, AccountAsyncTask.CMD_DOWNLOAD, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                if (code == RETURN_CODE_SUCCESS) {
                    JSONObject content = (JSONObject) response.getContent();
                    if(content != null) {
                        fromJson(content);
                        saveToSharedPreference();
                    }
                }
                if(callback != null)
                    callback.onFinish(code);
            }
        }).execute(this);
    }

    // 不支持删除网络服务器端的账户信息
    @Override
    public void delete(Context context, ICodeCallback callback) {
        throw new IllegalStateException();
    }

    @NonNull
    @Override
    public String toString() {
        return "AccountId: " + accountId + ",UserName: " + userName + ",Password: " + password + ",NickName：" + nickName + ' '
                + ",gender:" + gender + ",birthday:" + birthday + ",weight:" + weight + ",height:" + height
                + ",Note：" + note + ",icon: " + icon + ",isWebLoginSuccess:" + isWebLoginSuccess
                + ", shareInfos: " + shareInfos + ", contactPeople: " + contactPeople;
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

    private void saveToSharedPreference() {
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
