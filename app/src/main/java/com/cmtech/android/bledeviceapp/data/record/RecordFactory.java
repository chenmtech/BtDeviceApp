package com.cmtech.android.bledeviceapp.data.record;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.common
 * ClassName:      RecordFactory
 * Description:    各种记录类的工厂
 * Author:         作者名
 * CreateDate:     2020/5/5 上午6:54
 * UpdateUser:     更新者
 * UpdateDate:     2020/5/5 上午6:54
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class RecordFactory {
    public static BasicRecord create(RecordType type, String ver, int creatorId, long createTime, String devAddress,
                                     int sampleRate, int channelNum, String gain, String unit) {
        Class<? extends BasicRecord> recordClass = type.getRecordClass();
        if(recordClass != null) {
            try {
                Constructor<? extends BasicRecord> constructor =
                        recordClass.getDeclaredConstructor(String.class, int.class, long.class, String.class,
                                int.class, int.class, String.class, String.class);
                constructor.setAccessible(true);
                return constructor.newInstance(ver, creatorId, createTime, devAddress,
                        sampleRate, channelNum, gain, unit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
