package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgRecord;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.EcgCommentFactory;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.IEcgComment;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.bmefile.AbstractRandomAccessBmeFile;
import com.cmtech.bmefile.BmeFileHead30;
import com.cmtech.bmefile.DataIOUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EcgFile: 心电文件类，可随机访问
 * Created by bme on 2018/11/20.
 */

public class EcgFile extends AbstractRandomAccessBmeFile {
    private final EcgFileHead ecgHead;
    private final long dataBeginPointer; // 数据起始位置指针
    private List<Short> hrList = new ArrayList<>(); // 心率列表
    private List<EcgNormalComment> commentList = new ArrayList<>(); // 留言列表

    // 打开已有文件
    public static EcgFile open(String fileName) {
        try {
            return new EcgFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 打开已有EcgFile文件
    private EcgFile(String fileName) throws IOException {
        super(fileName);
        ecgHead = new EcgFileHead();
        ecgHead.readFromStream(raf);
        setDataNum(DataIOUtil.readInt(raf, head.getByteOrder()));
        dataBeginPointer = raf.getFilePointer(); // 标记数据开始的位置指针
        raf.seek(dataBeginPointer + getDataNum() * head.getDataType().getByteNum());
        int hrLength = ByteUtil.reverseInt(raf.readInt());
        for(int i = 0; i < hrLength; i++) {
            hrList.add(ByteUtil.reverseShort(raf.readShort()));
        }
        int commentSize = ByteUtil.reverseInt(raf.readInt());
        for(int i = 0; i < commentSize; i++) {
            IEcgComment comment = EcgCommentFactory.readFromStream(raf);
            if(comment instanceof EcgNormalComment) {
                commentList.add((EcgNormalComment) comment);
            }
        }
        raf.seek(dataBeginPointer); // 回到数据开始位置
    }

    public static EcgFile create(File directory, EcgRecord record) {
        String fileName = directory.getAbsolutePath() + File.separator + record.getRecordName() + ".bme";
        try {
            return new EcgFile(fileName, record);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 创建新文件时使用的私有构造器
    private EcgFile(String fileName, EcgRecord record) throws IOException {
        super(fileName, record.getBmeHead());
        this.ecgHead = record.getEcgHead();
        ecgHead.writeToStream(raf);
        setDataNum(record.getDataNumInSignal());
        DataIOUtil.writeInt(raf, getDataNum(), head.getByteOrder());
        dataBeginPointer = raf.getFilePointer(); // 标记数据开始的位置指针
        record.openSigFile();
        for(int i = 0; i < getDataNum(); i++) {
            DataIOUtil.writeInt(raf, record.readData(), head.getByteOrder());
        }
        record.closeSigFile();
        // 写心率信息
        hrList = record.getHrList();
        raf.writeInt(ByteUtil.reverseInt(hrList.size()));
        for(short hr : hrList) {
            raf.writeShort(ByteUtil.reverseShort(hr));
        }
        // 写留言信息
        commentList = record.getCommentList();
        raf.writeInt(ByteUtil.reverseInt(commentList.size()));
        for(EcgNormalComment comment : commentList) {
            EcgCommentFactory.writeToStream(comment, raf);
        }
    }

    public EcgFileHead getEcgHead() {
        return ecgHead;
    }
    public List<EcgNormalComment> getCommentList() {
        return commentList;
    }
    public User getCreator() {
        return ecgHead.getCreator();
    }
    public String getCreatorName() {
        return getCreator().getName();
    }
    public long getCreateTime() {
        return ((BmeFileHead30)getBmeFileHead()).getCreateTime();
    }
    public List<Short> getHrList() {
        return hrList;
    }
    public void setHrList(List<Short> hrList) {
        this.hrList = hrList;
    }
    public String getMacAddress() {
        return ecgHead.getMacAddress();
    }
    public int getCaliValue() {
        return ((BmeFileHead30)head).getCaliValue();
    }

    @Override
    protected int availableDataFromCurrentPos() {
        return 0;
    }

    @Override
    public String toString() {
        return super.toString() + "-" + ecgHead + "-"
                + "心率数：" + hrList.size() + ';'
                + "心率信息：" + hrList + ';'
                + "留言数：" + commentList.size() + ';'
                + "留言：" + commentList;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        EcgFile other = (EcgFile) otherObject;
        return getFileName().equals(other.getFileName());
    }

    @Override
    public int hashCode() {
        return getFileName().hashCode();
    }
}
