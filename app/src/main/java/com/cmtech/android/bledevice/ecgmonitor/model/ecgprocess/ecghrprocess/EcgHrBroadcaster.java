package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.IEcgHrProcessor.INVALID_HR;

/**
 * EcgHrBroadcaster: 心率广播器，实现心率值的广播功能
 * Created by Chenm, 2018-12-24
 */

public class EcgHrBroadcaster {
    private List<IEcgHrObserver> observers = new ArrayList<>();

    public void registerObserver(IEcgHrObserver observer) {
        if(!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void notifyObserver(int hr) {
        if(hr != INVALID_HR) {
            for (IEcgHrObserver observer : observers) {
                observer.updateHr(hr);
            }
        }
    }

    public void removeObserver(IEcgHrObserver observer) {
        observers.remove(observer);
    }
}
