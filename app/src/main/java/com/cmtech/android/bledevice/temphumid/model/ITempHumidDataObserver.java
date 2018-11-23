package com.cmtech.android.bledevice.temphumid.model;

public interface ITempHumidDataObserver {
    void updateCurrentTempHumidData();
    void updateHistoryTempHumidData();
    void updateWaveView(int xRes, float yRes, int gridWidth);
}
