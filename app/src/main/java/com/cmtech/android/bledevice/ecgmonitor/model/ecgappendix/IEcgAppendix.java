package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * IEcgAppendix: 心电附加信息接口
 * Created by bme on 2019/1/9.
 */

public interface IEcgAppendix {
    void readFromStream(DataInput in) throws IOException;
    void writeToStream(DataOutput out) throws IOException;
    int length();
    EcgAppendixType getType();
}
