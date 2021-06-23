package com.cmtech.android.bledevice.ptt.model;

import android.util.Pair;

import com.cmtech.android.bledeviceapp.dataproc.ecgproc.preproc.qrsdetbyhamilton.QrsDetector;
import com.cmtech.android.bledeviceapp.util.MathUtil;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;

/**
 * PttDetector: 处理同步ECG和PPG信号，获取PTT值
 */
public class PttDetector {
    private static final int QRS_WIDTH_MS = 200; // QRS波宽度，单位:毫秒
    private static final int DELTA_PTT = 0;
    private static final int FOOT_PTT = 1;

    private final int sampleRate; // 采样率，Hz
    private final int maxLength; // 数据缓存允许最大长度，如果超过该长度，则认为信号长时间没有出现QRS波，信号将被清空
    private final int halfQrsWidth; // QRS波一半宽度的样本数，用于确定QRS波范围，以精确定位R波峰位置
    private final List<Integer> ecgData = new ArrayList<>(); // ECG信号缓存
    private final List<Integer> ppgData = new ArrayList<>(); // PPG信号缓存
    private final List<Integer> deltaPpg = new ArrayList<>(); // PPG一阶导数缓存
    private int length = 0; // 当前信号缓存数据长度
    private int qrsNum = 0; // 当前捕捉到的QRS波个数
    private int qrsPos = 0; // 第二个QRS波或R波峰位置
    private int ppg_1 = 0; // 用于中心差分求PPG一阶导数
    private int ppg_2 = 0; // 用于中心差分求PPG一阶导数

    private final QrsDetector qrsDetector; // QRS波位置检测器

    public PttDetector(int sampleRate) {
        this.sampleRate = sampleRate;

        maxLength = 4*sampleRate;
        halfQrsWidth = QRS_WIDTH_MS*sampleRate/1000/2;

        qrsDetector = new QrsDetector(sampleRate);
        initialize();
    }

    // 处理同步的ECG和PPG信号，返回检测到的R波峰时刻到PPG最大导数时刻的PTT
    public int findDeltaPtt(int ecg, int ppg) {
        return findPtt(ecg, ppg, DELTA_PTT);
    }

    // 处理同步的ECG和PPG信号，返回检测到的R波峰时刻到PPG波谷时刻的PTT
    public int findFootPtt(int ecg, int ppg) {
        return findPtt(ecg, ppg, FOOT_PTT);
    }

    /**
     * 处理同步的ECG和PPG信号，返回检测到的R波峰时刻到PPG最大导数时刻的PTT
     * @param ecg ECG信号
     * @param ppg PPG信号
     * @param whichPtt 指定求哪个PTT
     * @return 如果检测到PTT，则返回PTT值（单位：ms),否则返回0
     */
    private int findPtt(int ecg, int ppg, int whichPtt) {
        // 数据长度超过最大长度限制，初始化
        if(length >= maxLength) {
            initialize();
            return 0;
        }

        // 获取QRS波出现位置离当前时刻的延时样本数，以定位QRS波
        int delay = qrsDetector.detectQrs(ecg);

        // 如果是QRS波
        if(delay != 0) {
            qrsNum++;
        }

        // 如果已经有QRS波出现，保存信号
        if(qrsNum > 0) {
            ecgData.add(ecg);
            ppgData.add(ppg);
            deltaPpg.add(getDeltaPpg(ppg));

            length++;
        }

        // 如果发现QRS波
        if(delay != 0) {
            // 如果发现第二个QRS波，记录QRS波峰位置
            if (qrsNum == 2) {
                qrsPos = Math.max(0, length - delay + 1);
                return 0;
            }

            // 如果发现第三个QRS波，开始处理第二个QRS波，并计算第一个QRS波与第二个QRS波之间的PTT
            if (qrsNum == 3) {
                // 精确定位R波峰位置
                ViseLog.e("QRS Position:" + qrsPos);
                // QRS波的搜索范围
                int minPos = Math.max(qrsPos - halfQrsWidth, 0);
                int maxPos = Math.min(qrsPos + halfQrsWidth, length);
                Pair<Integer, Integer> rlt = MathUtil.intMax(ecgData, minPos, maxPos); // 假设R波峰为QRS波的最大值，搜索R波峰位置
                qrsPos = rlt.first; // 得到R波峰精确位置
                ViseLog.e("R Position:" + qrsPos);

                // 获取第一个R波和第二个R波之间的PPG最大导数位置
                /*List<Integer> ecgTmp = ecgData.subList(0, qrsPos);
                List<Integer> ppgTmp = ppgData.subList(0, qrsPos);
                List<Integer> deltaPpgTmp = deltaPpg.subList(0, qrsPos);
                ViseLog.e("ECG:" + ecgTmp);
                ViseLog.e("PPG:" + ppgTmp);
                ViseLog.e("Delta PPG:" + deltaPpgTmp);*/
                rlt = MathUtil.intMax(deltaPpg, 0, qrsPos);
                int deltaPpgPos = Math.max(0, rlt.first - 1); // 中心差分的deltaPPG有一位的延时，所以要减一
                double maxDelta = rlt.second;
                ViseLog.e("Delta PPG Max Position:" + deltaPpgPos);

                int ptt = 0;
                // 如果是DELTA_PTT，将PPG一阶导数最大值位置换算为PTT
                if(whichPtt == DELTA_PTT) {
                    ptt = (int) Math.round(deltaPpgPos * 1000.0 / sampleRate);
                    ViseLog.e("PTT:" + ptt);
                }
                // 否则是FOOT_PTT，继续寻找foot PPG位置，并将该位置换算为PTT
                else {
                    rlt = MathUtil.intMin(ppgData, 0, deltaPpgPos);
                    int footPpg = rlt.second;
                    int footPpgPos = (int)Math.round((footPpg - ppgData.get(deltaPpgPos))/maxDelta + deltaPpgPos);
                    ptt = (int) Math.round(footPpgPos * 1000.0 / sampleRate);
                    ViseLog.e("PTT:" + ptt);
                }

                // 把第二个R波之前的数据都删除掉，使得第二波R波变为第一个，第三个变为第二个
                ecgData.subList(0, qrsPos).clear();
                ppgData.subList(0, qrsPos).clear();
                deltaPpg.subList(0, qrsPos).clear();

                // 更新变量
                length = ecgData.size();
                qrsPos = length - delay + 1; // 更新第二个QRS波位置
                qrsNum--;

                return ptt;
            }
        }

        return 0;
    }

    // 初始化
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

    // 用中心差分计算PPG信号的一阶导数
    private int getDeltaPpg(int ppg) {
        int delta = ppg - ppg_2;
        ppg_2 = ppg_1;
        ppg_1 = ppg;
        return delta;
    }
}
