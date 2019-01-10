package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import java.io.DataInput;
import java.io.DataOutput;

public interface IEcgAppendix {
    String getCreator();
    void setCreator(String creator);
    long getCreateTime();
    void setCreateTime(long createTime);
    boolean readFromStream(DataInput in);
    boolean writeToStream(DataOutput out);
    int length();
    EcgAppendixType getType();
    int getSecondInEcg();
    String getContent();
}
