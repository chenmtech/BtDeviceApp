package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DataIOUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * EcgComment: 心电留言
 * Created by bme on 2018/11/21.
 */

public class EcgComment {
    private static final int COMMENTATOR_LEN = 10;       // 留言人名字符数
    private static final int CONTENT_LEN = 50;           // 留言内容字符数

    private String commentator = "匿名";                  // 留言人
    private long createdTime;                             // 留言时间
    private int secondInEcg = -1;                         // 留言时,对应于Ecg信号中的秒数
    private String content = "无内容";                     // 留言内容

    public String getCommentator() {
        return commentator;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public int getSecondInEcg() {
        return secondInEcg;
    }

    public void setSecondInEcg(int secondInEcg) {
        this.secondInEcg = secondInEcg;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public EcgComment() {

    }

    public EcgComment(String commentator, long createdTime, String content) {
        this(commentator, createdTime, -1, content);
    }

    public EcgComment(String commentator, long createdTime, int secondInEcg, String content) {
        this.commentator = commentator;
        this.createdTime = createdTime;
        this.secondInEcg = secondInEcg;
        this.content = content;
    }

    public boolean readFromStream(DataInput in) {
        try {
            // 读留言人
            commentator = DataIOUtil.readFixedString(COMMENTATOR_LEN, in);
            // 读留言时间
            createdTime = ByteUtil.reverseLong(in.readLong());
            // 读留言秒数
            secondInEcg = ByteUtil.reverseInt(in.readInt());
            // 读留言内容
            content = DataIOUtil.readFixedString(CONTENT_LEN, in);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean writeToStream(DataOutput out) {
        try {
            // 写留言人
            DataIOUtil.writeFixedString(commentator, COMMENTATOR_LEN, out);
            // 写留言时间
            out.writeLong(ByteUtil.reverseLong(createdTime));
            // 写留言秒数
            out.writeInt(ByteUtil.reverseInt(secondInEcg));
            // 写留言内容
            DataIOUtil.writeFixedString(content, CONTENT_LEN, out);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static int length() {
        return  12 + 2*(COMMENTATOR_LEN + CONTENT_LEN);
    }

    @Override
    public String toString() {
        return commentator +
                "在" + DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(createdTime) +
                "说：第" + DateTimeUtil.secToTime(secondInEcg) + "秒，" + content + '\n';
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;

        EcgComment otherComment = (EcgComment)otherObject;

        return  (commentator.equals(otherComment.commentator) && (createdTime == otherComment.createdTime));
    }

    @Override
    public int hashCode() {
        return (int)(commentator.hashCode() + createdTime);
    }
}
