package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendixFactory;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgHrInfoAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;
import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * EcgFileTail: 心电文件中的尾部类
 * Created by bme on 2019/1/19.
 */

public class EcgFileTail {
    private EcgHrInfoAppendix hrInfoAppendix = new EcgHrInfoAppendix(); // 心率信息
    private List<EcgNormalComment> commentList = new ArrayList<>(); // 留言信息列表

    public EcgFileTail() {

    }

    public void setHrList(List<Integer> hrList) {
        hrInfoAppendix.setHrList(hrList);
    }

    /**
     * 从数据输入流读取
     * @param raf：数据输入流
     * @return 是否成功读取
     */
    public boolean readFromStream(RandomAccessFile raf) {
        try {
            raf.seek(raf.length() - 8);
            long tailEndPointer = raf.getFilePointer();
            long tailLength = ByteUtil.reverseLong(raf.readLong());
            long appendixLength = tailLength - 8;
            raf.seek(tailEndPointer - appendixLength);

            // 读留言信息
            while (raf.getFilePointer() < tailEndPointer) {
                IEcgAppendix appendix = EcgAppendixFactory.readFromStream(raf);
                if(appendix != null) {
                    if(appendix instanceof EcgHrInfoAppendix) {
                        hrInfoAppendix = (EcgHrInfoAppendix)appendix;
                    } else if(appendix instanceof EcgNormalComment){
                        addComment((EcgNormalComment) appendix);
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * 写出到数据输出流当前指针指向的位置
     * @param raf：数据输出流
     * @return 是否成功写出
     */
    public boolean writeToStream(RandomAccessFile raf) {
        try {
            long filePointer = raf.getFilePointer();
            long length = length();
            raf.setLength(raf.getFilePointer() + length);
            raf.seek(filePointer);

            // 写附加信息
            for(EcgNormalComment appendix : commentList) {
                EcgAppendixFactory.writeToStream(appendix, raf);
            }

            // 写心率信息
            EcgAppendixFactory.writeToStream(hrInfoAppendix, raf);

            // 最后写入附加信息总长度
            raf.writeLong(ByteUtil.reverseLong(length));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[心电文件尾："
                + "心率数：" + hrInfoAppendix.getHrList().size() + ";"
                + "心率信息：" + hrInfoAppendix + ';'
                + "留言数：" + commentList.size() + ";"
                + "留言：" + Arrays.toString(commentList.toArray()) + ";"
                + "文件尾长度：" + length() + "]";
    }

    // 添加留言信息
    public void addComment(EcgNormalComment comment) {
        commentList.add(comment);
    }

    // 删除留言信息
    public void deleteComment(EcgNormalComment comment) {
        commentList.remove(comment);
    }

    // 获取留言信息列表
    public List<EcgNormalComment> getCommentList() { return commentList; }

    public void setCommentList(List<EcgNormalComment> commentList) {
        this.commentList = commentList;
    }

    // 获取留言信息数
    public int getCommentNum() {
        return commentList.size();
    }

    /**
     * 获取EcgFileTail字节长度：所有留言长度 + 尾部长度（long 8字节）
      */
    public int length() {
        int length = hrInfoAppendix.length(); // 心率信息长度

        for(EcgNormalComment appendix : commentList) {
            length += appendix.length();
        }

        return length + 8; // "加8"是指包含最后的附加信息长度long类型
    }
}
