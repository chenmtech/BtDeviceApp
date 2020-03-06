package com.cmtech.android.bledevice.hrmonitor.model;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;

import java.util.ArrayList;
import java.util.Date;

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
    private final long time;
    private final byte flag;
    private final int bpm;
    private final int energy;
    private final ArrayList<Integer> RRIntervals = new ArrayList<>();

    public BleHeartRateData(byte[] data) {
        if(data == null || data.length < 2) {
            throw new IllegalArgumentException();
        }

        time = new Date().getTime();

        flag = data[0];

        int next = 1;

        if((flag & 0x01) == 0) {
            bpm = UnsignedUtil.getUnsignedByte(data[next]);
            next++;
        } else {
            bpm = UnsignedUtil.getUnsignedShort(ByteUtil.getShort(new byte[] {data[next], data[next+1]}));
            next += 2;
        }

        if((flag & 0x08) != 0) {
            energy = UnsignedUtil.getUnsignedShort(ByteUtil.getShort(new byte[] {data[next], data[next+1]}));
            next += 2;
        } else {
            energy = -1;
        }

        if((flag & 0x10) != 0) {
            int RRNum = (data.length - next)/2;
            for(int i = 0; i < RRNum; i++) {
                RRIntervals.add(UnsignedUtil.getUnsignedShort(ByteUtil.getShort(new byte[] {data[next], data[next+1]})));
                next += 2;
            }
        }
    }

    public int getBpm() {
        return bpm;
    }

    public int getEnergy() {
        return energy;
    }

    public long getTime() { return time;}

    @Override
    public String toString() {
        return "BleHeartRateData{" +
                "time=" + DateTimeUtil.timeToString(time) +
                "flag=" + flag +
                ", bpm=" + bpm +
                ", energy=" + energy +
                ", RRIntervals=" + RRIntervals +
                '}';
    }
}
