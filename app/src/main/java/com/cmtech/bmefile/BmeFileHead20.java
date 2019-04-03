package com.cmtech.bmefile;

import com.cmtech.android.bledeviceapp.util.DataIOUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteOrder;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * BmeFileHead20: Bme文件头,2.0版本
 * created by chenm, 2018-02-14
 */

public class BmeFileHead20 extends BmeFileHead {
	static final byte[] VER = new byte[] {0x00, 0x02}; // 版本号
	private static final ByteOrder DEFAULT_BYTE_ORDER = BIG_ENDIAN; // 缺省字节序
	private static final byte BIG_ENDIAN_CODE = 0; // big endian code
	private static final byte LITTLE_ENDIAN_CODE = 1; // little endian code
    private static final int BYTE_ORDER_BYTE_NUM = 1;

	private ByteOrder byteOrder = DEFAULT_BYTE_ORDER;
	
	BmeFileHead20() {
		super();
	}
	
	public BmeFileHead20(ByteOrder byteOrder) {
		super();
		this.byteOrder = byteOrder;
	}
	
	public BmeFileHead20(ByteOrder byteOrder, String info, BmeFileDataType dataType, int sampleRate) {
		super(info, dataType, sampleRate);
		this.byteOrder = byteOrder;
	}
	
	public BmeFileHead20(BmeFileHead fileHead) {
		super(fileHead);
		byteOrder = fileHead.getByteOrder();
	}
	
	@Override
	public byte[] getVersion() {
		return BmeFileHead20.VER;
	}

	@Override
	public ByteOrder getByteOrder() {
		return byteOrder;
	}
	
	@Override
	public void setByteOrder(ByteOrder byteOrder) {
		this.byteOrder = byteOrder;
	}

	@Override
	public void readFromStream(DataInput in) throws IOException{
        byte byteOrderCode = in.readByte(); // 读字节序code
        byteOrder = getByteOrderFromCode(byteOrderCode);
        int infoLen = DataIOUtil.readInt(in, byteOrder);
        byte[] str = new byte[infoLen];
        in.readFully(str); // 读info
        setInfo(new String(str));
        int dataTypeCode = in.readByte(); // 读数据类型code
        setDataType(BmeFileDataType.getFromCode(dataTypeCode));
        setSampleRate(DataIOUtil.readInt(in, byteOrder)); // 读采样率
	}

	private ByteOrder getByteOrderFromCode(byte code) {
	    if(code == BIG_ENDIAN_CODE) {
	        return BIG_ENDIAN;
        } else if(code == LITTLE_ENDIAN_CODE) {
	        return LITTLE_ENDIAN;
        } else {
	        throw new IllegalArgumentException();
        }
    }

	@Override
	public void writeToStream(DataOutput out) throws IOException{
        // 写字节序code
        if (byteOrder == BIG_ENDIAN) {
            out.writeByte(BIG_ENDIAN_CODE);
        } else {
            out.writeByte(LITTLE_ENDIAN_CODE);
        }
        byte[] infoBytes = getInfo().getBytes();
        int infoLen = infoBytes.length;
        DataIOUtil.writeInt(out, infoLen, byteOrder); // 写infoLen
        out.write(infoBytes); // 写info
        out.writeByte((byte) getDataType().getCode()); // 写数据类型code
        DataIOUtil.writeInt(out, getSampleRate(), byteOrder); // 写采样率
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":"
				+ getByteOrder() + ";"
				+ super.toString();
	}

    public int getLength() {
        return BYTE_ORDER_BYTE_NUM + super.getLength();
    }
}
