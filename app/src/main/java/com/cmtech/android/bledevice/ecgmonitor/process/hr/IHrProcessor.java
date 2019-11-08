package com.cmtech.android.bledevice.ecgmonitor.process.hr;


/**
  *
  * ClassName:      IHrProcessor
  * Description:    心率值处理器接口
  * Author:         chenm
  * CreateDate:     2018-12-07 08:03
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-15 08:03
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public interface IHrProcessor {
    void process(short hr); // 心率值处理
    void reset(); // 重置
}
