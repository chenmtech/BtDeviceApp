package com.cmtech.android.bledevice.ecgmonitor.model;

/**
 * EcgMonitorState: EcgMonitor的状态类型
 * Created by bme on 2018/12/07.
 */

public enum EcgMonitorState {
    INIT(0x00, true, false),
    CALIBRATING(0x01, false, false),
    CALIBRATED(0x02, true, false),
    SAMPLEING(0x03, false, true);

    /**
     *
     * @param code
     * @param canStart: 是否可以开始采集信号
     * @param canStop：是否可以停止采集信号
     */
    EcgMonitorState(int code, boolean canStart, boolean canStop) {
        this.code = code;
        this.canStart = canStart;
        this.canStop = canStop;
    }

    private int code;
    private boolean canStart;
    private boolean canStop;

    public int getCode() {
        return code;
    }

    public boolean canStart() { return canStart; }

    public boolean canStop() { return canStop; }

    public static EcgMonitorState getFromCode(int code) {
        for(EcgMonitorState ele : EcgMonitorState.values()) {
            if(ele.code == code) {
                return ele;
            }
        }
        return null;
    }
}
