package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DataIOUtil;
import com.cmtech.bmefile.exception.FileException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * EcgFileHead: 心电文件头
 * Created by bme on 2018/11/21.
 */

public class EcgFileHead10 {
    public static EcgFileHead10 createDefaultEcgFileHead() {
        return new EcgFileHead10("", "", EcgLeadType.LEAD_I);
    }

    private static final int MACADDRESS_LEN = 12;           // mac地址字符数
    private static final int CREATEDPERSON_LEN = 10;        // 创建人名字符数

    private static final byte[] ECG = {'E', 'C', 'G'};                 // 心电文件头标识
    private static final byte[] VER = new byte[] {0x00, 0x01};         // 心电文件头版本号1.0，便于以后升级
    private String createdPerson = "";                                  // 创建人
    private String macAddress = "";                                     // 设备地址
    private EcgLeadType leadType = EcgLeadType.LEAD_I;                  // 导联类型
    private List<EcgComment> commentList = new ArrayList<>();       // 留言列表

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getCreatedPerson() {
        return createdPerson;
    }

    public void setCreatedPerson(String createdPerson) {
        this.createdPerson = createdPerson;
    }

    public EcgLeadType getLeadType() {
        return leadType;
    }

    public void setLeadType(EcgLeadType leadType) {
        this.leadType = leadType;
    }

    public List<EcgComment> getCommentList() {
        return commentList;
    }

    public EcgFileHead10(String createdPerson, String macAddress, EcgLeadType leadType) {
        this.createdPerson = createdPerson;
        this.macAddress = macAddress;
        this.leadType = leadType;
    }

    public void readFromStream(DataInput in) throws FileException {
        try {
            // 读心电文件标识
            byte[] ecg = new byte[3];
            in.readFully(ecg);
            if(!Arrays.equals(ecg, ECG)) {
                throw new FileException("", "ECG文件格式不对");
            }
            // 读版本号
            byte[] ver = new byte[2];
            in.readFully(ver);
            // 读创建人
            createdPerson = DataIOUtil.readFixedString(CREATEDPERSON_LEN, in);
            // 读macAddress
            macAddress = DataIOUtil.readFixedString(MACADDRESS_LEN, in);
            // 读导联类型
            leadType = EcgLeadType.getFromCode(ByteUtil.reverseInt(in.readInt()));
            // 读留言数
            int commentNum = ByteUtil.reverseInt(in.readInt());
            // 读留言
            for(int i = 0; i < commentNum; i++) {
                EcgComment comment = new EcgComment();
                comment.readFromStream(in);
                commentList.add(comment);
            }
            // 按留言时间排序
            Collections.sort(commentList, new Comparator<EcgComment>() {
                @Override
                public int compare(EcgComment o1, EcgComment o2) {
                    return (int)(o1.getCreatedTime() - o2.getCreatedTime());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException("", "读心电文件头错误");
        }
    }

    public void writeToStream(DataOutput out) throws FileException {
        try {
            // 写心电文件标识
            out.write(ECG);
            // 写版本号
            out.write(VER);
            // 写创建人
            DataIOUtil.writeFixedString(createdPerson, CREATEDPERSON_LEN, out);
            // 写macAddress
            DataIOUtil.writeFixedString(macAddress, MACADDRESS_LEN, out);
            // 写导联类型
            out.writeInt(ByteUtil.reverseInt(leadType.getCode()));
            // 写留言数
            out.writeInt(ByteUtil.reverseInt(commentList.size()));
            // 写留言
            for(int i = 0; i < commentList.size(); i++) {
                commentList.get(i).writeToStream(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException("", "写心电文件头错误");
        }
    }

    @Override
    public String toString() {
        return "[心电文件头信息："
                + "版本号：" + Arrays.toString(VER) + ";"
                + "采集人：" + createdPerson + ";"
                + "设备地址：" + macAddress + ";"
                + "导联类型：" + leadType.getDescription() + ";"
                + "留言数：" + commentList.size() + ";"
                + "留言：" + Arrays.toString(commentList.toArray()) + "]";
    }

    // 添加留言
    public void addComment(EcgComment aComment) {
        commentList.add(aComment);
    }

    // 删除留言
    public void deleteComment(EcgComment aComment) {
        commentList.remove(aComment);
    }

    // 获取留言数
    public int getCommentsNum() {
        return commentList.size();
    }

    // EcgFileHead字节长度：3个字节的{E,C,G} + 2个字节的版本号 + 创建人 + 创建设备MAC + 导联类型（4字节）+ 所有Comments
    public int length() {
        return 3 + 2 + CREATEDPERSON_LEN*2 + MACADDRESS_LEN *2 + 4 + getCommentsLength();
    }

    // 所有评论占用的字节数，包括一个int的commentNum
    private int getCommentsLength() {
        return 4 + commentList.size()* EcgComment.length();
    }
}
