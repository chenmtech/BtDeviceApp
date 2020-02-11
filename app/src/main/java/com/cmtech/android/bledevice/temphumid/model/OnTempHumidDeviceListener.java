package com.cmtech.android.bledevice.temphumid.model;

public interface OnTempHumidDeviceListener {
    void onTempHumidDataUpdated(BleTempHumidData tempHumidData); // Temp&Humid data updated
}
