package com.cmtech.dsp.bmefile;

import com.cmtech.dsp.bmefile.exception.FileException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessBmeFile extends BmeFile {
    protected long dataBeginPointer = 0;
    protected RandomAccessFile raf;

    protected RandomAccessBmeFile(String fileName) throws FileException {
        super(fileName);

        setDataBeginPointer();
    }

    protected RandomAccessBmeFile(String fileName, BmeFileHead head) throws FileException{
        super(fileName, head);

        setDataBeginPointer();
    }

    protected void setDataBeginPointer() throws FileException {
        try {
            dataBeginPointer = raf.getFilePointer();
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException(getFileName(), "构造文件失败");
        }
    }

    // 打开已有文件
    public static RandomAccessBmeFile openBmeFile(String fileName) throws FileException{
        return new RandomAccessBmeFile(fileName);
    }

    // 用缺省文件头创建新的文件
    public static RandomAccessBmeFile createBmeFile(String fileName) throws FileException{
        return new RandomAccessBmeFile(fileName, DEFAULT_BMEFILE_HEAD);
    }

    // 用指定的文件头创建新的文件
    public static RandomAccessBmeFile createBmeFile(String fileName, BmeFileHead head) throws FileException{
        return new RandomAccessBmeFile(fileName, head);
    }

    public void createInputStream() throws FileNotFoundException {
        raf = new RandomAccessFile(file, "rw");
        in = raf;
        out = raf;
    }

    public void createOutputStream() throws FileNotFoundException {
        raf = new RandomAccessFile(file, "rw");
        in = raf;
        out = raf;
    }

    @Override
    public int getDataNum() {
        return (int)((file.length()-dataBeginPointer)/fileHead.getDataType().getTypeLength());
    }

    public int availableData() {
        if(in != null) {
            try {
                return (int)((raf.length()-raf.getFilePointer())/fileHead.getDataType().getTypeLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public boolean isEof() throws IOException {
        if(raf == null) return true;
        return (raf.length() == raf.getFilePointer());
    }

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