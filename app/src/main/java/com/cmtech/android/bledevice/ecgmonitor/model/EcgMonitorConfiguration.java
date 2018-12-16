package com.cmtech.android.bledevice.ecgmonitor.model;

import java.io.Serializable;

public class EcgMonitorConfiguration implements Serializable {
    private boolean warnWhenDisconnect = true;
    private boolean warnWhenHrException = true;
    private int hrLowLimit = 50;
    private int hrHighLimit = 100;


    public EcgMonitorConfiguration() {

    }



}
