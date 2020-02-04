package com.cmtech.android.bledevice.hrmonitor.model;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;

import java.util.ArrayList;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      BleHeartRateData
 * Description:    BLE心率数据
 * Author:         作者名
 * CreateDate:     2020-02-05 07:12
 * UpdateUser:     更新者
 * UpdateDate:     2020-02-05 07:12
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleHeartRateData {
    private byte flag;
    private int bpm;
    private int energy;
    private ArrayList<Integer> rrIntervals;

    public BleHeartRateData(byte[] data) {
        if(data == null || data.length < 2) {
            throw new IllegalArgumentException();
        }

        flag = data[0];

        if((flag & 0x01) == 0) {
            bpm = UnsignedUtil.getUnsignedByte(data[1]);
        } else {
            if(data.length < 3) {
                throw new IllegalArgumentException();
            }
            bpm = UnsignedUtil.getUnsignedShort(ByteUtil.getShort(new byte[] {data[1], data[2]}));
        }
    }

    public int getBpm() {
        return bpm;
    }
}
