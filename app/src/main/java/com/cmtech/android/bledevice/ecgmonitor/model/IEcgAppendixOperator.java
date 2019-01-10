package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;

public interface IEcgAppendixOperator {
    // 删除一条附加信息
    void deleteComment(IEcgAppendix comment);

    // 定位一条附加信息
    void locateComment(IEcgAppendix comment);
}
