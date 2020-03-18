package com.cmtech.android.bledevice.hrmonitor.model;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      HrConfiguration
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/19 上午6:26
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/19 上午6:26
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HrConfiguration extends LitePalSupport implements Serializable {
    private final static long serialVersionUID = 1L;

    private int id; // id
    private String address = ""; // mac地址
    private boolean isWarn = true; // hr异常时是否报警
    private int hrLow = 50; // hr异常的下限
    private int hrHigh = 180; // hr异常的上限

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public boolean isWarn() {
        return isWarn;
    }
    public void setWarn(boolean warn) {
        this.isWarn = warn;
    }
    public int getHrLow() {
        return hrLow;
    }
    public void setHrLow(int hrLow) {
        this.hrLow = hrLow;
    }
    public int getHrHigh() {
        return hrHigh;
    }
    public void setHrHigh(int hrHigh) {
        this.hrHigh = hrHigh;
    }

    public void copyFrom(HrConfiguration config) {
        isWarn = config.isWarn;
        hrLow = config.hrLow;
        hrHigh = config.hrHigh;
    }
}
