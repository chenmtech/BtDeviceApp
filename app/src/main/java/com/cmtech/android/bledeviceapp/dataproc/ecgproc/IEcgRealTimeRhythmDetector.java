package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;

public interface IEcgRealTimeRhythmDetector {
    //------------------------------------------------------------内部接口
    // 心律检测结果回调接口
    interface IEcgRhythmDetectCallback {
        // 心律检测信息更新
        void onRhythmInfoUpdated(EcgRhythmDetectItem item);
    }

    // 获取版本号
    String getVer();

    // 获取提供者
    String getProvider();

    // 处理一个心电信号值，要求归一化为毫伏值
    void process(float ecgSignalmV);

    // 重置
    void reset();

    // 销毁
    void destroy();
}
