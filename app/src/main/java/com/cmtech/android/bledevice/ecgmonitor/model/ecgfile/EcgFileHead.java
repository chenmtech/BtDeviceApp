package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.DataIOUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

/**
 * EcgFileHead: 心电文件头
 * Created by bme on 2018/11/21.
 */

public class EcgFileHead {
    private static final int MACADDRESS_CHAR_NUM = 12; // 蓝牙设备mac地址字符数
    private static final byte[] TAG = {'E', 'C', 'G'}; // 心电文件标识
    private static final byte[] VER = new byte[] {0x01, 0x01}; // 心电文件头版本号1.1，便于以后升级
    private static final int LEAD_TYPE_BYTE_NUM = 1;

    private User creator = new User(); // 创建人

    private String macAddress = ""; // 蓝牙设备地址

    private EcgLeadType leadType = EcgLeadType.LEAD_I; // 导联类型

    EcgFileHead() {

    }

    EcgFileHead(User creator, String macAddress, EcgLeadType leadType) {
        this.creator = creator;
        this.macAddress = macAddress;
        this.leadType = leadType;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    User getCreator() {
        return creator;
    }

    public EcgLeadType getLeadType() {
        return leadType;
    }

    public void readFromStream(DataInput in) throws IOException{
        // 读心电文件标识
        byte[] ecg = new byte[3];
        in.readFully(ecg);

        if (!Arrays.equals(ecg, TAG)) {
            throw new IOException("The ecg file format is wrong.");
        }
        // 读版本号
        byte[] ver = new byte[2];
        in.readFully(ver);

        // 读创建人信息
        creator.readFromStream(in);

        // 读macAddress
        macAddress = DataIOUtil.readFixedString(in, MACADDRESS_CHAR_NUM);

        // 读导联类型
        leadType = EcgLeadType.getFromCode(in.readByte());
    }

    public void writeToStream(DataOutput out) throws IOException{
        // 写心电文件标识
        out.write(TAG);

        // 写版本号
        out.write(VER);

        // 写创建人信息
        creator.writeToStream(out);

        // 写macAddress
        DataIOUtil.writeFixedString(out, macAddress, MACADDRESS_CHAR_NUM);

        // 写导联类型
        out.writeByte(leadType.getCode());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ':'
                + "版本号：" + Arrays.toString(VER) + ';'
                + creator.toString() + ';'
                + "设备地址：" + macAddress + ';'
                + "导联类型：" + leadType.getDescription();
    }

    public int length() {
        return TAG.length + VER.length + creator.length() + MACADDRESS_CHAR_NUM *2 + LEAD_TYPE_BYTE_NUM;
    }
}
