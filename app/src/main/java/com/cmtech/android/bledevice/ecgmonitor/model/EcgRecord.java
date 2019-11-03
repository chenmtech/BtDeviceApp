package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgHrAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFileHead;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.bmefile.BmeFileDataType;
import com.cmtech.bmefile.BmeFileHead30;
import com.cmtech.bmefile.DataIOUtil;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.DIR_ECG_SIGNAL;
import static com.cmtech.bmefile.BmeFileHead.INVALID_SAMPLE_RATE;

public class EcgRecord extends LitePalSupport {
    private int id;
    @Column(unique = true)
    private final String recordName;
    private final BmeFileHead30 bmeHead;
    private final EcgFileHead ecgHead;
    private String sigFileName = "";
    @Column(ignore = true)
    private RandomAccessFile sigRaf;
    private List<Short> hrList;
    private final List<EcgNormalComment> commentList;

    private EcgRecord(BmeFileHead30 bmeHead, EcgFileHead ecgHead, String recordName) {
        this(bmeHead, ecgHead, recordName, new ArrayList<Short>(), new ArrayList<EcgNormalComment>());
    }

    private EcgRecord(BmeFileHead30 bmeHead, EcgFileHead ecgHead, String recordName, List<Short> hrList, List<EcgNormalComment> commentList) {
        this.bmeHead = bmeHead;
        this.ecgHead = ecgHead;
        this.recordName = recordName;
        this.sigFileName = DIR_ECG_SIGNAL.getAbsolutePath() + "sig_" + recordName + ".bme";
        this.hrList = hrList;
        this.commentList = commentList;
    }

    // 创建新文件
    public static EcgRecord create(User creator, int sampleRate, int value1mV, String macAddress, EcgLeadType leadType) {
        long time = new Date().getTime();
        // 创建bmeFileHead文件头
        BmeFileHead30 bmeFileHead = new BmeFileHead30("an ecg file", BmeFileDataType.INT32, sampleRate, value1mV, time);
        // 创建ecgFileHead文件头
        String address = EcgMonitorUtil.cutMacAddressColon(macAddress);
        EcgFileHead ecgFileHead = new EcgFileHead(creator, address, leadType);

        String recordName = EcgMonitorUtil.makeRecordName(macAddress, time);
        return new EcgRecord(bmeFileHead, ecgFileHead, recordName);
    }

    public static EcgRecord create(EcgFile ecgFile) {
        BmeFileHead30 bmeHead = (BmeFileHead30) ecgFile.getBmeFileHead();
        EcgFileHead ecgHead = ecgFile.getEcgFileHead();
        String fileName = ecgFile.getFileName();
        String recordName = fileName.substring(fileName.lastIndexOf(File.separator) + 1, fileName.lastIndexOf("."));
        EcgRecord record = new EcgRecord(bmeHead, ecgHead, recordName, ecgFile.getHrList(), ecgFile.getCommentList());
        try {
            record.openSigFile();
            record.readSignal(ecgFile);
            record.closeSigFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return record;
    }

    public void openSigFile() throws IOException{
        if(sigRaf == null) {
            sigRaf = createRandomAccessFile(sigFileName);
        } else {
            sigRaf.seek(0);
        }
    }

    public void readSignal(EcgFile ecgFile) throws IOException{
        ecgFile.seekData(0);
        while (!ecgFile.isEOD()) {
            DataIOUtil.writeInt(sigRaf, ecgFile.readInt(), bmeHead.getByteOrder());
        }
        ecgFile.seekData(0);
        sigRaf.seek(0);
    }

    public void closeSigFile() throws IOException {
        if(sigRaf != null) {
            sigRaf.close();
            sigRaf = null;
        }
    }

    private RandomAccessFile createRandomAccessFile(String fileName) {
        File file = new File(fileName);
        try {
            if(file.exists() || file.createNewFile()) {
                return new RandomAccessFile(file, "rw");
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
    // 读单个byte数据
    public byte readByte() throws IOException{
        return sigRaf.readByte();
    }
    // 读单个int数据
    public int readInt() throws IOException {
        return DataIOUtil.readInt(sigRaf, bmeHead.getByteOrder());
    }
    // 读单个double数据
    public double readDouble() throws IOException{
        return DataIOUtil.readDouble(sigRaf, bmeHead.getByteOrder());
    }
    // 写单个byte数据
    public void writeData(byte data) throws IOException{
        sigRaf.writeByte(data);
    }
    // 写单个int数据
    public void writeData(int data) throws IOException{
        DataIOUtil.writeInt(sigRaf, data, bmeHead.getByteOrder());
    }
    // 写单个double数据
    public void writeData(double data) throws IOException{
        DataIOUtil.writeDouble(sigRaf, data, bmeHead.getByteOrder());
    }
    // 写byte数组
    public void writeData(byte[] data) throws IOException{
        for(byte num : data) {
            writeData(num);
        }
    }
    // 写int数组
    public void writeData(int[] data) throws IOException{
        for(int num : data) {
            writeData(num);
        }
    }
    // 写double数组
    public void writeData(double[] data) throws IOException{
        for(double num : data) {
            writeData(num);
        }
    }
    // 将文件指针定位到某个数据位置
    public void seekData(int dataNum) {
        try {
            if(sigRaf != null)
                sigRaf.seek(dataNum * getDataType().getByteNum());
        } catch (IOException e) {
            ViseLog.e("seekData " + dataNum + "is wrong.");
        }
    }
    public int getDataNum() {
        if(sigRaf != null) {
            try {
                return (int)(sigRaf.getFilePointer() / getDataType().getByteNum());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
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
        return bmeHead + "-" + ecgHead + "-" + sigFileName + "-" + hrList + "-" +commentList;
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
}
