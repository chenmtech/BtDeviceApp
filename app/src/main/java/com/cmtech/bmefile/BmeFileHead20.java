/**
 * Project Name:DSP_JAVA
 * File Name:BmeFileHead20.java
 * Package Name:com.cmtech.dsp.file
 * Date:2018年2月14日上午8:46:47
 * Copyright (c) 2018, e_yujunquan@163.com All Rights Reserved.
 *
 */
package com.cmtech.bmefile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.bmefile.exception.FileException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * ClassName: BmeFileHead20
 * Function: TODO ADD FUNCTION. 
 * Reason: TODO ADD REASON(可选). 
 * date: 2018年2月14日 上午8:46:47 
 *
 * @author bme
 * @version 
 * @since JDK 1.6
 */
public class BmeFileHead20 extends BmeFileHead {
	public static final byte[] VER = new byte[] {0x00, 0x02};
	
	private static final byte MSB = 0;
	private static final byte LSB = 1;
	
	private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
	
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
	public BmeFileHead setByteOrder(ByteOrder byteOrder) {
		this.byteOrder = byteOrder;
		return this;
	}

	@Override
	public void readFromStream(DataInput in) throws FileException {
		try {
			byte order = in.readByte();
            int infoLen;
            if(order == MSB) {
				infoLen = in.readInt();
				byteOrder = ByteOrder.BIG_ENDIAN;
			} else {
				infoLen = ByteUtil.reverseInt(in.readInt());
				byteOrder = ByteOrder.LITTLE_ENDIAN;
			}
			byte[] str = new byte[infoLen];
			in.readFully(str);
			setInfo(new String(str));
			int dataType = in.readByte();
			setDataType(BmeFileDataType.UNKNOWN);
			for(BmeFileDataType type : BmeFileDataType.values()) {
				if(dataType == type.getCode()) {
					setDataType(type);
					break;
				}
			}
			if(order == MSB) {
				setFs(in.readInt());
			} else {
				setFs(ByteUtil.reverseInt(in.readInt()));
			}
		} catch(IOException ioe) {
			throw new FileException("文件头", "读入错误");
		}
	}

	@Override
	public void writeToStream(DataOutput out) throws FileException {
		try {
			int infoLen = getInfo().getBytes().length;
			if(byteOrder == ByteOrder.BIG_ENDIAN) {
				out.writeByte(MSB);
				out.writeInt(infoLen);
			} else {
				out.writeByte(LSB);
				out.writeInt(ByteUtil.reverseInt(infoLen));
			}
			out.write(getInfo().getBytes());
			out.writeByte((byte)getDataType().getCode());
			if(byteOrder == ByteOrder.BIG_ENDIAN) {
				out.writeInt(getFs());
			} else {
				out.writeInt(ByteUtil.reverseInt(getFs()));
			}
		} catch(IOException ioe) {
			throw new FileException("文件头", "写出错误");
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

	// super.getLength() + byteOrder(1字节)
    public int getLength() {
        return super.getLength() + 1;
    }
}
