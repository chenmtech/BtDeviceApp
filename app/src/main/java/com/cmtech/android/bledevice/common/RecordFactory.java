package com.cmtech.android.bledevice.common;

import com.cmtech.android.bledevice.hrm.model.BleEcgRecord10;
import com.cmtech.android.bledevice.hrm.model.BleHrRecord10;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.common
 * ClassName:      RecordFactory
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/5/5 上午6:54
 * UpdateUser:     更新者
 * UpdateDate:     2020/5/5 上午6:54
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class RecordFactory {
    public static IRecord create(RecordType type, long createTime, String devAddress, Account creator) {
        switch (type) {
            case ECG:
                return new BleEcgRecord10(createTime, devAddress, creator);

            case HR:
                return new BleHrRecord10(createTime, devAddress, creator);

            default:
                return null;
        }
    }

    public static IRecord create(int typeCode, long createTime, String devAddress, Account creator) {
        return create(RecordType.getType(typeCode), createTime, devAddress, creator);
    }

    public static IRecord createFromJson(RecordType type, JSONObject json) {
        if(json == null) {
            throw new NullPointerException("The json is null.");
        }

        switch (type) {
            case ECG:
                return BleEcgRecord10.createFromJson(json);

            case HR:
                return BleHrRecord10.createFromJson(json);

            default:
                return null;
        }
    }

    public static IRecord createFromJson(int typeCode, JSONObject json) {
        return createFromJson(RecordType.getType(typeCode), json);
    }
}
