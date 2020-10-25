package com.cmtech.android.bledeviceapp.util;

import java.util.LinkedList;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.util
 * ClassName:      FixSizeLinkedList
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/10/25 上午8:25
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/25 上午8:25
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class FixSizeLinkedList<T> extends LinkedList<T> {

    // 定义缓存的容量
    private final int capacity;

    public FixSizeLinkedList(int capacity) {
        super();
        this.capacity = capacity;
    }

    @Override
    public boolean add(T e) {
        // 超过长度，移除最后一个
        if (size() >= capacity) {
            super.removeFirst();
        }
        return super.add(e);
    }
}

