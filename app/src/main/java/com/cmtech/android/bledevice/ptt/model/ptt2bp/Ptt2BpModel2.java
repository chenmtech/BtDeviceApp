package com.cmtech.android.bledevice.ptt.model.ptt2bp;

import android.util.Pair;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ptt.model.ptt2bp
 * ClassName:      Ptt2BpModel2
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2021/6/22 上午6:43
 * UpdateUser:     更新者
 * UpdateDate:     2021/6/22 上午6:43
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class Ptt2BpModel2 implements IPtt2BpModel{
    private final static double GAMMA = 0.017;
    private double ptt0;
    private double sbp0;
    private double dbp0;

    public Ptt2BpModel2(double ptt0, double sbp0, double dbp0) {
        this.ptt0 = ptt0;
        this.sbp0 = sbp0;
        this.dbp0 = dbp0;
    }

    @Override
    public Pair<Double, Double> getBp(double ptt) {
        double tmp = 2*(ptt-ptt0)/GAMMA/ptt0;
        return new Pair<>(sbp0 - tmp, dbp0 - tmp);
    }
}
