package com.cmtech.android.bledevice.ecgmonitor.process.hr;

import static com.cmtech.android.bledevice.ecgmonitor.process.signal.EcgSignalProcessor.INVALID_HR;


/**
  *
  * ClassName:      HrStatisticProcessor
  * Description:    心率值统计处理器，包括记录每次心率值，并实现心率的统计分析，发布心率信息
  * Author:         chenm
  * CreateDate:     2018-12-07 08:04
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-15 08:04
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class HrStatisticProcessor implements IHrProcessor {
    private final OnHrStatisticInfoUpdatedListener listener; // 心率统计信息监听器
    private final HrStatisticsInfo hrStatisticInfo; // 心率统计信息分析仪

    public interface OnHrStatisticInfoUpdatedListener {
        void onHrStatisticInfoUpdated(HrStatisticsInfo hrStatisticsInfo); // 心率统计信息更新
    }

    public HrStatisticProcessor(int hrFilterTime, OnHrStatisticInfoUpdatedListener listener) {
        hrStatisticInfo = new HrStatisticsInfo(hrFilterTime);
        this.listener = listener;
    }

    public HrStatisticsInfo getHrStatisticInfo() {
        return hrStatisticInfo;
    }

    // 更新心率统计信息
    public void updateHrStatisticInfo() {
        if(listener != null)
            listener.onHrStatisticInfoUpdated(hrStatisticInfo);
    }

    // 重置
    @Override
    public synchronized void reset() {
        hrStatisticInfo.clear();
        updateHrStatisticInfo();
    }

    @Override
    public synchronized void process(short hr) {
        if(hr != INVALID_HR) {
            if(hrStatisticInfo.process(hr)) {
                updateHrStatisticInfo();
            }
        }
    }
}
