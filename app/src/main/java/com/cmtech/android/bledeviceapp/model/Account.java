package com.cmtech.android.bledeviceapp.model;

import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_IMAGE;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.model.ContactPerson.AGREE;
import static com.cmtech.android.bledeviceapp.model.ContactPerson.WAITING;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_ADD_CONTACT;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_AGREE_CONTACT;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_RESET_PASSWORD;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_DELETE_CONTACT;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_DOWNLOAD_ACCOUNT;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_DOWNLOAD_CONTACT_ACCOUNT_INFO;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_DOWNLOAD_CONTACT_INFO;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_LOGIN;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_SIGNUP;
import static com.cmtech.android.bledeviceapp.util.WebService11Util.CMD_UPLOAD_ACCOUNT;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
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
import java.util.Calendar;
import java.util.List;

/**
  *
  * ClassName:      Account
  * Description:    账户类,代表当前登录的账户
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

    // 联系人列表
    // 账户的联系人包括两类人：第一类、对方申请了等待你批准的；第二类、双方已经同意建立联系的。
    private final List<ContactPerson> contacts = new ArrayList<>();

    //--------------------------------------------------------------静态函数
    // 从shared preference创建账户
    public static Account createFromSharedPreference() {
        SharedPreferences settings = MyApplication.getContext().getSharedPreferences("Account", Context.MODE_PRIVATE);
        Account account = new Account();
        account.accountId = settings.getInt("accountId", INVALID_ID);
        account.userName = settings.getString("userName", "");
        account.password = settings.getString("password", "");
        account.nickName = settings.getString("nickName", "匿名");
        account.note = settings.getString("note", "");
        account.icon = settings.getString("icon", "");
        account.gender = settings.getInt("gender", MALE);
        Calendar defaultBirth = Calendar.getInstance();
        defaultBirth.clear();
        defaultBirth.set(1990,0,1);
        account.birthday = settings.getLong("birthday", defaultBirth.getTimeInMillis());
        account.weight = settings.getInt("weight", 80);
        account.height = settings.getInt("height", 180);
        return account;
    }

    //--------------------------------------------------私有构造器
    // 私有构造器，只能通过createFromSharedPreference()来创建账户
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
    public String getNickNameOrId() {
        if(TextUtils.isEmpty(nickName)) {
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

    // 设置账户个人隐私信息
    public void setAccountPrivateInfos(int gender, long birthday, int weight, int height) {
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

        LitePal.deleteAll(ContactPerson.class);

        if(!TextUtils.isEmpty(icon)) {
            try {
                FileUtil.deleteFile(new File(icon));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 获取已经同意的联系人的账户ID列表
    public List<Integer> getAgreedContactIdList() {
        List<Integer> ids = new ArrayList<>();
        for(ContactPerson cp : contacts) {
            if(cp.getStatus() == AGREE) {
                ids.add(cp.getAccountId());
            }
        }
        return ids;
    }

    // 获取已经同意的联系人列表
    public List<ContactPerson> getAgreedContactList() {
        List<ContactPerson> cps = new ArrayList<>();
        for(ContactPerson cp : contacts) {
            if(cp.getStatus() == AGREE) {
                cps.add(cp);
            }
        }
        return cps;
    }

    // 获取等待批准的联系人的账户ID列表
    public List<Integer> getWaitingContactIdList() {
        List<Integer> ids = new ArrayList<>();
        for(ContactPerson cp : contacts) {
            if(cp.getStatus() == WAITING) {
                ids.add(cp.getAccountId());
            }
        }
        return ids;
    }

    // 返回联系人列表
    public List<ContactPerson> getContacts() {
        return contacts;
    }

    // 获取指定账户ID号对应的联系人对象
    public ContactPerson getContactPerson(int id) {
        for(ContactPerson cp : contacts) {
            if(cp.getAccountId() == id) return cp;
        }
        return null;
    }

    // 从本地数据库读取联系人，按状态排序
    public void readContactFromLocalDb() {
        this.contacts.clear();
        this.contacts.addAll(LitePal.order("status desc").find(ContactPerson.class));
    }

    // 账户注册
    public void signUp(Context context, ICodeCallback callback) {
        new WebServiceAsyncTask(context, "正在注册，请稍等...", CMD_SIGNUP, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                if(callback != null)
                    callback.onFinish(response.getCode(), response.getMsg());
            }
        }).execute(this);
    }

    // 账户登录
    public void login(Context context, ICodeCallback callback) {
        new WebServiceAsyncTask(context, "正在登录，请稍等...", CMD_LOGIN, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                String msg = response.getMsg();
                if(code == RCODE_SUCCESS) {
                    JSONObject content = (JSONObject) response.getContent();
                    if(content == null) {
                        code = RCODE_DATA_ERR;
                        msg = "数据错误";
                    }
                    else {
                        try {
                            setAccountId(content.getInt("id"));
                            setWebLoginSuccess(accountId != INVALID_ID);
                        } catch (JSONException e) {
                            code = RCODE_DATA_ERR;
                            msg = "数据错误";
                        }
                    }
                }
                if(callback != null)
                    callback.onFinish(code, msg);
            }
        }).execute(this);
    }

    // 重置密码
    public void resetPassword(Context context, ICodeCallback callback) {
        new WebServiceAsyncTask(context, "正在重置密码，请稍等...", CMD_RESET_PASSWORD, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                if(callback != null)
                    callback.onFinish(response.getCode(), response.getMsg());
            }
        }).execute(this);
    }

    // 下载联系人列表，并更新到本地数据库中
    public void downloadContactInfo(Context context, String showStr, ICodeCallback callback) {
        new WebServiceAsyncTask(context, showStr, CMD_DOWNLOAD_CONTACT_INFO, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                String msg = response.getMsg();
                if(code == RCODE_SUCCESS) {
                    JSONArray jsonArr = (JSONArray) response.getContent();
                    if(jsonArr != null) {
                        List<ContactPerson> revCps = new ArrayList<>();
                        for (int i = 0; i < jsonArr.length(); i++) {
                            try {
                                JSONObject json = (JSONObject) jsonArr.get(i);
                                if (json == null) continue;
                                int fromId = json.getInt("fromId");
                                int toId = json.getInt("toId");
                                int status = json.getInt("status");
                                int contactId = (fromId == accountId) ? toId : fromId;
                                ContactPerson newCp = new ContactPerson(contactId, status);
                                revCps.add(newCp);
                                ContactPerson cpFind = LitePal.where("accountId = ?", ""+contactId)
                                        .findFirst(ContactPerson.class);
                                if(cpFind == null) // 一个新的联系人，保存
                                    newCp.save();
                                else { // 已有联系人，只需要改变状态，其他不要改变
                                    cpFind.setStatus(status);
                                    cpFind.save();
                                }
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }
                        // 移除在接收到的联系人列表中不存在的联系人，这些是被删除的联系人
                        List<ContactPerson> curCps = LitePal.findAll(ContactPerson.class);
                        for(ContactPerson cp : curCps) {
                            if(!revCps.contains(cp)) {
                                cp.delete();
                            }
                        }
                        readContactFromLocalDb();
                    }
                }
                if(callback != null)
                    callback.onFinish(code, msg);
            }
        }).execute(this);
    }

    // 下载所有联系人的账户信息（不包含隐私信息），并保存到本地数据库中
    public void downloadContactAccountInfo(Context context, String showStr, ICodeCallback callback) {
        List<Integer> contactIds = new ArrayList<>();
        for(ContactPerson cp : contacts) {
            contactIds.add(cp.getAccountId());
        }
        new WebServiceAsyncTask(context, showStr, CMD_DOWNLOAD_CONTACT_ACCOUNT_INFO, new Object[]{contactIds}, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                String msg = response.getMsg();
                if(code == RCODE_SUCCESS) {
                    JSONArray jsonArr = (JSONArray) response.getContent();
                    if(jsonArr != null) {
                        for (int i = 0; i < jsonArr.length(); i++) {
                            try {
                                JSONObject json = (JSONObject) jsonArr.get(i);
                                if (json == null) continue;
                                int accountId = json.getInt("accountId");
                                ContactPerson cpFind = LitePal.where("accountId = ?", ""+accountId)
                                        .findFirst(ContactPerson.class);
                                if(cpFind == null) continue;
                                cpFind.fromJson(json);
                                cpFind.save();
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }
                        readContactFromLocalDb();
                    }
                }
                if(callback != null)
                    callback.onFinish(code, msg);
            }
        }).execute(this);
    }


    // 申请添加一个联系人
    public void addNewContact(Context context, int contactId, ICodeCallback callback) {
        if(accountId == contactId) {
            Toast.makeText(context, "不能添加自己", Toast.LENGTH_SHORT).show();
            return;
        }

        // 已经同意了
        if(getAgreedContactIdList().contains(contactId)) {
            Toast.makeText(context, "不能重复添加", Toast.LENGTH_SHORT).show();
            return;
        }

        // 对方已经申请了，直接同意
        if(getWaitingContactIdList().contains(contactId)) {
            agreeContact(context, contactId, callback);
            return;
        }

        new WebServiceAsyncTask(context, "请稍等", CMD_ADD_CONTACT,
                new Object[]{contactId}, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                if(callback!=null) {
                    callback.onFinish(response.getCode(), response.getMsg());
                }
            }
        }).execute(this);
    }

    // 批准一个联系人的申请
    public void agreeContact(Context context, int contactId, ICodeCallback callback) {
        new WebServiceAsyncTask(context, "请稍等", CMD_AGREE_CONTACT, new Object[]{contactId}, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                if(callback!=null)
                    callback.onFinish(response.getCode(), response.getMsg());
            }
        }).execute(this);
    }

    // 删除一个联系人
    public void deleteContact(Context context, int contactId, ICodeCallback callback) {
        new WebServiceAsyncTask(context, "请稍等", CMD_DELETE_CONTACT, new Object[]{contactId}, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                if(callback!=null)
                    callback.onFinish(response.getCode(), response.getMsg());
            }
        }).execute(this);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
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
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = new JSONObject();
        json.put("ver", "1.0");
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
    }

    @Override
    public void upload(Context context, ICodeCallback callback) {
        new WebServiceAsyncTask(context, "正在上传，请稍等...", CMD_UPLOAD_ACCOUNT, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                if(callback!=null)
                    callback.onFinish(response.getCode(), response.getMsg());
            }
        }).execute(this);
    }

    @Override
    public void download(Context context, String showStr, ICodeCallback callback) {
        new WebServiceAsyncTask(context, showStr, CMD_DOWNLOAD_ACCOUNT, new IWebResponseCallback() {
            @Override
            public void onFinish(WebResponse response) {
                int code = response.getCode();
                String msg = response.getMsg();
                if (code == RCODE_SUCCESS) {
                    JSONObject content = (JSONObject) response.getContent();
                    if(content != null) {
                        try {
                            fromJson(content);
                            saveToSharedPreference();
                        } catch (JSONException ex) {
                            code = RCODE_DATA_ERR;
                            msg = "数据错误";
                        }
                    }
                }
                if(callback != null)
                    callback.onFinish(code, msg);
            }
        }).execute(this);
    }

    // 不支持删除服务器端的账户
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
                + ", contactPeople: " + contacts;
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
