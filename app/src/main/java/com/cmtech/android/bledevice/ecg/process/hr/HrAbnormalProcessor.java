package com.cmtech.android.bledevice.ecg.process.hr;

import com.cmtech.android.bledevice.ecg.interfac.IEcgDevice;

import static com.cmtech.android.bledevice.ecg.process.signal.EcgSignalProcessor.INVALID_HR;

/**
 * HrAbnormalProcessor: 心率异常处理器类
 * Created by Chenm, 2018-12-07
 */

public class HrAbnormalProcessor implements IHrProcessor {
    private static final int DEFAULT_HR_BUFFLEN = 5; // 心率值缓存长度

    private final IEcgDevice device;
    private int lowLimit; // 下限
    private int highLimit; // 上限
    private int[] buff; // 缓存
    private int index; // 缓存索引

    public HrAbnormalProcessor(IEcgDevice device) {
        this.device = device;
        reset();
    }

    // 重置
    @Override
    public void reset() {
        this.lowLimit = device.getConfig().getHrLowLimit();
        this.highLimit = device.getConfig().getHrHighLimit();
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
