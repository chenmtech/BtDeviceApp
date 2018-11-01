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

public class StreamBmeFile extends BmeFile {
    private StreamBmeFile(String fileName) throws FileException {
        super(fileName);
    }

    private StreamBmeFile(String fileName, BmeFileHead head) throws FileException{
        super(fileName, head);
    }

    // 打开已有文件
    public static StreamBmeFile openBmeFile(String fileName) throws FileException{
        return new StreamBmeFile(fileName);
    }

    // 用缺省文件头创建新的文件
    public static StreamBmeFile createBmeFile(String fileName) throws FileException{
        return new StreamBmeFile(fileName, DEFAULT_BMEFILE_HEAD);
    }

    // 用指定的文件头创建新的文件
    public static StreamBmeFile createBmeFile(String fileName, BmeFileHead head) throws FileException{
        return new StreamBmeFile(fileName, head);
    }

    public void createInputStream() throws FileNotFoundException {
        in = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(file)));
    }

    public void createOutputStream() throws FileNotFoundException {
        out = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(file)));
    }

    public int availableData() {
        if(in != null) {
            try {
                return ((DataInputStream)in).available()/fileHead.getDataType().getTypeLength();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public boolean isEof() throws IOException {
        return (((DataInputStream)in).available() <= 0);
    }

    public void close() throws FileException {
        try {
            if(in != null) {
                ((DataInputStream)in).close();
                in = null;
            }
            if(out != null) {
                ((DataOutputStream)out).close();
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
