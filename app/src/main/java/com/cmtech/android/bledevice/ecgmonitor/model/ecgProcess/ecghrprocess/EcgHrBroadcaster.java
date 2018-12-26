package com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess;

import java.util.ArrayList;
import java.util.List;

/**
 * EcgHrBroadcaster: 心率广播器，实现心率值的广播功能
 * Created by Chenm, 2018-12-24
 */

public class EcgHrBroadcaster implements IEcgHrProcessor {
    private List<IEcgHrObserver> observers = new ArrayList<>();

    public void registerObserver(IEcgHrObserver observer) {
        if(!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void process(int hr) {
        if(hr != INVALID_HR) {
            for (IEcgHrObserver observer : observers) {
                observer.update(hr);
            }
        }
    }

    public void removeObserver(IEcgHrObserver observer) {
        observers.remove(observer);
    }
}
