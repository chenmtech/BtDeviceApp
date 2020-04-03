package com.cmtech.android.bledevice.thermo.model;

public interface OnThermoDeviceListener {
    void onTempUpdated(float temp); // temperature updated
    void onHighestTempUpdated(float highestTemp); // highest temp updated
    void onTempTypeUpdated(byte type); // temperature type updated
    void onMeasIntervalUpdated(int interval); // measurement interval updated
    void onRecordStatusUpdated(boolean isRecord);
}
