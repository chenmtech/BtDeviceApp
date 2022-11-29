package com.cmtech.android.bledeviceapp.view;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.view
 * ClassName:      OnWaveViewListener
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2019-11-19 05:51
 * UpdateUser:     更新者
 * UpdateDate:     2019-11-19 05:51
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface OnWaveViewListener {
    /**
     * 更新显示状态
     * @param show 当前是否显示，true-显示，false-不显示
     */
    void onShowStateUpdated(boolean show);
}
