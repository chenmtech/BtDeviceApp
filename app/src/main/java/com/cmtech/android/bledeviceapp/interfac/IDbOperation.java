package com.cmtech.android.bledeviceapp.interfac;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.interfac
 * ClassName:      IDbOperation
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/10/8 上午4:58
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/8 上午4:58
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public interface IDbOperation {
    boolean retrieve(); // 从数据库获取记录的信息
    boolean insert(); // 插入一条记录到数据库
    boolean deleteInDb(); // 删除一条记录
    boolean update(); // 更新数据库中的记录信息
}
