package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;


import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * EcgRestMarker: 心电安静状态标记
 * Created by bme on 2019/1/9.
 */

public class EcgRestMarker extends EcgAppendix implements IEcgAppendixDataLocation{
    private long beginLocation; // 起始定位
    private long endLocation; // 终止定位

    public long getBeginLocation() {
        return beginLocation;
    }

    public void setBeginLocation(long beginLocation) {
        this.beginLocation = beginLocation;
    }

    public long getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(long endLocation) {
        this.endLocation = endLocation;
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
            beginLocation = ByteUtil.reverseLong(in.readLong());
            // 读终止Number
            endLocation = ByteUtil.reverseLong(in.readLong());
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
            out.writeLong(ByteUtil.reverseLong(beginLocation));
            // 写终止Number
            out.writeLong(ByteUtil.reverseLong(endLocation));
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
    public long getDataLocation() {
        return beginLocation;
    }

    @Override
    public EcgAppendixType getType() {
        return EcgAppendixType.REST_MARKER;
    }

    @Override
    public String toString() {
        return super.toString() + "标记：安静[" + beginLocation + ":" + endLocation + "]";
    }

    @Override
    public String toString(int sampleRate) {
        if(sampleRate <= 0)
            return toString();
        else
            return super.toString() + "标记：安静[" + beginLocation/sampleRate + ":" + endLocation/sampleRate + "]" + "秒";
    }
}
