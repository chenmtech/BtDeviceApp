package com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess;

/**
 * EcgHrWarner: 心率报警器类
 * Created by Chenm, 2018-12-07
 */

public class EcgHrWarner implements IEcgHrProcessor {
    private static final int DEFAULT_HR_BUFFLEN = 5;

    private boolean abnormal = false;
    public boolean isAbnormal() {
        return abnormal;
    }

    private int hrLowLimit;
    private int hrHighLimit;
    private int[] hrBuff;
    private int hrIndex;

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
        abnormal = false;
    }

    @Override
    public void process(int hr) {
        if(hr != INVALID_HR) {
            hrBuff[hrIndex++] = hr;
            abnormal = checkHrAbnormal();
            hrIndex = hrIndex % hrBuff.length;
        }
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
