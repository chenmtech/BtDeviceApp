package com.cmtech.android.bledevice.record;

import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.model.Account;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    public static BasicRecord create(RecordType type, long createTime, String devAddress, Account creator) {
        Class<? extends BasicRecord> recordClass = type.getRecordClass();
        if(recordClass != null) {
            try {
                Constructor<? extends BasicRecord> constructor = recordClass.getDeclaredConstructor(long.class, String.class, Account.class);
                constructor.setAccessible(true);
                return constructor.newInstance(createTime, devAddress, creator);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
