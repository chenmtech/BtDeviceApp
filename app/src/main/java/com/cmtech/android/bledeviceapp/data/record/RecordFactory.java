package com.cmtech.android.bledeviceapp.data.record;

import com.cmtech.android.bledeviceapp.model.Account;

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
    public static BasicRecord create(RecordType type, String ver, long createTime, String devAddress, Account creator) {
        Class<? extends BasicRecord> recordClass = type.getRecordClass();
        if(recordClass != null) {
            try {
                Constructor<? extends BasicRecord> constructor = recordClass.getDeclaredConstructor(String.class, long.class, String.class, Account.class);
                constructor.setAccessible(true);
                return constructor.newInstance(ver, createTime, devAddress, creator);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
