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

    // 处理一个心电信号值
    void process(short ecgSignal);

    // 对心电记录创建诊断报告
    EcgReport createReport(BleEcgRecord record);

    // 重置
    void reset();

    // 关闭
    void close();
}
