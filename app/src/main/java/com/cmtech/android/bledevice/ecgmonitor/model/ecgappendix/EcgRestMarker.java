package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;


import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * EcgRestMarker: 心电安静状态标记
 * Created by bme on 2019/1/9.
 */

public class EcgRestMarker extends EcgAppendix {
    private long startNum;
    private long endNum;

    public long getStartNum() {
        return startNum;
    }

    public void setStartNum(long startNum) {
        this.startNum = startNum;
    }

    public long getEndNum() {
        return endNum;
    }

    public void setEndNum(long endNum) {
        this.endNum = endNum;
    }

    public EcgRestMarker() {
        super();
    }

    public EcgRestMarker(String creator, long createTime) {
        super(creator, createTime);
    }

    @Override
    public boolean readFromStream(DataInput in) {
        try {
            if(!super.readFromStream(in)) return false;
            // 读起始Number
            startNum = ByteUtil.reverseLong(in.readLong());
            // 读终止Number
            endNum = ByteUtil.reverseLong(in.readLong());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean writeToStream(DataOutput out) {
        try {
            if(!super.writeToStream(out)) return false;
            // 写起始Number
            out.writeLong(ByteUtil.reverseLong(startNum));
            // 写终止Number
            out.writeLong(ByteUtil.reverseLong(endNum));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public int length() {
        return  super.length() + 2*8;
    }

    @Override
    public EcgAppendixType getType() {
        return EcgAppendixType.REST_MARKER;
    }

    @Override
    public String toString() {
        return super.toString() +
                "安静状态[" + startNum + ":" + endNum + "]\n";
    }
}
