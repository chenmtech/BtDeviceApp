package com.cmtech.android.bledevice.ecgmonitor.model.ecghrprocess;

import com.vise.log.ViseLog;

import java.util.Arrays;

public class EcgHrProcessor implements IEcgHrProcessor {
    private static final int DEFAULT_HR_LOW_LIMIT = 50;
    private static final int DEFAULT_HR_HIGH_LIMIT = 100;
    private static final int DEFAULT_HR_BUFFLEN = 5;

    private boolean warn;

    public boolean isWarn() {
        return warn;
    }

    private int hrLowLimit;
    private int hrHighLimit;

    // 心率从0~200以上，每隔10bpm为一个统计单位，高于200的都统计为200，因此统计直方图需要21个值
    private int[] hrHistgram = new int[21];

    public int[] getHrHistgram() {
        return hrHistgram;
    }

    public void setHrWarnThreshold(int low, int high) {
        hrLowLimit = low;
        hrHighLimit = high;
        int half = (low+high)/2;
        for(int i = 0; i < hrBuff.length; i++) {
            hrBuff[i] = half;
        }
        hrIndex = 0;
        warn = false;
    }

    private int[] hrBuff = new int[DEFAULT_HR_BUFFLEN];
    private int hrIndex = 0;

    public EcgHrProcessor() {
        setHrWarnThreshold(DEFAULT_HR_LOW_LIMIT, DEFAULT_HR_HIGH_LIMIT);
        for(int i = 0; i < hrHistgram.length; i++) {
            hrHistgram[i] = 0;
        }
    }

    @Override
    public void process(int hr) {
        if(hr != 0) {
            hrBuff[hrIndex++] = hr;
            int i = (hr >= 200) ? 20 : hr/10;
            hrHistgram[i]++;
            ViseLog.e("hrHistogram" + Arrays.toString(hrHistgram));
            warn = checkHrWarn();
            hrIndex = hrIndex % hrBuff.length;
        }
    }

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
