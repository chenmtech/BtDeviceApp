package com.cmtech.android.bledevice.record;

import com.cmtech.android.bledeviceapp.model.User;
import com.vise.log.ViseLog;

import org.json.JSONObject;
import org.litepal.LitePal;

import java.lang.reflect.Constructor;
import java.util.List;

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
    public static Class<? extends IRecord> getRecordClass(RecordType type) {
        switch (type) {
            case ALL:
                return BasicRecord.class;

            case ECG:
                return BleEcgRecord10.class;

            case HR:
                return BleHrRecord10.class;

            case THERMO:
                return BleThermoRecord10.class;

            case EEG:
                return BleEegRecord10.class;

            case TH:
                return BleTempHumidRecord10.class;

            default:
                return null;
        }
    }

    public static IRecord create(RecordType type, long createTime, String devAddress, User creator, String note) {
        Class<? extends IRecord> recordClass = getRecordClass(type);
        if(recordClass != null) {
            try {
                Constructor<? extends IRecord> constructor = recordClass.getDeclaredConstructor(long.class, String.class, User.class, String.class);
                constructor.setAccessible(true);
                return constructor.newInstance(createTime, devAddress, creator, note);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static IRecord createFromJson(RecordType type, JSONObject json) {
        if(json == null) {
            return null;
        }

        Class<? extends IRecord> recordClass = getRecordClass(type);
        if(recordClass != null) {
            try {
                Constructor constructor = recordClass.getDeclaredConstructor(JSONObject.class);
                constructor.setAccessible(true);
                return recordClass.cast(constructor.newInstance(json));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<? extends IRecord> createBasicFromLocalDb(RecordType type, User creator, long from, int num) {
        if(creator == null) {
            return null;
        }

        Class<? extends IRecord> recordClass = getRecordClass(type);
        if(recordClass != null) {
            try {
                return LitePal.select(BasicRecord.QUERY_STR)
                        .where("creatorPlat = ? and creatorId = ? and createTime < ?", creator.getPlatName(), creator.getPlatId(), ""+from)
                        .order("createTime desc").limit(num).find(recordClass);
            } catch (Exception e) {
                ViseLog.e(e);
                return null;
            }
        }
        return null;
    }

    public static IRecord createFromLocalDb(RecordType type, long createTime, String devAddress) {
        Class<? extends IRecord> recordClass = getRecordClass(type);
        if(recordClass != null) {
            try {
                return LitePal.where("createTime = ? and devAddress = ?", ""+createTime, devAddress).findFirst(recordClass);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
