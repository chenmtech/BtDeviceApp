package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.EcgSignalProcessor.INVALID_HR;

/**
 * HrAbnormalProcessor: 心率异常处理器类
 * Created by Chenm, 2018-12-07
 */

public class HrAbnormalProcessor implements IHrProcessor {
    private static final int DEFAULT_HR_BUFFLEN = 5; // 心率值缓存长度

    private final EcgMonitorDevice device;
    private int lowLimit; // 下限
    private int highLimit; // 上限
    private int[] buff; // 缓存
    private int index; // 缓存索引

    public HrAbnormalProcessor(EcgMonitorDevice device) {
        this.device = device;
        lowLimit = device.getConfig().getHrLowLimit();
        highLimit = device.getConfig().getHrHighLimit();
        reset(lowLimit, highLimit);
    }

    // 重置参数
    public void reset(int lowLimit, int highLimit) {
        this.lowLimit = lowLimit;
        this.highLimit = highLimit;
        int half = (lowLimit+highLimit)/2;
        buff = new int[DEFAULT_HR_BUFFLEN];
        for(int i = 0; i < DEFAULT_HR_BUFFLEN; i++) {
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
