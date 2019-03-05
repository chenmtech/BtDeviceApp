package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import com.cmtech.android.bledeviceapp.model.UserAccount;

import java.io.DataInput;
import java.io.DataOutput;

/**
 * IEcgAppendix: 心电附加信息接口
 * Created by bme on 2019/1/9.
 */

public interface IEcgAppendix {
    UserAccount getCreator(); // 获取创建人
    String getCreatorName(); // 获取创建人名
    //void setCreatorName(String creatorName); // 设置创建人
    long getCreateTime(); // 获取创建时间
    void setCreateTime(long createTime); // 设置创建时间
    boolean readFromStream(DataInput in); // 从数据流中读
    boolean writeToStream(DataOutput out); // 写入数据流
    int length(); // 获取字节长度
    EcgAppendixType getType(); // 获取附加信息类型
    String toStringWithSampleRate(int sampleRate); // 带采样率的字符串输出
    boolean isReply(); // 是否是回复信息
    void setReply(boolean isReply);
}
