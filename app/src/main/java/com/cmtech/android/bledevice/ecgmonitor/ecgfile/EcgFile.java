package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import com.cmtech.dsp.bmefile.BmeFileHead;
import com.cmtech.dsp.bmefile.RandomAccessBmeFile;
import com.cmtech.dsp.bmefile.exception.FileException;

import java.io.IOException;
import java.io.RandomAccessFile;

public class EcgFile extends RandomAccessBmeFile {
    private EcgFileHead ecgFileHead = new EcgFileHead();
    private long ecgFileHeadPointer;

    private EcgFile(String fileName) throws FileException {
        super(fileName);
        try {
            ecgFileHeadPointer = raf.getFilePointer();
            ecgFileHead.readFromStream(raf);
            setDataBeginPointer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private EcgFile(String fileName, BmeFileHead head, EcgFileHead ecgFileHead) throws FileException {
        super(fileName, head);
        try {
            ecgFileHeadPointer = raf.getFilePointer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.ecgFileHead = ecgFileHead;
        ecgFileHead.writeToStream(raf);
        setDataBeginPointer();
    }

    // 打开已有文件
    public static EcgFile openBmeFile(String fileName) throws FileException{
        return new EcgFile(fileName);
    }

    // 用缺省文件头创建新的文件
    public static EcgFile createBmeFile(String fileName) throws FileException{
        return new EcgFile(fileName, DEFAULT_BMEFILE_HEAD, new EcgFileHead());
    }

    // 用指定的文件头创建新的文件
    public static EcgFile createBmeFile(String fileName, BmeFileHead head, EcgFileHead ecgFileHead) throws FileException{
        return new EcgFile(fileName, head, ecgFileHead);
    }

    public EcgFileHead getEcgFileHead() {
        return ecgFileHead;
    }

    public String getCommentString() {
        if(ecgFileHead.getCommentsNum()==0) return "";
        StringBuilder builder = new StringBuilder();
        for(EcgFileComment comment : ecgFileHead.getCommentList()) {
            builder.append(comment.toString());
        }
        return builder.toString();
    }

    public void addComment(EcgFileComment comment) {
        try {
            long rafPos = raf.getFilePointer(); // 记录当前文件位置，操作完成后要回到原来的位置

            pushFileEndBlockBackToSomeBytes(raf, dataBeginPointer, EcgFileComment.getLength(), 256);

            ecgFileHead.addComment(comment);
            raf.seek(ecgFileHeadPointer);
            ecgFileHead.writeToStream(raf);

            // 要修改数据开始指针
            dataBeginPointer = raf.getFilePointer();

            // 还原文件位置
            raf.seek(rafPos+EcgFileComment.getLength());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将文件raf的尾块从fromPointer开始，往后推移paddingLength个字节
     * 推移时分小块小块进行，每次移动blockLengthPerTime个字节
    */
    private void pushFileEndBlockBackToSomeBytes(RandomAccessFile raf, long fromPointer, int paddingLength, int blockLengthPerTime) throws IOException{
        raf.setLength(raf.length() + paddingLength);        // 先增加文件长度
        raf.seek(raf.length() - paddingLength);      // 移动到文件尾
        int blockLen = 0;                // 记录每次要移动的块长度，一般都是等于blockLengthPerTime，但是当最后移动时可能小于blockLengthPerTime
        long blockBeginPointer = 0;      // 记录每次要移动的块开始位置
        while(raf.getFilePointer() > fromPointer)      // 保证每次要移动的块尾都在fromPointer后面，否则就不需要移动了
        {
            if(raf.getFilePointer() - blockLengthPerTime < fromPointer) {       // 如果剩下的块不够blockLengthPerTime
                blockLen = (int)(raf.getFilePointer() - fromPointer);
                blockBeginPointer = fromPointer;    // 只需要从fromPointer开始
            } else {
                blockLen = blockLengthPerTime;
                blockBeginPointer = raf.getFilePointer() - blockLengthPerTime;
            }
            byte[] buffer = new byte[blockLen];     // 建立长度为blockLen的缓存
            raf.seek(blockBeginPointer);            // 将文件指针移动到块开始位置
            raf.readFully(buffer);                  // 读
            raf.seek(blockBeginPointer + paddingLength);     // 往后移动paddingLength
            raf.write(buffer);                      // 写
            raf.seek(blockBeginPointer);            // 定位到块的开始位置，也就是下次块的尾部
        }
    }


    @Override
    public String toString() {
        return super.toString() + ";" + ecgFileHead;
        /*try {
            return ""+raf.length()+";"+ecgFileHeadPointer+";"+dataBeginPointer+";"+ecgFileHead.getCommentsNum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";*/
    }

    public void seekData(int dataNum) {
        try {
            raf.seek(dataBeginPointer + dataNum * getDataType().getTypeLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
