package com.cmtech.bmefile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * StreamBmeFile: 一般Bme文件
 * created by chenm, 2018-08-30
 */

public class StreamBmeFile extends BmeFile {
    // 用于打开已有流文件
    private StreamBmeFile(String fileName) throws IOException {
        super(fileName);
        dataNum = availableData();
    }

    // 用于创建新的流文件
    private StreamBmeFile(String fileName, BmeFileHead head) throws IOException{
        super(fileName, head);
        dataNum = 0;
    }

    // 打开已有文件
    public static StreamBmeFile openBmeFile(String fileName) throws IOException{
        return new StreamBmeFile(fileName);
    }

    // 用缺省文件头创建新的文件
    public static StreamBmeFile createBmeFile(String fileName) throws IOException{
        return new StreamBmeFile(fileName, DEFAULT_BMEFILE_HEAD);
    }

    // 用指定的文件头创建新的文件
    public static StreamBmeFile createBmeFile(String fileName, BmeFileHead head) throws IOException{
        return new StreamBmeFile(fileName, head);
    }

    @Override
    protected boolean createInputStream() {
        try {
            in = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean createOutputStream() {
        try {
            out = new DataOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(file)));
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    // 获取从当前文件指针位置开始，还可获取多少个数据
    @Override
    protected int availableData() {
        if(in != null) {
            try {
                return ((DataInputStream)in).available()/fileHead.getDataType().getTypeLength();
            } catch (IOException e) {
                return 0;
            }
        }
        return 0;
    }

    @Override
    protected boolean isEof() throws IOException {
        return (((DataInputStream)in).available() <= 0);
    }

    @Override
    public void close() throws IOException {
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
            throw new IOException(file.getName() + "关闭文件错误");
        } finally {
            fileInOperation.remove(file.getCanonicalPath());
        }
    }
}
