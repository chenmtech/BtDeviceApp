package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import com.cmtech.android.bledeviceapp.model.UserAccount;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * EcgAppendix: 心电附加信息类
 * Created by bme on 2019/1/9.
 */

public abstract class EcgAppendix implements IEcgAppendix{
    private UserAccount creator = new UserAccount(); // 创建人
    private long createTime; // 创建时间
    private boolean isReply = false; // 是否是回复信息

    @Override
    public UserAccount getCreator() {
        return creator;
    }

    @Override
    public String getCreatorName() {
        return creator.getUserName();
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
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

    public EcgAppendix(UserAccount creator, long createTime, boolean isReply) {
        this(creator, createTime);
        this.isReply = isReply;
    }

    @Override
    public boolean readFromStream(DataInput in) {
        try {
            // 读创建人
            creator.readFromStream(in);
            // 读创建时间
            createTime = ByteUtil.reverseLong(in.readLong());
            // 读是否是回复信息
            isReply = in.readBoolean();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean writeToStream(DataOutput out) {
        try {
            // 写创建人
            creator.writeToStream(out);
            // 写创建时间
            out.writeLong(ByteUtil.reverseLong(createTime));
            // 写是否是回复信息
            out.writeBoolean(isReply);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public int length() {
        return  creator.length() + 8 + 1;
    }

    @Override
    public boolean isReply() {
        return isReply;
    }

    @Override
    public void setReply(boolean reply) {
        isReply = reply;
    }

    @Override
    public String toString() {
        return creator.getUserName() + "@" + DateTimeUtil.timeToShortStringWithTodayYesterday(createTime) + '\n';
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;

        EcgAppendix other = (EcgAppendix)otherObject;

        return  (creator.getPhone().equals(other.creator.getPhone()) && (createTime == other.createTime) );
    }

    @Override
    public int hashCode() {
        return (int)(creator.hashCode() + createTime);
    }
}
