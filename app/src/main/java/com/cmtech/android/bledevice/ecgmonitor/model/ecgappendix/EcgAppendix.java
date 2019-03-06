package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import com.cmtech.android.bledeviceapp.model.UserAccount;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DataIOUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

/**
 * EcgAppendix: 心电留言类
 * Created by bme on 2019/1/9.
 */

public class EcgAppendix{
    private static final int CONTENT_CHAR_LEN = 500; // 留言内容字符数

    private UserAccount creator = new UserAccount(); // 创建人
    private long createTime = -1; // 创建时间
    private String content = ""; // 内容

    public static EcgAppendix createDefaultAppendix() {
        UserAccount creator = UserAccountManager.getInstance().getUserAccount();
        long createTime = new Date().getTime();
        return new EcgAppendix(creator, createTime);
    }

    public UserAccount getCreator() {
        return creator;
    }

    public String getCreatorName() {
        return creator.getUserName();
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void appendContent(String content) {
        if(this.content.equals("")) {
            this.content = content;
            return;
        }
        this.content += ('\n' + content);
    }

    public EcgAppendix() {

    }

    public EcgAppendix(UserAccount creator, long createTime) {
        try {
            this.creator = (UserAccount) creator.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        this.createTime = createTime;
    }

    public EcgAppendix(UserAccount creator, long createTime, String content) {
        this(creator, createTime);
        this.content = content;
    }

    public boolean readFromStream(DataInput in) {
        try {
            // 读创建人
            creator.readFromStream(in);
            // 读创建时间
            createTime = ByteUtil.reverseLong(in.readLong());
            // 读留言内容
            content = DataIOUtil.readFixedString(CONTENT_CHAR_LEN, in);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean writeToStream(DataOutput out) {
        try {
            // 写创建人
            creator.writeToStream(out);
            // 写创建时间
            out.writeLong(ByteUtil.reverseLong(createTime));
            // 写留言内容
            DataIOUtil.writeFixedString(content, CONTENT_CHAR_LEN, out);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public int length() {
        return  creator.length() + 8 + 2* CONTENT_CHAR_LEN;
    }

    public String toString() {
        return creator.getUserName() + "@" + DateTimeUtil.timeToShortStringWithTodayYesterday(createTime) + content;
    }

    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;

        EcgAppendix other = (EcgAppendix)otherObject;

        return  (creator.getPhone().equals(other.creator.getPhone()) && (createTime == other.createTime) );
    }

    public int hashCode() {
        return (int)(creator.hashCode() + createTime);
    }
}
