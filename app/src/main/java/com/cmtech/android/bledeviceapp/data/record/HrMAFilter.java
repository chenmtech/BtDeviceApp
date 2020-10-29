package com.cmtech.android.bledeviceapp.data.record;

import com.vise.log.ViseLog;

import static com.cmtech.android.bledevice.hrm.model.HrmDevice.INVALID_HEART_RATE;

// moving average filter
public class HrMAFilter {
    private final int timeSpan; // hr filter width, unit: second

    private int sumTmp = 0;
    private int numTmp = 0;
    private int periodTmp = 0;

    HrMAFilter(int timeSpan) {
        if(timeSpan <= 0) {
            throw new IllegalArgumentException("The filter's time span must be larger than zero.");
        }

        this.timeSpan = timeSpan;
    }

    public short process(short hr, int interval) {
        short filteredHr = INVALID_HEART_RATE;
        sumTmp += hr;
        numTmp++;
        periodTmp += interval;
        if(periodTmp >= timeSpan) {
            filteredHr = (short)(sumTmp / numTmp);
            periodTmp -= timeSpan;
            sumTmp = 0;
            numTmp = 0;
        }
        ViseLog.e(interval + " " + periodTmp + " " + filteredHr);
        return filteredHr;
    }
}
