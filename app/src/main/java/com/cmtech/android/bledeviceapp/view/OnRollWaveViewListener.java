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
    /**
     * 更新当前显示的数据点在整个数据集中的位置和经过的时间
     * @param loc 数据位置，用当前数据的样本序号表示
     * @param sec 经过的时间，单位秒
     */
    void onDataLocationUpdated(long loc, int sec);
}
