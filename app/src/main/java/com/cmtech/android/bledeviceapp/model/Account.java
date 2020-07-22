package com.cmtech.android.bledeviceapp.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;

import com.cmtech.android.bledeviceapp.interfac.IMyJson;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static com.cmtech.android.bledeviceapp.AppConstant.DIR_IMAGE;

/**
  *
  * ClassName:      Account
  * Description:    Account class
  * Author:         chenm
  * CreateDate:     2018/10/27 上午3:57
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/20 上午3:57
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class Account extends LitePalSupport implements Serializable, IMyJson {
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
    /* download user's web icon to local file system
    public void downloadIcon(final IDownloadUserIconCallback callback) {
        HttpUtils.requestGet(icon, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    byte[] bytes = Objects.requireNonNull(response.body()).bytes();
                    final Bitmap bitmap = BitmapUtil.byteToBitmap(bytes);
                    String fileName = id+".jpg";
                    assert DIR_IMAGE != null;
                    File toFile = FileUtil.getFile(DIR_IMAGE, fileName);
                    BitmapUtil.saveBitmap(bitmap, toFile);
                    localIcon = toFile.getCanonicalPath();
                    save();
                    if(callback != null) {
                        callback.onSuccess(localIcon);
                    }
                }
            }
        });
    }
    */

    @Override
    public boolean setDataFromJson(JSONObject json) throws JSONException {
        if(json == null) {
            return false;
        }

        name = json.getString("name");
        note = json.getString("note");
        String iconStr = json.getString("iconStr");
        if(!TextUtils.isEmpty(iconStr)) {
            Bitmap bitmap = BitmapUtil.byteToBitmap(Base64.decode(iconStr, Base64.DEFAULT));
            if(bitmap != null) {
                assert DIR_IMAGE != null;
                File toFile = FileUtil.getFile(DIR_IMAGE, platName+platId + ".jpg");
                BitmapUtil.saveBitmap(bitmap, toFile);
                try {
                    icon = toFile.getCanonicalPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return save();
    }

    @Override
    public JSONObject toJson() throws JSONException {
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

    /*public interface IDownloadUserIconCallback {
        void onSuccess(String iconFilePath);
    }*/
}
