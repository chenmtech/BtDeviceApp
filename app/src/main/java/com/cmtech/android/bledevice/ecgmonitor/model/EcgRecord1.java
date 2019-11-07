package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.EcgCommentFactory;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.bmefile.DataIOUtil;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledeviceapp.BleDeviceConstant.DIR_CACHE;

public class EcgRecord1 extends LitePalSupport {
    private static final byte[] ECG = {'E', 'C', 'G'}; // 心电标识
    private static final int RECORD_NAME_LEN = 30;
    public static final int MACADDRESS_CHAR_NUM = 12; // 蓝牙设备mac地址字符数
    public static final int INVALID_SAMPLE_RATE = -1; // 无效采样率

    private int id;
    @Column(unique = true)
    private String recordName; // 记录名
    private long createTime = 0; // 创建时间
    private User creator; // 创建人
    private long modifyTime; // 修改时间
    private int sampleRate = INVALID_SAMPLE_RATE; // 采样频率
    private int caliValue = 1; // 定标值。作用是所有数据都需要与定标值相除，得到实际数据代表的物理信号大小
    private String macAddress = ""; // 蓝牙设备地址
    private int leadTypeCode = EcgLeadType.LEAD_I.getCode(); // 导联类型代码
    private String sigFileName = ""; // 信号文件名
    private int dataNum; // 信号中的数据个数
    @Column(ignore = true)
    private RandomAccessFile sigRaf; // 信号文件RAF
    private List<Short> hrList; // 心率值列表
    private List<EcgNormalComment> commentList; // 留言列表

    private EcgRecord1() {
        recordName = "";
        //bmeHead = null;
        //ecgHead = null;
        hrList = new ArrayList<>();
        commentList = new ArrayList<>();
    }

