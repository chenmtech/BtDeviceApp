package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DataIOUtil;
import com.cmtech.bmefile.exception.FileException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class EcgFileHead {
    public static final int MACADDRESS_CHAR_NUM = 12;
    public static final int CREATEDPERSON_LEN = 10;

    public static final byte[] ECG = {'E', 'C', 'G'};

    private String macAddress = "";
    private String createdPerson = "";

    private List<EcgFileComment> commentList = new ArrayList<>();

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

    public EcgFileHead() {

    }

    public EcgFileHead(String createdPerson, String macAddress) {
        this.createdPerson = createdPerson;
        this.macAddress = macAddress;
    }

    public void readFromStream(DataInput in) throws FileException {
        try {
            byte[] ecg = new byte[3];
            in.readFully(ecg);
            if(!Arrays.equals(ecg, ECG)) {
                throw new FileException("", "ECG文件格式不对");
            }

            createdPerson = DataIOUtil.readFixedString(CREATEDPERSON_LEN, in);
            macAddress = DataIOUtil.readFixedString(MACADDRESS_CHAR_NUM, in);
            int commentNum = ByteUtil.reverseInt(in.readInt());
            for(int i = 0; i < commentNum; i++) {
                EcgFileComment comment = new EcgFileComment();
                comment.readFromStream(in);
                commentList.add(comment);
            }
            commentList.sort(new Comparator<EcgFileComment>() {
                @Override
                public int compare(EcgFileComment o1, EcgFileComment o2) {
                    return (int)(o1.getCommentTime() - o2.getCommentTime());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException("", "读心电文件头错误");
        }
    }

    public void writeToStream(DataOutput out) throws FileException {
        try {
            out.write(ECG);
            DataIOUtil.writeFixedString(createdPerson, CREATEDPERSON_LEN, out);
            DataIOUtil.writeFixedString(macAddress, MACADDRESS_CHAR_NUM, out);
            out.writeInt(ByteUtil.reverseInt(commentList.size()));
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
                + "采集人：" + createdPerson + ";"
                + "设备地址：" + macAddress + ";"
                //+ "评论数：" + commentList.size() + "]";
                + "评论：" + Arrays.toString(commentList.toArray()) + "]";
    }

    // 所有评论占用的字节数，包括一个int的commentNum
    public int getCommentsLength() {
        return 4 + commentList.size()*EcgFileComment.getLength();
    }

    public void addComment(EcgFileComment aComment) {
        commentList.add(aComment);
    }

    public void deleteComment(EcgFileComment aComment) {
        commentList.remove(aComment);
    }

    public int getCommentsNum() {
        return commentList.size();
    }

    public List<EcgFileComment> getCommentList() {
        return commentList;
    }

    // 3个字节的{E,C,G} + 创建人 + 创建设备MAC + 所有Comments
    public int getLength() {
        return 3 + CREATEDPERSON_LEN*2 + MACADDRESS_CHAR_NUM *2 + getCommentsLength();
    }
}
