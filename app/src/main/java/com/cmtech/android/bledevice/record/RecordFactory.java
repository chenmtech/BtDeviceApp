package com.cmtech.android.bledevice.record;

import com.cmtech.android.bledeviceapp.model.User;

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
            case ECG:
                return BleEcgRecord10.class;

            case HR:
                return BleHrRecord10.class;

            case THERMO:
                return BleThermoRecord10.class;

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
                return (IRecord) constructor.newInstance(createTime, devAddress, creator, note);
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
                Constructor<? extends IRecord> constructor = recordClass.getDeclaredConstructor(JSONObject.class);
                constructor.setAccessible(true);
                return (IRecord) constructor.newInstance(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<IRecord> createFromLocalDb(RecordType type, User creator, long from, int num) {
        if(creator == null) {
            return null;
        }

        Class<? extends IRecord> recordClass = getRecordClass(type);
        if(recordClass != null) {
            try {
                String str = (String)recordClass.getField("INIT_STR").get(null);
                return (List<IRecord>) LitePal.select(str)
                        .where("creatorPlat = ? and creatorId = ? and createTime < ?", creator.getPlatName(), creator.getPlatId(), ""+from)
                        .order("createTime desc").limit(num).find(recordClass);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
