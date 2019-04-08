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

    public interface IEcgHrAbnormalListener {
        void onNotifyEcgHrAbnormal(); // 通知心率异常
    }

    private int lowLimit; // 下限
    private int highLimit; // 上限
    private int[] buff; // 缓存
    private int index; // 缓存索引

    private List<IEcgHrAbnormalListener> listeners = new ArrayList<>(); // 心率异常监听器

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
    public synchronized void process(short hr) {
        if(hr != INVALID_HR) {
            buff[index++] = hr;
            if(checkHrAbnormal()) {
                notifyAllListeners();
            }
            index = index % buff.length;
        }
    }

    public void addEcgHrAbnormalListener(IEcgHrAbnormalListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private void notifyAllListeners() {
        for (IEcgHrAbnormalListener listener : listeners) {
            listener.onNotifyEcgHrAbnormal();
        }
    }

    public void close() {
        listeners.clear();
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
