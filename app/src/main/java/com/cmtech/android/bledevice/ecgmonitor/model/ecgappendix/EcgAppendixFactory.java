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
        switch (type) {
            case HR_INFO:
                return new EcgHrInfoAppendix();
            case NORMAL_COMMENT:
                return new EcgNormalComment();
            default:
                return null;
        }
    }

    public static IEcgAppendix readFromStream(DataInput in) {
        try {
            // 读类型
            EcgAppendixType type = EcgAppendixType.getFromCode(ByteUtil.reverseInt(in.readInt()));
            if(type != null) {
                IEcgAppendix appendix = create(type);
                if(appendix != null) {
                    appendix.readFromStream(in);
                    return appendix;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public static boolean writeToStream(IEcgAppendix appendix, DataOutput out) {
        try {
            // 写类型
            out.writeInt(ByteUtil.reverseInt(appendix.getType().getCode()));
            appendix.writeToStream(out);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
