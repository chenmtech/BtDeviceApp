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

    private static IEcgAppendix create(EcgAppendixType type) {
        if(type == null) return null;

        switch (type) {
            case HR_INFO:
                return new EcgHrInfoAppendix();
            case NORMAL_COMMENT:
                return new EcgNormalComment();
            default:
                return null;
        }
    }

    public static IEcgAppendix readFromStream(DataInput in) throws IOException{
        if(in == null) throw new IllegalArgumentException();

        EcgAppendixType type = EcgAppendixType.getFromCode(ByteUtil.reverseInt(in.readInt()));

        IEcgAppendix appendix = create(type);

        if(appendix != null) {
            appendix.readFromStream(in);
            return appendix;
        }

        return null;
    }

    public static void writeToStream(IEcgAppendix appendix, DataOutput out) throws IOException{
        if(out == null) throw new IllegalArgumentException();

        if(appendix == null) return;

        out.writeInt(ByteUtil.reverseInt(appendix.getType().getCode()));

        appendix.writeToStream(out);
    }
}
