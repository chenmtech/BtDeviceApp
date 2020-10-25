package com.cmtech.android.bledeviceapp.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
    private final int capacity;

    public FixSizeLinkedList(int capacity) {
        super();
        this.capacity = capacity;
    }

    @Override
    public boolean add(T e) {
        while (size() >= capacity) {
            super.removeFirst();
        }
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = super.addAll(c);

        while(size() > capacity) {
            super.removeFirst();
        }

        return result;
    }
}

