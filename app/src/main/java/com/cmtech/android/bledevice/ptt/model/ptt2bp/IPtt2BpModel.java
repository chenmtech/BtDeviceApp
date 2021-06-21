package com.cmtech.android.bledevice.ptt.model.ptt2bp;

import android.util.Pair;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ptt.model.ptt2bp
 * ClassName:      Ptt2Bp
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2021/6/22 上午6:41
 * UpdateUser:     更新者
 * UpdateDate:     2021/6/22 上午6:41
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
interface IPtt2BpModel {
    Pair<Double, Double> getBp(double ptt);
}
