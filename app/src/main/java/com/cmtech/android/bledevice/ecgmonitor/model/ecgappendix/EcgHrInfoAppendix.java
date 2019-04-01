package com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix;

import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EcgHrInfoAppendix extends EcgAppendix{
    private static final int HR_LIST_LEN_BYTE_NUM = 4;
    private static final int BYTE_NUM_PER_HR_VALUE = 4;

    private List<Integer> hrList = new ArrayList<>();

    public EcgHrInfoAppendix() {

    }

    public void setHrList(List<Integer> hrList) {
        this.hrList = hrList;
    }

    public List<Integer> getHrList() {
        return hrList;
    }

    @Override
    public void readFromStream(DataInput in) throws IOException {
        if(in == null) throw new IllegalArgumentException();

        int hrLength = ByteUtil.reverseInt(in.readInt());

        for(int i = 0; i < hrLength; i++) {
            hrList.add(ByteUtil.reverseInt(in.readInt()));
        }
    }

    @Override
    public void writeToStream(DataOutput out) throws IOException{
        if(out == null) throw new IllegalArgumentException();

        out.writeInt(ByteUtil.reverseInt(hrList.size()));

        for(int hr : hrList) {
            out.writeInt(ByteUtil.reverseInt(hr));
        }
    }

    @Override
    public int length() {
        return super.length() + HR_LIST_LEN_BYTE_NUM + hrList.size()* BYTE_NUM_PER_HR_VALUE;
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
