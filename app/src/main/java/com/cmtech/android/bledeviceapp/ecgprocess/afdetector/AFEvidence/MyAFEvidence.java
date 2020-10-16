package com.cmtech.android.bledeviceapp.ecgprocess.afdetector.AFEvidence;

import java.util.List;

public class MyAFEvidence {
	private static int MIN_MS = -600;
	private static int MAX_MS = 600;
	private static int BIN_SIZE = 40;
	private static int MAX_BIN = (MAX_MS - MIN_MS)/BIN_SIZE-1;
	
	private int OriginCount = 0;
	
	private MyHistogram hist = new MyHistogram();

	public MyAFEvidence() {
		
	}
	
	public void process(List<Double> RR) {
		 for(int i = 1; i < RR.size()-1; i++) {
         	addPoint(RR.get(i)-RR.get(i-1), RR.get(i+1)-RR.get(i));
         }
	}
	
	public void clear() {
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
	
	public int getAFEvidence() {
		System.out.println("Irreg:" + hist.getIrregularityEvidence() + "Origin:" + OriginCount + "PACE:" + hist.getPACEvidence());
		return hist.getIrregularityEvidence() - OriginCount -2*hist.getPACEvidence();
	}
	
	public void printHistogram() {
		for(int i = 0; i <= 29; i++) {
			for(int j = 0; j <= 29; j++) {
				System.out.printf("%7s", hist.getBelongSegLabel(i, j));
			}
			System.out.println();
		}
	}
	
	private boolean isOutLier(double xMs, double yMs) {
		return (xMs >= 1500 || yMs >= 1500);
	}
	
	
}
