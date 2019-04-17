package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgSignalProcessor.INVALID_HR;

/**
 * EcgHrProcessor: 心率记录仪，记录每次心率值，并实现心率的统计分析，发布心率信息
 *
 * Created by Chenm, 2018-12-07
 */

public class EcgHrProcessor implements IEcgHrProcessor {

    public interface OnEcgHrInfoUpdateListener {
        void onEcgHrInfoUpdated(EcgHrInfoObject hrInfoObject); // 心率信息更新
    }

    private OnEcgHrInfoUpdateListener listener;

    private final EcgHrInfoObject hrInfoObject;

    private List<Short> hrList = new ArrayList<>();

    private boolean isRecord = true;

    public EcgHrProcessor(int secondInHrFilter, OnEcgHrInfoUpdateListener listener) {
        hrInfoObject = new EcgHrInfoObject(secondInHrFilter);

        this.listener = listener;
    }

    public List<Short> getHrList() {
        return hrList;
    }


    public EcgHrInfoObject getHrInfoObject() {
        return hrInfoObject;
    }

    public void setRecord(boolean record) {
        isRecord = record;
    }

    // 更新心率信息
    public void updateHrInfo() {
        if(listener != null)
            listener.onEcgHrInfoUpdated(hrInfoObject);
    }

    // 重置心率数据
    public synchronized void reset() {
        hrList.clear();

        hrInfoObject.clear();

        updateHrInfo();
    }

    @Override
    public synchronized void process(short hr) {
        if(hr != INVALID_HR && isRecord) {
            hrList.add(hr);

            if(hrInfoObject.process(hr)) {
                updateHrInfo();
            }
        }
    }

    public void close() {
        isRecord = false;
        listener = null;
    }
}
