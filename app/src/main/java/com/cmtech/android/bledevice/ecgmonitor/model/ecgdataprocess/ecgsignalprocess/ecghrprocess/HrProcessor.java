package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.ecghrprocess;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.EcgSignalProcessor.INVALID_HR;


/**
  *
  * ClassName:      HrProcessor
  * Description:    心率值处理器，包括记录每次心率值，并实现心率的统计分析，发布心率信息
  * Author:         chenm
  * CreateDate:     2018-12-07 08:04
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-15 08:04
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class HrProcessor implements IHrOperator {
    private OnHrStatisticInfoListener listener; // 心率统计信息监听器
    private final EcgHrStatisticInfoAnalyzer hrStatisticInfoAnalyzer;
    private final List<Short> hrList;
    private boolean isRecord = true;

    public HrProcessor(List<Short> hrList, int hrFilterTimeInSecond, OnHrStatisticInfoListener listener) {
        if(hrList == null)
            this.hrList = new ArrayList<>();
        else
            this.hrList = hrList;
        hrStatisticInfoAnalyzer = new EcgHrStatisticInfoAnalyzer(this.hrList, hrFilterTimeInSecond);
        this.listener = listener;
    }

    public List<Short> getHrList() {
        return hrList;
    }

    public EcgHrStatisticInfoAnalyzer getHrStatisticInfoAnalyzer() {
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

    // 重置心率数据
    public synchronized void reset() {
        hrList.clear();

        hrStatisticInfoAnalyzer.clear();

        updateHrStatisticInfo();
    }

    @Override
    public synchronized void operate(short hr) {
        if(hr != INVALID_HR && isRecord) {
            hrList.add(hr);

            if(hrStatisticInfoAnalyzer.process(hr)) {
                updateHrStatisticInfo();
            }
        }
    }

    @Override
    public void close() {
        isRecord = false;
        listener = null;
    }
}
