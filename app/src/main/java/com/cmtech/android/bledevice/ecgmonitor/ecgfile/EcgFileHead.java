package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import com.cmtech.android.bledeviceapp.util.DataIOUtil;
import com.cmtech.dsp.exception.FileException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class EcgFileHead {
    public static final int MAC_ADDRESS_LEN = 12;
    private String macAddress = "";
    private long timeInMillis;

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public EcgFileHead() {

    }

    public EcgFileHead(String macAddress, long timeInMillis) {
        this.macAddress = macAddress;
        this.timeInMillis = timeInMillis;
    }

    public void readFromStream(DataInput in) throws FileException {
        try {
            macAddress = DataIOUtil.readFixedString(MAC_ADDRESS_LEN, in);
            timeInMillis = in.readLong();
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException("", "读心电文件头错误");
        }
    }

    public void writeToStream(DataOutput out) throws FileException {
        try {
            DataIOUtil.writeFixedString(macAddress, MAC_ADDRESS_LEN, out);
            out.writeLong(timeInMillis);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException("", "写心电文件头错误");
        }
    }

    @Override
    public String toString() {
        return "[心电文件头信息："
                + "设备地址：" + macAddress + ";"
                + "创建时间：" + timeInMillis + "]";
    }
}
