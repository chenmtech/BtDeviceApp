package com.cmtech.android.bledeviceapp.ecgalgorithm.afdetector.AFEvidence;

public class MyHistogram {
	private MySeg[] segs = new MySeg[13];
	
	public MyHistogram() {
		createSegs();
	}
	
	public void addPoint(int x, int y) {
		for(MySeg seg : segs) {
			if(seg.addPoint(x, y)) {
				return;
			}
		}
	}
	
	public boolean contains(int x, int y) {
		for(MySeg seg : segs) {
			if(seg.contains(x, y)) {
				return true;
			}
		}
		return false;
	}
	
	public int getSegBinCount(int num) {
		return segs[num].getBinCount();
	}
	
	public int getSegPointCount(int num) {
		return segs[num].getPointCount();
	}

	public int getIrregularityEvidence() {
		int sum = 0;
		for(int i = 1; i <= 12; i++) {
			sum += segs[i].getBinCount();
		}
		return sum;
	}
	
	public int getPACEvidence() {
		int sum = 0; 
		for(int i = 1; i <= 4; i++) {
			sum += (segs[i].getPointCount() - segs[i].getBinCount());
		}
		int[] arr = {5,6,10};
		for(int i = 0; i < arr.length; i++)
			sum += (segs[arr[i]].getPointCount() - segs[arr[i]].getBinCount());
		
		int[] arr1 = {7,8,12};
		for(int i = 0; i < arr.length; i++)
			sum -= (segs[arr1[i]].getPointCount() - segs[arr1[i]].getBinCount());
		
		return sum;
	}
	
	public void clear() {
		for(MySeg seg : segs) {
			seg.clear();
		}
	}
	
	public String getBelongSegLabel(int x, int y) {
		for(int i = 0; i < segs.length; i++) {
			if(segs[i].contains(x, y)) {
				return "s"+i;
			}
		}
		return "s?";
	}
	
	private void createSegs() {
		for(int i = 0; i < segs.length; i++) {
			segs[i] = new MySeg();
		}
		createSeg0();
		createSeg12_11_10_9();
		createSeg5_7_6_8();
		createSeg2_1_3_4();
	}
	
	private void createSeg0() {
		MySeg seg = segs[0];
		seg.addBin(13,14);
		seg.addBin(13,15);
		for(int i = 14; i <= 15; i++) 
			for(int j = 13; j <= 16; j++)
				seg.addBin(i, j);
		seg.addBin(16,14);
		seg.addBin(16,15);
	}
	
	private void createSeg12_11_10_9() {
		MySeg seg12 = segs[12];
		MySeg seg11 = segs[11];
		MySeg seg10 = segs[10];
		MySeg seg9 = segs[9];
		
		for(int i = 15; i <= 29; i++) {
			for(int j = 15; j <= 29; j++) {
				if(contains(i, j)) continue;
				
				if(i >= j-2 && i <= j+2) {
					seg12.addBin(i, j);
					seg11.addBin(i, 29-j);
					seg10.addBin(29-i, 29-j);
					seg9.addBin(29-i, j);
				}
			}
		}		
	}
	
	private void createSeg5_7_6_8() {
		MySeg seg5 = segs[5];
		MySeg seg7 = segs[7];
		MySeg seg6 = segs[6];
		MySeg seg8 = segs[8];
		
		for(int i = 0; i <= 14; i++) {
			for(int j = 13; j <= 16; j++) {
				if(contains(i, j)) continue;
				
				seg5.addBin(i, j);
				seg7.addBin(29-i, j);
				seg6.addBin(29-j, i);
				seg8.addBin(29-j, 29-i);				
			}
		}		
	}
	
	private void createSeg2_1_3_4() {
		MySeg seg2 = segs[2];
		MySeg seg1 = segs[1];
		MySeg seg3 = segs[3];
		MySeg seg4 = segs[4];
		
		for(int i = 0; i <= 12; i++) {
			for(int j = 0; j <= 12; j++) {
				if(segs[10].contains(i, j)) continue;
				
				seg2.addBin(i, j);
				seg1.addBin(i, 29-j);
				seg3.addBin(29-i, j);
				seg4.addBin(29-i, 29-j);				
			}
		}		
	}
	
}
