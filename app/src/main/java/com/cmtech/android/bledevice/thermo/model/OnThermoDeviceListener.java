package com.cmtech.android.bledevice.thermo.model;

public interface OnThermoDeviceListener {
    void onTemperatureUpdated(float temp); // temperature updated
    void onTemperatureTypeUpdated(byte type); // temperature type updated
    void onMeasIntervalUpdated(int interval); // measurement interval updated
}
