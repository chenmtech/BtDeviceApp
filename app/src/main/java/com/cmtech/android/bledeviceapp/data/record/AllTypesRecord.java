package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.ALL;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
 * ClassName:      AllTypeRecord
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/10/8 上午5:27
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/8 上午5:27
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class AllTypesRecord extends BasicRecord {
    private AllTypesRecord(String ver, int accountId, long createTime, String devAddress) {
        super(ALL, ver, accountId, createTime, devAddress);
    }
}
