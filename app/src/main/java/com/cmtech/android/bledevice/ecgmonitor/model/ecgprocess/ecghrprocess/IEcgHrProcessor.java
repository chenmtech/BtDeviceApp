package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess;

/**
 * IEcgHrProcessor: 心率处理器接口
 * Created by Chenm, 2018-12-07
 */

public interface IEcgHrProcessor{
    // 无效心率值常量
    int INVALID_HR = 0;

    // 处理心率
    void process(int hr);
}
