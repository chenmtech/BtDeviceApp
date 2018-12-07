package com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.IEcgProcessor;

/**
 * IEcgHrProcessor: 心率处理器接口
 * Created by Chenm, 2018-12-07
 */

public interface IEcgHrProcessor extends IEcgProcessor{
    // 无效心率值常量
    int INVALID_HR = 0;

    // 处理心率
    void process(int hr);
}
