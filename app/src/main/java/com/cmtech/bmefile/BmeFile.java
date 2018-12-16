/**
 * Project Name:DSP_JAVA
 * File Name:BmeFile.java
 * Package Name:com.cmtech.bmefile
 * Date:2018年2月11日上午6:23:50
 * Copyright (c) 2018, e_yujunquan@163.com All Rights Reserved.
 *
 */
package com.cmtech.bmefile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.bmefile.exception.FileException;
import com.vise.log.ViseLog;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
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
	
	private static final byte[] BME = {'B', 'M', 'E'};
	
	protected File file;
	
	protected DataInput in;
	protected DataOutput out;
	
	protected final BmeFileHead fileHead;

	protected int dataNum;
    public int getDataNum() {
        return dataNum;
    }

    // 为已存在文件创建BmeFile
	protected BmeFile(String fileName) throws FileException{
		checkFile(fileName);
		ViseLog.e("hi4");
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

    public abstract void createInputStream() throws FileNotFoundException;
    public abstract void createOutputStream() throws FileNotFoundException;
	public abstract int availableData();
    public abstract boolean isEof() throws IOException;
    public abstract void close() throws FileException;
	
	// 读单个int数据
    public int readInt() throws FileException{
        if(in == null || fileHead == null) {
            throw new FileException("", "请先打开文件");
        }

        int data = 0;
        try {
            if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                data = in.readInt();
            } else {
                data = ByteUtil.reverseInt(in.readInt());
            }
        } catch(IOException ioe) {
            throw new FileException(file.getName(), "读数据错误");
        }
        return data;
    }

    // 读单个byte数据
    public byte readByte() throws FileException{
        if(in == null || fileHead == null) {
            throw new FileException("", "请先打开文件");
        }

        byte data = 0;
        try {
            data = in.readByte();
        } catch(IOException ioe) {
            throw new FileException(file.getName(), "读数据错误");
        }
        return data;
    }

    // 读单个double数据
    public double readDouble() throws FileException{
        if(in == null || fileHead == null) {
            throw new FileException("", "请先打开文件");
        }

        double data = 0;
        try {
            if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                data = in.readDouble();
            } else {
                data = ByteUtil.reverseDouble(in.readDouble());
            }
        } catch(IOException ioe) {
            throw new FileException(file.getName(), "读数据错误");
        }
        return data;
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

    public BmeFile writeData(int data) throws FileException{
        try {
            if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                out.writeInt(data);
            } else {
                out.writeInt(ByteUtil.reverseInt(data));
            }
            dataNum++;
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
                out.writeDouble(ByteUtil.reverseDouble(data));
            }
            dataNum++;
        } catch(IOException ioe) {
            throw new FileException(file.getName(), "写数据错误");
        }
        return this;
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
			    for(double num : data) {
			        out.writeDouble(num);
			        dataNum++;
                }
			} else {
                for(double num : data) {
                    out.writeDouble(ByteUtil.reverseDouble(num));
                    dataNum++;
                }
			}
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
			    for(int num : data) {
			        out.writeInt(num);
			        dataNum++;
                }
			} else {
			    for(int num : data) {
			        out.writeInt(ByteUtil.reverseInt(num));
			        dataNum++;
                }
			}
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
		    for(byte num : data) {
		        out.writeByte(num);
		        dataNum++;
            }
		} catch(IOException ioe) {
			throw new FileException(file.getName(), "写数据错误");
		}
		return this;
	}



	
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
