package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * EcgAppendixFactory: 心电附信类工厂
 * Created by bme on 2019/1/10.
 */

public class EcgAppendixFactory {
    public static IEcgAppendix readFromStream(DataInput in) throws IOException{
        if(in == null) throw new IllegalArgumentException("The data input is null.");

        EcgAppendixType type = EcgAppendixType.getFromCode(ByteUtil.reverseInt(in.readInt()));
        IEcgAppendix appendix = create(type);
        if(appendix != null) {
            appendix.readFromStream(in);
            return appendix;
        }
        return null;
    }

    public static void writeToStream(IEcgAppendix appendix, DataOutput out) throws IOException{
        if(out == null || appendix == null) return;
        out.writeInt(ByteUtil.reverseInt(appendix.getType().getCode())); // 写类型码
        appendix.writeToStream(out);
    }

    private static IEcgAppendix create(EcgAppendixType type) {
        if(type == null) return null;
        switch (type) {
            case HEART_RATE:
                return EcgHrAppendix.create();
            case NORMAL_COMMENT:
                return EcgNormalComment.create();
            default:
                return null;
        }
    }
}
