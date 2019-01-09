package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DataIOUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * EcgAppendix: 心电附信类
 * Created by bme on 2019/1/9.
 */

public abstract class EcgAppendix {
    private static final int CREATOR_LEN = 10; // 创建人名字符数

    private EcgAppendixType type = EcgAppendixType.INVALID_APPENDIX; // 类型
    private String creator = "匿名"; // 创建人
    private long createTime; // 创建时间

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public EcgAppendixType getType() {
        return type;
    }

    public void setType(EcgAppendixType type) {
        this.type = type;
    }

    public EcgAppendix() {

    }

    public EcgAppendix(EcgAppendixType type, String creator, long createTime) {
        this.creator = creator;
        this.createTime = createTime;
        this.type = type;
    }

    public boolean readFromStream(DataInput in) {
        try {
            // 读类型
            type = EcgAppendixType.getFromCode(ByteUtil.reverseInt(in.readInt()));
            // 读创建人
            creator = DataIOUtil.readFixedString(CREATOR_LEN, in);
            // 读创建时间
            createTime = ByteUtil.reverseLong(in.readLong());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean writeToStream(DataOutput out) {
        try {
            // 写类型
            out.writeInt(ByteUtil.reverseInt(type.getCode()));
            // 写创建人
            DataIOUtil.writeFixedString(creator, CREATOR_LEN, out);
            // 写创建时间
            out.writeLong(ByteUtil.reverseLong(createTime));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static int length() {
        return  2*CREATOR_LEN + 8 + 4;
    }

    @Override
    public String toString() {
        return creator +
                "@" + DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(createTime);
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;

        EcgAppendix other = (EcgAppendix)otherObject;

        return  (creator.equals(other.creator) && (createTime == other.createTime) && (type == other.type) );
    }

    @Override
    public int hashCode() {
        return (int)(creator.hashCode() + createTime + type.getCode());
    }
}
