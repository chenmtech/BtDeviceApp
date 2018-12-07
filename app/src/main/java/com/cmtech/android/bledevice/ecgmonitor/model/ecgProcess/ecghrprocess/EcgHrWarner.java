package com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess;

/**
 * EcgHrWarner: 心率报警器类
 * Created by Chenm, 2018-12-07
 */

public class EcgHrWarner implements IEcgHrProcessor {
    private static final int DEFAULT_HR_LOW_LIMIT = 50;
    private static final int DEFAULT_HR_HIGH_LIMIT = 100;
    private static final int DEFAULT_HR_BUFFLEN = 5;

    private boolean warn = false;
    public boolean isWarn() {
        return warn;
    }

    private int hrLowLimit;
    private int hrHighLimit;
    private int[] hrBuff;
    private int hrIndex;

    public EcgHrWarner() {
        this(DEFAULT_HR_LOW_LIMIT, DEFAULT_HR_HIGH_LIMIT, DEFAULT_HR_BUFFLEN);
    }

    public EcgHrWarner(int lowLimit, int highLimit, int buffLen) {
        setHrWarn(lowLimit, highLimit, buffLen);
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
        warn = false;
    }

    @Override
    public void process(int hr) {
        if(hr != 0) {
            hrBuff[hrIndex++] = hr;
            warn = checkHrWarn();
            hrIndex = hrIndex % hrBuff.length;
        }
    }

    // 是否需要报警
    private boolean checkHrWarn() {
        boolean warn = true;
        for(int hr : hrBuff) {
            if(hr > hrLowLimit && hr < hrHighLimit) {
                warn = false;
                break;
            }
        }
        return warn;
    }
}
