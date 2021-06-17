package com.cmtech.android.bledevice.ptt.model;

import android.util.Pair;

import com.cmtech.android.bledeviceapp.dataproc.ecgproc.preproc.qrsdetbyhamilton.QrsDetector;
import com.cmtech.android.bledeviceapp.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

public class PttDetector {
    private static final int QRS_WIDTH_MS = 200; // QRS波宽度，unit:毫秒

    private int sampleRate = 250;
    private int maxLength = 4*sampleRate;
    private int halfQrsWidth = QRS_WIDTH_MS*sampleRate/1000/2; // QRS波一半宽度点数
    private final List<Integer> ecgData = new ArrayList<>();
    private final List<Integer> ppgData = new ArrayList<>();
    private final List<Integer> deltaPpg = new ArrayList<>();
    private int length = 0;
    private int qrsNum = 0;
    private int qrsPos = 0;
    private int ppg_1 = 0;
    private int ppg_2 = 0;


    private final QrsDetector qrsDetector;

    public PttDetector(int sampleRate) {
        this.sampleRate = sampleRate;
        maxLength = 4*sampleRate;
        halfQrsWidth = QRS_WIDTH_MS*sampleRate/1000/2;

        qrsDetector = new QrsDetector(sampleRate);
        initialize();
    }

    public int process(int ecg, int ppg) {
        // 数据长度超过最大长度限制，清空数据，初始化
        if(length >= maxLength) {
            initialize();
            return 0;
        }

        // 如果是QRS波
        if(qrsDetector.outputRRInterval(ecg) != 0) {
            qrsNum++;
        }

        // 如果已经有QRS波出现，保存数据
        if(qrsNum > 0) {
            ecgData.add(ecg);
            ppgData.add(ppg);
            deltaPpg.add(getDeltaPpg(ppg));

            length++;
        }

        // 如果发现第二个QRS波，记录QRS波峰位置
        if(qrsNum == 2) {
            qrsPos = length-1;
            return 0;
        }

        // 如果发现第三个QRS波，开始处理第二个QRS波，并计算PTT和BP
        if(qrsNum == 3) {
            // 精确定位R波
            int minPos = Math.min(qrsPos-halfQrsWidth, 0);
            int maxPos = Math.max(qrsPos+halfQrsWidth, length);
            Pair<Integer, Integer> rlt = MathUtil.intMax(ecgData, minPos, maxPos);
            qrsPos = rlt.first;

            // 获取第一个R波和第二个R波之间的PPG最大导数位置
            rlt = MathUtil.intMax(deltaPpg, 0, qrsPos);
            int ppgPos = Math.max(0, rlt.first-1); // 中心差分的deltaPPG有一位的延时

            // 计算PTT
            int ptt = (int) Math.round(ppgPos*1000.0/sampleRate);

            // 把第二个R波之前的数据都删除掉
            ecgData.subList(0, qrsPos).clear();
            ppgData.subList(0, qrsPos).clear();
            deltaPpg.subList(0, qrsPos).clear();

            // 更新数据
            length = ecgData.size();
            qrsNum--;
            qrsPos = length-1-qrsPos;

            return ptt;
        }

        return 0;
    }

    public void initialize() {
        ecgData.clear();
        ppgData.clear();
        deltaPpg.clear();
        length = 0;
        qrsNum = 0;
        qrsPos = 0;
        ppg_1 = 0;
        ppg_2 = 0;
    }

    private int getDeltaPpg(int ppg) {
        int delta = ppg - ppg_2;
        ppg_2 = ppg_1;
        ppg_1 = ppg;
        return delta;
    }
}
