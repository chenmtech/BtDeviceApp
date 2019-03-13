package com.cmtech.bmefile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * RandomwAccessBmeFile: 可随机访问的BmeFile
 * Created by Chenm, 2018-12-03
 */

public abstract class RandomAccessBmeFile extends BmeFile {
    protected long dataBeginPointer = 0; // 文件数据起始位置指针
    protected RandomAccessFile raf;

    protected RandomAccessBmeFile(String fileName) throws IOException {
        super(fileName);
    }

    protected RandomAccessBmeFile(String fileName, BmeFileHead head) throws IOException{
        super(fileName, head);
    }

    @Override
    protected boolean createInputStream() {
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
    protected boolean createOutputStream() {
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
    protected boolean isEof() throws IOException {
        return (raf == null ||raf.length() == raf.getFilePointer());
    }

    @Override
    public void close() throws IOException {
        try {
            if(raf != null) {
                raf.close();
            }
        } catch(IOException ioe) {
            throw new IOException(file.getName() + "关闭文件错误");
        } finally {
            //fileInOperation.remove(file.getCanonicalPath());
            in = null;
            out = null;
            raf = null;
        }
    }
}