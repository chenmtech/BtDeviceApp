package com.cmtech.android.bledeviceapp.data.record;

import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 用来存放记录中的信号数据的文件类
 */
public class RecordFile {
    // 文件句柄
    private RandomAccessFile raf;

    // 记录中每个数据占用的字节数bytes per datum
    private final int datumByteNum;

    // 包含的数据个数
    private int size;

    public RecordFile(String fileName, int datumByteNum, String mode) throws IOException {
        this.datumByteNum = datumByteNum;

        // 打开文件
        if(mode.equals("o")) {
            File file = FileUtil.getFile(BasicRecord.SIG_FILE_PATH, fileName);
            if (file.exists() && file.renameTo(file)) {
                raf = new RandomAccessFile(file, "r");
                size = (int) (raf.length() / datumByteNum);
            } else {
                throw new IOException("The file can't be opened.");
            }
        }
        // 创建文件
        else if(mode.equals("c")) {
            File file = FileUtil.getFile(BasicRecord.SIG_FILE_PATH, fileName);
            if((!file.exists()) && file.createNewFile()) {
                raf = new RandomAccessFile(file, "rw");
                size = 0;
            } else {
                throw new IOException();
            }
        } else {
            throw new IOException();
        }
    }

    // 文件数据数量
    public int size() {
        return size;
    }

    // 读单个short数据
    public int readShort() throws IOException {
        return raf.readShort();
    }

    // 写入单个short数据
    public void writeShort(short data) throws IOException{
        raf.writeShort(data);
        size++;
    }

    // 读单个int数据
    public int readInt() throws IOException {
        return raf.readInt();
    }

    // 写入单个int数据
    public void writeInt(int data) throws IOException{
        raf.writeInt(data);
        size++;
    }

    // 读单个float数据
    public float readFloat() throws IOException {
        return raf.readFloat();
    }

    // 写入单个float数据
    public void writeFloat(float data) throws IOException{
        raf.writeFloat(data);
        size++;
    }

    // 是否到达文件末尾
    public boolean isEof() throws IOException {
        return (raf.length() == raf.getFilePointer());
    }

    // 文件指针定位到某个数据
    public void seekData(int pos) throws IOException {
        raf.seek((long) pos * datumByteNum);
    }

    // 文件是否为空
    public boolean isEmpty() {
        return size==0;
    }

    // 获取当前文件指针指向的数据位置
    public int getCurrentPos() throws IOException{
        return (int) (raf.getFilePointer() / datumByteNum);
    }

    // 关闭文件
    public void close() throws IOException {
        if(raf != null) {
            raf.close();
            raf = null;
        }
    }
}
