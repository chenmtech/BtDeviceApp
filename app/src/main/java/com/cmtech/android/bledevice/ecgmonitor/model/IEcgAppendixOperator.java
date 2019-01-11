package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;

public interface IEcgAppendixOperator {
    void deleteAppendix(IEcgAppendix appendix); // 删除一条附加信息
    void locateAppendix(IEcgAppendix appendix); // 定位一条附加信息
}
