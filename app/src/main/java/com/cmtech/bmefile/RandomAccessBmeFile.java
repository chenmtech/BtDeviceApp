package com.cmtech.bmefile;

import java.io.IOException;

/**
 * RandomAccessBmeFile: 一般的可随机访问的BmeFile
 * Created by Chenm, 2018-12-18
 */

public class RandomAccessBmeFile extends AbstractRandomAccessBmeFile {
    private final long dataBeginPointer; // 数据起始位置指针

    private RandomAccessBmeFile(String fileName) throws IOException {
        super(fileName);
        dataBeginPointer = raf.getFilePointer();
        setDataNum(availableDataFromCurrentPos());
    }

    private RandomAccessBmeFile(String fileName, BmeFileHead head) throws IOException{
        super(fileName, head);
        dataBeginPointer = raf.getFilePointer();
        setDataNum(0);
    }

    // 打开已有文件
    public static RandomAccessBmeFile open(String fileName) throws IOException{
        return new RandomAccessBmeFile(fileName);
    }

    // 用缺省文件头创建新的文件
    public static RandomAccessBmeFile create(String fileName) throws IOException{
        return new RandomAccessBmeFile(fileName, DEFAULT_BME_FILE_HEAD);
    }

    // 用指定的文件头创建新的文件
    public static RandomAccessBmeFile create(String fileName, BmeFileHead head) throws IOException{
        return new RandomAccessBmeFile(fileName, head);
    }

    @Override
    protected int availableDataFromCurrentPos() {
        try {
            return (int)((raf.length()-raf.getFilePointer())/ head.getDataType().getByteNum());
        } catch (IOException e) {
            return 0;
        }
    }
}
