package com.cmtech.android.bledevice.ecg.interfac;

import com.cmtech.android.bledevice.ecg.enumeration.EcgCommentType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * IEcgComment: 心电留言接口
 * Created by bme on 2019/1/9.
 */

public interface IEcgComment {
    void readFromStream(DataInput in) throws IOException; // 读入
    void writeToStream(DataOutput out) throws IOException; // 写出
    int length(); // 长度
    EcgCommentType getType(); // 类型
}
