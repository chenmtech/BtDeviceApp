package com.cmtech.android.bledevice.record;

import android.text.TextUtils;

import com.cmtech.android.bledeviceapp.model.Account;
import com.vise.log.ViseLog;

import org.json.JSONObject;
import org.litepal.LitePal;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.cmtech.android.bledevice.record.RecordType.ALL;
import static com.cmtech.android.bledeviceapp.global.AppConstant.SUPPORT_RECORD_TYPES;

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
    public static Class<? extends BasicRecord> getRecordClass(RecordType type) {
        switch (type) {
            case ALL:
                return AllTypeRecord.class;

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

    public static BasicRecord create(RecordType type, long createTime, String devAddress, Account creator) {
        Class<? extends BasicRecord> recordClass = getRecordClass(type);
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

    public static List<? extends BasicRecord> createRecordListFromLocalDb(RecordType type, Account creator, long fromTime, String noteFilterStr, int num) {
        List<RecordType> types = new ArrayList<>();
        if(type == RecordType.ALL) {
            for(RecordType t : RecordType.values()) {
                if(t != RecordType.ALL) {
                    types.add(t);
                }
            }
        }
        else
            types.add(type);

        List<BasicRecord> records = new ArrayList<>();
        for(RecordType t : types) {
            Class<? extends BasicRecord> recordClass = getRecordClass(t);
            if (recordClass != null) {
                try {
                    if(TextUtils.isEmpty(noteFilterStr)) {
                        records.addAll(LitePal.select(BasicRecord.QUERY_STR)
                                .where("creatorPlat = ? and creatorId = ? and createTime < ?", creator.getPlatName(), creator.getPlatId(), "" + fromTime)
                                .order("createTime desc").limit(num).find(recordClass, true));
                    } else {
                        records.addAll(LitePal.select(BasicRecord.QUERY_STR)
                                .where("creatorPlat = ? and creatorId = ? and createTime < ? and note like ?", creator.getPlatName(), creator.getPlatId(), "" + fromTime, "%"+noteFilterStr+"%")
                                .order("createTime desc").limit(num).find(recordClass, true));
                    }
                } catch (Exception e) {
                    ViseLog.e(e);
                }
            }
        }
        if(records.isEmpty()) return null;
        Collections.sort(records, new Comparator<BasicRecord>() {
            @Override
            public int compare(BasicRecord o1, BasicRecord o2) {
                int rlt = 0;
                if(o2.getCreateTime() > o1.getCreateTime()) rlt = 1;
                else if(o2.getCreateTime() < o1.getCreateTime()) rlt = -1;
                return rlt;
            }
        });
        return records.subList(0, Math.min(records.size(), num));
    }

    public static BasicRecord createFromLocalDb(RecordType type, long createTime, String devAddress) {
        Class<? extends BasicRecord> recordClass = getRecordClass(type);
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
