package com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess;

import java.util.ArrayList;
import java.util.List;

/**
 * EcgHrWarner: 心率报警器类
 * Created by Chenm, 2018-12-07
 */

public class EcgHrWarner implements IEcgHrProcessor {
    private static final int DEFAULT_HR_BUFFLEN = 5;

    private int hrLowLimit;
    private int hrHighLimit;
    private int[] hrBuff;
    private int hrIndex;

    private List<IEcgHrAbnormalObserver> observers = new ArrayList<>();

    public EcgHrWarner(int lowLimit, int highLimit) {
        setHrWarn(lowLimit, highLimit);
    }

    // 设置参数
    public void setHrWarn(int lowLimit, int highLimit) {
        setHrWarn(lowLimit, highLimit, DEFAULT_HR_BUFFLEN);
    }

    // 设置参数
    public void setHrWarn(int lowLimit, int highLimit, int buffLen) {
        hrLowLimit = lowLimit;
        hrHighLimit = highLimit;
        int half = (lowLimit+highLimit)/2;
        hrBuff = new int[buffLen];
        for(int i = 0; i < buffLen; i++) {
            hrBuff[i] = half;
        }
        hrIndex = 0;
    }

    @Override
    public void process(int hr) {
        if(hr != INVALID_HR) {
            hrBuff[hrIndex++] = hr;
            if(checkHrAbnormal()) {
                notifyObserver();
            }
            hrIndex = hrIndex % hrBuff.length;
        }
    }

    public void registerObserver(IEcgHrAbnormalObserver observer) {
        if(!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void notifyObserver() {
        for (IEcgHrAbnormalObserver observer : observers) {
            observer.hrAbnormal();
        }
    }

    public void removeObserver(IEcgHrAbnormalObserver observer) {
        observers.remove(observer);
    }

    // 是否需要报警
    private boolean checkHrAbnormal() {
        boolean abnormal = true;
        for(int hr : hrBuff) {
            if(hr > hrLowLimit && hr < hrHighLimit) {
                abnormal = false;
                break;
            }
        }
        return abnormal;
    }
}
