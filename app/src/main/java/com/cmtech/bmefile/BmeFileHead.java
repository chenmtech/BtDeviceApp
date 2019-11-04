package com.cmtech.bmefile;

import org.litepal.crud.LitePalSupport;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * BmeFileHead: Bme文件头
 * created by chenm, 2018-02-12
 */

public abstract class BmeFileHead extends LitePalSupport {
    private static final int INFO_LENGTH_BYTE_NUM = 4; // 信息长度的字节数
    private static final int DATA_TYPE_BYTE_NUM = 1; // 数据类型的字节数
    private static final int SAMPLE_RATE_BYTE_NUM = 4; // 采样率的字节数
    public static final int INVALID_SAMPLE_RATE = -1; // 无效采样率

    private String info = "Unknown"; // 信息字符串
    private int dataTypeCode = BmeFileDataType.DOUBLE.getCode(); // 数据类型码
    private int sampleRate = INVALID_SAMPLE_RATE; // 采样频率

    public BmeFileHead() {
    }

    public BmeFileHead(String info, BmeFileDataType dataType, int sampleRate) {
        this.info = info;
        this.dataTypeCode = dataType.getCode();
        this.sampleRate = sampleRate;
    }

    public BmeFileHead(BmeFileHead fileHead) {
        info = fileHead.info;
        dataTypeCode = fileHead.dataTypeCode;
        sampleRate = fileHead.sampleRate;
    }

    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }
    public BmeFileDataType getDataType() {
        return BmeFileDataType.getFromCode(dataTypeCode);
    }
    public void setDataType(BmeFileDataType dataType) {
        this.dataTypeCode = dataType.getCode();
    }
    public int getSampleRate() {
        return sampleRate;
    }
    public void setSampleRate(int fs) {
        this.sampleRate = fs;
    }
    public int getLength() {
        return INFO_LENGTH_BYTE_NUM + info.getBytes().length + DATA_TYPE_BYTE_NUM + SAMPLE_RATE_BYTE_NUM;
    }

    @Override
    public String toString() {
        return "BmeFileHead:"
                + info + ";"
                + BmeFileDataType.getFromCode(dataTypeCode) + ";"
                + sampleRate;
    }

    public abstract byte[] getVersion(); // 获取版本号
    public abstract ByteOrder getByteOrder(); // 获取数据字节序
    public abstract void setByteOrder(ByteOrder byteOrder); // 设置数据字节序
    public abstract void readFromStream(DataInput in) throws IOException; // 从输入流读取文件头
    public abstract void writeToStream(DataOutput out) throws IOException; // 将文件头写到输出流
}
