package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface IEcgAppendix {
    int APPENDIX_TYPE_BYTE_NUM = 4; // 附加信息类型的字节数
    void readFromStream(DataInput in) throws IOException;
    void writeToStream(DataOutput out) throws IOException;
    int length();
    EcgAppendixType getType();
}
