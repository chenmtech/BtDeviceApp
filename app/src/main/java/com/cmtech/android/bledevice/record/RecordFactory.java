package com.cmtech.android.bledevice.record;

import com.cmtech.android.bledeviceapp.model.User;
import com.vise.log.ViseLog;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
                Constructor constructor = recordClass.getDeclaredConstructor(long.class, String.class, User.class, String.class);
                constructor.setAccessible(true);
                return (IRecord) constructor.newInstance(createTime, devAddress, creator, note);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static IRecord create(int typeCode, long createTime, String devAddress, User creator, String note) {
        return create(RecordType.getType(typeCode), createTime, devAddress, creator, note);
    }

    public static IRecord createFromJson(RecordType type, JSONObject json) {
        if(json == null) {
            throw new NullPointerException("The json is null.");
        }

        Class<? extends IRecord> recordClass = getRecordClass(type);
        if(recordClass != null) {
            try {
                Method method = recordClass.getDeclaredMethod("createFromJson", JSONObject.class);
                method.setAccessible(true);
                return (IRecord) method.invoke(null, json);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static IRecord createFromJson(int typeCode, JSONObject json) {
        return createFromJson(RecordType.getType(typeCode), json);
    }

    public static List<IRecord> createFromLocalDb(RecordType type, User creator, long fromTime, int num) {
        if(creator == null) {
            throw new NullPointerException("The creator is null.");
        }

        Class<? extends IRecord> recordClass = getRecordClass(type);
        if(recordClass != null) {
            try {
                Method method = recordClass.getDeclaredMethod("createFromLocalDb", User.class, long.class, int.class);
                method.setAccessible(true);
                return (List<IRecord>) method.invoke(null, creator, fromTime, num);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                ViseLog.e(e.toString());
            }
        }
        return null;
    }

    public static List<IRecord> createFromLocalDb(int typeCode, User creator, long fromTime, int num) {
        return createFromLocalDb(RecordType.getType(typeCode), creator, fromTime, num);
    }
}
