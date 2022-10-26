package com.cmtech.android.bledeviceapp.data.record;

import java.lang.reflect.Constructor;

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
    public static BasicRecord create(RecordType type, String ver, int accountId, long createTime, String devAddress) {
        Class<? extends BasicRecord> recordClass = type.getRecordClass();
        if(recordClass != null) {
            try {
                Constructor<? extends BasicRecord> constructor = recordClass.getDeclaredConstructor(String.class, int.class, long.class, String.class);
                constructor.setAccessible(true);
                return constructor.newInstance(ver, accountId, createTime, devAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
