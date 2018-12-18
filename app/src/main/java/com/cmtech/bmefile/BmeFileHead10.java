package com.cmtech.bmefile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.bmefile.exception.FileException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * BmeFileHead10: Bme文件头,1.0版本
 * created by chenm, 2018-02-12
 */

public class BmeFileHead10 extends BmeFileHead {
    // 版本号
	public static final byte[] VER = new byte[] {0x00, 0x01};
	// 字节序,1.0版本为little endian
    private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	
	public BmeFileHead10() {
		super();
	}
	
	public BmeFileHead10(String info, BmeFileDataType dataType, int fs) {
		super(info, dataType, fs);
	}
	
	public BmeFileHead10(BmeFileHead fileHead) {
		super(fileHead);
	}
	
	@Override
	public byte[] getVersion() {
		return BmeFileHead10.VER;
	}

	@Override
	public ByteOrder getByteOrder() {
		return BYTE_ORDER;
	}
	
	@Override
	public void setByteOrder(ByteOrder byteOrder) {
        if(byteOrder != ByteOrder.LITTLE_ENDIAN) {
            throw new IllegalArgumentException("字节序必须为Little Endian.");
        }
	}
	
	@Override
	public boolean readFromStream(DataInput in){
	    try {
            int infoLen = ByteUtil.reverseInt(in.readInt()); // 读info字节长度
            byte[] str = new byte[infoLen];
            in.readFully(str); // 读info
            setInfo(new String(str));
            int dataTypeCode = in.readByte(); // 读数据类型code
            setDataType(BmeFileDataType.getFromCode(dataTypeCode));
            setFs(ByteUtil.reverseInt(in.readInt())); // 读采样频率
            return true;
        } catch (IOException e) {
	        return false;
        }
	}

    @Override
	public boolean writeToStream(DataOutput out){
	    try {
            int infoLen = getInfo().getBytes().length;
            out.writeInt(ByteUtil.reverseInt(infoLen)); // 写infoLen
            out.write(getInfo().getBytes()); // 写info
            out.writeByte((byte) getDataType().getCode()); // 写数据类型code
            out.writeInt(ByteUtil.reverseInt(getFs())); // 写采样频率
            return true;
        } catch (IOException e) {
	        return false;
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
				+ getFs() + "]";
	}
}
