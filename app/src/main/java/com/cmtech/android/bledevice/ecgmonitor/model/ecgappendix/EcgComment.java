package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

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

public class EcgComment extends EcgAppendix{
    private static final int CONTENT_LEN = 50;           // 留言内容字符数

    private int secondInEcg = -1; // 留言时,对应于Ecg信号中的秒数
    private String content = "无内容"; // 留言内容

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
        super();
        super.setType(EcgAppendixType.NORMAL_COMMENT);
    }

    public EcgComment(String creator, long createTime, String content) {
        this(creator, createTime, -1, content);
    }

    public EcgComment(String creator, long createTime, int secondInEcg, String content) {
        super(EcgAppendixType.NORMAL_COMMENT, creator, createTime);
        this.secondInEcg = secondInEcg;
        this.content = content;
    }

    @Override
    public boolean readFromStream(DataInput in) {
        try {
            if(!super.readFromStream(in)) return false;
            if(super.getType() != EcgAppendixType.NORMAL_COMMENT) return false;
            // 读留言秒数
            secondInEcg = ByteUtil.reverseInt(in.readInt());
            // 读留言内容
            content = DataIOUtil.readFixedString(CONTENT_LEN, in);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean writeToStream(DataOutput out) {
        try {
            if(!super.writeToStream(out)) return false;
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
        return  EcgAppendix.length() + 2*CONTENT_LEN + 4;
    }

    @Override
    public String toString() {
        return super.toString() +
                "留言：第" + DateTimeUtil.secToTime(secondInEcg) + "秒，" + content + '\n';
    }
}
