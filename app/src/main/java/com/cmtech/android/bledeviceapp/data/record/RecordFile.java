package com.cmtech.android.bledeviceapp.data.record;

import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RecordFile {
    private File file; // 文件
    private RandomAccessFile raf;
    private int size;

    public RecordFile(String fileName, String mode) throws IOException {
        if(mode.equals("o")) {
            File file = FileUtil.getFile(BasicRecord.SIG_PATH, fileName);
            if (file.exists() && file.renameTo(file)) {
                this.file = file;
                raf = new RandomAccessFile(file, "rw");
                size = (int) (raf.length() / 2);
            } else {
                throw new IOException("The file can't be opened.");
            }
        } else if(mode.equals("c")) {
            File file = FileUtil.getFile(BasicRecord.SIG_PATH, fileName);
            if(!file.exists()) {
                this.file = file;
                if(!file.createNewFile()) {
                    throw new IOException();
                }
                raf = new RandomAccessFile(file, "rw");
                size = 0;
            } else {
                throw new IOException();
            }
        } else {
            throw new IOException();
        }
    }

    public int size() {
        return size;
    }

    // 读单个数据
    public int readData() throws IOException {
        return raf.readShort();
    }

    // 写单个数据
    public void writeData(short data) throws IOException{
        raf.writeShort(data);
        size++;
    }

    public boolean isEof() throws IOException {
        return (raf == null || raf.length() == raf.getFilePointer());
    }

    public void seekData(int pos) throws IOException {
        raf.seek(pos* 2L);
    }

    public boolean isEmpty() {
        return size==0;
    }

    public long getFilePointer() throws IOException {
        return raf.getFilePointer();
    }

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
