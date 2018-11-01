package com.cmtech.dsp.bmefile;

import com.cmtech.dsp.exception.FileException;
import com.cmtech.dsp.util.FormatTransfer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class BmeFileHead30 extends BmeFileHead10 {
    public static final byte[] VER = new byte[] {0x00, 0x03};

    // 1mV定标值
    private int calibrationValue = 1;

    public int getCalibrationValue() {
        return calibrationValue;
    }

    public void setCalibrationValue(int calibrationValue) {
        this.calibrationValue = calibrationValue;
    }

    public BmeFileHead30() {
        super();
    }

    public BmeFileHead30(int calibrationValue) {
        super();
        this.calibrationValue = calibrationValue;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeToStream(DataOutput out) throws FileException {
        super.writeToStream(out);
        try {
            out.write(FormatTransfer.toLH(getCalibrationValue()));
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
                + getCalibrationValue() + "]";
    }

    public int getLength() {
        return super.getLength() + 4;
    }
}
