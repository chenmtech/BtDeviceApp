package com.cmtech.bmefile;

import com.cmtech.android.bledeviceapp.util.DataIOUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * BmeFile: Bme文件
 * created by chenm, 2018-02-11
 */

public abstract class BmeFile {
	//protected static Set<String> fileInOperation = new HashSet<>(); // 已经打开的文件列表
	static final BmeFileHead DEFAULT_BMEFILE_HEAD = BmeFileHeadFactory.createDefault(); // 缺省文件头
	private static final byte[] BME = {'B', 'M', 'E'}; // BmeFile标识符
	
	protected File file; // 文件

	protected DataInput in; // 输入流

	protected DataOutput out; // 输出流

	protected BmeFileHead fileHead; // 文件头

	protected int dataNum; // 文件包含的数据个数

    // 为已存在文件生成BmeFile
	BmeFile(String fileName) throws IOException{
        File file = new File(fileName);
        if(file.exists() && file.renameTo(file)) {
            this.file = file;
            fileHead = readFileHead();
            if(fileHead == null) {
                throw new IOException("bmefile format wrong:" + fileName);
            }
        } else {
            throw new IOException();
        }
	}

	// 为不存在的文件创建BmeFile
	BmeFile(String fileName, BmeFileHead head) throws IOException{
        File file = new File(fileName);
        if(!file.exists()) {
            this.file = file;
            createNewFileUsingHead(head);
        } else {
            throw new IOException();
        }
	}

    public int getDataNum() {
        return dataNum;
    }

    // 判断文件是否在操作中
    public boolean isActive() {
        if(file == null) return false;
        if(!file.exists()) return false;
        return !file.renameTo(file);
    }

	private BmeFileHead readFileHead() throws IOException{
        if(in != null || out != null)
            throw new IllegalStateException();

        BmeFileHead fileHead;
        createIOStream();
        byte[] bme = new byte[3];
        in.readFully(bme); // 读BmeFile标识符
        if(!Arrays.equals(bme, BME))
            return null;

        byte[] ver = new byte[2];
        in.readFully(ver); // 读版本号

        fileHead = BmeFileHeadFactory.createByVersionCode(ver);

        fileHead.readFromStream(in);

		return fileHead;
	}

    private void createNewFileUsingHead(BmeFileHead head) throws IOException{
        if(head == null)
            throw new IllegalArgumentException();

        if(in != null || out != null)
            throw new IllegalStateException();

        if(!file.createNewFile()) {
            throw new IOException();
        }

        createIOStream();

        out.write(BME); // 写BmeFile文件标识符

        out.write(head.getVersion()); // 写版本号

        head.writeToStream(out);

        fileHead = head;
    }

    // 读单个byte数据
    public byte readByte() throws IOException{
        return in.readByte();
    }

    // 读单个int数据
    public int readInt() throws IOException {
        return DataIOUtil.readInt(in, fileHead.getByteOrder());
    }

    // 读单个double数据
    public double readDouble() throws IOException{
        return DataIOUtil.readDouble(in, fileHead.getByteOrder());
    }

    // 写单个byte数据
    public void writeData(byte data) throws IOException{
        out.writeByte(data);
        dataNum++;
    }

    // 写单个int数据
    public void writeData(int data) throws IOException{
        DataIOUtil.writeInt(out, data, fileHead.getByteOrder());
        dataNum++;
    }

    // 写单个double数据
    public void writeData(double data) throws IOException{
        DataIOUtil.writeDouble(out, data, fileHead.getByteOrder());
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
	
	public String getInfo() {
		return (fileHead == null) ? "" : fileHead.getInfo();
	}

	public BmeFileDataType getDataType() {
		return (fileHead == null) ? null : fileHead.getDataType();
	}

	public int getSampleRate() {
		return (fileHead == null) ? -1 : fileHead.getSampleRate();
	}
	
	public byte[] getVersion() {
		return (fileHead == null) ? null : fileHead.getVersion();
	}
	
	public ByteOrder getByteOrder() {
		return (fileHead == null) ? null : fileHead.getByteOrder();
	}
	
	public BmeFileHead getBmeFileHead() {
		return fileHead;
	}

	@Override
	public String toString() {
		return "[文件名：" + getFileName() + ":"+ fileHead + "; 数据个数：" + getDataNum() + "]";
	}

    protected abstract void createIOStream() throws FileNotFoundException;
    protected abstract int availableDataFromCurrentPos();
    protected abstract boolean isEof() throws IOException;
    public abstract void close() throws IOException;
}
