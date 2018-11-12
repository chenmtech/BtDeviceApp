package com.cmtech.bmefile;

import com.cmtech.bmefile.exception.FileException;
import com.cmtech.dsp.util.FormatTransfer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class BmeFileHead30 extends BmeFileHead10 {
    public static final byte[] VER = new byte[] {0x00, 0x03};

    // 1mV定标值
    private int calibrationValue = 1;

    // 文件创建时间
    private long createdTime = 0;

    public int getCalibrationValue() {
        return calibrationValue;
    }

    public void setCalibrationValue(int calibrationValue) {
        this.calibrationValue = calibrationValue;
    }

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
    public void readFromStream(DataInput in) throws FileException {
        super.readFromStream(in);
        try {
            setCalibrationValue(FormatTransfer.reverseInt(in.readInt()));
            setCreatedTime(FormatTransfer.reverseLong(in.readLong()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeToStream(DataOutput out) throws FileException {
        super.writeToStream(out);
        try {
            out.write(FormatTransfer.toLH(getCalibrationValue()));
            out.write(FormatTransfer.toLH(getCreatedTime()));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    // super.getLength() + calibrationValue(4字节) + createdTime(8字节)
    public int getLength() {
        return super.getLength() + 12;
    }
}
