package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import com.cmtech.android.bledeviceapp.util.DataIOUtil;
import com.cmtech.dsp.exception.FileException;
import com.vise.log.ViseLog;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EcgFileHead {
    public static final int MACADDRESS_CHAR_NUM = 12;
    private String macAddress = "";
    private long fileCreatedTime;

    private List<EcgFileComment> commentList = new ArrayList<>();



    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public long getFileCreatedTime() {
        return fileCreatedTime;
    }

    public void setFileCreatedTime(long fileCreatedTime) {
        this.fileCreatedTime = fileCreatedTime;
    }

    public EcgFileHead() {

    }

    public EcgFileHead(String macAddress, long fileCreatedTime) {
        this.macAddress = macAddress;
        this.fileCreatedTime = fileCreatedTime;
    }

    public void readFromStream(DataInput in) throws FileException {
        try {
            macAddress = DataIOUtil.readFixedString(MACADDRESS_CHAR_NUM, in);
            fileCreatedTime = in.readLong();
            int commentNum = in.readInt();
            for(int i = 0; i < commentNum; i++) {
                EcgFileComment comment = new EcgFileComment();
                comment.readFromStream(in);
                commentList.add(comment);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException("", "读心电文件头错误");
        }
    }

    public void writeToStream(DataOutput out) throws FileException {
        try {
            DataIOUtil.writeFixedString(macAddress, MACADDRESS_CHAR_NUM, out);
            out.writeLong(fileCreatedTime);
            out.writeInt(commentList.size());
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
                + "设备地址：" + macAddress + ";"
                + "创建时间：" + fileCreatedTime + ";"
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

    public int getCommentsNum() {
        return commentList.size();
    }

    public List<EcgFileComment> getCommentList() {
        return commentList;
    }

    public int getLength() {
        return MACADDRESS_CHAR_NUM *2 + 8 + getCommentsLength();
    }
}
