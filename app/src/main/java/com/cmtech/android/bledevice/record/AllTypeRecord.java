package com.cmtech.android.bledevice.record;

import com.cmtech.android.bledeviceapp.model.Account;

import static com.cmtech.android.bledevice.record.RecordType.ALL;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.record
 * ClassName:      AllTypeRecord
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/10/8 上午5:27
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/8 上午5:27
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class AllTypeRecord extends BasicRecord {
    private AllTypeRecord(long createTime, String devAddress, Account creator) {
        super(ALL, createTime, devAddress, creator);
    }
}
