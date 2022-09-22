package com.cmtech.android.bledeviceapp.data.record;

import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 用来存放记录中的信号数据的文件类
 * 每个数据都是short类型
 */
public class RecordFile {
    // 记录中每个数据占用的字节数
    private static final int BYTES_PER_DATUM = 2;

    private RandomAccessFile raf;

    // 文件包含的数据个数
    private int size;

    public RecordFile(String fileName, String mode) throws IOException {
        // 打开文件
        if(mode.equals("o")) {
            File file = FileUtil.getFile(BasicRecord.SIG_PATH, fileName);
            if (file.exists() && file.renameTo(file)) {
                raf = new RandomAccessFile(file, "r");
                size = (int) (raf.length() / BYTES_PER_DATUM);
            } else {
                throw new IOException("The file can't be opened.");
            }
        } else if(mode.equals("c")) { // 创建文件
            File file = FileUtil.getFile(BasicRecord.SIG_PATH, fileName);
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

    // 读单个数据
    public int readData() throws IOException {
        return raf.readShort();
    }

    // 写入单个数据
    public void writeData(short data) throws IOException{
        raf.writeShort(data);
        size++;
    }

    // 是否到达文件末尾
    public boolean isEof() throws IOException {
        return (raf.length() == raf.getFilePointer());
    }

    // 文件指针定位到某个数据
    public void seekData(int pos) throws IOException {
        raf.seek((long) pos * BYTES_PER_DATUM);
    }

    // 文件是否为空
    public boolean isEmpty() {
        return size==0;
    }

    // 获取当前文件指针指向的数据位置
    public int getCurrentPos() throws IOException{
        return (int) (raf.getFilePointer() / BYTES_PER_DATUM);
    }

    // 关闭文件
    public void close() throws IOException {
        try {
            if(raf != null) {
                raf.close();
            }
        } catch(IOException e) {
            throw new IOException(e);
        } finally {
            raf = null;
        }
    }
}
