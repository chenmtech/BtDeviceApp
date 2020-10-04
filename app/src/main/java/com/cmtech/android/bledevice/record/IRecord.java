package com.cmtech.android.bledevice.record;

import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.interfac.IWebOperation;

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
public interface IRecord extends IJsonable, IWebOperation {
    int INVALID_ID = -1;

    int getId();
    String getVer();
    int getTypeCode();
    String getName();
    long getCreateTime();
    void setCreateTime(long createTime);
    String getDevAddress();
    String getCreatorPlat();
    String getCreatorId();
    String getCreatorName();
    String getNote();
    void setNote(String note);
    int getRecordSecond();
    boolean needUpload();
    void setNeedUpload(boolean needUpload);
    boolean noSignal();
}
