package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EcgHrInfoAppendix implements IEcgAppendix{
    private static final int HR_VALUE_BYTE_NUM = 4;

    private List<Integer> hrList = new ArrayList<>();

    public EcgHrInfoAppendix() {

    }

    public EcgHrInfoAppendix(List<Integer> hrList) {
        if(hrList != null)
            this.hrList = hrList;
    }

    public void setHrList(List<Integer> hrList) {
        this.hrList = hrList;
    }

    public List<Integer> getHrList() {
        return hrList;
    }

    @Override
    public void readFromStream(DataInput in) throws IOException {
        // 读心率数据
        int hrLength = ByteUtil.reverseInt(in.readInt());
        for(int i = 0; i < hrLength; i++) {
            hrList.add(ByteUtil.reverseInt(in.readInt()));
        }
    }

    @Override
    public void writeToStream(DataOutput out) throws IOException{
        // 写心率信息
        out.writeInt(ByteUtil.reverseInt(hrList.size()));
        for(int hr : hrList) {
            out.writeInt(ByteUtil.reverseInt(hr));
        }
    }

    @Override
    public int length() {
        return 4 + hrList.size()*HR_VALUE_BYTE_NUM;
    }

    @Override
    public EcgAppendixType getType() {
        return EcgAppendixType.HR_INFO;
    }

    @Override
    public String toString() {
        return hrList.toString();
    }
}
