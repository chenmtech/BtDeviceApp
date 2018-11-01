package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import com.cmtech.dsp.bmefile.BmeFileHead;
import com.cmtech.dsp.bmefile.RandomAccessBmeFile;
import com.cmtech.dsp.exception.FileException;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.util.Arrays;

public class EcgFile extends RandomAccessBmeFile {
    private EcgFileHead ecgFileHead = new EcgFileHead();
    private long ecgFileHeadPointer;

    private EcgFile(String fileName) throws FileException {
        super(fileName);
        try {
            ecgFileHeadPointer = raf.getFilePointer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ecgFileHead.readFromStream(raf);
        setDataBeginPointer();
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

    public int getDataNum() {
        try {
            return (int)((raf.length()-dataBeginPointer)/fileHead.getDataType().getTypeLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void addComment(EcgFileComment comment) {
        try {
            long rafPos = raf.getFilePointer();

            raf.setLength(raf.length() + EcgFileComment.getLength());
            //raf.seek(dataBeginPointer);

            //byte[] cache = new byte[EcgFileComment.getLength()];
            //int byteRead = raf.read(cache);
            //raf.skipBytes(-byteRead);
            //comment.writeToStream(raf);
            /*do {
                byte[] tmpCache = Arrays.copyOf(cache, byteRead);
                byteRead = raf.read(cache);
                if(byteRead == -1) break;
                raf.skipBytes(-byteRead);
                raf.write(tmpCache);
            }while(true);*/

            ecgFileHead.addComment(comment);
            raf.seek(ecgFileHeadPointer);
            ecgFileHead.writeToStream(raf);

            dataBeginPointer = raf.getFilePointer();
            //raf.seek(rafPos+EcgFileComment.getLength());
            //ecgFileHead.addComment(comment);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return super.toString() + "; 数据个数：" + getDataNum() + ";" + ecgFileHead;
        /*try {
            return ""+raf.length()+";"+ecgFileHeadPointer+";"+dataBeginPointer+";"+ecgFileHead.getCommentsNum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";*/
    }
}
