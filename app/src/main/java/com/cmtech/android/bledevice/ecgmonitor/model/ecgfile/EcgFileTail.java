package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendixFactory;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgHrInfoAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;
import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


/**
 * EcgFileTail: 心电文件中的尾部类
 * Created by bme on 2019/1/19.
 */

public class EcgFileTail {
    private static final int FILETAIL_LEN_BYTE_NUM = 8;

    private EcgHrInfoAppendix hrInfoAppendix = new EcgHrInfoAppendix(); // 心率信息

    private List<EcgNormalComment> commentList = new ArrayList<>(); // 留言信息列表

    EcgFileTail() {
    }

    void setHrList(List<Integer> hrList) {
        hrInfoAppendix.setHrList(hrList);
    }

    List<Integer> getHrList() {
        return hrInfoAppendix.getHrList();
    }

    /**
     * 从数据输入流读取
     * @param raf：数据输入流
     */
    public void readFromStream(RandomAccessFile raf) throws IOException{
        raf.seek(raf.length() - FILETAIL_LEN_BYTE_NUM);

        long tailEndPointer = raf.getFilePointer();

        long tailLength = ByteUtil.reverseLong(raf.readLong());

        long appendixLength = tailLength - FILETAIL_LEN_BYTE_NUM;

        raf.seek(tailEndPointer - appendixLength);

        // 读留言信息
        while (raf.getFilePointer() < tailEndPointer) {
            IEcgAppendix appendix = EcgAppendixFactory.readFromStream(raf);

            if(appendix != null) {
                if(appendix instanceof EcgHrInfoAppendix) {
                    hrInfoAppendix = (EcgHrInfoAppendix)appendix;
                } else if(appendix instanceof EcgNormalComment){
                    commentList.add((EcgNormalComment) appendix);
                }
            }
        }
    }

    /**
     * 写出到数据输出流当前指针指向的位置
     * @param raf：数据输出流
     */
    public void writeToStream(RandomAccessFile raf) throws IOException{
        long filePointer = raf.getFilePointer();
        long tailLength = length();

        raf.setLength(filePointer + tailLength);

        raf.seek(filePointer);

        // 写留言信息
        for(EcgNormalComment comment : commentList) {
            EcgAppendixFactory.writeToStream(comment, raf);
        }

        // 写心率信息
        EcgAppendixFactory.writeToStream(hrInfoAppendix, raf);

        // 最后写入附加信息总长度
        raf.writeLong(ByteUtil.reverseLong(tailLength));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ':'
                + "心率数：" + hrInfoAppendix.getHrList().size() + ';'
                + "心率信息：" + hrInfoAppendix + ';'
                + "留言数：" + commentList.size() + ';'
                + "留言：" + commentList + ';'
                + "文件尾长度：" + length();
    }

    // 添加留言信息
    void addComment(EcgNormalComment comment) {
        commentList.add(comment);
    }

    // 删除留言信息
    void deleteComment(EcgNormalComment comment) {
        commentList.remove(comment);
    }

    // 获取留言信息列表
    List<EcgNormalComment> getCommentList() { return commentList; }

    /**
     * 获取EcgFileTail字节长度：所有留言长度 + 尾部长度（long 8字节）
      */
    public int length() {
        int length = hrInfoAppendix.length(); // 心率信息长度

        for(EcgNormalComment appendix : commentList) {
            length += appendix.length();
        }

        return length + FILETAIL_LEN_BYTE_NUM;
    }
}
