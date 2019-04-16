package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgSignalProcessor.INVALID_HR;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrInfoObject.HrHistogramElement;

/**
 * EcgHrRecorder: 心率记录仪，记录每次心率值，并实现心率的统计分析，发布心率信息
 *
 * Created by Chenm, 2018-12-07
 */

public class EcgHrRecorder implements IEcgHrProcessor {
    private static final int MIN_INTERVAL_IN_HISTOGRAM = 10;

    private EcgHrInfoObject hrInfoObject = new EcgHrInfoObject();

    // 心率信息更新监听器
    public interface OnEcgHrInfoUpdateListener {
        void onEcgHrInfoUpdated(EcgHrInfoObject hrInfoObject);
    }

    private OnEcgHrInfoUpdateListener listener;

    private List<Short> hrList;

    private boolean isRecord = true;

    public EcgHrRecorder(OnEcgHrInfoUpdateListener listener) {
        this(null, listener);
    }

    public EcgHrRecorder(List<Short> hrList, OnEcgHrInfoUpdateListener listener) {
        this.listener = listener;
        if(hrList != null)
            this.hrList = hrList;
        else
            this.hrList = new ArrayList<>();
    }

    public List<Short> getHrList() {
        return hrList;
    }

    public void setHrList(List<Short> hrList) {
        this.hrList = hrList;
    }

    public void setRecord(boolean record) {
        isRecord = record;
    }

    // 更新心率信息
    public void getHrInfo(int secondInHrFilter) {
        EcgHrInfoObject hrInfoObject = new EcgHrInfoObject(hrList, secondInHrFilter);
        if(listener != null)
            listener.onEcgHrInfoUpdated(hrInfoObject);
    }

    // 重置心率数据
    public synchronized void reset() {
        hrList.clear();
        if(listener != null)
            listener.onEcgHrInfoUpdated(new EcgHrInfoObject());

    }

    @Override
    public synchronized void process(short hr) {
        if(hr != INVALID_HR && isRecord) {
            hrList.add(hr);
        }
    }

    public void close() {
        isRecord = false;
        listener = null;
    }
}
