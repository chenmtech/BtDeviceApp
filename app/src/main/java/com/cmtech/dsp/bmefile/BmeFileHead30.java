package com.cmtech.dsp.bmefile;

import com.cmtech.dsp.bmefile.BmeFileHead10;
import com.cmtech.dsp.exception.FileException;
import com.cmtech.dsp.util.FormatTransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class BmeFileHead30 extends BmeFileHead10 {
    public static final byte[] VER = new byte[] {0x00, 0x03};

    // 1mV定标值
    private int value1mV = 1;

    public int getValue1mV() {
        return value1mV;
    }

    public void setValue1mV(int value1mV) {
        this.value1mV = value1mV;
    }

    public BmeFileHead30() {
        super();
    }

    public BmeFileHead30(int value1mV) {
        super();
        this.value1mV = value1mV;
    }

    @Override
    public byte[] getVersion() {
        return BmeFileHead30.VER;
    }

    @Override
    public void readFromStream(DataInputStream in) throws FileException {
        super.readFromStream(in);
        try {
            setValue1mV(FormatTransfer.reverseInt(in.readInt()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeToStream(DataOutputStream out) throws FileException {
        super.writeToStream(out);
        try {
            out.write(FormatTransfer.toLH(getValue1mV()));
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
                + getValue1mV() + "]";
    }
}
