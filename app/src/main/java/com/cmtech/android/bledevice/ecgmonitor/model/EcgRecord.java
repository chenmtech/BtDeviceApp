package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFileHead;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.bmefile.BmeFileDataType;
import com.cmtech.bmefile.BmeFileHead30;
import com.cmtech.bmefile.DataIOUtil;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledeviceapp.BleDeviceConstant.DIR_CACHE;
import static com.cmtech.bmefile.BmeFileHead.INVALID_SAMPLE_RATE;

public class EcgRecord extends LitePalSupport {
    private int id;
    @Column(unique = true)
    private final String recordName;
    private long lastModifyTime;
    private final BmeFileHead30 bmeHead;
    private final EcgFileHead ecgHead;
    private String sigFileName = "";
    private int dataNum;
    @Column(ignore = true)
    private RandomAccessFile sigRaf;
    private List<Short> hrList;
    private List<EcgNormalComment> commentList;

    private EcgRecord(String recordName, BmeFileHead30 bmeHead, EcgFileHead ecgHead) throws IOException{
        this(recordName, bmeHead, ecgHead, new ArrayList<Short>(), new ArrayList<EcgNormalComment>());
    }

    private EcgRecord(String recordName, BmeFileHead30 bmeHead, EcgFileHead ecgHead, List<Short> hrList, List<EcgNormalComment> commentList) throws IOException{
        this.recordName = recordName;
        this.bmeHead = bmeHead;
        this.ecgHead = ecgHead;
        this.sigFileName = DIR_CACHE.getAbsolutePath() + File.separator + "sig_" + recordName + ".bme";
        createSigFile();
        this.hrList = hrList;
        this.commentList = commentList;
        this.lastModifyTime = new Date().getTime();
    }

    // 创建信号文件
    private void createSigFile() throws IOException{
        File sigFile = new File(sigFileName);
        if(sigFile.exists() && !sigFile.delete()) {
            throw new IOException();
        }
        if(!sigFile.createNewFile())
            throw new IOException();
    }

