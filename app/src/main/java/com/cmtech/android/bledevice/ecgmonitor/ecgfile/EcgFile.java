package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import com.cmtech.dsp.bmefile.BmeFileHead;
import com.cmtech.dsp.bmefile.RandomAccessBmeFile;
import com.cmtech.dsp.exception.FileException;

public class EcgFile extends RandomAccessBmeFile {
    private EcgFileHead ecgFileHead = new EcgFileHead();

    private EcgFile(String fileName) throws FileException {
        super(fileName);
        ecgFileHead.readFromStream(raf);
        setDataBeginMark();
    }

    private EcgFile(String fileName, BmeFileHead head, EcgFileHead ecgFileHead) throws FileException {
        super(fileName, head);
        this.ecgFileHead = ecgFileHead;
        ecgFileHead.writeToStream(raf);
        setDataBeginMark();
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
}
