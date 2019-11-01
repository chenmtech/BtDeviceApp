package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorUtil;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFileHead;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.bmefile.BmeFileDataType;
import com.cmtech.bmefile.BmeFileHead30;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EcgRecord extends LitePalSupport {
    private int id;
    private final BmeFileHead30 bmeHead;
    private final EcgFileHead ecgHead;
    private String sigFileName = "";
    private String hrFileName = "";
    private List<EcgNormalComment> commentList = new ArrayList<>();

    private EcgRecord(BmeFileHead30 bmeHead, EcgFileHead ecgHead, String sigFileName, String hrFileName) {
        this.bmeHead = bmeHead;
        this.ecgHead = ecgHead;
        this.sigFileName = sigFileName;
        this.hrFileName = hrFileName;
    }

    // 创建新文件
    public static EcgRecord create(User creator, int sampleRate, int value1mV, String macAddress, EcgLeadType leadType) {
        long time = new Date().getTime();
        // 创建bmeFileHead文件头
        BmeFileHead30 bmeFileHead = new BmeFileHead30("an ecg file", BmeFileDataType.INT32, sampleRate, value1mV, time);
        // 创建ecgFileHead文件头
        String address = EcgMonitorUtil.cutMacAddressColon(macAddress);
        EcgFileHead ecgFileHead = new EcgFileHead(creator, address, leadType);

        String fileName = EcgMonitorUtil.makeFileName(macAddress, time);
        return new EcgRecord(bmeFileHead, ecgFileHead, "sig_"+fileName, "hr_"+fileName);
    }

    public List<EcgNormalComment> getCommentList() {
        return commentList;
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
    // 添加一条留言
    public void addComment(EcgNormalComment comment) {
        commentList.add(comment);
    }

    // 多条留言
    public void addComment(List<EcgNormalComment> comments) {
        commentList.addAll(comments);
    }

    // 删除一条留言
    public void deleteComment(EcgNormalComment comment) {
        commentList.remove(comment);
    }

    @Override
    public String toString() {
        return super.toString() + "-" + ecgHead + "-" + commentList;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        EcgRecord other = (EcgRecord) otherObject;
        return id == ((EcgRecord) otherObject).id;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(id).hashCode();
    }
}
