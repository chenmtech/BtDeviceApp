package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

/**
 * OnEcgProcessListener: 与心电处理相关的所有监听器接口
 * Created by chenm on 2019/4/8
 *
 */

/**
  *
  * ClassName:      OnEcgProcessListener
  * Description:    与心电处理相关的所有监听器接口
  * Author:         chenm
  * CreateDate:     2019-04-08 07:07
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-15 07:07
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public interface OnEcgProcessListener extends OnSignalValueUpdateListener, OnHrValueUpdateListener, OnHrAbnormalListener, OnHrStatisticInfoListener {
}
