/**
 * Project Name:DSP_JAVA
 * File Name:BmeFile.java
 * Package Name:com.cmtech.dsp.bmefile
 * Date:2018年2月11日上午6:23:50
 * Copyright (c) 2018, e_yujunquan@163.com All Rights Reserved.
 *
 */
package com.cmtech.dsp.bmefile;

import com.cmtech.dsp.bmefile.exception.FileException;
import com.cmtech.dsp.util.FormatTransfer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ClassName: BmeFile
 * Function: TODO ADD FUNCTION. 
 * Reason: TODO ADD REASON(可选). 
 * date: 2018年2月11日 上午6:23:50 
 *
 * @author bme
 * @version 
 * @since JDK 1.6
 */
public abstract class BmeFile {
	protected static Set<String> fileInOperation = new HashSet<>();
	protected static final BmeFileHead DEFAULT_BMEFILE_HEAD = BmeFileHeadFactory.createDefault();
	
	public static final byte[] BME = {'B', 'M', 'E'};
	
	protected File file;
	
	protected DataInput in;
	protected DataOutput out;
	
	protected final BmeFileHead fileHead;

	private int dataNum;
    public int getDataNum() {
        return dataNum;
    }

    // 为已存在文件创建BmeFile
	protected BmeFile(String fileName) throws FileException{
		checkFile(fileName);
		fileHead = open();
		dataNum = availableData();
	}

	// 为不存在的文件创建BmeFile
	protected BmeFile(String fileName, BmeFileHead head) throws FileException{
		checkFile(fileName);
		fileHead = createUsingHead(head);
		dataNum = 0;
	}
	
	private void checkFile(String fileName) throws FileException{
		if(fileInOperation.contains(fileName))
			throw new FileException(fileName, "文件已经打开");
		else {
			file = new File(fileName);
			fileInOperation.add(fileName);
		}
	}

	private BmeFileHead open() throws FileException{
		BmeFileHead fileHead;
		
		if(file == null) 
			throw new FileException("", "文件未正常设置");
		if(!file.exists())
			throw new FileException(file.getName(), "文件不存在");
		if(in != null || out != null)
			throw new FileException(file.getName(), "文件已经打开，需要关闭后重新打开");
		
		try	{
            createInputStream();
			byte[] bme = new byte[3];
			in.readFully(bme);
			if(!Arrays.equals(bme, BME)) throw new FileException(file.getName(), "文件格式不对");
			byte[] ver = new byte[2];
			in.readFully(ver);
			fileHead = BmeFileHeadFactory.create(ver);
			fileHead.readFromStream(in);
		} catch (IOException e) {
			throw new FileException(file.getName(), "文件打开错误");
		}
		return fileHead;
	}

	public abstract void createInputStream() throws FileNotFoundException;

    private BmeFileHead createUsingHead(BmeFileHead head) throws FileException{
        if(file == null)
            throw new FileException("", "文件路径设置错误");
        if(head == null)
            throw new FileException("file head", "文件头错误");
        if(in != null || out != null)
            throw new FileException(file.getName(), "文件已经打开，需要关闭后重新打开");

        try {
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile();
            createOutputStream();
            out.write(BME);
            out.write(head.getVersion());
            head.writeToStream(out);
        } catch(IOException ioe) {
            throw new FileException(file.getName(), "创建文件错误");
        }
        return head;
    }

    public abstract void createOutputStream() throws FileNotFoundException;

	public abstract int availableData();
	
