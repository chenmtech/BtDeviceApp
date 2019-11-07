package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgRecord;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.EcgCommentFactory;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.IEcgComment;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.bmefile.AbstractRandomAccessBmeFile;
import com.cmtech.bmefile.DataIOUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EcgFile: 心电文件类
 * Created by bme on 2018/11/20.
 */

public class EcgFile extends AbstractRandomAccessBmeFile {
    private final long modifyTime;
    private final EcgFileHead ecgHead; // ECG文件头
    private final List<Short> hrList; // 心率值列表
    private final List<EcgNormalComment> commentList; // 一般留言列表

    // 为已存在Ecg文件构造EcgFile
    private EcgFile(String fileName) throws IOException {
        super(fileName);
        modifyTime = ByteUtil.reverseLong(raf.readLong());
        ecgHead = new EcgFileHead();
        ecgHead.readFromStream(raf);
        setDataNum(ByteUtil.reverseInt(raf.readInt()));
        long dataBeginPointer = raf.getFilePointer(); // 标记数据开始的位置指针
        raf.seek(dataBeginPointer + getDataNum() * head.getDataType().getByteNum()); // 跳过心电数据
        hrList = new ArrayList<>();
        int hrLength = ByteUtil.reverseInt(raf.readInt());
        for(int i = 0; i < hrLength; i++) {
            hrList.add(ByteUtil.reverseShort(raf.readShort()));
        }
        commentList = new ArrayList<>();
        int commentSize = ByteUtil.reverseInt(raf.readInt());
        for(int i = 0; i < commentSize; i++) {
            IEcgComment comment = EcgCommentFactory.readFromStream(raf);
            if(comment instanceof EcgNormalComment) {
                commentList.add((EcgNormalComment) comment);
            }
        }
        raf.seek(dataBeginPointer); // 回到数据开始位置
    }


    // 用Ecg记录构造EcgFile
    private EcgFile(String fileName, EcgRecord record) throws IOException {
        super(fileName, record.getBmeHead());
        this.modifyTime = record.getModifyTime();
        raf.writeLong(ByteUtil.reverseLong(modifyTime));
        this.ecgHead = record.getEcgHead();
        ecgHead.writeToStream(raf);
        setDataNum(record.getDataNum());
        raf.writeInt(ByteUtil.reverseInt(getDataNum()));
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
        for(EcgComment comment : commentList) {
            EcgCommentFactory.writeToStream(comment, raf);
        }
    }

    // 打开已有文件
    public static EcgFile open(String fileName) {
        try {
            return new EcgFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 为Ecg记录创建文件
    public static String createEcgFile(File directory, EcgRecord record) {
        String fileName = directory.getAbsolutePath() + File.separator + record.getRecordName() + ".bme";
        try {
            File file = new File(fileName);
            if(file.exists()) file.delete();
            EcgFile ecgFile = new EcgFile(fileName, record);
            ecgFile.close();
            return ecgFile.getFileName();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getModifyTime() {
        return modifyTime;
    }
    public EcgFileHead getEcgHead() {
        return ecgHead;
    }
    public List<EcgNormalComment> getCommentList() {
        return commentList;
    }
    public List<Short> getHrList() {
        return hrList;
    }

    @Override
    protected int availableDataFromCurrentPos() {
        throw new IllegalStateException();
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
