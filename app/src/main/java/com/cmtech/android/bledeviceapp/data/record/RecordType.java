package com.cmtech.android.bledeviceapp.data.record;

import android.app.Activity;

import com.cmtech.android.bledevice.eeg.activityfragment.EegRecordActivity;
import com.cmtech.android.bledevice.hrm.activityfragment.EcgRecordActivity;
import com.cmtech.android.bledevice.hrm.activityfragment.HrRecordActivity;
import com.cmtech.android.bledevice.ppg.activityfragment.PpgRecordActivity;
import com.cmtech.android.bledevice.ptt.activityfragment.PttRecordActivity;
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
    ALL(0, R.string.all_type_record, R.mipmap.ic_all_type_record_24px, AllTypesRecord.class, null),
    ECG(1, R.string.ecg_record, R.mipmap.ic_ecg_24px, BleEcgRecord.class, EcgRecordActivity.class),
    HR(2, R.string.hr_record, R.mipmap.ic_hr_24px, BleHrRecord.class, HrRecordActivity.class),
    THERMO(3, R.string.thermo_record, R.mipmap.ic_thermo_24px, BleThermoRecord.class, ThermoRecordActivity.class),
    TH(4, R.string.th_record, R.drawable.ic_thm_default_icon, BleTempHumidRecord.class, null),
    EEG(5, R.string.eeg_record, R.mipmap.ic_eeg_24px, BleEegRecord.class, EegRecordActivity.class),
    PPG(6, R.string.ppg_record, R.mipmap.ic_ppg_24px, BlePpgRecord.class, PpgRecordActivity.class),
    PTT(7, R.string.ptt_record, R.mipmap.ic_unknown_device, BlePttRecord.class, PttRecordActivity.class);

    private final String name;
    private final int code;
    private final int iconId;
    private final Class<? extends BasicRecord> recordClass;
    private final Class<? extends Activity> actClass;

    RecordType(int code, int nameId, int imgId, Class<? extends BasicRecord> recordClass, Class<? extends Activity> actClass) {
        this.name = MyApplication.getStr(nameId);
        this.code = code;
        this.iconId = imgId;
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

    public int getIconId() {
        return iconId;
    }

    public Class<? extends BasicRecord> getRecordClass() {
        return recordClass;
    }

    public Class<? extends Activity> getActivityClass() {
        return actClass;
    }
}
