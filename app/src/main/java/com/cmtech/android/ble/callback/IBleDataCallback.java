package com.cmtech.android.ble.callback;

import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.exception.BleException;

/**
 *
 * ClassName:      IBleDataCallback
 * Description:    数据操作回调接口
 * Author:         chenm
 * CreateDate:     2019-09-19 07:02
 * UpdateUser:     chenm
 * UpdateDate:     2019-09-19 07:02
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public interface IBleDataCallback {
    void onSuccess(byte[] data, BleGattElement element); // 数据操作成功
    void onFailure(BleException exception); // 数据操作失败
}
