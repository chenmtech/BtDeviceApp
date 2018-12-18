package com.cmtech.bmefile;

import java.io.IOException;

/**
 * CommonRandomAccessBmeFile: 一般的可随机访问的BmeFile
 * Created by Chenm, 2018-12-18
 */

public class CommonRandomAccessBmeFile extends RandomAccessBmeFile {
    private CommonRandomAccessBmeFile(String fileName) throws IOException {
        super(fileName);

        try {
            dataBeginPointer = raf.getFilePointer();
        } catch (IOException e) {
            throw new IOException("打开文件错误");
        }

        dataNum = availableData();
    }

    private CommonRandomAccessBmeFile(String fileName, BmeFileHead head) throws IOException{
        super(fileName, head);

        try {
            dataBeginPointer = raf.getFilePointer();
        } catch (IOException e) {
            throw new IOException("创建文件错误");
        }

        dataNum = 0;
    }

    // 打开已有文件
    public static CommonRandomAccessBmeFile openBmeFile(String fileName) throws IOException{
        return new CommonRandomAccessBmeFile(fileName);
    }

    // 用缺省文件头创建新的文件
    public static CommonRandomAccessBmeFile createBmeFile(String fileName) throws IOException{
        return new CommonRandomAccessBmeFile(fileName, DEFAULT_BMEFILE_HEAD);
    }

    // 用指定的文件头创建新的文件
    public static CommonRandomAccessBmeFile createBmeFile(String fileName, BmeFileHead head) throws IOException{
        return new CommonRandomAccessBmeFile(fileName, head);
    }

    @Override
    protected int availableData() {
        if(in != null) {
            try {
                return (int)((raf.length()-raf.getFilePointer())/fileHead.getDataType().getTypeLength());
            } catch (IOException e) {
                return 0;
            }
        }
        return 0;
    }
}
