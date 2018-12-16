package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.bmefile.BmeFileHead;
import com.cmtech.bmefile.BmeFileHead30;
import com.cmtech.bmefile.RandomAccessBmeFile;
import com.cmtech.bmefile.exception.FileException;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECG_BLOCK_LEN;

/**
 * EcgFile: 心电文件类，可随机访问
 * Created by bme on 2018/11/20.
 */

public class EcgFile extends RandomAccessBmeFile {

    private EcgFileHead ecgFileHead = EcgFileHead.createDefaultEcgFileHead();       // EcgFileHead
    private EcgFileTail ecgFileTail = new EcgFileTail();        // Ecg文件尾
    private long ecgFileHeadPointer;                                               // EcgFileHead在文件中的位置指针

    // 打开已有文件
    public static EcgFile openBmeFile(String fileName) throws FileException{
        return new EcgFile(fileName);
    }

    // 打开文件时使用的私有构造器
    private EcgFile(String fileName) throws FileException {
        super(fileName);
        try {
            ecgFileHeadPointer = raf.getFilePointer();      // 标记EcgFileHead位置指针
            ecgFileHead.readFromStream(raf);                 // 读EcgFileHead
            ViseLog.e("hi1");
            dataBeginPointer = raf.getFilePointer();        // 标记数据开始的位置指针
            ecgFileTail.readFromStream(raf);
            ViseLog.e("hi2");
            raf.seek(dataBeginPointer);
            dataNum = availableData();
            ViseLog.e("hi3");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 用指定的文件头创建新的文件
    public static EcgFile createBmeFile(String fileName, BmeFileHead head, EcgFileHead ecgFileHead) throws FileException{
        return new EcgFile(fileName, head, ecgFileHead);
    }

    // 创建新文件时使用的私有构造器
    private EcgFile(String fileName, BmeFileHead head, EcgFileHead ecgFileHead) throws FileException {
        super(fileName, head);
        try {
            ecgFileHeadPointer = raf.getFilePointer();      // 标记EcgFileHead位置指针
            this.ecgFileHead = ecgFileHead;
            ecgFileHead.writeToStream(raf);                   // 写EcgFileHead
            dataBeginPointer = raf.getFilePointer();        // 标记数据开始的位置指针
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int availableData() {
        if(raf != null && ecgFileTail != null) {
            try {
                return (int)((raf.length() - ecgFileTail.length() - raf.getFilePointer())/fileHead.getDataType().getTypeLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public void close() {
        try {
            raf.seek(dataBeginPointer + dataNum * fileHead.getDataType().getTypeLength());
            ecgFileTail.writeToStream(raf);
            super.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileException e) {
            e.printStackTrace();
        }
    }

    public boolean noMoreData() {
        if(raf == null) return true;

        try {
            return (raf.getFilePointer() >= dataBeginPointer + dataNum*fileHead.getDataType().getTypeLength());
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }

    public EcgFileHead getEcgFileHead() {
        return ecgFileHead;
    }

    public EcgFileTail getEcgFileTail() { return ecgFileTail; }

    public List<EcgComment> getCommentList() {
        return ecgFileTail.getCommentList();
    }

    public String getCreatedPerson() {
        return ecgFileHead.getCreatedPerson();
    }

    public long getCreatedTime() {
        return ((BmeFileHead30)getBmeFileHead()).getCreatedTime();
    }

    public int getCommentsNum() {
        return ecgFileTail.getCommentsNum();
    }

    // 将文件指针定位到某个数据位置
    public void seekData(int dataNum) {
        try {
            raf.seek(dataBeginPointer + dataNum * getDataType().getTypeLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 为文件添加一条留言
    public void addComment(EcgComment comment) {
        ecgFileTail.addComment(comment);
    }

    // 添加多条留言
    public void addComments(List<EcgComment> comments) {
        for(EcgComment comment : comments) {
            ecgFileTail.addComment(comment);
        }
    }

    // 删除一条留言
    public void deleteComment(EcgComment comment) {
        if(ecgFileTail.getCommentList().contains(comment)) {
            // 删除留言
            ecgFileTail.deleteComment(comment);
        }
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

    // 输出所有留言字符串，用于调试
    public String getCommentString() {
        if(ecgFileTail.getCommentsNum() == 0) return "";
        StringBuilder builder = new StringBuilder();
        for(EcgComment comment : ecgFileTail.getCommentList()) {
            builder.append(comment.toString());
        }
        return builder.toString();
    }
}
