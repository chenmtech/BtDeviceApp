package com.cmtech.android.bledevice.interf;

import org.json.JSONObject;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.interf
 * ClassName:      IRecord
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/2 下午2:42
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/2 下午2:42
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface IRecord {
    public int getId();
    public long getCreateTime();
    public String getDevAddress();
    public String getRecordName() ;
    public String getCreatorPlat();
    public String getCreatorId();
    public String getCreatorName();
    public String getDesc();
    public int getRecordTypeCode();
    public JSONObject toJson();
    public boolean updateFromJson(JSONObject json);
}
