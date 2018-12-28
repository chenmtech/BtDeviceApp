package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

/**
 * IEcgHrValueObserver: 心率值观察者接口
 * Created by Chenm, 2018-12-26
 */

public interface IEcgHrValueObserver {
    void updateHrValue(int hr); // 更新心率值
}
