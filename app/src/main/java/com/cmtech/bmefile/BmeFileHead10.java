package com.cmtech.bmefile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * BmeFileHead10: Bme文件头,1.0版本
 * created by chenm, 2018-02-12
 */

public class BmeFileHead10 extends BmeFileHead {
	static final byte[] VER = new byte[] {0x00, 0x01}; // 版本号

    private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN; // 字节序,1.0版本为little endian
	
	BmeFileHead10() {
		super();
	}
	
	BmeFileHead10(String info, BmeFileDataType dataType, int sampleRate) {
		super(info, dataType, sampleRate);
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
            throw new IllegalArgumentException("The byteOrder must be Little Endian.");
        }
	}
	
	@Override
	public void readFromStream(DataInput in) throws IOException{
        int infoLen = ByteUtil.reverseInt(in.readInt()); // 读info字节长度
        byte[] str = new byte[infoLen];
        in.readFully(str); // 读info
        setInfo(new String(str));
        int dataTypeCode = in.readByte(); // 读数据类型code
        setDataType(BmeFileDataType.getFromCode(dataTypeCode));
        setSampleRate(ByteUtil.reverseInt(in.readInt())); // 读采样频率
	}

    @Override
	public void writeToStream(DataOutput out) throws IOException{
	    byte[] infoBytes = getInfo().getBytes();
        int infoLen = infoBytes.length;
        out.writeInt(ByteUtil.reverseInt(infoLen)); // 写infoLen
        out.write(infoBytes); // 写info
        out.writeByte((byte) getDataType().getCode()); // 写数据类型code
        out.writeInt(ByteUtil.reverseInt(getSampleRate())); // 写采样频率
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + super.toString();
	}
}
