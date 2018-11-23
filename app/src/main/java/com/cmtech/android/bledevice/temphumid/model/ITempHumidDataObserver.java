package com.cmtech.android.bledevice.temphumid.model;

public interface ITempHumidDataObserver {
    // 更新当前温湿度
    void updateCurrentData();
    //  添加一个温湿度历史数据
    void addHistoryData(TempHumidData data);
}
