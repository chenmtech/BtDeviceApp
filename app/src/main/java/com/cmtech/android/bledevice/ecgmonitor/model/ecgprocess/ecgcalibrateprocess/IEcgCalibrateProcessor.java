package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgcalibrateprocess;

/**
 * IEcgCalibrateProcessor: 心电信号定标处理器接口，用于定标（归一化）信号值
 * Created by bme on 2018/12/06.
 */

public interface IEcgCalibrateProcessor {
    // 对输入信号值做定标处理
    int process(int data);
}
