package com.cmtech.android.bledevice.ecg.record;

import com.cmtech.android.bledevice.IEcgRecord;
import com.cmtech.android.bledevice.ecg.enumeration.EcgLeadType;
import com.cmtech.android.bledevice.ecg.record.ecgcomment.EcgComment;
import com.cmtech.android.bledevice.ecg.record.ecgcomment.EcgCommentFactory;
import com.cmtech.android.bledevice.ecg.record.ecgcomment.EcgNormalComment;
import com.cmtech.android.bledevice.ecg.util.EcgMonitorUtil;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.bmefile.DataIOUtil;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledeviceapp.AppConstant.DIR_CACHE;

public class EcgRecord extends LitePalSupport implements IEcgRecord {
    private static final byte[] ECG = {'E', 'C', 'G'}; // 心电标识
    private static final int DEVICE_ADDRESS_CHAR_NUM = 12; // 设备地址字符数
    public static final int INVALID_SAMPLE_RATE = -1; // 无效采样率

    private int id;
    private String deviceAddress; // 设备地址
    private long createTime; // 创建时间
    private Account creator; // 创建人
    private long modifyTime; // 修改时间
    private int sampleRate; // 采样频率
    private int caliValue; // 标定值
    private int leadTypeCode; // 导联类型代码
    private int dataNum; // 信号数据个数
    private String sigFileName; // 信号文件名
    @Column(ignore = true)
    private RandomAccessFile sigRaf; // 信号文件RAF
    private List<Short> hrList; // 心率列表
    private List<EcgNormalComment> commentList; // 留言列表

    private boolean isModify;

    private EcgRecord() {
        deviceAddress = "";
        createTime = 0;
        creator = null;
        modifyTime = 0;
        sampleRate = INVALID_SAMPLE_RATE;
        caliValue = 1;
        leadTypeCode = EcgLeadType.LEAD_I.getCode();
        sigFileName = "";
        dataNum = 0;
        sigRaf = null;
        hrList = null;
        commentList = null;
        isModify = false;
    }

    // 创建新记录
    public static EcgRecord create(Account creator, int sampleRate, int caliValue, String deviceAddress, EcgLeadType leadType) {
        if(creator == null) {
            throw new NullPointerException("The creator is null.");
        }
        if(DIR_CACHE == null) {
            throw new NullPointerException("The cache dir is null");
        }
        EcgRecord record = new EcgRecord();
        record.deviceAddress = EcgMonitorUtil.deleteColon(deviceAddress);
        record.createTime = new Date().getTime();
        record.creator = (Account) creator.clone();
        record.modifyTime = record.createTime;
        record.sampleRate = sampleRate;
        record.caliValue = caliValue;
        record.leadTypeCode = leadType.getCode();
        record.dataNum = 0;
        record.hrList = new ArrayList<>();
        record.commentList = new ArrayList<>();
        record.sigFileName = DIR_CACHE.getAbsolutePath() + File.separator + "sig_" + record.getRecordName() + ".bme";
        if(record.createSigFile()) {
            return record;
        }
        return null;
    }

