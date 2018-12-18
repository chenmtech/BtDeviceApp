package com.cmtech.bmefile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * BmeFileHead20: Bme文件头,2.0版本
 * created by chenm, 2018-02-14
 */

public class BmeFileHead20 extends BmeFileHead {
    // 版本号
	public static final byte[] VER = new byte[] {0x00, 0x02};
	// 缺省字节序
	private static final ByteOrder DEFAULT_BYTE_ORDER = BIG_ENDIAN;

	private static final byte BIG_ENDIAN_CODE = 0; // big endian code
	private static final byte LITTLE_ENDIAN_CODE = 1; // little endian code

	// 字节序
	private ByteOrder byteOrder = DEFAULT_BYTE_ORDER;
	
	public BmeFileHead20() {
		super();
	}
	
	public BmeFileHead20(ByteOrder byteOrder) {
		super();
		this.byteOrder = byteOrder;
	}
	
	public BmeFileHead20(ByteOrder byteOrder, String info, BmeFileDataType dataType, int fs) {
		super(info, dataType, fs);
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
	public boolean readFromStream(DataInput in){
	    try {
            byte byteOrderCode = in.readByte(); // 读字节序code
            byteOrder = (byteOrderCode == BIG_ENDIAN_CODE) ? BIG_ENDIAN : LITTLE_ENDIAN;
            int infoLen = readInt(in); // 读infoLen
            byte[] str = new byte[infoLen];
            in.readFully(str); // 读info
            setInfo(new String(str));
            int dataTypeCode = in.readByte(); // 读数据类型code
            setDataType(BmeFileDataType.getFromCode(dataTypeCode));
            setFs(readInt(in)); // 读采样率
            return true;
        } catch (IOException e) {
	        return false;
        }
	}

	@Override
	public boolean writeToStream(DataOutput out){
	    try {
            // 写字节序code
            if (byteOrder == BIG_ENDIAN) {
                out.writeByte(BIG_ENDIAN_CODE);
            } else {
                out.writeByte(LITTLE_ENDIAN_CODE);
            }
            int infoLen = getInfo().getBytes().length;
            writeInt(out, infoLen); // 写infoLen
            out.write(getInfo().getBytes()); // 写info
            out.writeByte((byte) getDataType().getCode()); // 写数据类型code
            writeInt(out, getFs()); // 写采样率
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

	// 文件头字节长度：super.getLength() + byteOrder(1字节)
    public int getLength() {
        return super.getLength() + 1;
    }

    private int readInt(DataInput in) throws IOException{
	    return (byteOrder == BIG_ENDIAN) ? in.readInt() : ByteUtil.reverseInt(in.readInt());
    }

    private void writeInt(DataOutput out, int data) throws IOException{
        if ((byteOrder == BIG_ENDIAN)) {
            out.writeInt(data);
        } else {
            out.writeInt(ByteUtil.reverseInt(data));
        }
    }
}
