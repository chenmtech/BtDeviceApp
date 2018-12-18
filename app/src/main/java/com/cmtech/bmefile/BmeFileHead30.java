package com.cmtech.bmefile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

/**
 * BmeFileHead30: Bme文件头,3.0版本
 * created by chenm, 2018-06-10
 */

public class BmeFileHead30 extends BmeFileHead10 {
    public static final byte[] VER = new byte[] {0x00, 0x03}; // 版本号

    private int calibrationValue = 1; // 定标值
    public int getCalibrationValue() {
        return calibrationValue;
    }
    public void setCalibrationValue(int calibrationValue) {
        this.calibrationValue = calibrationValue;
    }

    private long createdTime = 0; // 文件创建时间
    public long getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public BmeFileHead30() {
        super();
    }

    public BmeFileHead30(int calibrationValue, long createdTime) {
        super();
        this.calibrationValue = calibrationValue;
        this.createdTime = createdTime;
    }

    @Override
    public byte[] getVersion() {
        return BmeFileHead30.VER;
    }

    @Override
    public boolean readFromStream(DataInput in){
        boolean result = super.readFromStream(in);
        if(result) {
            try {
                setCalibrationValue(ByteUtil.reverseInt(in.readInt())); // 读定标值
                setCreatedTime(ByteUtil.reverseLong(in.readLong())); // 读创建时间
            } catch (IOException e) {
                result = false;
            }
        }
        return result;
    }

    @Override
    public boolean writeToStream(DataOutput out) {
        boolean result = super.writeToStream(out);
        if(result) {
            try {
                out.writeInt(ByteUtil.reverseInt(getCalibrationValue())); // 写定标值
                out.writeLong(ByteUtil.reverseLong(getCreatedTime())); // 写创建时间
            } catch (IOException e) {
                result = false;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "[文件头信息："
                + getClass().getSimpleName() + ";"
                + Arrays.toString(getVersion()) + ";"
                + getByteOrder() + ";"
                + getInfo() + ";"
                + getDataType() + ";"
                + getFs() + ";"
                + getCalibrationValue() + ";"
                + getCreatedTime() + "]";
    }

    // 文件头字节长度：super.getLength() + calibrationValue(4字节) + createdTime(8字节)
    public int getLength() {
        return super.getLength() + 12;
    }
}
