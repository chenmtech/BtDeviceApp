package com.cmtech.bmefile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * BmeFileHead30: Bme文件头,3.0版本
 * created by chenm, 2018-06-10
 */

public class BmeFileHead30 extends BmeFileHead10 {
    static final byte[] VER = new byte[] {0x00, 0x03}; // 版本号
    private static final int CALI_VALUE_BYTE_NUM = 4;
    private static final int CREATE_TIME_BYTE_NUM = 8;

    private int calibrationValue = 1; // 标定值
    private long createdTime = 0; // 创建时间

    BmeFileHead30() {
        super();
    }

    public BmeFileHead30(String info, BmeFileDataType dataType, int sampleRate, int calibrationValue, long createdTime) {
        super(info, dataType, sampleRate);
        this.calibrationValue = calibrationValue;
        this.createdTime = createdTime;
    }

    public int getCalibrationValue() {
        return calibrationValue;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    @Override
    public byte[] getVersion() {
        return BmeFileHead30.VER;
    }

    @Override
    public void readFromStream(DataInput in) throws IOException{
        super.readFromStream(in);
        calibrationValue = ByteUtil.reverseInt(in.readInt());
        createdTime = ByteUtil.reverseLong(in.readLong());
    }

    @Override
    public void writeToStream(DataOutput out) throws IOException{
        super.writeToStream(out);
        out.writeInt(ByteUtil.reverseInt(calibrationValue)); // 写定标值
        out.writeLong(ByteUtil.reverseLong(createdTime)); // 写创建时间
    }

    @Override
    public String toString() {
        return "BmeFileHead30:"
                + super.toString()
                + calibrationValue + ";"
                + createdTime;
    }

    public int getLength() {
        return super.getLength() + CALI_VALUE_BYTE_NUM + CREATE_TIME_BYTE_NUM;
    }
}
