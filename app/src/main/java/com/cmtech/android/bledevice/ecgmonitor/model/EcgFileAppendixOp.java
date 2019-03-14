package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendix;

import java.util.ArrayList;
import java.util.List;

public class EcgFileAppendixOp {
    private List<EcgAppendix> appendixList = new ArrayList<>();

    public EcgFileAppendixOp() {

    }

    public EcgFileAppendixOp(List<EcgAppendix> appendixList) {
        this.appendixList = appendixList;
    }

    public List<EcgAppendix> getAppendixList() {
        return appendixList;
    }

    public void setAppendixList(List<EcgAppendix> appendixList) {
        this.appendixList = appendixList;
    }
}
