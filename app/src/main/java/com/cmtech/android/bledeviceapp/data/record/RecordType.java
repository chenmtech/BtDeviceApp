package com.cmtech.android.bledeviceapp.data.record;

import android.app.Activity;

import com.cmtech.android.bledevice.eeg.activityfragment.EegRecordActivity;
import com.cmtech.android.bledevice.hrm.activityfragment.EcgRecordActivity;
import com.cmtech.android.bledevice.hrm.activityfragment.HrRecordActivity;
import com.cmtech.android.bledevice.thermo.activityfragment.ThermoRecordActivity;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.common
 * ClassName:      RecordType
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/5/4 上午5:34
 * UpdateUser:     更新者
 * UpdateDate:     2020/5/4 上午5:34
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public enum RecordType {
    ALL(0, R.string.all_record, R.mipmap.ic_all_record, AllTypeRecord.class, null),
    ECG(1, R.string.ecg_record, R.mipmap.ic_ecg_24px, BleEcgRecord10.class, EcgRecordActivity.class),
    HR(2, R.string.hr_record, R.mipmap.ic_hr_24px, BleHrRecord10.class, HrRecordActivity.class),
    THERMO(3, R.string.thermo_record, R.mipmap.ic_thermo_24px, BleThermoRecord10.class, ThermoRecordActivity.class),
    TH(4, R.string.th_record, R.drawable.ic_thm_default_icon, BleTempHumidRecord10.class, null),
    EEG(5, R.string.eeg_record, R.mipmap.ic_eeg_24px, BleEegRecord10.class, EegRecordActivity.class);

    private String name;
    private int code;
    private int imgId;
    private Class<? extends BasicRecord> recordClass;
    private Class<? extends Activity> actClass;

    RecordType(int code, int nameId, int imgId, Class<? extends BasicRecord> recordClass, Class<? extends Activity> actClass) {
        this.name = MyApplication.getStr(nameId);
        this.code = code;
        this.imgId = imgId;
        this.recordClass = recordClass;
        this.actClass = actClass;
    }

    public static RecordType fromCode(int code) {
        for (RecordType type : RecordType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return ALL;
    }

    public static String getName(int code) {
        for (RecordType type : RecordType.values()) {
            if (type.getCode() == code) {
                return type.name;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public int getImgId() {
        return imgId;
    }

    public Class<? extends BasicRecord> getRecordClass() {
        return recordClass;
    }

    public Class<? extends Activity> getActivityClass() {
        return actClass;
    }
}
