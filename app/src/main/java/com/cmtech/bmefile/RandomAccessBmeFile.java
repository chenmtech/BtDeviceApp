package com.cmtech.bmefile;

import com.cmtech.bmefile.exception.FileException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * RandomwAccessBmeFile: 可随机访问的BmeFile
 * Created by Chenm, 2018-12-03
 */

public class RandomAccessBmeFile extends BmeFile {
    protected long dataBeginPointer = 0; // 文件数据起始位置指针
    protected RandomAccessFile raf;

    protected RandomAccessBmeFile(String fileName) throws IOException {
        super(fileName);

        try {
            dataBeginPointer = raf.getFilePointer();
        } catch (IOException e) {
            throw new IOException("打开文件错误");
        }

        dataNum = availableData();
    }

    protected RandomAccessBmeFile(String fileName, BmeFileHead head) throws IOException{
        super(fileName, head);

        try {
            dataBeginPointer = raf.getFilePointer();
        } catch (IOException e) {
            throw new IOException("创建文件错误");
        }

        dataNum = 0;
    }

    // 打开已有文件
    public static RandomAccessBmeFile openBmeFile(String fileName) throws IOException{
        return new RandomAccessBmeFile(fileName);
    }

    // 用缺省文件头创建新的文件
    public static RandomAccessBmeFile createBmeFile(String fileName) throws IOException{
        return new RandomAccessBmeFile(fileName, DEFAULT_BMEFILE_HEAD);
    }

    // 用指定的文件头创建新的文件
    public static RandomAccessBmeFile createBmeFile(String fileName, BmeFileHead head) throws IOException{
        return new RandomAccessBmeFile(fileName, head);
    }

    @Override
    public boolean createInputStream() {
        try {
            raf = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            return false;
        }
        in = raf;
        out = raf;
        return true;
    }

    @Override
    public boolean createOutputStream() {
        try {
            raf = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            return false;
        }
        in = raf;
        out = raf;
        return true;
    }

    @Override
    public int availableData() {
        if(in != null) {
            try {
                return (int)((raf.length()-raf.getFilePointer())/fileHead.getDataType().getTypeLength());
            } catch (IOException e) {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public boolean isEof() throws IOException {
        if(raf == null) return true;
        return (raf.length() == raf.getFilePointer());
    }

    @Override
    public void close() throws FileException {
        try {
            if(raf != null) {
                raf.close();
                in = null;
                out = null;
                raf = null;
            }
        } catch(IOException ioe) {
            throw new FileException(file.getName(), "关闭文件操作错误");
        } finally {
            try {
                fileInOperation.remove(file.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}