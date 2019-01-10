package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import com.cmtech.android.bledeviceapp.util.DataIOUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * EcgNormalComment: 心电一般留言
 * Created by bme on 2018/11/21.
 */

public class EcgNormalComment extends EcgAppendix{
    private static final int CONTENT_LEN = 50;           // 留言内容字符数

    private String content = "无内容"; // 留言内容

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public EcgNormalComment() {
        super();
    }

    public EcgNormalComment(String creator, long createTime, String content) {
        super(creator, createTime);
        this.content = content;
    }

    @Override
    public boolean readFromStream(DataInput in) {
        try {
            if(!super.readFromStream(in)) return false;
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
            // 写留言内容
            DataIOUtil.writeFixedString(content, CONTENT_LEN, out);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public int length() {
        return  super.length() + 2*CONTENT_LEN;
    }

    @Override
    public EcgAppendixType getType() {
        return EcgAppendixType.NORMAL_COMMENT;
    }

    @Override
    public String toString() {
        return super.toString() +
                "留言：" + content + '\n';
    }
}
