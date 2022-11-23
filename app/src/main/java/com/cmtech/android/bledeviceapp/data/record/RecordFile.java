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
    private final int bytePerDatum;

    // 信号通道数
    private final int channels;

    // 每一帧数据占用的字节数，bytePerFrame = bytePerDatum * channels
    private final int bytePerFrame;

    // 包含的数据帧数
    private int size;

    public RecordFile(String fileName, int bytePerDatum, int channels, String mode) throws IOException {
        this.bytePerDatum = bytePerDatum;
        this.channels = channels;
        bytePerFrame = this.bytePerDatum * this.channels;

        // 打开文件
        if(mode.equals("o")) {
            File file = FileUtil.getFile(BasicRecord.SIG_FILE_PATH, fileName);
            if (file.exists() && file.renameTo(file)) {
                raf = new RandomAccessFile(file, "r");
                size = (int) (raf.length() / this.bytePerFrame);
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

    // 读单个short类型的帧数据
    public int[] readShort() throws IOException {
        int[] data = new int[channels];
        for(int i = 0; i < channels; i++)
            data[i] = raf.readShort();

        return data;
    }

    // 写入单个short类型的帧数据
    public void writeShort(short[] data) throws IOException{
        assert data.length == channels;
        for(short d : data)
            raf.writeShort(d);
        size++;
    }

    // 读单个int类型的帧数据
    public int[] readInt() throws IOException {
        int[] data = new int[channels];
        for(int i = 0; i < channels; i++)
            data[i] = raf.readInt();

        return data;
    }

    // 写入单个int类型的帧数据
    public void writeInt(int[] data) throws IOException{
        assert data.length == channels;
        for(int d : data)
            raf.writeInt(d);
        size++;
    }

/*    // 读单个float类型的帧数据
    public float readFloat() throws IOException {
        return raf.readFloat();
    }

    // 写入单个float类型的帧数据
    public void writeFloat(float data) throws IOException{
        raf.writeFloat(data);
        size++;
    }*/

    // 是否到达文件末尾
    public boolean isEof() throws IOException {
        return (raf.length() == raf.getFilePointer());
    }

    // 文件指针定位到某个数据帧
    public void seek(int pos) throws IOException {
        raf.seek((long) pos * bytePerFrame);
    }

    // 文件是否为空
    public boolean isEmpty() {
        return size==0;
    }

    // 获取当前文件指针指向的数据帧位置
    public int getCurrentPos() throws IOException{
        return (int) (raf.getFilePointer() / bytePerFrame);
    }

    // 关闭文件
    public void close() throws IOException {
        if(raf != null) {
            raf.close();
            raf = null;
        }
    }
}
