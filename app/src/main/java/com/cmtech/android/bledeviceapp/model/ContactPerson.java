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

public class ContactPerson extends LitePalSupport implements Serializable, IJsonable {
    private int id;
    private int accountId = INVALID_ID;
    private String nickName = ""; // nick name
    private String note = ""; // note
    private String icon = ""; // icon file path in local disk

    public ContactPerson() {
    }

    public int getAccountId() {
        return accountId;
    }
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
    public String getNickName() {
        if(nickName.equals("")) {
            return "ID："+accountId;
        } else {
            return nickName;
        }
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
    public void fromJson(JSONObject json)  throws JSONException {
        accountId = json.getInt("accountId");
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
        JSONObject json = new JSONObject();
        json.put("accountId", accountId);
        json.put("nickName", nickName);
        json.put("note", note);

        String iconStr = "";
        if(!TextUtils.isEmpty(icon)) {
            Bitmap bitmap = BitmapFactory.decodeFile(icon);
            iconStr = BitmapUtil.bitmapToString(bitmap);
            ViseLog.e("icon len:" + iconStr.length());
        }
        json.put("iconStr", iconStr);
        return json;
    }

    public void remove() {
        if(!TextUtils.isEmpty(icon)) {
            try {
                FileUtil.deleteFile(new File(icon));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "AccountId: " + accountId + ",NickName：" + nickName + ' '
                + ",Note：" + note + ",icon: " + icon;
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