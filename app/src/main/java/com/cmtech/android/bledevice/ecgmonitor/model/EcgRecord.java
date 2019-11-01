package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFileHead;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.bmefile.BmeFileDataType;
import com.cmtech.bmefile.BmeFileHead30;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cmtech.bmefile.BmeFileHead.INVALID_SAMPLE_RATE;

public class EcgRecord extends LitePalSupport {
    private int id;
    private final BmeFileHead30 bmeHead;
    private final EcgFileHead ecgHead;
    private String sigFileName = "";
    @Column(ignore = true)
    private RandomAccessFile sigRaf;
    private String hrFileName = "";
    @Column(ignore = true)
    private RandomAccessFile hrRaf;
    private List<EcgNormalComment> commentList = new ArrayList<>();

    private EcgRecord(BmeFileHead30 bmeHead, EcgFileHead ecgHead, String recordName) throws IOException{
        this.bmeHead = bmeHead;
        this.ecgHead = ecgHead;

        String sigFileName = "sig_" + recordName;
        RandomAccessFile raf = createRandomAccessFile(sigFileName);
        if(raf == null) {
            throw new IOException();
        }
        this.sigFileName = sigFileName;
        this.sigRaf = raf;

        String hrFileName = "hr_" + recordName;
        raf = createRandomAccessFile(hrFileName);
        if(raf == null) {
            throw new IOException();
        }
        this.hrFileName = hrFileName;
        this.hrRaf = raf;
    }

    private RandomAccessFile createRandomAccessFile(String fileName) {
        File file = new File(fileName);
        try {
            if(file.exists() || file.createNewFile()) {
                return new RandomAccessFile(file, "rw");
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 创建新文件
    public static EcgRecord create(User creator, int sampleRate, int value1mV, String macAddress, EcgLeadType leadType) {
        long time = new Date().getTime();
        // 创建bmeFileHead文件头
        BmeFileHead30 bmeFileHead = new BmeFileHead30("an ecg file", BmeFileDataType.INT32, sampleRate, value1mV, time);
        // 创建ecgFileHead文件头
        String address = EcgMonitorUtil.cutMacAddressColon(macAddress);
        EcgFileHead ecgFileHead = new EcgFileHead(creator, address, leadType);

        String recordName = EcgMonitorUtil.makeFileName(macAddress, time);
        try {
            return new EcgRecord(bmeFileHead, ecgFileHead, recordName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BmeFileDataType getDataType() {
        return (bmeHead == null) ? null : bmeHead.getDataType();
    }
    public int getSampleRate() {
        return (bmeHead == null) ? INVALID_SAMPLE_RATE : bmeHead.getSampleRate();
    }
    public byte[] getVersion() {
        return (bmeHead == null) ? null : bmeHead.getVersion();
    }
    public ByteOrder getByteOrder() {
        return (bmeHead == null) ? null : bmeHead.getByteOrder();
    }
    public User getCreator() {
        return ecgHead.getCreator();
    }
    public String getCreatorName() {
        return getCreator().getName();
    }
    public long getCreateTime() {
        return bmeHead.getCreateTime();
    }
    public List<Short> getHrList() {
        return null;
    }
    public void setHrList(List<Short> hrList) {
    }
    public String getMacAddress() {
        return ecgHead.getMacAddress();
    }
    public List<EcgNormalComment> getCommentList() {
        return commentList;
    }
    // 添加一条留言
    public void addComment(EcgNormalComment comment) {
        commentList.add(comment);
    }
    // 删除一条留言
    public void deleteComment(EcgNormalComment comment) {
        commentList.remove(comment);
    }


    public void close() {
        try {
            if(sigRaf != null) {
                sigRaf.close();
            }
            if(hrRaf != null) {
                hrRaf.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            sigRaf = null;
            hrRaf = null;
        }
    }

    @Override
    public String toString() {
        return bmeHead + "-" + ecgHead + "-" + sigFileName + "-" + hrFileName + "-" +commentList;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        EcgRecord other = (EcgRecord) otherObject;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(id).hashCode();
    }
}
