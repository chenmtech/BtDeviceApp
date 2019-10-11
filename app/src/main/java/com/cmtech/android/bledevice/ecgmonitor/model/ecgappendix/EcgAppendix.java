package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

/**
 * EcgAppendix: 心电附加信息抽象类
 * Created by bme on 2019/4/1.
 */

public abstract class EcgAppendix implements IEcgAppendix {
    private static final int TYPE_BYTE_NUM = 4; // 附加信息类型的字节数

    @Override
    public int length() {
        return TYPE_BYTE_NUM;
    }
}