    // 创建新记录
    public static EcgRecord1 create(User creator, int sampleRate, int caliValue, String macAddress, EcgLeadType leadType) {
        EcgRecord1 record = new EcgRecord1();
        record.createTime = new Date().getTime();
        record.creator = (User) creator.clone();
        record.modifyTime = record.createTime;
        record.recordName = EcgMonitorUtil.makeRecordName(macAddress, record.createTime);
        record.sampleRate = sampleRate;
        record.caliValue = caliValue;
        record.macAddress = EcgMonitorUtil.cutColon(macAddress);
        record.leadTypeCode = leadType.getCode();
        record.dataNum = 0;
        record.hrList = new ArrayList<>();
        record.commentList = new ArrayList<>();
        record.sigFileName = DIR_CACHE.getAbsolutePath() + File.separator + "sig_" + record.recordName + ".bme";
        try {
            record.createSigFile();
            return record;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static EcgRecord1 load(String fileName) {
        File file = new File(fileName);
        RandomAccessFile raf = null;
        try {
            if(file.exists() && file.renameTo(file)) {
                EcgRecord1 record = new EcgRecord1();
                raf = new RandomAccessFile(file, "r");
                // 读标识符
                byte[] ecg = new byte[3];
                raf.readFully(ecg);
                if(!Arrays.equals(ecg, ECG))
                    return null;
                record.recordName = DataIOUtil.readFixedString(raf, RECORD_NAME_LEN);
                record.createTime = raf.readLong();
                record.creator = new User();
                record.creator.readFromStream(raf);
                record.modifyTime = raf.readLong();
                record.sampleRate = raf.readInt();
                record.caliValue = raf.readInt();
                record.macAddress = DataIOUtil.readFixedString(raf, MACADDRESS_CHAR_NUM);
                record.leadTypeCode = (int) raf.readByte();
                record.dataNum = raf.readInt();
                record.sigFileName = DIR_CACHE.getAbsolutePath() + File.separator + "sig_" + record.recordName + ".bme";
                record.createSigFile();
                record.openSigFile();
                for(int i = 0 ; i < record.dataNum; i++) {
                    record.sigRaf.writeInt(raf.readInt());
                }
                record.closeSigFile();
                // 读心率信息
                int hrLen = raf.readInt();
                for(int i = 0; i < hrLen; i++) {
                    record.hrList.add(raf.readShort());
                }
                // 写留言信息
                int commentLen = raf.readInt();
                for(int i = 0; i < commentLen; i++) {
                    record.commentList.add((EcgNormalComment) EcgCommentFactory.readFromStream(raf));
                }
                raf.close();
                return record;
            } else {
                throw new IOException("The file can't be opened.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if(raf != null)
                    raf.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    // 保存为EcgFile
    public boolean save(String fileName) {
        File file = new File(fileName);
        RandomAccessFile raf = null;
        try {
            if(file.exists()) file.delete();
            if(!file.createNewFile()) {
                throw new IOException();
            }
            raf = new RandomAccessFile(file, "w");
            raf.write(ECG); // 写BmeFile文件标识符
            DataIOUtil.writeFixedString(raf, recordName, RECORD_NAME_LEN); // 写记录名
            raf.writeLong(createTime); // 写创建时间
            creator.writeToStream(raf); // 写创建人信息
            raf.writeLong(modifyTime); // 写修改时间
            raf.writeInt(sampleRate); // 写采样频率
            raf.writeInt(caliValue); // 写定标值
            DataIOUtil.writeFixedString(raf, macAddress, MACADDRESS_CHAR_NUM); // 写设备地址
            raf.writeByte(leadTypeCode); // 写导联类型
            raf.writeInt(dataNum); // 写数据个数
            openSigFile();
            for(int i = 0; i < getDataNum(); i++) {
                raf.writeInt(readData());
            }
            closeSigFile();
            // 写心率信息
            raf.writeInt(hrList.size());
            for(short hr : hrList) {
                raf.writeShort(hr);
            }
            // 写留言信息
            raf.writeInt(commentList.size());
            for(EcgComment comment : commentList) {
                EcgCommentFactory.writeToStream(comment, raf);
            }
            raf.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if(raf != null)
                    raf.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if(file.exists()) file.delete();
            return false;
        }
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

    // 打开信号文件: 赋值sigRaf
    public void openSigFile() throws IOException{
        if(sigRaf != null) sigRaf.close();
        File sigFile = new File(sigFileName);
        if(!sigFile.exists()) throw new IOException();
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
            if(sigRaf != null) sigRaf.close();
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
    public long getModifyTime() {
        return modifyTime;
    }
    public String getSigFileName() {
        return sigFileName;
    }
    public int getDataNum() {
        return dataNum;
    }
    public int getSampleRate() {
        return sampleRate;
    }

    public User getCreator() {
        return creator;
    }
    public String getCreatorName() {
        return getCreator().getName();
    }
    public long getCreateTime() {
        return createTime;
    }
    public String getMacAddress() {
        return macAddress;
    }
    public int getCaliValue() {
        return caliValue;
    }
    public List<EcgNormalComment> getCommentList() {
        return commentList;
    }
    // 添加一条留言
    public void addComment(EcgNormalComment comment) {
        if(!commentList.contains(comment))
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
        return sigRaf.readInt();
    }
    // 写单个信号数据
    public void writeData(int data) throws IOException{
        sigRaf.writeInt(data);
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
                sigRaf.seek(dataNum * 4);
        } catch (IOException e) {
            ViseLog.e("seekData wrong.");
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
        return DateTimeUtil.timeToShortStringWithTodayYesterday(modifyTime) + "-" + sigFileName + "-" + dataNum + "-" + hrList + "-" +commentList;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        EcgRecord1 other = (EcgRecord1) otherObject;
        return recordName.equals(other.recordName);
    }

    @Override
    public int hashCode() {
        return recordName.hashCode();
    }

    @Override
    public boolean save() {
        this.modifyTime = new Date().getTime();
        for(EcgNormalComment comment : commentList) {
            comment.save();
        }
        return super.save();
    }

    @Override
    public int delete() {
        for(EcgNormalComment comment : commentList) {
            comment.delete();
        }
        return super.delete();
    }
}
