package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DataIOUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * EcgLocatedComment: 可数据定位的留言
 * Created by bme on 2019/1/11.
 */

public class EcgLocatedComment extends EcgNormalComment implements IEcgAppendixDataLocation{
    private long dataLocation = -1; // 数据定位

    public EcgLocatedComment() {
        super();
    }

    public EcgLocatedComment(String creator, long createTime, String content, long dataLocation) {
        super(creator, createTime, content);
        this.dataLocation = dataLocation;
    }

    @Override
    public boolean readFromStream(DataInput in) {
        try {
            if(!super.readFromStream(in)) return false;
            // 读数据定位
            dataLocation = ByteUtil.reverseLong(in.readLong());
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
            out.writeLong(ByteUtil.reverseLong(dataLocation));
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
        return super.toString() + "[" + dataLocation + "]";
    }

    @Override
    public long getDataLocation() {
        return dataLocation;
    }

}
