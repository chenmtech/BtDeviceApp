package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendixFactory;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgHrInfoAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.bmefile.AbstractRandomAccessBmeFile;
import com.cmtech.bmefile.BmeFileDataType;
import com.cmtech.bmefile.BmeFileHead;
import com.cmtech.bmefile.BmeFileHead30;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledeviceapp.BleDeviceConstant.DIR_CACHE;

/**
 * EcgFile: 心电文件类，可随机访问
 * Created by bme on 2018/11/20.
 */

public class EcgFile extends AbstractRandomAccessBmeFile {
    private final EcgFileHead ecgFileHead;
    private final EcgFileTail ecgFileTail;
    private final long dataBeginPointer; // 数据起始位置指针
    private long dataEndPointer;  // 数据结束的文件位置指针

    private static class EcgFileTail {
        static final int FILE_TAIL_LEN_BYTE_NUM = 8;
        EcgHrInfoAppendix hrInfoAppendix = EcgHrInfoAppendix.create(); // 心率信息
        List<EcgNormalComment> commentList = new ArrayList<>(); // 留言信息列表
        boolean needWriteHrInfo; // 是否需要写心率信息
        long hrInfoEndPointer; // 心率信息结束指针

        private EcgFileTail() {
        }

        /**
         * 从数据输入流读取
         * @param raf：数据输入流
         */
        static EcgFileTail readFromStream(RandomAccessFile raf) throws IOException{
            EcgFileTail fileTail = new EcgFileTail();

            raf.seek(raf.length() - FILE_TAIL_LEN_BYTE_NUM);
            long tailEndPointer = raf.getFilePointer();
            long tailLength = ByteUtil.reverseLong(raf.readLong());
            long appendixLength = tailLength - FILE_TAIL_LEN_BYTE_NUM;

            raf.seek(tailEndPointer - appendixLength);
            // 读心率信息
            IEcgAppendix appendix = EcgAppendixFactory.readFromStream(raf);
            if(!(appendix instanceof EcgHrInfoAppendix)) {
                throw new IOException();
            }
            fileTail.hrInfoAppendix = (EcgHrInfoAppendix)appendix;
            fileTail.hrInfoEndPointer = raf.getFilePointer();

            // 读留言信息
            while (raf.getFilePointer() < tailEndPointer) {
                appendix = EcgAppendixFactory.readFromStream(raf);
                if(appendix instanceof EcgNormalComment) {
                    fileTail.commentList.add((EcgNormalComment) appendix);
                }
            }
            return fileTail;
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
                EcgAppendixFactory.writeToStream(hrInfoAppendix, raf);
                hrInfoEndPointer = raf.getFilePointer();
                needWriteHrInfo = false;
            } else {
                raf.seek(hrInfoEndPointer);
            }

            // 写留言信息
            for(EcgNormalComment comment : commentList) {
                EcgAppendixFactory.writeToStream(comment, raf);
            }

            // 最后写入文件尾总长度
            raf.writeLong(ByteUtil.reverseLong(tailLength));
        }

        @Override
        public String toString() {
            return "EcgFileTail:"
                    + "心率数：" + hrInfoAppendix.getHrList().size() + ';'
                    + "心率信息：" + hrInfoAppendix + ';'
                    + "留言数：" + commentList.size() + ';'
                    + "留言：" + commentList + ';'
                    + "文件尾长度：" + length();
        }

