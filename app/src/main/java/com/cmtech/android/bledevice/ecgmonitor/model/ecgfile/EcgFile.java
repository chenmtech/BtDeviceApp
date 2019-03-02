package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.IEcgAppendix;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.bmefile.BmeFileDataType;
import com.cmtech.bmefile.BmeFileHead;
import com.cmtech.bmefile.BmeFileHead30;
import com.cmtech.bmefile.RandomAccessBmeFile;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledevice.core.BleDeviceConstant.CACHEDIR;

/**
 * EcgFile: 心电文件类，可随机访问
 * Created by bme on 2018/11/20.
 */

public class EcgFile extends RandomAccessBmeFile {
    private EcgFileHead ecgFileHead = EcgFileHead.createDefaultEcgFileHead(); // EcgFileHead
    private EcgFileTail ecgFileTail = new EcgFileTail(); // Ecg文件尾
    private final long ecgFileHeadPointer; // EcgFileHead在文件中的位置指针
    private long dataEndPointer;  // 数据结束的文件位置指针

    // 打开文件时使用的私有构造器
    private EcgFile(String fileName) throws IOException {
        super(fileName);

        try {
            ecgFileHeadPointer = raf.getFilePointer(); // 标记EcgFileHead位置指针
            if (!ecgFileHead.readFromStream(raf)) { // 读EcgFileHead
                throw new IOException();
            }

            dataBeginPointer = raf.getFilePointer(); // 标记数据开始的位置指针

            if (!ecgFileTail.readFromStream(raf)) { // 读EcgFileTail
                throw new IOException();
            }

            dataEndPointer = raf.length() - ecgFileTail.length();

            raf.seek(dataBeginPointer); // 回到数据开始位置
            dataNum = availableData(); // 获取数据个数
        } catch (IOException e) {
            throw new IOException("打开文件错误");
        }
    }

    // 创建新文件时使用的私有构造器
    private EcgFile(String fileName, BmeFileHead head, EcgFileHead ecgFileHead) throws IOException {
        super(fileName, head);

        try {
            ecgFileHeadPointer = raf.getFilePointer(); // 标记EcgFileHead位置指针

            this.ecgFileHead = ecgFileHead;
            if (!ecgFileHead.writeToStream(raf)) { // 写EcgFileHead
                throw new IOException();
            }

            dataBeginPointer = raf.getFilePointer(); // 标记数据开始的位置指针

            dataEndPointer = dataBeginPointer;

            dataNum = 0;
        } catch (IOException e) {
            throw new IOException("创建文件错误");
        }
    }

    // 打开已有文件
    public static EcgFile open(String fileName) throws IOException{
        return new EcgFile(fileName);
    }

    // 用指定的文件头创建新的文件
    public static EcgFile create(String fileName, BmeFileHead head, EcgFileHead ecgFileHead) throws IOException{
        return new EcgFile(fileName, head, ecgFileHead);
    }

