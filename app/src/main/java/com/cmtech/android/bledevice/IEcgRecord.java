package com.cmtech.android.bledevice;

import java.io.IOException;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice
 * ClassName:      IEcgRecord
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/28 下午5:00
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/28 下午5:00
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface IEcgRecord {
    int getSampleRate();
    int getCaliValue();
    boolean isEOD();
    void seekData(int pos);
    int readData() throws IOException;
    int getDataNum();
    int getDataNumSaved();
}
