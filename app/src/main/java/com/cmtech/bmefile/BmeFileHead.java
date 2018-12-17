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
    // 信息字符串
	private String info = "Unknown";
    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }

	// 数据类型
	private BmeFileDataType dataType = BmeFileDataType.DOUBLE;
    public BmeFileDataType getDataType() {
        return dataType;
    }
    public void setDataType(BmeFileDataType dataType) {
        this.dataType = dataType;
    }

	// 采样频率
	private int fs = -1;
    public int getFs() {
        return fs;
    }
    public void setFs(int fs) {
        this.fs = fs;
    }
	
	public BmeFileHead() {
	}
	
	public BmeFileHead(String info, BmeFileDataType dataType, int fs) {
		this.info = info;
		this.dataType = dataType;
		this.fs = fs;
	}
	
	public BmeFileHead(BmeFileHead fileHead) {
		info = fileHead.info;
		dataType = fileHead.dataType;
		fs = fileHead.fs;
	}

	// BmeFileHead字节长度：infoLen(4字节) + info + dataType(1字节) + fs(4字节)
	public int getLength() {
	    return 4 + info.getBytes().length + 1 + 4;
    }

    // 获取文件头版本号
    public abstract byte[] getVersion();
    // 获取文件数据字节序
	public abstract ByteOrder getByteOrder();
	// 设置文件数据字节序
	public abstract void setByteOrder(ByteOrder byteOrder);
	// 从输入流读取文件头
	public abstract void readFromStream(DataInput in) throws IOException;
	// 将文件头写到输出流
	public abstract void writeToStream(DataOutput out) throws IOException;
}
