package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import com.cmtech.android.bledeviceapp.model.UserAccount;
import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * EcgLocatedComment: 可数据定位的留言
 * Created by bme on 2019/1/11.
 */

public class EcgLocatedComment extends EcgNormalComment implements IEcgAppendixDataLocation{
    private long location = -1; // 数据定位

    public void setLocation(long location) { this.location = location; }

    public EcgLocatedComment() {
        super();
    }

    public EcgLocatedComment(UserAccount creator, long createTime, String content, long location) {
        super(creator, createTime, content);
        this.location = location;
    }

    @Override
    public boolean readFromStream(DataInput in) {
        try {
            if(!super.readFromStream(in)) return false;
            // 读数据定位
            location = ByteUtil.reverseLong(in.readLong());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean writeToStream(DataOutput out) {
        try {
            if(!super.writeToStream(out)) return false;
            // 写数据定位
            out.writeLong(ByteUtil.reverseLong(location));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public int length() {
        return  super.length() + 8;
    }

    @Override
    public EcgAppendixType getType() {
        return EcgAppendixType.LOCATED_COMMENT;
    }

    @Override
    public String toString() {
        return "第" + location + "个数据，" + super.toString();
    }

    @Override
    public String toStringWithSampleRate(int sampleRate) {
        if(sampleRate <= 0)
            return toString();
        else
            return "第" + String.valueOf(location /sampleRate) + "秒，" + super.toString();
    }

    @Override
    public long getLocation() {
        return location;
    }

}
