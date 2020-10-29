package com.cmtech.android.bledeviceapp.dataproc.ecgalgorithm.afdetector.AFEvidence;

import java.util.LinkedList;
import java.util.List;

public class MySeg {
	private List<MyBin> bins = new LinkedList<>();
	
	public MySeg() {
	}
	
	public void addBin(MyBin bin) {
		if(!bins.contains(bin)) {
			bins.add(bin);
		}
	}
	
	public void addBin(int x, int y) {
		addBin(new MyBin(x, y));
	}
	
	public boolean contains(int x, int y) {
		for(MyBin bin : bins) {
			if(bin.within(x, y)) return true;
		}
		return false;
	}
	
	public boolean addPoint(int x, int y) {
		for(MyBin bin : bins) {
			if(bin.within(x, y)) {
				bin.addCount();
				return true;
			}
		}
		return false;
	}
	
	public int getBinCount() {
		int sum = 0;
		for(MyBin bin : bins) {
			sum += bin.getBinCount();
		}
		return sum;
	}
	
	public int getPointCount() {
		int sum = 0;
		for(MyBin bin : bins) {
			sum += bin.getPointCount();
		}
		return sum;
	}
	
	public void clear() {
		for(MyBin bin : bins) {
			bin.clearCount();
		}
	}

}
