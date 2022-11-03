package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;

public interface IEcgRealTimeRhythmDetector {
    //------------------------------------------------------------内部接口
    // 心律异常检测结果回调接口
    interface IEcgRhythmDetectCallback {
        // 心律异常信息更新
        void onRhythmInfoUpdated(EcgRhythmDetectItem item);
    }

    void process(short ecgSignal);
    EcgReport createReport(BleEcgRecord record);
    void reset();
    void close();
}
