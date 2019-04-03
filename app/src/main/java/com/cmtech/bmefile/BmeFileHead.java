package com.cmtech.bmefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * BmeFileHead: Bme文件头
 * created by chenm, 2018-02-12
 */

public abstract class BmeFileHead {
    private static final int INFO_LENGTH_BYTE_NUM = 4;
    private static final int DATA_TYPE_BYTE_NUM = 1;
    private static final int SAMPLE_RATE_BYTE_NUM = 4;

	private String info = "Unknown"; // 信息字符串

	private BmeFileDataType dataType = BmeFileDataType.DOUBLE; // 数据类型

	private int sampleRate = -1; // 采样频率

	public BmeFileHead() {
	}
	
	public BmeFileHead(String info, BmeFileDataType dataType, int sampleRate) {
		this.info = info;
		this.dataType = dataType;
		this.sampleRate = sampleRate;
	}
	
	public BmeFileHead(BmeFileHead fileHead) {
		info = fileHead.info;
		dataType = fileHead.dataType;
		sampleRate = fileHead.sampleRate;
	}

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public BmeFileDataType getDataType() {
        return dataType;
    }

    public void setDataType(BmeFileDataType dataType) {
        this.dataType = dataType;
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
        return getClass().getSimpleName() + ":"
                + info + ";"
                + dataType + ";"
                + sampleRate;
    }

    public abstract byte[] getVersion(); // 获取版本号

	public abstract ByteOrder getByteOrder(); // 获取数据字节序

	public abstract void setByteOrder(ByteOrder byteOrder); // 设置数据字节序

	public abstract void readFromStream(DataInput in) throws IOException; // 从输入流读取文件头

	public abstract void writeToStream(DataOutput out) throws IOException; // 将文件头写到输出流
}
