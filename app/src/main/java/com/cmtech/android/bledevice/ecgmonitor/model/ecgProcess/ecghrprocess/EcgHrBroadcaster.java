package com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess;

import java.util.ArrayList;
import java.util.List;

/**
 * EcgHrBroadcaster: 心率广播器，实现基本的心率处理功能，其他心率处理功能用装饰器来装饰它
 * Created by Chenm, 2018-12-24
 */

public class EcgHrBroadcaster implements IEcgHrProcessor {

    // 无效心率值常量
    private final static int INVALID_HR = 0;

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
