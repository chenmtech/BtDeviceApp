package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import com.cmtech.bmefile.BmeFileHead;
import com.cmtech.bmefile.RandomAccessBmeFile;
import com.cmtech.bmefile.exception.FileException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class EcgFile extends RandomAccessBmeFile {
    private static final int OP_BLOCK_LEN = 512;        // 添加和删除评论时，每次移动的文件数据块的大小

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

            pushTerminalBlockBackSomeBytes(raf, dataBeginPointer, EcgFileComment.getLength(), OP_BLOCK_LEN);

            ecgFileHead.addComment(comment);
            raf.seek(ecgFileHeadPointer);
            ecgFileHead.writeToStream(raf);

            // 要修改数据开始指针
            dataBeginPointer = raf.getFilePointer();

            // 还原文件位置
            raf.seek(rafPos + EcgFileComment.getLength());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileException e) {
            e.printStackTrace();
        }
    }

    public void addComments(List<EcgFileComment> comments) {
        try {
            long rafPos = raf.getFilePointer(); // 记录当前文件位置，操作完成后要回到原来的位置

            pushTerminalBlockBackSomeBytes(raf, dataBeginPointer, EcgFileComment.getLength()*comments.size(), OP_BLOCK_LEN);

            for(EcgFileComment comment : comments) {
                ecgFileHead.addComment(comment);
            }
            raf.seek(ecgFileHeadPointer);
            ecgFileHead.writeToStream(raf);

            // 要修改数据开始指针
            dataBeginPointer = raf.getFilePointer();

            // 还原文件位置
            raf.seek(rafPos + EcgFileComment.getLength()*comments.size());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileException e) {
            e.printStackTrace();
        }
    }

    public void deleteComment(EcgFileComment comment) {
        if(ecgFileHead.getCommentList().contains(comment)) {
            try {
                long rafPos = raf.getFilePointer();

                ecgFileHead.deleteComment(comment);
                raf.seek(ecgFileHeadPointer);
                ecgFileHead.writeToStream(raf);

                long newDataBeginPointer = raf.getFilePointer();

                pullTerminalBlockForwardSomeBytes(raf, dataBeginPointer, EcgFileComment.getLength(), OP_BLOCK_LEN);

                dataBeginPointer = newDataBeginPointer;
                raf.seek(rafPos - EcgFileComment.getLength());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (FileException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 将文件raf的尾块从begin开始，往后推移paddingLength个字节
     * 推移时分小块进行，每次移动opBlockLengthPerTime个字节
    */
    private void pushTerminalBlockBackSomeBytes(RandomAccessFile raf, long begin, int length, int opLengthEachTime) throws IOException{
        raf.setLength(raf.length() + length);        // 先增加文件长度
        raf.seek(raf.length() - length);      // 移动到文件尾
        int blockLen = 0;                // 记录每次要移动的块长度，一般都是等于opLengthEachTime，但是当最后移动时可能小于opLengthEachTime
        long blockBeginPointer = 0;      // 记录每次要移动的块开始位置
        while(raf.getFilePointer() > begin)      // 保证每次要移动的块尾都在begin后面，否则就不需要移动了
        {
            if(raf.getFilePointer() - opLengthEachTime < begin) {       // 如果剩下的块不够opLengthEachTime
                blockLen = (int)(raf.getFilePointer() - begin);
                blockBeginPointer = begin;    // 只需要从begin开始
            } else {
                blockLen = opLengthEachTime;
                blockBeginPointer = raf.getFilePointer() - opLengthEachTime;
            }
            byte[] buffer = new byte[blockLen];     // 建立长度为blockLen的缓存
            raf.seek(blockBeginPointer);            // 将文件指针移动到块开始位置
            raf.readFully(buffer);                  // 读
            raf.seek(blockBeginPointer + length);     // 往后移动length
            raf.write(buffer);                      // 写
            raf.seek(blockBeginPointer);            // 定位到块的开始位置，也就是下次块的尾部
        }
    }

    private void pullTerminalBlockForwardSomeBytes(RandomAccessFile raf, long begin, int length, int opLengthEachTime) throws IOException{
        raf.seek(begin);

        int blockLen = 0;                // 记录每次要移动的块长度，一般都是等于blockLengthPerTime，但是当最后移动时可能小于blockLengthPerTime
        long blockBeginPointer = raf.getFilePointer();      // 记录每次要移动的块开始位置
        while(blockBeginPointer < raf.length())      // 保证每次要移动的块尾都在fromPointer后面，否则就不需要移动了
        {
            if(blockBeginPointer + opLengthEachTime > raf.length()) {       // 如果剩下的块不够blockLengthPerTime
                blockLen = (int)(raf.length() - blockBeginPointer);
            } else {
                blockLen = opLengthEachTime;
            }
            byte[] buffer = new byte[blockLen];     // 建立长度为blockLen的缓存
            raf.readFully(buffer);                  // 读
            raf.seek(blockBeginPointer - length);     // 往后移动paddingLength
            raf.write(buffer);                      // 写
            blockBeginPointer += blockLen;
            raf.seek(blockBeginPointer);            // 定位到块的开始位置，也就是下次块的尾部
        }
        raf.setLength(raf.length() - length);        // 先增加文件长度
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
