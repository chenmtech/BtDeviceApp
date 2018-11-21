package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DataIOUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.bmefile.exception.FileException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * EcgComment: 心电留言
 * Created by bme on 2018/11/21.
 */

public class EcgComment {
    private static final int COMMENTATOR_LEN = 10;       // 留言人名字符数
    private static final int COMMENT_LEN = 50;           // 留言内容字符数

    private String commentator = "匿名";                  // 留言人
    private long commentTime;                             // 留言时间
    private String comment = "无内容";                    // 留言内容

    public String getCommentator() {
        return commentator;
    }

    public long getCommentTime() {
        return commentTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public EcgComment() {

    }

    public EcgComment(String commentator, long commentTime, String comment) {
        this.commentator = commentator;
        this.commentTime = commentTime;
        this.comment = comment;
    }

    public void readFromStream(DataInput in) throws FileException {
        try {
            // 读留言人
            commentator = DataIOUtil.readFixedString(COMMENTATOR_LEN, in);
            // 读留言时间
            commentTime = ByteUtil.reverseLong(in.readLong());
            // 读留言内容
            comment = DataIOUtil.readFixedString(COMMENT_LEN, in);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException("", "读心电文件头错误");
        }
    }

    public void writeToStream(DataOutput out) throws FileException {
        try {
            // 写留言人
            DataIOUtil.writeFixedString(commentator, COMMENTATOR_LEN, out);
            // 写留言时间
            out.writeLong(ByteUtil.reverseLong(commentTime));
            // 写留言内容
            DataIOUtil.writeFixedString(comment, COMMENT_LEN, out);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException("", "写心电文件头错误");
        }
    }

    public static int length() {
        return  8 + 2*(COMMENTATOR_LEN +COMMENT_LEN);
    }

    @Override
    public String toString() {
        return commentator +
                "在" + DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(commentTime) +
                "说：" + comment + '\n';
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;

        EcgComment otherComment = (EcgComment)otherObject;

        return  (commentator.equals(otherComment.commentator) && (commentTime == otherComment.commentTime));
    }

    @Override
    public int hashCode() {
        return (int)(commentator.hashCode() + commentTime);
    }
}
