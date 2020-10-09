package com.cmtech.android.bledeviceapp.interfac;

import org.json.JSONObject;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.interfac
 * ClassName:      IJsonable
 * Description:    可以与JSON格式交换数据的接口
 * Author:         作者名
 * CreateDate:     2020/5/4 上午5:21
 * UpdateUser:     更新者
 * UpdateDate:     2020/5/4 上午5:21
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface IJsonable {
    void fromJson(JSONObject json);
    JSONObject toJson();
}
