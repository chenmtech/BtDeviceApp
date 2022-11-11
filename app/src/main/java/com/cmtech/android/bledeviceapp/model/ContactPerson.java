package com.cmtech.android.bledeviceapp.model;

import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_IMAGE;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.vise.log.ViseLog;
import com.vise.utils.cipher.BASE64;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
  *
  * ClassName:      ContactPerson
  * Description:    联系人
  * Author:         chenm
  * CreateDate:     2022/10/28 上午3:57
  * UpdateUser:     chenm
  * UpdateDate:     2022/10/28 上午3:57
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class ContactPerson extends LitePalSupport implements IJsonable {
    //---------------------------------------------------------静态常量
    // 联系人的两个状态。
    public static final int WAITING = 0; // 当对方在申请成为你的联系人，而你还没有批准时，状态为WAITING；
    public static final int AGREE = 1; // 当双方都同意成为联系人时，则状态为AGREE

    private int id;

    // 账户ID
    private int accountId;

    // 申请的状态
    private int status;

    // 昵称
    private String nickName = "";

    // 简介
    private String note = "";

    // 头像图标文件本地路径名
    private String icon = "";

    //------------------------------------------------------构造器

    /**
     *
     * @param accountId 联系人的账户ID
     * @param status 联系人的状态
     */
    public ContactPerson(int accountId, int status) {
        this.accountId = accountId;
        this.status = status;
    }

    //------------------------------------------------------实例方法
    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName.trim();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note.trim();
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon.trim();
    }

    @Override
    public void fromJson(JSONObject json)  throws JSONException {
        //accountId = json.getInt("accountId");
        nickName = json.getString("nickName");
        note = json.getString("note");
        String iconStr = json.getString("iconStr");
        if (!TextUtils.isEmpty(iconStr)) {
            Bitmap bitmap = BitmapUtil.byteToBitmap(BASE64.decode(iconStr));
            if (bitmap != null) {
                try {
                    assert DIR_IMAGE != null;
                    File iconFile = FileUtil.getFile(DIR_IMAGE, "CP"+accountId + ".jpg");
                    BitmapUtil.saveBitmap(bitmap, iconFile);
                    icon = iconFile.getCanonicalPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public JSONObject toJson()  throws JSONException{
        throw new IllegalStateException("can't access toJson().");
    }

    // 重载了LitePalSupport中的delete，增加了删除头像图标文件的操作
    @Override
    public int delete() {
        int rows = super.delete();
        if(!TextUtils.isEmpty(icon)) {
            try {
                FileUtil.deleteFile(new File(icon));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rows;
    }

    @NonNull
    @Override
    public String toString() {
        return "AccountId: " + accountId + ",NickName：" + nickName + ' '
                + ",Note：" + note + ",icon: " + icon + ",status: " + status;
    }

    @Override
    public int hashCode() {
        return accountId;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(!(otherObject instanceof ContactPerson)) return false;
        ContactPerson other = (ContactPerson) otherObject;
        return accountId == other.accountId;
    }
}
