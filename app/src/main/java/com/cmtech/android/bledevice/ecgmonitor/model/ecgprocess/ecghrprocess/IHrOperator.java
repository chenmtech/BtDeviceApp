package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess;


/**
  *
  * ClassName:      IHrOperator
  * Description:    心率操作接口
  * Author:         chenm
  * CreateDate:     2018-12-07 08:03
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-15 08:03
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public interface IHrOperator {
    void operate(short hr); // 心率操作
    void close(); // 关闭
}
