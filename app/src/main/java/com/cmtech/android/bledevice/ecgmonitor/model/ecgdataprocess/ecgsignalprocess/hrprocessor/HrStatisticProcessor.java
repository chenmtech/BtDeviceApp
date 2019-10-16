package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.EcgSignalProcessor.INVALID_HR;


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
    private final EcgHrStatisticsInfo hrStatisticInfoAnalyzer; // 心率统计信息分析仪
    private final List<Short> hrList = new ArrayList<>(); // 心率值list
    private boolean isRecord = true; // 是否记录心率

    public interface OnHrStatisticInfoUpdatedListener {
        void onHrStatisticInfoUpdated(EcgHrStatisticsInfo hrInfoObject); // 心率统计信息更新
    }

    public HrStatisticProcessor(int hrFilterTimeInSecond, OnHrStatisticInfoUpdatedListener listener) {
        hrStatisticInfoAnalyzer = new EcgHrStatisticsInfo(hrFilterTimeInSecond);
        this.listener = listener;
    }

    public List<Short> getHrList() {
        return hrList;
    }

    public EcgHrStatisticsInfo getHrStatisticInfoAnalyzer() {
        return hrStatisticInfoAnalyzer;
    }

    public void setRecord(boolean record) {
        isRecord = record;
    }

    // 更新心率统计信息
    public void updateHrStatisticInfo() {
        if(listener != null)
            listener.onHrStatisticInfoUpdated(hrStatisticInfoAnalyzer);
    }

    // 重置
    @Override
    public synchronized void reset() {
        hrList.clear();
        hrStatisticInfoAnalyzer.clear();
        updateHrStatisticInfo();
    }

    @Override
    public synchronized void process(short hr) {
        if(hr != INVALID_HR && isRecord) {
            hrList.add(hr);

            if(hrStatisticInfoAnalyzer.process(hr)) {
                updateHrStatisticInfo();
            }
        }
    }
}
