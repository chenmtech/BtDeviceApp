package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.bmefile.exception.FileException;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EcgFileTail {
    private List<EcgComment> commentList = new ArrayList<>();

    public EcgFileTail() {

    }

    public void readFromStream(RandomAccessFile raf) throws FileException {
        try {
            raf.seek(raf.length() - 8);
            long tailEndPointer = raf.getFilePointer();
            long length = ByteUtil.reverseLong(raf.readLong());
            int commentNum = (int)(length/EcgComment.length());
            raf.seek(tailEndPointer - length);
            // 读留言
            for(int i = 0; i < commentNum; i++) {
                EcgComment comment = new EcgComment();
                comment.readFromStream(raf);
                commentList.add(comment);
            }
            // 按留言时间排序
            Collections.sort(commentList, new Comparator<EcgComment>() {
                @Override
                public int compare(EcgComment o1, EcgComment o2) {
                    return (int)(o1.getCreatedTime() - o2.getCreatedTime());
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException("", "读心电文件头错误");
        }
    }

    public void writeToStream(RandomAccessFile raf) throws FileException {
        try {
            long filePointer = raf.getFilePointer();
            long length = commentList.size()*EcgComment.length();
            raf.setLength(raf.getFilePointer() + length + 8);
            raf.seek(filePointer);

            // 写留言
            for(int i = 0; i < commentList.size(); i++) {
                commentList.get(i).writeToStream(raf);
            }

            raf.writeLong(ByteUtil.reverseLong(length));
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException("", "写心电文件头错误");
        }
    }

    @Override
    public String toString() {
        return "[心电文件尾信息："
                + "留言数：" + commentList.size() + ";"
                + "留言：" + Arrays.toString(commentList.toArray()) + "]";
    }

    // 添加留言
    public void addComment(EcgComment aComment) {
        commentList.add(aComment);
    }

    // 删除留言
    public void deleteComment(EcgComment aComment) {
        commentList.remove(aComment);
    }

    public List<EcgComment> getCommentList() { return commentList; }

    // 获取留言数
    public int getCommentsNum() {
        return commentList.size();
    }

    // EcgFileTail字节长度
    public int length() {
        return commentList.size()*EcgComment.length() + 8;
    }
}
