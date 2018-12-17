package com.cmtech.bmefile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;

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
 * BmeFile: Bme文件
 * created by chenm, 2018-02-11
 */

public abstract class BmeFile {
    // 已经打开的文件列表
	protected static Set<String> fileInOperation = new HashSet<>();
	protected static final BmeFileHead DEFAULT_BMEFILE_HEAD = BmeFileHeadFactory.createDefault(); // 缺省文件头
	
	private static final byte[] BME = {'B', 'M', 'E'}; // BmeFile标识符
	
	protected File file; // 文件
	protected DataInput in; // 输入流
	protected DataOutput out; // 输出流
	protected final BmeFileHead fileHead; // 文件头
	protected int dataNum; // 文件包含的数据个数
    public int getDataNum() {
        return dataNum;
    }

    // 为已存在文件创建BmeFile
	protected BmeFile(String fileName) throws IOException{
		checkFile(fileName);
		fileHead = open();
		dataNum = availableData();
	}

	// 为不存在的文件创建BmeFile
	protected BmeFile(String fileName, BmeFileHead head) throws IOException{
		checkFile(fileName);
		fileHead = createUsingHead(head);
		dataNum = 0;
	}
	
	private void checkFile(String fileName) throws IOException{
		if(fileInOperation.contains(fileName))
			throw new IOException(fileName + "文件已经打开");
		else {
			file = new File(fileName);
			fileInOperation.add(fileName);
		}
	}

	private BmeFileHead open() throws IOException{
		BmeFileHead fileHead;
		
		if(file == null) 
			throw new IOException("文件未正常设置");
		if(!file.exists())
			throw new IOException(file.getName() + "文件不存在");
		if(in != null || out != null)
			throw new IOException(file.getName() + "文件已经打开，需要关闭后重新打开");
		
		try	{
            createInputStream();
			byte[] bme = new byte[3];
			in.readFully(bme);
			if(!Arrays.equals(bme, BME)) throw new IOException(file.getName() + "文件格式不对");
			byte[] ver = new byte[2];
			in.readFully(ver);
			fileHead = BmeFileHeadFactory.create(ver);
			fileHead.readFromStream(in);
		} catch (IOException e) {
			throw new IOException(file.getName() + "文件打开错误");
		}
		return fileHead;
	}



    private BmeFileHead createUsingHead(BmeFileHead head) throws IOException{
        if(file == null)
            throw new IOException("文件路径设置错误");
        if(head == null)
            throw new IOException("file head" + "文件头错误");
        if(in != null || out != null)
            throw new IOException(file.getName() + "文件已经打开，需要关闭后重新打开");

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
            throw new IOException(file.getName() + "创建文件错误");
        }
        return head;
    }

    public abstract void createInputStream() throws FileNotFoundException;
    public abstract void createOutputStream() throws FileNotFoundException;
	public abstract int availableData();
    public abstract boolean isEof() throws IOException;
    public abstract void close() throws IOException;
	
	// 读单个int数据
    public int readInt() throws IOException {
        if(in == null || fileHead == null) {
            throw new IOException("请先打开文件");
        }

        int data = 0;
        try {
            if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                data = in.readInt();
            } else {
                data = ByteUtil.reverseInt(in.readInt());
            }
        } catch(IOException ioe) {
            throw new IOException(file.getName() + "读数据错误");
        }
        return data;
    }

    // 读单个byte数据
    public byte readByte() throws IOException{
        if(in == null || fileHead == null) {
            throw new IOException("请先打开文件");
        }

        byte data = 0;
        try {
            data = in.readByte();
        } catch(IOException ioe) {
            throw new IOException(file.getName() + "读数据错误");
        }
        return data;
    }

    // 读单个double数据
    public double readDouble() throws IOException{
        if(in == null || fileHead == null) {
            throw new IOException("请先打开文件");
        }

        double data = 0;
        try {
            if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                data = in.readDouble();
            } else {
                data = ByteUtil.reverseDouble(in.readDouble());
            }
        } catch(IOException ioe) {
            throw new IOException(file.getName() + "读数据错误");
        }
        return data;
    }


    public BmeFile writeData(byte data) throws IOException{
        try {
            out.writeByte(data);
            dataNum++;
        } catch(IOException ioe) {
            throw new IOException(file.getName() + "写数据错误");
        }
        return this;
    }

    public BmeFile writeData(int data) throws IOException{
        try {
            if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                out.writeInt(data);
            } else {
                out.writeInt(ByteUtil.reverseInt(data));
            }
            dataNum++;
        } catch(IOException ioe) {
            throw new IOException(file.getName() + "写数据错误");
        }
        return this;
    }


    public BmeFile writeData(double data) throws IOException{
        try {
            if(fileHead.getByteOrder() == ByteOrder.BIG_ENDIAN) {
                out.writeDouble(data);
            } else {
                out.writeDouble(ByteUtil.reverseDouble(data));
            }
            dataNum++;
        } catch(IOException ioe) {
            throw new IOException(file.getName() + "写数据错误");
        }
        return this;
    }


	public BmeFile writeData(double[] data) throws IOException{
		if(out == null || fileHead == null) {
			throw new IOException("请先创建文件");
		}
		
		if(fileHead.getDataType() != BmeFileDataType.DOUBLE) {
			throw new IOException("写入数据类型错误");
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
			throw new IOException(file.getName() + "写数据错误");
		}
		return this;
	}


	
	public BmeFile writeData(int[] data) throws IOException{
		if(out == null || fileHead == null) {
			throw new IOException("请先创建文件");
		}
		
		if(fileHead.getDataType() != BmeFileDataType.INT32) {
			throw new IOException("写入数据类型错误");
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
			throw new IOException(file.getName() + "写数据错误");
		}
		return this;
	}
	

	
	public BmeFile writeData(byte[] data) throws IOException{
		if(out == null || fileHead == null) {
			throw new IOException("请先创建文件");
		}
		
		if(fileHead.getDataType() != BmeFileDataType.UINT8) {
			throw new IOException("写入数据类型错误");
		}
		
		try {
		    for(byte num : data) {
		        out.writeByte(num);
		        dataNum++;
            }
		} catch(IOException ioe) {
			throw new IOException(file.getName() + "写数据错误");
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
