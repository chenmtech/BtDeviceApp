package com.cmtech.android.bledevice.ecgmonitor.record.ecgcomment;

import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgCommentType;
import com.cmtech.android.bledevice.ecgmonitor.interfac.IEcgComment;

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

        EcgCommentType type = EcgCommentType.getFromCode(in.readInt());
        IEcgComment comment = create(type);
        if(comment != null) {
            comment.readFromStream(in);
            return comment;
        }
        return null;
    }

    public static void writeToStream(IEcgComment comment, DataOutput out) throws IOException{
        if(out == null || comment == null) return;
        out.writeInt(comment.getType().getCode()); // 写类型码
        comment.writeToStream(out);
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
