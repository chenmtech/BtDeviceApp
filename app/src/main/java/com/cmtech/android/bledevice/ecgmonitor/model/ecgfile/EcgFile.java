package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgRecord;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.EcgCommentFactory;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.IEcgComment;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.bmefile.AbstractRandomAccessBmeFile;
import com.cmtech.bmefile.BmeFileHead;
import com.cmtech.bmefile.BmeFileHead30;
import com.cmtech.bmefile.DataIOUtil;
import com.vise.log.ViseLog;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledeviceapp.BleDeviceConstant.DIR_CACHE;

/**
 * EcgFile: 心电文件类，可随机访问
 * Created by bme on 2018/11/20.
 */

public class EcgFile extends AbstractRandomAccessBmeFile {
    private final EcgFileHead ecgHead;
    private final EcgFileTail ecgTail;
    private final long dataBeginPointer; // 数据起始的文件位置指针
    private long dataEndPointer;  // 数据结束的文件位置指针

    // Ecg文件尾：包括心率和留言
    private static class EcgFileTail {
        static final int LENGTH_BYTE_NUM = 8;
        List<Short> hrList = new ArrayList<>(); // 心率列表
        List<EcgNormalComment> commentList = new ArrayList<>(); // 留言列表
        boolean needWriteHrInfo; // 是否需要写心率信息
        long hrEndPointer; // 心率信息结束指针

        private EcgFileTail() {
        }

        /**
         * 从数据输入流读取
         * @param raf：数据输入流
         */
        static EcgFileTail readFromStream(RandomAccessFile raf) throws IOException{
            EcgFileTail tail = new EcgFileTail();

            raf.seek(raf.length() - LENGTH_BYTE_NUM);
            long tailEndPointer = raf.getFilePointer();
            long tailLength = ByteUtil.reverseLong(raf.readLong());
            long appendixLength = tailLength - LENGTH_BYTE_NUM;

            raf.seek(tailEndPointer - appendixLength);
            // 读心率信息
            int hrLength = ByteUtil.reverseInt(raf.readInt());
            for(int i = 0; i < hrLength; i++) {
                tail.hrList.add(ByteUtil.reverseShort(raf.readShort()));
            }
            tail.hrEndPointer = raf.getFilePointer();

            // 读留言信息
            while (raf.getFilePointer() < tailEndPointer) {
                IEcgComment comment = EcgCommentFactory.readFromStream(raf);
                if(comment instanceof EcgNormalComment) {
                    tail.commentList.add((EcgNormalComment) comment);
                }
            }
            return tail;
        }

        /**
         * 写出到数据输出流指向的dataEndPointer位置
         * @param raf：数据输出流
         */
        void writeToStream(RandomAccessFile raf, long dataEndPointer) throws IOException{
            long tailLength = length();
            raf.setLength(dataEndPointer + tailLength);
            if(needWriteHrInfo) {
                raf.seek(dataEndPointer);
                // 写心率信息
                raf.writeInt(ByteUtil.reverseInt(hrList.size()));
                for(short hr : hrList) {
                    raf.writeShort(ByteUtil.reverseShort(hr));
                }
                hrEndPointer = raf.getFilePointer();
                needWriteHrInfo = false;
            } else {
                raf.seek(hrEndPointer);
            }
            // 写留言信息
            for(EcgNormalComment comment : commentList) {
                EcgCommentFactory.writeToStream(comment, raf);
            }
            // 最后写入文件尾总长度
            raf.writeLong(ByteUtil.reverseLong(tailLength));
        }

        @Override
        public String toString() {
            return "EcgFileTail:"
                    + "心率数：" + hrList.size() + ';'
                    + "心率信息：" + hrList + ';'
                    + "留言数：" + commentList.size() + ';'
                    + "留言：" + commentList + ';'
                    + "文件尾长度：" + length();
        }

        int length() {
            int length = 4 + hrList.size()*2; // 心率信息长度
            for(EcgNormalComment comment : commentList) {
                length += comment.length();
            }
            return length + LENGTH_BYTE_NUM;
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

    // 打开已有EcgFile文件
    private EcgFile(String fileName) throws IOException {
        super(fileName);
        ecgHead = new EcgFileHead();
        ecgHead.readFromStream(raf);
        dataBeginPointer = raf.getFilePointer(); // 标记数据开始的位置指针
        ecgTail = EcgFileTail.readFromStream(raf);
        ecgTail.needWriteHrInfo = false;
        dataEndPointer = raf.length() - ecgTail.length();
        raf.seek(dataBeginPointer); // 回到数据开始位置
        setDataNum(availableDataFromCurrentPos()); // 获取数据个数
    }

    // 创建新文件时使用的私有构造器
    private EcgFile(String fileName, BmeFileHead head, EcgFileHead ecgHead, String sigFileName, EcgFileTail ecgTail) throws IOException {
        super(fileName, head);
        this.ecgHead = ecgHead;
        ecgHead.writeToStream(raf);
        dataBeginPointer = raf.getFilePointer(); // 标记数据开始的位置指针
        File sigFile = new File(sigFileName);
        RandomAccessFile rafSig = new RandomAccessFile(sigFile, "rw");
        while(rafSig.getFilePointer() < rafSig.length()) {
            DataIOUtil.writeInt(raf, rafSig.readInt(), head.getByteOrder());
        }
        dataEndPointer = raf.getFilePointer();
        setDataNum(0);
        this.ecgTail = ecgTail;
        this.ecgTail.needWriteHrInfo = true;
    }

    public static EcgFile create(EcgRecord record) throws IOException {
        String fileName = DIR_CACHE.getAbsolutePath() + File.separator + record.getRecordName() + ".bme";
        EcgFileTail ecgFileTail = new EcgFileTail();
        ecgFileTail.needWriteHrInfo = true;
        ecgFileTail.hrList = record.getHrList();
        ecgFileTail.commentList = record.getCommentList();
        return new EcgFile(fileName, record.getBmeHead(), record.getEcgHead(), record.getSigFileName(), ecgFileTail);
    }

    public EcgFileHead getEcgHead() {
        return ecgHead;
    }
    public List<EcgNormalComment> getCommentList() {
        return ecgTail.commentList;
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
        return ecgTail.hrList;
    }
    public void setHrList(List<Short> hrList) {
        ecgTail.hrList = hrList;
    }
    public String getMacAddress() {
        return ecgHead.getMacAddress();
    }
    public int getCaliValue() {
        return ((BmeFileHead30)head).getCaliValue();
    }

    @Override
    protected int availableDataFromCurrentPos() {
        if(raf != null) {
            try {
                return (int)((dataEndPointer - raf.getFilePointer())/ head.getDataType().getByteNum());
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    private long updateDataEndPointer() {
        return dataBeginPointer + getDataNum() * head.getDataType().getByteNum();
    }

    // 是否已经到达数据尾部
    public boolean isEOD() {
        if(raf == null) return true;
        try {
            return (raf.getFilePointer() >= dataEndPointer);
        } catch (IOException e) {
            return true;
        }
    }

    // 将文件指针定位到某个数据位置
    public void seekData(int dataNum) {
        try {
            raf.seek(dataBeginPointer + dataNum * getDataType().getByteNum());
        } catch (IOException e) {
            ViseLog.e("seekData " + dataNum + "is wrong.");
        }
    }

    @Override
    public String toString() {
        return super.toString() + "-" + ecgHead + "-" + ecgTail;
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
