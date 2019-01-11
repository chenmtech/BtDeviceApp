package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DataIOUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * EcgAppendix: 心电附加信息类
 * Created by bme on 2019/1/9.
 */

public abstract class EcgAppendix implements IEcgAppendix{
    private static final int CREATOR_CHAR_LEN = 10; // 创建人名字符数

    private String creator = "匿名"; // 创建人
    private long createTime; // 创建时间

    @Override
    public String getCreator() {
        return creator;
    }

    @Override
    public void setCreator(String creator) {
        this.creator = creator;
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

    public EcgAppendix(String creator, long createTime) {
        this.creator = creator;
        this.createTime = createTime;
    }

    @Override
    public boolean readFromStream(DataInput in) {
        try {
            // 读创建人
            creator = DataIOUtil.readFixedString(CREATOR_CHAR_LEN, in);
            // 读创建时间
            createTime = ByteUtil.reverseLong(in.readLong());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean writeToStream(DataOutput out) {
        try {
            // 写创建人
            DataIOUtil.writeFixedString(creator, CREATOR_CHAR_LEN, out);
            // 写创建时间
            out.writeLong(ByteUtil.reverseLong(createTime));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public int length() {
        return  2* CREATOR_CHAR_LEN + 8;
    }

    @Override
    public String toString() {
        return creator + "@" + DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(createTime) + '\n';
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;

        EcgAppendix other = (EcgAppendix)otherObject;

        return  (creator.equals(other.creator) && (createTime == other.createTime) );
    }

    @Override
    public int hashCode() {
        return (int)(creator.hashCode() + createTime);
    }
}
