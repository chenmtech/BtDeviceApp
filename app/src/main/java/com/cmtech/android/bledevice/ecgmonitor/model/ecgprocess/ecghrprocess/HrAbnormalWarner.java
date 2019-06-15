package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.OnHrAbnormalListener;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgProcessor.INVALID_HR;

/**
 * HrAbnormalWarner: 心率异常报警器类
 * Created by Chenm, 2018-12-07
 */

public class HrAbnormalWarner implements IHrOperator {
    private static final int DEFAULT_HR_BUFFLEN = 5; // 心率值缓存长度


    private int lowLimit; // 下限
    private int highLimit; // 上限
    private int[] buff; // 缓存
    private int index; // 缓存索引

    private List<OnHrAbnormalListener> listeners = new ArrayList<>(); // 心率异常监听器

    public HrAbnormalWarner(int lowLimit, int highLimit) {
        initialize(lowLimit, highLimit);
    }

    // 设置参数
    public void initialize(int lowLimit, int highLimit) {
        initialize(lowLimit, highLimit, DEFAULT_HR_BUFFLEN);
    }

    // 设置参数
    public void initialize(int lowLimit, int highLimit, int buffLen) {
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
    public synchronized void operate(short hr) {
        if(hr != INVALID_HR) {
            buff[index++] = hr;
            if(checkHrAbnormal()) {
                notifyAllListeners();
            }
            index = index % buff.length;
        }
    }

    public void addHrAbnormalListener(OnHrAbnormalListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private void notifyAllListeners() {
        for (OnHrAbnormalListener listener : listeners) {
            listener.onHrAbnormalNotified();
        }
    }

    @Override
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
