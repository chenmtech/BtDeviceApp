package com.cmtech.android.bledeviceapp.dataproc.ecgalgorithm.qrsdetbyhamilton;

import java.util.ArrayList;
import java.util.List;

public class QrsDetectorWithQRSInfo extends QrsDetector {
	private List<Long> qrsPositions = new ArrayList<>();
	private List<Integer> rrIntervals = new ArrayList<>();
	
	public QrsDetectorWithQRSInfo(int sampleRate) {
		super(sampleRate);
		// TODO Auto-generated constructor stub
	}

	// input one datum
	// return two R wave interval if a new R wave is detected after the old R wave was detected, or 0
	@Override
	public int outputRRInterval(int datum) {
		int RRInterval = 0;
		
		int delay = detectQrs(datum);
		if(delay != 0) {
			if(firstPeak) {
				firstPeak = false;
				qrsPositions.add((long) (RRCount-delay));
				//System.out.println(""+delay);
			} else {
				RRInterval = RRCount-delay+1;
				rrIntervals.add(RRInterval);
				qrsPositions.add(qrsPositions.get(qrsPositions.size()-1) + RRInterval);
			}
			RRCount = delay;
		} else {
			RRCount++;
		}
		return RRInterval;
	}

	public List<Long> getQrsPositions() {
		return qrsPositions;
	}

	public List<Integer> getRrIntervals() {
		return rrIntervals;
	}
	
	
}
