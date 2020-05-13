package com.cmtech.android.bledeviceapp.model;

import android.graphics.Bitmap;

import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledeviceapp.AppConstant.DIR_IMAGE;

/**
  *
  * ClassName:      User
  * Description:    User class
  * Author:         chenm
  * CreateDate:     2018/10/27 上午3:57
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/20 上午3:57
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class User extends LitePalSupport implements Serializable{
    private int id; // id
    private String platName = ""; // platform name
    private String platId = ""; // platform ID
    private String name = ""; // name
    private String icon = ""; // web address of icon
    private String note = ""; // note
    private String localIcon = ""; // local file address of icon

    public User() {
    }

    public User(String platName, String platId, String name, String icon) {
        this.platName = platName;
        this.platId = platId;
        this.name = name;
        this.icon = icon;
    }

    public User(User user) {
        this.platName = user.platName;
        this.platId = user.platId;
        this.name = user.name;
        this.icon = user.icon;
        this.note = user.note;
        this.localIcon = user.localIcon;
    }

    public int getId() {
        return id;
    }
    public String getPlatName() { return platName;}
    public String getPlatId() {
        return platId;
    }
    public String getShortPlatId() {
        if(platId.length() > 3) {
            return String.format("%s****%s", platId.substring(0, 3), platId.substring(platId.length() - 3));
        } else
            return platId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public String getLocalIcon() {
        return localIcon;
    }
    public void setLocalIcon(String localIcon) {
        this.localIcon = localIcon;
    }
    // download user's web icon to local file system
    public void downloadIcon(final IDownloadLocalIcon callback) {
        HttpUtils.requestGet(icon, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        byte[] bytes = response.body().bytes();
                        final Bitmap bitmap = BitmapUtil.byteToBitmap(bytes);
                        String fileName = id+".jpg";
                        File toFile = FileUtil.getFile(DIR_IMAGE, fileName);
                        BitmapUtil.saveBitmap(bitmap, toFile);
                        localIcon = toFile.getCanonicalPath();
                        save();
                        if(callback != null) {
                            callback.onSuccess(localIcon);
                        }
                    }
                }
            }
        });

    }

    @Override
    public String toString() {
        return "Plat: " + getPlatName() + " Id: " + getShortPlatId() + " Name：" + name + ' '
                + " Icon：" + icon + " Note：" + note + " localIcon: " + localIcon;
    }

    @Override
    public int hashCode() {
        return (platName + platId).hashCode();
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(!(otherObject instanceof User)) return false;
        User other = (User) otherObject;
        return (platName + platId).equals(other.platName+other.platId);
    }

    public interface IDownloadLocalIcon {
        void onSuccess(String localIcon);
    }
}
