package com.cmtech.android.bledeviceapp.view;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.view
 * ClassName:      OnRollWaveViewListener
 * Description:    滚动波形视图监听器接口
 * Author:         作者名
 * CreateDate:     2020/10/24 下午2:43
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/24 下午2:43
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface OnRollWaveViewListener extends OnWaveViewListener{
    void onDataLocationUpdated(long location, int second); // 数据位置更新
}
