package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.EcgSignalProcessor.INVALID_HR;

/**
 * HrAbnormalWarner: 心率异常报警器类
 * Created by Chenm, 2018-12-07
 */

public class HrAbnormalWarner implements IHrProcessor {
    private static final int DEFAULT_HR_BUFFLEN = 5; // 心率值缓存长度

    private final EcgMonitorDevice device;
    private int lowLimit; // 下限
    private int highLimit; // 上限
    private int[] buff; // 缓存
    private int index; // 缓存索引

    public HrAbnormalWarner(EcgMonitorDevice device, int lowLimit, int highLimit) {
        this.device = device;

        reset(lowLimit, highLimit);
    }

    // 设置参数
    public void reset(int lowLimit, int highLimit) {
        reset(lowLimit, highLimit, DEFAULT_HR_BUFFLEN);
    }

    // 设置参数
    private void reset(int lowLimit, int highLimit, int buffLen) {
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
                device.notifyHrAbnormal();
            }
            index = index % buff.length;
        }
    }

    @Override
    public void close() {

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
