package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import com.cmtech.dsp.bmefile.BmeFileHead;
import com.cmtech.dsp.bmefile.RandomAccessBmeFile;
import com.cmtech.dsp.exception.FileException;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

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

    @Override
    public int getDataNum() {
        try {
            return (int)((raf.length()-dataBeginPointer)/fileHead.getDataType().getTypeLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
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
            long rafPos = raf.getFilePointer();

            raf.setLength(raf.length() + EcgFileComment.getLength());
            raf.seek(raf.length()-EcgFileComment.getLength());
            int copyNum = 0;
            while(raf.getFilePointer() > dataBeginPointer) {
                if(raf.getFilePointer() - EcgFileComment.getLength() < dataBeginPointer) {
                    copyNum = (int)(raf.getFilePointer()-dataBeginPointer);
                } else {
                    copyNum = EcgFileComment.getLength();
                }
                byte[] cache = new byte[copyNum];
                raf.seek(raf.getFilePointer()-copyNum);
                raf.readFully(cache);
                raf.write(cache);
                raf.seek(raf.getFilePointer()-2*copyNum);
            }

            ecgFileHead.addComment(comment);
            raf.seek(ecgFileHeadPointer);
            ecgFileHead.writeToStream(raf);

            dataBeginPointer = raf.getFilePointer();
            raf.seek(rafPos+EcgFileComment.getLength());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileException e) {
            e.printStackTrace();
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
}
