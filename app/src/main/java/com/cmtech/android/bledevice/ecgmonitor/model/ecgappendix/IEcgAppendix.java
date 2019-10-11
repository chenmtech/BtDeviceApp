package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * IEcgAppendix: 心电附加信息接口
 * Created by bme on 2019/1/9.
 */

public interface IEcgAppendix {
    void readFromStream(DataInput in) throws IOException; // 读入
    void writeToStream(DataOutput out) throws IOException; // 写出
    int length(); // 长度
    EcgAppendixType getType(); // 类型
}
