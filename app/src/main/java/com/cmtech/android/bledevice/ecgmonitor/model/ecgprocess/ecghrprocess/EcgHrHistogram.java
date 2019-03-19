package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.EcgSignalProcessor.INVALID_HR;

/**
 * EcgHrHistogram: 心率直方图类
 * Created by Chenm, 2018-12-07
 */

public class EcgHrHistogram implements IEcgHrProcessor {
    private final static int HISTOGRAM_LEN = 21; // 心率从0~200以上，每隔10bpm为一个统计单位，高于200bpm的都统计为200，因此统计直方图需要21个值

    private int[] histgram = new int[HISTOGRAM_LEN]; // 心率直方图缓存

    public EcgHrHistogram() {
        for(int i = 0; i < HISTOGRAM_LEN; i++) {
            histgram[i] = 0;
        }
    }

    // 重置直方图
    public void clear() {
        for(int i = 0; i < HISTOGRAM_LEN; i++) {
            histgram[i] = 0;
        }
    }

    // 获取直方图
    public int[] getHistgram() {
        return histgram;
    }

    // 设置直方图
    public void setHistgram(int[] histgram) {
        if(histgram != null)
            this.histgram = histgram;
    }

    // 获取归一化直方图
    public double[] getNormHistogram() {
        long sum = getTotalBeats();
        double[] norm = new double[HISTOGRAM_LEN];
        for(int i = 0; i < HISTOGRAM_LEN; i++) {
            norm[i] = ((double)histgram[i])/sum;
        }
        return norm;
    }

    // 获取总的心跳次数
    public long getTotalBeats() {
        long sum = 0;
        for(int num : histgram) {
            sum += num;
        }
        return sum;
    }

    @Override
    public void process(int hr) {
        if(hr != INVALID_HR) {
            int i = (hr >= 200) ? 20 : hr/10;
            histgram[i]++;
        }
    }
}
