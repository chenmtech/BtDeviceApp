package com.cmtech.android.bledevice.ecgmonitor.record.ecgcomment;

import com.cmtech.android.bledevice.ecgmonitor.interfac.IEcgComment;

import org.litepal.crud.LitePalSupport;

/**
 * EcgComment: 心电留言抽象类
 * Created by bme on 2019/4/1.
 */

public abstract class EcgComment extends LitePalSupport implements IEcgComment {
    private static final int TYPE_BYTE_NUM = 4; // 留言类型的字节数

    @Override
    public int length() {
        return TYPE_BYTE_NUM;
    }
}