    // 从文件加载记录
    public static EcgRecord load(String fileName) {
        if(DIR_CACHE == null) {
            throw new NullPointerException("The cache dir is null");
        }
        File file = new File(fileName);
        RandomAccessFile raf = null;
        try {
            if(file.exists() && file.renameTo(file)) {
                EcgRecord record = new EcgRecord();
                raf = new RandomAccessFile(file, "r");
                // 读标识符
                byte[] ecg = new byte[3];
                raf.readFully(ecg);
                if(!Arrays.equals(ecg, ECG)) {
                    raf.close();
                    return null;
                }
                record.deviceAddress = DataIOUtil.readFixedString(raf, DEVICE_ADDRESS_CHAR_NUM);
                record.createTime = raf.readLong();
                record.creator = new Account();
                record.creator.readFromStream(raf);
                record.modifyTime = raf.readLong();
                record.sampleRate = raf.readInt();
                record.caliValue = raf.readInt();
                record.leadTypeCode = (int) raf.readByte();
                record.dataNum = raf.readInt();
                record.sigFileName = DIR_CACHE.getAbsolutePath() + File.separator + "sig_" + record.getRecordName() + ".bme";
                if(!record.createSigFile()) {
                    raf.close();
                    return null;
                }
                record.openSigFile();
                for(int i = 0 ; i < record.dataNum; i++) {
                    record.sigRaf.writeInt(raf.readInt());
                }
                record.closeSigFile();
                // 读心率信息
                int hrLen = raf.readInt();
                record.hrList = new ArrayList<>();
                for(int i = 0; i < hrLen; i++) {
                    record.hrList.add(raf.readShort());
                }
                // 写留言信息
                int commentLen = raf.readInt();
                record.commentList = new ArrayList<>();
                for(int i = 0; i < commentLen; i++) {
                    record.commentList.add((EcgNormalComment) EcgCommentFactory.readFromStream(raf));
                }
                raf.close();
                return record;
            } else {
                throw new IOException("The ecg record file can't be opened.");
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

    // 将记录保存为文件
    public boolean save(String fileName) {
        File file = new File(fileName);
        RandomAccessFile raf = null;
        try {
            if(file.exists()) file.delete();
            if(!file.createNewFile()) {
                throw new IOException();
            }
            raf = new RandomAccessFile(file, "rw");
            raf.write(ECG); // 写BmeFile文件标识符
            DataIOUtil.writeFixedString(raf, deviceAddress, DEVICE_ADDRESS_CHAR_NUM); // 写设备地址
            raf.writeLong(createTime); // 写创建时间
            creator.writeToStream(raf); // 写创建人信息
            raf.writeLong(modifyTime); // 写修改时间
            raf.writeInt(sampleRate); // 写采样频率
            raf.writeInt(caliValue); // 写定标值
            raf.writeByte(leadTypeCode); // 写导联类型
            raf.writeInt(dataNum); // 写数据个数
            openSigFile();
            for(int i = 0; i < dataNum; i++) {
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
    private boolean createSigFile() {
        File sigFile = new File(sigFileName);
        if(sigFile.exists() && !sigFile.delete()) {
            return false;
        }
        try {
            return sigFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
        return deviceAddress + createTime;
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
    // 获取记录的秒数
    public int getRecordSecond() {
        return dataNum /sampleRate;
    }
    public int getSampleRate() {
        return sampleRate;
    }
    public Account getCreator() {
        if(creator == null) {
            creator = LitePal.where("ecgrecord_id=?", String.valueOf(id)).findFirst(Account.class);
        }
        return creator;
    }
    public String getCreatorName() {
        return getCreator().getName();
    }
    public long getCreateTime() {
        return createTime;
    }
    public String getDeviceAddress() {
        return deviceAddress;
    }
    public int getCaliValue() {
        return caliValue;
    }
    public List<EcgNormalComment> getCommentList() {
        if(commentList == null)
            commentList = LitePal.where("ecgrecord_id = ?", String.valueOf(id)).find(EcgNormalComment.class);
        return commentList;
    }
    // 添加一条留言
    public void addComment(EcgNormalComment comment) {
        if(!commentList.contains(comment)) {
            if(commentList.add(comment))
                isModify = true;
        }
    }
    // 删除一条留言
    public void deleteComment(EcgNormalComment comment) {
        if(commentList.remove(comment)) {
            isModify = true;
        }
    }
    public void addHr(short hr) {
        hrList.add(hr);
        isModify = true;
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
        isModify = true;
    }
    // 写信号数组
    public void writeData(int[] data) throws IOException{
        for(int num : data) {
            writeData(num);
        }
    }
    // 将信号文件指针定位到某个数据位置
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
        return deviceAddress + "-" + createTime + "-" + creator + DateTimeUtil.timeToShortStringWithTodayYesterday(modifyTime) + "-" + sigFileName + "-" + dataNum + "-" + hrList + "-" +commentList;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        EcgRecord other = (EcgRecord) otherObject;
        return deviceAddress.equals(other.deviceAddress) && createTime == other.createTime;
    }

    @Override
    public int hashCode() {
        return deviceAddress.hashCode() + 31 * ((Long)createTime).hashCode();
    }

    @Override
    public boolean save() {
        if(isModify)
            this.modifyTime = new Date().getTime();
        if(creator != null)
            creator.save();
        if(commentList != null) {
            for (EcgNormalComment comment : commentList) {
                comment.save();
            }
        }
        return super.save();
    }

    @Override
    public int delete() {
        if(creator != null)
            creator.delete();

        if(commentList != null) {
            for (EcgNormalComment comment : commentList) {
                comment.delete();
            }
        }
        return super.delete();
    }
}