	public double[] readData(double[] d) throws FileException{
		if(in == null || fileHead == null) {
			throw new FileException("", "请先打开文件");
		}
		
		if(fileHead.getDataType() != BmeFileDataType.DOUBLE) {
			throw new FileException(file.getName(), "读取数据类型错误");
		}
		
		List<Double> lst = new ArrayList<Double>();
		double[] data;
		byte[] buf = new byte[8];
		ByteBuffer big = ByteBuffer.wrap(buf).order(ByteOrder.BIG_ENDIAN);
		ByteBuffer little = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
		try {
			if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
				while(!isEof())
					lst.add(in.readDouble());				
			} else {
				while(!isEof()) {
					little.putDouble(0, in.readDouble());
					lst.add(big.getDouble(0));
				}
			}
			
			data = new double[lst.size()];
			for(int i = 0; i < lst.size(); i++) {
				data[i] = lst.get(i);
			}
		} catch(IOException ioe) {
			throw new FileException(file.getName(), "读数据错误");
		}
		return data;
	}

    public int readData() throws FileException{
        if(in == null || fileHead == null) {
            throw new FileException("", "请先打开文件");
        }

        if(fileHead.getDataType() != BmeFileDataType.INT32) {
            throw new FileException(file.getName(), "读取数据类型错误");
        }

        int data = 0;
        byte[] buf = new byte[4];
        ByteBuffer big = ByteBuffer.wrap(buf).order(ByteOrder.BIG_ENDIAN);
        ByteBuffer little = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        try {
            if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                data = in.readInt();
            } else {
                little.putInt(0, in.readInt());
                data = big.getInt(0);
            }
        } catch(IOException ioe) {
            throw new FileException(file.getName(), "读数据错误");
        }
        return data;
    }
	
	public int[] readData(int[] d) throws FileException{
		if(in == null || fileHead == null) {
			throw new FileException("", "请先打开文件");
		}
		
		if(fileHead.getDataType() != BmeFileDataType.INT32) {
			throw new FileException(file.getName(), "读取数据类型错误");
		}
		
		List<Integer> lst = new ArrayList<Integer>();
		int[] data = new int[0];
		byte[] buf = new byte[4];
		ByteBuffer big = ByteBuffer.wrap(buf).order(ByteOrder.BIG_ENDIAN);
		ByteBuffer little = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
		try {
			if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
				while(!isEof())
					lst.add(in.readInt());				
			} else {
				while(isEof()) {
					little.putInt(0, in.readInt());
					lst.add(big.getInt(0));
				}
			}
			
			data = new int[lst.size()];
			for(int i = 0; i < lst.size(); i++) {
				data[i] = lst.get(i);
			}
		} catch(IOException ioe) {
			throw new FileException(file.getName(), "读数据错误");
		}
		return data;
	}
	
	public byte[] readData(byte[] d) throws FileException{
		if(in == null || fileHead == null) {
			throw new FileException("", "请先打开文件");
		}
		
		if(fileHead.getDataType() != BmeFileDataType.UINT8) {
			throw new FileException(file.getName(), "读取数据类型错误");
		}
		
		List<Byte> lst = new ArrayList<>();
		byte[] data = new byte[0];
		try {
			while(!isEof())
				lst.add(in.readByte());
			
			data = new byte[lst.size()];
			for(int i = 0; i < lst.size(); i++) {
				data[i] = lst.get(i);
			}
		} catch(IOException ioe) {
			throw new FileException(file.getName(), "读数据错误");
		}
		return data;
	}
	
	public BmeFile writeData(double[] data) throws FileException{
		if(out == null || fileHead == null) {
			throw new FileException("", "请先创建文件");
		}
		
		if(fileHead.getDataType() != BmeFileDataType.DOUBLE) {
			throw new FileException("", "写入数据类型错误");
		}
		
		try {
			if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
				for(int i = 0; i < data.length; i++) {
					out.writeDouble(data[i]);
					dataNum++;
				}				
			} else {
				for(int i = 0; i < data.length; i++) {
					out.write(FormatTransfer.toLH(data[i]));
					dataNum++;
				}
			}
		} catch(IOException ioe) {
			throw new FileException(file.getName(), "写数据错误");
		}
		return this;
	}
	
	public BmeFile writeData(double data) throws FileException{
		try {
			if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
				out.writeDouble(data);			
			} else {
				out.write(FormatTransfer.toLH(data));
			}
			dataNum++;
		} catch(IOException ioe) {
			throw new FileException(file.getName(), "写数据错误");
		}
		return this;
	}
	
	public BmeFile writeData(int[] data) throws FileException{
		if(out == null || fileHead == null) {
			throw new FileException("", "请先创建文件");
		}
		
		if(fileHead.getDataType() != BmeFileDataType.INT32) {
			throw new FileException("", "写入数据类型错误");
		}
		
		try {
			if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
				for(int i = 0; i < data.length; i++) {
					out.writeInt(data[i]);
					dataNum++;
				}				
			} else {
				for(int i = 0; i < data.length; i++) {
					out.write(FormatTransfer.toLH(data[i]));
					dataNum++;
				}
			}
		} catch(IOException ioe) {
			throw new FileException(file.getName(), "写数据错误");
		}
		return this;
	}
	
	public BmeFile writeData(int data) throws FileException{
		try {
			if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
				out.writeInt(data);			
			} else {
				out.write(FormatTransfer.toLH(data));
			}
			dataNum++;
		} catch(IOException ioe) {
			throw new FileException(file.getName(), "写数据错误");
		}
		return this;
	}
	
	public BmeFile writeData(byte[] data) throws FileException{
		if(out == null || fileHead == null) {
			throw new FileException("", "请先创建文件");
		}
		
		if(fileHead.getDataType() != BmeFileDataType.UINT8) {
			throw new FileException("", "写入数据类型错误");
		}
		
		try {
			for(int i = 0; i < data.length; i++) {
				out.writeByte(data[i]);
				dataNum++;
			}
		} catch(IOException ioe) {
			throw new FileException(file.getName(), "写数据错误");
		}
		return this;
	}
	
	public BmeFile writeData(byte data) throws FileException{
		try {
			out.writeByte(data);
			dataNum++;
		} catch(IOException ioe) {
			throw new FileException(file.getName(), "写数据错误");
		}
		return this;
	}

	public abstract boolean isEof() throws IOException;
	public abstract void close() throws FileException;
	
	public File getFile() {
	    return file;
    }

	public String getFileName() {
		if(file == null) return "";
		return file.toString();
	}
	
	public String getInfo() {
		if(fileHead == null) return "";
		return fileHead.getInfo();
	}

	public BmeFileDataType getDataType() {
		if(fileHead == null) return null;
		return fileHead.getDataType();
	}

	public int getFs() {
		if(fileHead == null) return -1;
		return fileHead.getFs();
	}
	
	public byte[] getVersion() {
		if(fileHead == null) return null;
		return fileHead.getVersion();
	}
	
	public ByteOrder getByteOrder() {
		if(fileHead == null) return null;
		return fileHead.getByteOrder();
	}
	
	public BmeFileHead getBmeFileHead() {
		return fileHead;
	}

	@Override
	public String toString() {
		return "[文件名：" + getFileName() + ":"+ fileHead + "; 数据个数：" + getDataNum() + "]";
	}
}
