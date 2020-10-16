package com.cmtech.android.bledeviceapp.ecgprocess.afdetector.AFEvidence;

import java.util.List;

public class MyAFEvidence {
	private static final int MIN_MS = -600;
	private static final int MAX_MS = 600;
	private static final int BIN_SIZE = 40;
	private static final int MAX_BIN = (MAX_MS - MIN_MS)/BIN_SIZE-1;

	private static final int THRESHOLD = 15;

	public static final int NON_AF = 0;
    public static final int AF = 1;
    public static final int UNDETERMIN = 2;

	private int classifyResult = UNDETERMIN;
	private int afe = 0;

    private int OriginCount = 0;
    private final MyHistogram hist;

    private static MyAFEvidence instance;

    private MyAFEvidence() {
        hist = new MyHistogram();
    }

    public static MyAFEvidence getInstance(){
        if(instance == null){
            synchronized (MyAFEvidence.class){
                if(instance == null){
                    instance = new MyAFEvidence();
                }
            }
        }
        return instance;
    }
	
	public void process(List<Double> RR) {
        if(RR.size() < 10) {
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

	public int getClassifyResult() {
        return classifyResult;
    }
	
	private void clear() {
		OriginCount = 0;
		hist.clear();
	}
	
	private void addPoint(double xMs, double yMs) {
		// 在原点，OriginCount计数
		if(Math.abs(xMs) < 20 && Math.abs(yMs) < 20)
			OriginCount++;
		
		// 异常点，丢弃
		if(isOutLier(xMs, yMs)) return;
		
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
		return hist.getIrregularityEvidence() - OriginCount -2*hist.getPACEvidence();
	}
	
	private boolean isOutLier(double xMs, double yMs) {
		return (xMs >= 1500 || yMs >= 1500);
	}
	
	
}
