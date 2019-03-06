package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
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
    public static EcgFileHead createDefaultEcgFileHead() {
        return new EcgFileHead(new User(), "", EcgLeadType.LEAD_I);
    }

    private static final int MACADDRESS_LEN = 12;           // mac地址字符数

    private static final byte[] ECGFILE_TAG = {'E', 'C', 'G'};          // 心电文件标识
    private static final byte[] VER = new byte[] {0x01, 0x01};         // 心电文件头版本号1.1，便于以后升级
    private User creator; // 创建人
    private String macAddress = "";                                     // 设备地址
    private EcgLeadType leadType = EcgLeadType.LEAD_I;                  // 导联类型

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public User getCreator() {
        return creator;
    }

    public String getCreatorName() {
        return creator.getUserName();
    }

    public EcgLeadType getLeadType() {
        return leadType;
    }

    public void setLeadType(EcgLeadType leadType) {
        this.leadType = leadType;
    }

    public EcgFileHead(User creator, String macAddress, EcgLeadType leadType) {
        this.creator = creator;
        this.macAddress = macAddress;
        this.leadType = leadType;
    }

    public boolean readFromStream(DataInput in) {
        try {
            // 读心电文件标识
            byte[] ecg = new byte[3];
            in.readFully(ecg);
            if (!Arrays.equals(ecg, ECGFILE_TAG)) {
                return false;
            }
            // 读版本号
            byte[] ver = new byte[2];
            in.readFully(ver);
            // 读创建人信息
            creator.readFromStream(in);
            // 读macAddress
            macAddress = DataIOUtil.readFixedString(MACADDRESS_LEN, in);
            // 读导联类型
            leadType = EcgLeadType.getFromCode(ByteUtil.reverseInt(in.readInt()));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean writeToStream(DataOutput out) {
        try {
            // 写心电文件标识
            out.write(ECGFILE_TAG);
            // 写版本号
            out.write(VER);
            // 写创建人信息
            creator.writeToStream(out);
            // 写macAddress
            DataIOUtil.writeFixedString(macAddress, MACADDRESS_LEN, out);
            // 写导联类型
            out.writeInt(ByteUtil.reverseInt(leadType.getCode()));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[心电文件头信息："
                + "版本号：" + Arrays.toString(VER) + ";"
                + creator.toString() + ";"
                + "设备地址：" + macAddress + ";"
                + "导联类型：" + leadType.getDescription() + "]";
    }

    // EcgFileHead字节长度：3个字节的{E,C,G} + 2个字节的版本号 + 创建人 + 创建人备注 + 创建设备MAC + 导联类型（4字节）
    public int length() {
        return 3 + 2 + creator.length() + MACADDRESS_LEN *2 + 4;
    }
}