        int length() {
            int length = hrInfoAppendix.length(); // 心率信息长度
            for(EcgNormalComment appendix : commentList) {
                length += appendix.length();
            }
            return length + FILE_TAIL_LEN_BYTE_NUM;
        }
    }

    /**
     * 打开已有EcgFile文件时使用的私有构造器
     */
    private EcgFile(String fileName) throws IOException {
        super(fileName);
        ecgFileHead = new EcgFileHead();
        ecgFileHead.readFromStream(raf);
        dataBeginPointer = raf.getFilePointer(); // 标记数据开始的位置指针
        ecgFileTail = EcgFileTail.readFromStream(raf);
        ecgFileTail.needWriteHrInfo = false;
        dataEndPointer = raf.length() - ecgFileTail.length();
        raf.seek(dataBeginPointer); // 回到数据开始位置
        dataNum = availableDataFromCurrentPos(); // 获取数据个数
    }

    // 创建新文件时使用的私有构造器
    private EcgFile(String fileName, BmeFileHead head, final EcgFileHead ecgFileHead) throws IOException {
        super(fileName, head);
        this.ecgFileHead = ecgFileHead;
        ecgFileHead.writeToStream(raf);
        dataBeginPointer = raf.getFilePointer(); // 标记数据开始的位置指针
        dataEndPointer = dataBeginPointer;
        dataNum = 0;
        ecgFileTail = new EcgFileTail();
        ecgFileTail.needWriteHrInfo = true;
    }

    // 打开已有文件
    public static EcgFile open(String fileName) throws IOException{
        return new EcgFile(fileName);
    }

    // 创建新文件
    public static EcgFile create(int sampleRate, int value1mV, String macAddress, EcgLeadType leadType) throws IOException{
        EcgFile ecgFile;
        long fileCreateTime = new Date().getTime();
        // 创建bmeFileHead文件头
        BmeFileHead30 bmeFileHead = new BmeFileHead30("an ecg file", BmeFileDataType.INT32, sampleRate, value1mV, fileCreateTime);
        // 创建ecgFileHead文件头
        String simpleMacAddress = EcgMonitorUtil.cutColonInMacAddress(macAddress);
        EcgFileHead ecgFileHead = new EcgFileHead(UserManager.getInstance().getUser(), simpleMacAddress, leadType);
        // 创建ecgFile
        String fileName = EcgMonitorUtil.makeFileName(macAddress, fileCreateTime);
        ecgFile = EcgFile.create(FileUtil.getFile(DIR_CACHE, fileName).getCanonicalPath(), bmeFileHead, ecgFileHead);
        return ecgFile;
    }

    private static EcgFile create(String fileName, BmeFileHead head, EcgFileHead ecgFileHead) throws IOException{
        return new EcgFile(fileName, head, ecgFileHead);
    }

    public List<EcgNormalComment> getCommentList() {
        return ecgFileTail.commentList;
    }
    public User getCreator() {
        return ecgFileHead.getCreator();
    }
    public String getCreatorName() {
        return getCreator().getName();
    }
    public long getCreatedTime() {
        return ((BmeFileHead30)getBmeFileHead()).getCreatedTime();
    }
    public List<Short> getHrList() {
        return ecgFileTail.hrInfoAppendix.getHrList();
    }
    public void setHrList(List<Short> hrList) {
        ecgFileTail.hrInfoAppendix.setHrList(hrList);
    }

    @Override
    protected int availableDataFromCurrentPos() {
        if(raf != null) {
            try {
                return (int)((dataEndPointer - raf.getFilePointer())/fileHead.getDataType().getByteNum());
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    public synchronized void saveFileTail() throws IOException{
        long curPointer = raf.getFilePointer();
        dataEndPointer = updateDataEndPointer();
        ecgFileTail.writeToStream(raf, dataEndPointer);
        raf.seek(curPointer);
    }

    private long updateDataEndPointer() {
        return dataBeginPointer + dataNum * fileHead.getDataType().getByteNum();
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

    // 添加一条留言
    public void addComment(EcgNormalComment comment) {
        ecgFileTail.commentList.add(comment);
    }

    // 多条留言
    public void addComment(List<EcgNormalComment> comments) {
        ecgFileTail.commentList.addAll(comments);
    }

    // 删除一条留言
    public void deleteComment(EcgNormalComment comment) {
        ecgFileTail.commentList.remove(comment);
    }

    @Override
    public String toString() {
        return super.toString() + ";" + ecgFileHead + ";" + ecgFileTail;
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
