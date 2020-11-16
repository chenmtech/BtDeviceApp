package com.cmtech.android.bledeviceapp.dataproc.ecgproc.afdetector.AFEvidence;

import com.cmtech.android.bledeviceapp.dataproc.ecgproc.afdetector.IAFDetector;

import java.util.List;

public class MyAFEvidence implements IAFDetector {
    private static final int MIN_MS = -600;
    private static final int MAX_MS = 600;
    private static final int BIN_SIZE = 40;
    private static final int MAX_BIN = (MAX_MS - MIN_MS)/BIN_SIZE-1;

    private static final int MIN_RR_NUM = 10;

    private static final int THRESHOLD = 7;


    private int classifyResult = UNDETERMIN;
    private int afe = 0;

    private int originCount = 0;
    private final MyHistogram hist;

    public MyAFEvidence() {
        hist = new MyHistogram();
    }

    public void process(List<Double> RR) {
        if(RR == null || RR.size() < MIN_RR_NUM) {
            afe = 0;
            classifyResult = UNDETERMIN;
        } else {
            for (int i = 1; i < RR.size() - 1; i++) {
                addPoint(RR.get(i) - RR.get(i - 1), RR.get(i + 1) - RR.get(i));
            }
            afe = calculateAFEvidence();

            if (afe >= THRESHOLD) {
                classifyResult = AF;
            } else {
                classifyResult = NON_AF;
            }
        }
        clear();
    }

    public int getAFEvidence() {
        return afe;
    }

    @Override
    public int getClassifyResult() {
        return classifyResult;
    }

    private void clear() {
        originCount = 0;
        hist.clear();
    }

    private void addPoint(double xMs, double yMs) {
        // 异常点，丢弃
        if(isOutLier(xMs, yMs)) return;

        // 在原点，originCount计数
        if(Math.abs(xMs) < 20 && Math.abs(yMs) < 20)
            originCount++;

        // 添加到直方图
        xMs = (xMs - MIN_MS) / BIN_SIZE;
        int x;
        if(xMs < 0)
            x = 0;
        else if(xMs > MAX_BIN) {
            x = MAX_BIN;
        } else {
            x = (int)xMs;
        }

        yMs = (yMs - MIN_MS) / BIN_SIZE;
        int y;
        if(yMs < 0)
            y = 0;
        else if(yMs > MAX_BIN) {
            y = MAX_BIN;
        } else {
            y = (int)yMs;
        }

        hist.addPoint(x, y);

    }

    private int calculateAFEvidence() {
        return hist.getIrregularityEvidence() - originCount -2*hist.getPACEvidence();
    }

    private boolean isOutLier(double xMs, double yMs) {
        return (xMs >= 1500 || yMs >= 1500);
    }

    public void printHistogram() {
        for(int i = 0; i <= 29; i++) {
            for(int j = 0; j <= 29; j++) {
                System.out.printf("%7s", hist.getBelongSegLabel(i, j));
            }
            System.out.println();
        }
    }
}
