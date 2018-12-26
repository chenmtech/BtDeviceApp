package com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess;

/**
 * IEcgSignalObserver: 心电信号观察者接口
 * Created by Chenm, 2018-12-26
 */

public interface IEcgSignalObserver {
    void updateEcgSignal(int ecgSignal);
}
