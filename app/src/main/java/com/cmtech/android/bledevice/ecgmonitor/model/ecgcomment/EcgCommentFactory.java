package com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment;

import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * EcgCommentFactory: 心电留言类工厂
 * Created by bme on 2019/1/10.
 */

public class EcgCommentFactory {
    public static IEcgComment readFromStream(DataInput in) throws IOException{
        if(in == null) throw new IllegalArgumentException("The data input is null.");

        EcgCommentType type = EcgCommentType.getFromCode(ByteUtil.reverseInt(in.readInt()));
        IEcgComment appendix = create(type);
        if(appendix != null) {
            appendix.readFromStream(in);
            return appendix;
        }
        return null;
    }

    public static void writeToStream(IEcgComment appendix, DataOutput out) throws IOException{
        if(out == null || appendix == null) return;
        out.writeInt(ByteUtil.reverseInt(appendix.getType().getCode())); // 写类型码
        appendix.writeToStream(out);
    }

    private static IEcgComment create(EcgCommentType type) {
        if(type == null) return null;
        switch (type) {
            case NORMAL_COMMENT:
                return EcgNormalComment.create();
            default:
                return null;
        }
    }
}
