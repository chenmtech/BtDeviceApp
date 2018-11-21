package com.cmtech.android.bledevice.ecgmonitor.model.ecgfile;

import com.cmtech.bmefile.BmeFileHead;
import com.cmtech.bmefile.RandomAccessBmeFile;
import com.cmtech.bmefile.exception.FileException;

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
            dataBeginPointer = raf.getFilePointer();        // 标记数据开始的位置指针
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

    public EcgFileHead getEcgFileHead() {
        return ecgFileHead;
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
        try {
            long rafPos = raf.getFilePointer(); // 记录当前文件位置，操作完成后要回到原来的位置

            // 把文件数据向后移动EcgComment长度
            pushTerminalBlockBackSomeBytes(raf, dataBeginPointer, EcgComment.length(), ECG_BLOCK_LEN);

            // 更新文件头，并写入文件
            ecgFileHead.addComment(comment);
            raf.seek(ecgFileHeadPointer);
            ecgFileHead.writeToStream(raf);

            // 修改数据开始指针
            dataBeginPointer = raf.getFilePointer();

            // 还原文件位置
            raf.seek(rafPos + EcgComment.length());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileException e) {
            e.printStackTrace();
        }
    }

    // 添加多条留言
    public void addComments(List<EcgComment> comments) {
        try {
            long rafPos = raf.getFilePointer(); // 记录当前文件位置，操作完成后要回到原来的位置

            pushTerminalBlockBackSomeBytes(raf, dataBeginPointer, EcgComment.length()*comments.size(), ECG_BLOCK_LEN);

            for(EcgComment comment : comments) {
                ecgFileHead.addComment(comment);
            }
            raf.seek(ecgFileHeadPointer);
            ecgFileHead.writeToStream(raf);

            // 修改数据开始指针
            dataBeginPointer = raf.getFilePointer();

            // 还原文件位置
            raf.seek(rafPos + EcgComment.length()*comments.size());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileException e) {
            e.printStackTrace();
        }
    }

    // 删除一条留言
    public void deleteComment(EcgComment comment) {
        if(ecgFileHead.getCommentList().contains(comment)) {
            try {
                long rafPos = raf.getFilePointer(); // 记录当前文件位置，操作完成后要回到原来的位置

                // 删除留言
                ecgFileHead.deleteComment(comment);
                raf.seek(ecgFileHeadPointer);
                ecgFileHead.writeToStream(raf);

                // 记录新的数据起始位置
                long newDataBeginPointer = raf.getFilePointer();

                // 将数据往前移动EcgComment.length
                pullTerminalBlockForwardSomeBytes(raf, dataBeginPointer, EcgComment.length(), ECG_BLOCK_LEN);

                // 记录数据起始位置
                dataBeginPointer = newDataBeginPointer;

                // 还原文件位置
                raf.seek(rafPos - EcgComment.length());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (FileException e) {
                e.printStackTrace();
            }

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
        return super.toString() + ";" + ecgFileHead;
    }

    // 输出所有留言字符串，用于调试
    public String getCommentString() {
        if(ecgFileHead.getCommentsNum() == 0) return "";
        StringBuilder builder = new StringBuilder();
        for(EcgComment comment : ecgFileHead.getCommentList()) {
            builder.append(comment.toString());
        }
        return builder.toString();
    }
}
