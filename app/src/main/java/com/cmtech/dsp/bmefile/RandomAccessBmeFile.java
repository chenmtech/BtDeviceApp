package com.cmtech.dsp.bmefile;

import com.cmtech.dsp.exception.FileException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessBmeFile extends BmeFile {
    private RandomAccessBmeFile(String fileName) throws FileException {
        super(fileName);
    }

    private RandomAccessBmeFile(String fileName, BmeFileHead head) throws FileException{
        super(fileName, head);
    }

    // 打开已有文件
    public static RandomAccessBmeFile openBmeFile(String fileName) throws FileException{
        return new RandomAccessBmeFile(fileName);
    }

    // 用缺省文件头创建新的文件
    public static RandomAccessBmeFile createBmeFile(String fileName) throws FileException{
        return new RandomAccessBmeFile(fileName, DEFAULT_FILE_HEAD);
    }

    // 用指定的文件头创建新的文件
    public static RandomAccessBmeFile createBmeFile(String fileName, BmeFileHead head) throws FileException{
        return new RandomAccessBmeFile(fileName, head);
    }

    public void createInputStream() throws FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        in = raf;
        out = raf;
    }

    public void createOutputStream() throws FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        in = raf;
        out = raf;
    }

    public int availableData() {
        if(in != null) {
            try {
                RandomAccessFile raf = (RandomAccessFile)in;
                return (int)((raf.length()-raf.getFilePointer())/fileHead.getDataType().getTypeLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public boolean isEof() throws IOException {
        RandomAccessFile raf = (RandomAccessFile)in;
        return (raf.length() == raf.getFilePointer());
    }

    public void close() throws FileException {
        try {
            if(in != null) {
                ((RandomAccessFile)in).close();
                in = null;
                out = null;
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
