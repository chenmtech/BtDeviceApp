package com.cmtech.android.bledevice.ptt.model;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * ProjectName:    BtDeviceApp
 * Package:
 * ClassName:      PttCfg
 * Description:    configuration of PTT device
 * Author:         chenm
 * CreateDate:     2021/6/19 上午6:26
 * UpdateUser:     chenm
 * UpdateDate:     2021/6/19 上午6:26
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class PttCfg extends LitePalSupport implements Serializable {
    private final static long serialVersionUID = 1L;

    private int id; // id
    private String address = ""; // mac address
    private int ptt0 = 289;
    private int sbp0 = 130;
    private int dbp0 = 80;

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

    public int getPtt0() {
        return ptt0;
    }

    public void setPtt0(int ptt0) {
        this.ptt0 = ptt0;
    }

    public int getSbp0() {
        return sbp0;
    }

    public void setSbp0(int sbp0) {
        this.sbp0 = sbp0;
    }

    public int getDbp0() {
        return dbp0;
    }

    public void setDbp0(int dbp0) {
        this.dbp0 = dbp0;
    }

    public void copyFrom(PttCfg config) {
        ptt0 = config.ptt0;
        sbp0 = config.sbp0;
        dbp0 = config.dbp0;
    }
}
