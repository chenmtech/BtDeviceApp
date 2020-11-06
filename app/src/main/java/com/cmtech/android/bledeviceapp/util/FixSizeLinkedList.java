package com.cmtech.android.bledeviceapp.util;

import java.util.Collection;
import java.util.LinkedList;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.util
 * ClassName:      FixSizeLinkedList
 * Description:    固定大小的List
 * Author:         chenm
 * CreateDate:     2020/10/25 上午8:25
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/25 上午8:25
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class FixSizeLinkedList<T> extends LinkedList<T> {
    private int capacity;

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

    public void setCapacity(int capacity) {
        clear();
        this.capacity = capacity;
    }
}