    // 创建Ecg文件
    public static EcgFile create(int sampleRate, int calibrationValue, String macAddress, EcgLeadType leadType) {
        EcgFile ecgFile = null;
        // 创建bmeFileHead文件头
        BmeFileHead30 bmeFileHead = new BmeFileHead30();
        bmeFileHead.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        bmeFileHead.setDataType(BmeFileDataType.INT32);
        bmeFileHead.setFs(sampleRate);
        bmeFileHead.setInfo("这是一个心电文件。");
        bmeFileHead.setCalibrationValue(calibrationValue);
        long timeInMillis = new Date().getTime();
        bmeFileHead.setCreatedTime(timeInMillis);

        // 创建ecgFileHead文件头
        String simpleMacAddress = EcgMonitorUtil.cutColonMacAddress(macAddress);
        EcgFileHead ecgFileHead = new EcgFileHead(UserAccountManager.getInstance().getUserAccount(), simpleMacAddress, leadType);

        // 创建ecgFile
        String fileName = EcgMonitorUtil.createFileName(macAddress, timeInMillis);
        File toFile = FileUtil.getFile(CACHEDIR, fileName);
        try {
            fileName = toFile.getCanonicalPath();
            ecgFile = EcgFile.create(fileName, bmeFileHead, ecgFileHead);
            ViseLog.e(ecgFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ecgFile;
    }

    @Override
    protected int availableData() {
        if(raf != null) {
            try {
                return (int)((dataEndPointer - raf.getFilePointer())/fileHead.getDataType().getTypeLength());
            } catch (IOException e) {
                return 0;
            }
        }
        return 0;
    }

    public boolean saveFileTail() {
        try {
            dataEndPointer = dataBeginPointer + dataNum * fileHead.getDataType().getTypeLength();
            raf.seek(dataEndPointer);
            if(!ecgFileTail.writeToStream(raf))
                return false;
        } catch (IOException e) {
            return false;
        }
        return true;
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

    public EcgFileHead getEcgFileHead() {
        return ecgFileHead;
    }

    public EcgFileTail getEcgFileTail() { return ecgFileTail; }

    public List<IEcgAppendix> getAppendixList() {
        return ecgFileTail.getAppendixList();
    }

    public String getCreator() {
        return ecgFileHead.getCreator();
    }

    public long getCreateTime() {
        return ((BmeFileHead30)getBmeFileHead()).getCreatedTime();
    }

    public int getAppendixNum() {
        return ecgFileTail.getAppendixNum();
    }

    // 将文件指针定位到某个数据位置
    public boolean seekData(int dataNum) {
        try {
            raf.seek(dataBeginPointer + dataNum * getDataType().getTypeLength());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    // 为文件添加一条附加信息
    public void addAppendix(IEcgAppendix appendix) {
        ecgFileTail.addAppendix(appendix);
    }

    // 添加多条附加信息
    public void addAppendices(List<IEcgAppendix> appendices) {
        for(IEcgAppendix appendix : appendices) {
            ecgFileTail.addAppendix(appendix);
        }
    }

    // 删除一条附加信息
    public void deleteAppendix(IEcgAppendix appendix) {
        if(ecgFileTail.getAppendixList().contains(appendix)) {
            // 删除留言
            ecgFileTail.deleteAppendix(appendix);
        }
    }

    // 判断指定的数据位置是否位于标记中
    public boolean isWithinMarker(long location) {
        return ecgFileTail.isWithinMarker(location);
    }

    // 输出所有附加信息字符串，用于调试
    public String getAppendixString() {
        if(ecgFileTail.getAppendixNum() == 0) return "";
        StringBuilder builder = new StringBuilder();
        for(IEcgAppendix appendix : ecgFileTail.getAppendixList()) {
            builder.append(appendix.toString());
        }
        return builder.toString();
    }

    /**
     * 将文件raf的数据块从begin开始，往后推移length个字节
     * 推移时分块进行，每次移动opLengthEachTime个字节
    */
    private void pushTerminalBlockBackSomeBytes(RandomAccessFile raf, long begin, int length, int opLengthEachTime) throws IOException{
        raf.setLength(raf.length() + length);        // 先增加文件长度
        raf.seek(raf.length() - length);      // 移动到文件尾
        int blockLen = 0;                // 记录每次要移动的块长度，一般都是等于opLengthEachTime，但是当最后一个块移动时可能小于opLengthEachTime
        long blockBeginPointer = 0;      // 记录每次要移动的块开始位置
        while(raf.getFilePointer() > begin) {    // 保证每次要移动的块尾都在begin后面，否则就不需要移动了
            if(raf.getFilePointer() - opLengthEachTime < begin) {       // 如果剩下的块不够opLengthEachTime
                blockLen = (int)(raf.getFilePointer() - begin);
                blockBeginPointer = begin;    // 只需要从begin开始
            } else {
                blockLen = opLengthEachTime;
                blockBeginPointer = raf.getFilePointer() - opLengthEachTime;
            }
            byte[] buffer = new byte[blockLen];     // 建立长度为blockLen的缓存
            raf.seek(blockBeginPointer);            // 将文件指针移动到块开始位置
            raf.readFully(buffer);                  // 读块
            raf.seek(blockBeginPointer + length);     // 往后移动length
            raf.write(buffer);                      // 写块
            raf.seek(blockBeginPointer);            // 定位到块的开始位置，也就是下次要移动的块的尾部
        }
    }

    /**
     * 将文件raf的数据块从begin开始，向前移动length个字节
     * 移动时分块进行，每次移动opLengthEachTime个字节
     */
    private void pullTerminalBlockForwardSomeBytes(RandomAccessFile raf, long begin, int length, int opLengthEachTime) throws IOException{
        raf.seek(begin);            // 定位到起始位置

        int blockLen = 0;                // 记录每次要移动的块长度，一般都是等于opLengthEachTime，但是当最后一个块移动时可能小于opLengthEachTime
        long blockBeginPointer = raf.getFilePointer();      // 记录每次要移动的块开始位置
        while(blockBeginPointer < raf.length()) {     // 保证没有到达文件尾
            raf.seek(blockBeginPointer);            // 定位到块的开始位置
            if(blockBeginPointer + opLengthEachTime > raf.length()) {       // 如果剩下的块不够opLengthEachTime
                blockLen = (int)(raf.length() - blockBeginPointer);
            } else {
                blockLen = opLengthEachTime;
            }
            byte[] buffer = new byte[blockLen];     // 建立长度为blockLen的缓存
            raf.readFully(buffer);                  // 读块
            raf.seek(blockBeginPointer - length);     // 向前移动length
            raf.write(buffer);                      // 写块
            blockBeginPointer += blockLen;          // 修改下一个块的起始位置
        }
        raf.setLength(raf.length() - length);        // 减小文件长度length
    }


    @Override
    public String toString() {
        return super.toString() + ";" + ecgFileHead + ";" + ecgFileTail;
    }

}
