package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgSignalProcessor.INVALID_HR;

/**
 * EcgHrAbnormalWarner: 心率异常报警器类
 * Created by Chenm, 2018-12-07
 */

public class EcgHrAbnormalWarner implements IEcgHrProcessor {
    private static final int DEFAULT_HR_BUFFLEN = 5; // 心率值缓存长度

    private int lowLimit; // 下限
    private int highLimit; // 上限
    private int[] buff; // 缓存
    private int index; // 缓存索引
    private List<IEcgHrAbnormalObserver> observers = new ArrayList<>(); // 心率异常观察者

    public EcgHrAbnormalWarner(int lowLimit, int highLimit) {
        setHrWarn(lowLimit, highLimit);
    }

    // 设置参数
    public void setHrWarn(int lowLimit, int highLimit) {
        setHrWarn(lowLimit, highLimit, DEFAULT_HR_BUFFLEN);
    }

    // 设置参数
    public void setHrWarn(int lowLimit, int highLimit, int buffLen) {
        this.lowLimit = lowLimit;
        this.highLimit = highLimit;
        int half = (lowLimit+highLimit)/2;
        buff = new int[buffLen];
        for(int i = 0; i < buffLen; i++) {
            buff[i] = half;
        }
        index = 0;
    }

    @Override
    public void process(int hr) {
        if(hr != INVALID_HR) {
            buff[index++] = hr;
            if(checkHrAbnormal()) {
                notifyObserver();
            }
            index = index % buff.length;
        }
    }

    public void registerObserver(IEcgHrAbnormalObserver observer) {
        if(!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void notifyObserver() {
        for (IEcgHrAbnormalObserver observer : observers) {
            observer.notifyHrAbnormal();
        }
    }

    public void removeObserver(IEcgHrAbnormalObserver observer) {
        observers.remove(observer);
    }

    public void removeAllObserver() {
        observers.clear();
    }

    // 检查心率是否异常
    private boolean checkHrAbnormal() {
        for(int hr : buff) {
            if(hr > lowLimit && hr < highLimit) {
                return false;
            }
        }
        return true;
    }
}