    // 创建记录
    public static EcgRecord create(User creator, int sampleRate, int value1mV, String macAddress, EcgLeadType leadType) {
        long time = new Date().getTime();
        // 创建bmeFileHead文件头
        BmeFileHead30 bmeFileHead = new BmeFileHead30("an ecg file", BmeFileDataType.INT32, sampleRate, value1mV, time);
        // 创建ecgFileHead文件头
        String address = EcgMonitorUtil.cutMacAddressColon(macAddress);
        EcgFileHead ecgFileHead = new EcgFileHead(creator, address, leadType);

        String recordName = EcgMonitorUtil.makeRecordName(macAddress, time);
        try {
            return new EcgRecord(recordName, bmeFileHead, ecgFileHead);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static EcgRecord create(EcgFile ecgFile) {
        String fileName = ecgFile.getFileName();
        String recordName = fileName.substring(fileName.lastIndexOf(File.separator) + 1, fileName.lastIndexOf("."));
        BmeFileHead30 bmeHead = (BmeFileHead30) ecgFile.getBmeFileHead();
        EcgFileHead ecgHead = ecgFile.getEcgFileHead();
        try {
            EcgRecord record = new EcgRecord(recordName, bmeHead, ecgHead, ecgFile.getHrList(), ecgFile.getCommentList());
            record.openSigFile();
            while (!ecgFile.isEOD()) {
                DataIOUtil.writeInt(record.sigRaf, ecgFile.readInt(), bmeHead.getByteOrder());
                record.dataNum++;
            }
            record.closeSigFile();
            return record;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 打开信号文件: 赋值sigRaf
    public void openSigFile() throws IOException{
        File sigFile = new File(sigFileName);
        if(!sigFile.exists()) throw new IOException();
        if(sigRaf != null) sigRaf.close();
        sigRaf = new RandomAccessFile(sigFile, "rw");
    }

    // 关闭信号文件
    public void closeSigFile() throws IOException {
        if(sigRaf != null) {
            sigRaf.close();
            sigRaf = null;
        }
    }

    // 将信号文件移动到directory
    public void moveSigFileTo(File directory) throws IOException{
        File sigFile = new File(sigFileName);
        if(sigFile.exists() && directory != null && directory.isDirectory()) {
            if(sigRaf != null)
                sigRaf.close();
            File toFile = FileUtil.getFile(directory, sigFile.getName());
            FileUtil.moveFile(sigFile, toFile);
            this.sigFileName = toFile.getAbsolutePath();
        }
    }

    public int getId() {
        return id;
    }
    public String getRecordName() {
        return recordName;
    }
    public long getLastModifyTime() {
        return lastModifyTime;
    }
    public String getSigFileName() {
        return sigFileName;
    }
    public int getDataNum() {
        return dataNum;
    }
    public BmeFileDataType getDataType() {
        return (bmeHead == null) ? null : bmeHead.getDataType();
    }
    public int getSampleRate() {
        return (bmeHead == null) ? INVALID_SAMPLE_RATE : bmeHead.getSampleRate();
    }
    public byte[] getVersion() {
        return (bmeHead == null) ? null : bmeHead.getVersion();
    }
    public ByteOrder getByteOrder() {
        return (bmeHead == null) ? null : bmeHead.getByteOrder();
    }
    public User getCreator() {
        return ecgHead.getCreator();
    }
    public String getCreatorName() {
        return getCreator().getName();
    }
    public long getCreateTime() {
        return bmeHead.getCreateTime();
    }
    public String getMacAddress() {
        return ecgHead.getMacAddress();
    }
    public int getCaliValue() {
        return bmeHead.getCaliValue();
    }
    public List<EcgNormalComment> getCommentList() {
        return commentList;
    }
    // 添加一条留言
    public void addComment(EcgNormalComment comment) {
        commentList.add(comment);
    }
    // 删除一条留言
    public void deleteComment(EcgNormalComment comment) {
        commentList.remove(comment);
    }
    public void setHrList(List<Short> hrList) {
        this.hrList = hrList;
    }
    public List<Short> getHrList() {
        return hrList;
    }
    // 读单个信号数据
    public int readData() throws IOException {
        return DataIOUtil.readInt(sigRaf, bmeHead.getByteOrder());
    }
    // 写单个信号数据
    public void writeData(int data) throws IOException{
        DataIOUtil.writeInt(sigRaf, data, bmeHead.getByteOrder());
        dataNum++;
    }
    // 写信号数组
    public void writeData(int[] data) throws IOException{
        for(int num : data) {
            writeData(num);
        }
    }
    // 将文件指针定位到某个数据位置
    public void seekData(int dataNum) {
        try {
            if(sigRaf != null && dataNum >= 0 && dataNum <= this.dataNum)
                sigRaf.seek(dataNum * getDataType().getByteNum());
        } catch (IOException e) {
            ViseLog.e("seekData " + dataNum + "is wrong.");
        }
    }
    // 是否已经到达数据尾部
    public boolean isEOD() {
        try {
            return (sigRaf == null || sigRaf.getFilePointer() == sigRaf.length());
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public String toString() {
        return DateTimeUtil.timeToShortStringWithTodayYesterday(lastModifyTime) + bmeHead + "-" + ecgHead + "-" + sigFileName + "-" + hrList + "-" +commentList;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        EcgRecord other = (EcgRecord) otherObject;
        return recordName.equals(other.recordName);
    }

    @Override
    public int hashCode() {
        return recordName.hashCode();
    }

    @Override
    public boolean save() {
        this.lastModifyTime = new Date().getTime();
        bmeHead.save();
        ecgHead.save();
        for(EcgNormalComment comment : commentList) {
            comment.save();
        }
        return super.save();
    }
}
