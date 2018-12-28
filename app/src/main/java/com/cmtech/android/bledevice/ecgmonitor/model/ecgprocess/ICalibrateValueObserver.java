package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

/**
 * ICalibrateValueObserver: 标定值观察者接口
 * Created by Chenm, 2018-12-27
 */

public interface ICalibrateValueObserver {
    void updateCalibrateValue(int calibrateValue); // 更新标定值
}
