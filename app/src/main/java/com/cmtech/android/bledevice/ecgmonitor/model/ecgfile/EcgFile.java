package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.bmefile.AbstractRandomAccessBmeFile;
import com.cmtech.bmefile.BmeFileDataType;
import com.cmtech.bmefile.BmeFileHead;
import com.cmtech.bmefile.BmeFileHead30;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledevice.core.BleDeviceConstant.CACHEDIR;

/**
 * EcgFile: 心电文件类，可随机访问
 * Created by bme on 2018/11/20.
 */

public class EcgFile extends AbstractRandomAccessBmeFile {
    private EcgFileHead ecgFileHead = new EcgFileHead(); // EcgFileHead

    private EcgFileTail ecgFileTail = new EcgFileTail(); // Ecg文件尾

    private final long dataBeginPointer; // 数据起始位置指针

    private long dataEndPointer;  // 数据结束的文件位置指针

    /**
     * 打开已有EcgFile文件时使用的私有构造器
     */
    private EcgFile(String fileName) throws IOException {
        super(fileName);

        ecgFileHead.readFromStream(raf);

        dataBeginPointer = raf.getFilePointer(); // 标记数据开始的位置指针

        ecgFileTail.readFromStream(raf);

        dataEndPointer = raf.length() - ecgFileTail.length();

        raf.seek(dataBeginPointer); // 回到数据开始位置

        dataNum = availableDataFromCurrentPos(); // 获取数据个数
    }

    // 创建新文件时使用的私有构造器
    private EcgFile(String fileName, BmeFileHead head, EcgFileHead ecgFileHead) throws IOException {
        super(fileName, head);

        this.ecgFileHead = ecgFileHead;

        ecgFileHead.writeToStream(raf);

        dataBeginPointer = raf.getFilePointer(); // 标记数据开始的位置指针

        dataEndPointer = dataBeginPointer;

        dataNum = 0;
    }

    // 打开已有文件
    public static EcgFile open(String fileName) throws IOException{
        return new EcgFile(fileName);
    }

    // 创建Ecg文件
    public static EcgFile create(int sampleRate, int calibrationValue, String macAddress, EcgLeadType leadType) throws IOException{
        EcgFile ecgFile;

        long fileCreateTime = new Date().getTime();

        // 创建bmeFileHead文件头
        BmeFileHead30 bmeFileHead = new BmeFileHead30("an ecg file", BmeFileDataType.INT32, sampleRate, calibrationValue, fileCreateTime);

        // 创建ecgFileHead文件头
        String simpleMacAddress = EcgMonitorUtil.cutColonInMacAddress(macAddress);
        EcgFileHead ecgFileHead = new EcgFileHead(AccountManager.getInstance().getAccount(), simpleMacAddress, leadType);

        // 创建ecgFile
        String fileName = EcgMonitorUtil.makeFileName(macAddress, fileCreateTime);
        ecgFile = EcgFile.create(FileUtil.getFile(CACHEDIR, fileName).getCanonicalPath(), bmeFileHead, ecgFileHead);

        return ecgFile;
    }

    // 用指定的文件头创建新的文件
    private static EcgFile create(String fileName, BmeFileHead head, EcgFileHead ecgFileHead) throws IOException{
        return new EcgFile(fileName, head, ecgFileHead);
    }

    public List<EcgNormalComment> getCommentList() {
        return ecgFileTail.getCommentList();
    }

    public User getCreator() {
        return ecgFileHead.getCreator();
    }

    public String getCreatorName() {
        return getCreator().getUserName();
    }

    public long getCreateTime() {
        return ((BmeFileHead30)getBmeFileHead()).getCreateTime();
    }

    public List<Integer> getHrList() {
        return ecgFileTail.getHrList();
    }

    public void setHrList(List<Integer> hrList) {
        ecgFileTail.setHrList(hrList);
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

        dataEndPointer = calculateDataEndPointer();

        raf.seek(dataEndPointer);

        ecgFileTail.writeToStream(raf);

        raf.seek(curPointer);
    }

    private long calculateDataEndPointer() {
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
        ecgFileTail.addComment(comment);
    }

    // 多条留言
    public void addComment(List<EcgNormalComment> comments) {
        for(EcgNormalComment comment : comments) {
            ecgFileTail.addComment(comment);
        }
    }

    // 删除一条留言
    public void deleteComment(EcgNormalComment comment) {
        if(ecgFileTail.getCommentList().contains(comment)) {
            // 删除留言
            ecgFileTail.deleteComment(comment);
        }
    }

    @Override
    public String toString() {
        return super.toString() + ";" + ecgFileHead + ";" + ecgFileTail;
    }
}
