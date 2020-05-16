package com.cmtech.android.bledevice.record;

import com.cmtech.android.bledevice.hrm.view.EcgRecordActivity;
import com.cmtech.android.bledevice.hrm.view.HrRecordActivity;
import com.cmtech.android.bledevice.thermo.view.ThermoRecordActivity;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;

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
    UNKNOWN(0, R.string.unknown_record, 0, null),
    ECG(1, R.string.ecg_record, R.mipmap.ic_ecg_24px, EcgRecordActivity.class),
    HR(2, R.string.hr_record, R.mipmap.ic_hr_24px, HrRecordActivity.class),
    THERMO(3, R.string.thermo_record, R.mipmap.ic_thermo_24px, ThermoRecordActivity.class),
    TH(4, R.string.th_record, R.drawable.ic_thm_default_icon, null);

    private String name;
    private int code;
    private int imgId;
    private Class actClass;

    RecordType(int code, int nameId, int imgId, Class actClass) {
        this.name = MyApplication.getStr(nameId);
        this.code = code;
        this.imgId = imgId;
        this.actClass = actClass;
    }

    public static RecordType getType(int code) {
        for (RecordType type : RecordType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return UNKNOWN;
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

    public Class getActivityClass() {
        return actClass;
    }
}
