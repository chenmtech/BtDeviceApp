package com.cmtech.android.bledeviceapp.ecgalgorithm.afdetector.AFEvidence;


public class MyBin {
	private int x;
	private int y;
	private int count = 0;
	
	public MyBin(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean within(int x, int y) {
		return (this.x == x && this.y == y);
	}
	
	public int getPointCount() {
		return count;
	}
	
	public int getBinCount() {
		return (count == 0) ? 0 : 1;
	}
	
	public void addCount() {
		count++;
	}
	
	public void clearCount() {
		count = 0;
	}

	@Override
	public boolean equals(Object otherObject) {
		if(this == otherObject) return true;
		if(otherObject == null) return false;
		if(getClass() != otherObject.getClass()) return false;
		MyBin other = (MyBin)otherObject;
		return  (x == other.x && y == other.y);
	}

	@Override
	public int hashCode() {
		return x + 17*y;
	}	
	
}
