package com.cmtech.bmefile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

import static com.cmtech.bmefile.BmeFileHead.INVALID_SAMPLE_RATE;

/**
 * BmeFile: Bme文件
 * created by chenm, 2018-02-11
 */

public abstract class BmeFile {
	static final BmeFileHead DEFAULT_BME_FILE_HEAD = BmeFileHeadFactory.createDefault(); // 缺省文件头
	private static final byte[] BME = {'B', 'M', 'E'}; // BmeFile标识符
	protected File file; // 文件
	protected DataInput in; // 输入流
	protected DataOutput out; // 输出流
	protected BmeFileHead head; // 文件头
	private int dataNum; // 文件包含的数据个数

    // 为已存在文件生成BmeFile
	BmeFile(String fileName) throws IOException{
        File file = new File(fileName);
        if(file.exists() && file.renameTo(file)) {
            this.file = file;
            head = readHead();
            if(head == null) {
                throw new IOException("The bme file head is wrong: " + fileName);
            }
        } else {
            throw new IOException("The file can't be opened.");
        }
	}

	// 为不存在的文件创建BmeFile
	BmeFile(String fileName, BmeFileHead head) throws IOException{
        File file = new File(fileName);
        if(!file.exists()) {
            this.file = file;
            create(head);
        } else {
            throw new IOException();
        }
	}

    public int getDataNum() {
        return dataNum;
    }
    public void setDataNum(int dataNum) {
	    this.dataNum = dataNum;
    }
	private BmeFileHead readHead() throws IOException{
        if(in != null || out != null)
            throw new NullPointerException("The file IO is null.");

        createIOStream();
        // 读BmeFile标识符
        byte[] bme = new byte[3];
        in.readFully(bme);
        if(!Arrays.equals(bme, BME))
            return null;
        // 读版本号
        byte[] ver = new byte[2];
        in.readFully(ver);
        // 创建head，并从in初始化
        BmeFileHead head = BmeFileHeadFactory.createByVersionCode(ver);
        head.readFromStream(in);

		return head;
	}

    private void create(BmeFileHead head) throws IOException{
        if(head == null)
            throw new NullPointerException();
        if(in != null || out != null)
            throw new IllegalStateException();
        if(!file.createNewFile()) {
            throw new IOException();
        }

        createIOStream();
        out.write(BME); // 写BmeFile文件标识符
        out.write(head.getVersion()); // 写版本号
        head.writeToStream(out); // 写head
        this.head = head;
    }

    // 读单个byte数据
    public byte readByte() throws IOException{
        return in.readByte();
    }

    // 读单个int数据
    public int readInt() throws IOException {
        return DataIOUtil.readInt(in, head.getByteOrder());
    }

    // 读单个double数据
    public double readDouble() throws IOException{
        return DataIOUtil.readDouble(in, head.getByteOrder());
    }

    // 写单个byte数据
    public void writeData(byte data) throws IOException{
        out.writeByte(data);
        dataNum++;
    }

    // 写单个int数据
    public void writeData(int data) throws IOException{
        DataIOUtil.writeInt(out, data, head.getByteOrder());
        dataNum++;
    }

    // 写单个double数据
    public void writeData(double data) throws IOException{
        DataIOUtil.writeDouble(out, data, head.getByteOrder());
        dataNum++;
    }

    // 写byte数组
    public void writeData(byte[] data) throws IOException{
        for(byte num : data) {
            writeData(num);
        }
    }

    // 写int数组
    public void writeData(int[] data) throws IOException{
        for(int num : data) {
            writeData(num);
        }
    }

    // 写double数组
	public void writeData(double[] data) throws IOException{
        for(double num : data) {
            writeData(num);
        }
	}

	public File getFile() {
	    return file;
    }
	public String getFileName() {
		return (file == null) ? "" : file.toString();
	}
    public BmeFileHead getBmeFileHead() {
        return head;
    }
	public BmeFileDataType getDataType() {
		return (head == null) ? null : head.getDataType();
	}
	public int getSampleRate() {
		return (head == null) ? INVALID_SAMPLE_RATE : head.getSampleRate();
	}
	public byte[] getVersion() {
		return (head == null) ? null : head.getVersion();
	}
	public ByteOrder getByteOrder() {
		return (head == null) ? null : head.getByteOrder();
	}

	@Override
	public String toString() {
		return "[文件名：" + getFileName() + ":"+ head + "; 数据个数：" + getDataNum() + "]";
	}

    protected abstract void createIOStream() throws FileNotFoundException;
    protected abstract int availableDataFromCurrentPos();
    protected abstract boolean isEof() throws IOException;
    public abstract void close() throws IOException;
}
