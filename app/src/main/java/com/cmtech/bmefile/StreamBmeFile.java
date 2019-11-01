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
        dataNum = availableDataFromCurrentPos();
    }

    // 用于创建新的流文件
    private StreamBmeFile(String fileName, BmeFileHead head) throws IOException{
        super(fileName, head);
        dataNum = 0;
    }

    // 打开已有文件
    public static StreamBmeFile open(String fileName) throws IOException{
        return new StreamBmeFile(fileName);
    }

    // 用缺省文件头创建新的文件
    public static StreamBmeFile create(String fileName) throws IOException{
        return new StreamBmeFile(fileName, DEFAULT_BME_FILE_HEAD);
    }

    // 用指定的文件头创建新的文件
    public static StreamBmeFile create(String fileName, BmeFileHead head) throws IOException{
        return new StreamBmeFile(fileName, head);
    }

    @Override
    protected void createIOStream() throws FileNotFoundException{
        in = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(file)));

        out = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(file)));
    }

    // 获取从当前文件指针位置开始，还可获取多少个数据
    @Override
    protected int availableDataFromCurrentPos() {
        try {
            return ((DataInputStream)in).available()/ head.getDataType().getByteNum();
        } catch (IOException e) {
            return 0;
        }
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
            }
            if(out != null) {
                ((DataOutputStream)out).close();
            }
        } catch(IOException e) {
            throw new IOException(e);
        } finally {
            //fileInOperation.remove(file.getCanonicalPath());
            in = null;
            out = null;
        }
    }
}
