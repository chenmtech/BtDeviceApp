package com.cmtech.android.bledeviceapp.dataproc.ecgproc.preproc;

import android.util.Pair;

import com.cmtech.android.bledeviceapp.util.MathUtil;
import com.cmtech.dsp.filter.FIRFilter;
import com.cmtech.dsp.filter.IDigitalFilter;
import com.cmtech.dsp.filter.structure.StructType;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RWaveDetecter {
	private static final int QRS_WIDTH_MS = 300; // unit:ms
	private static final int SAMPLE_RATE = 360; // sample rate
	private static final int QRS_WIDTH = QRS_WIDTH_MS*SAMPLE_RATE/1000;
	private static final int QRS_HALF_WIDTH = QRS_WIDTH/2;
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> findRWaveAndBeatBeginPos(List<Short> ecgData, Map<String, Object> qrsAndRRInterval) {
		List<Long> qrsPos = (List<Long>) qrsAndRRInterval.get("QrsPos");
		List<Integer> rrInterval = (List<Integer>) qrsAndRRInterval.get("RRInterval");
		
		List<Float> d2= diffFilter(ecgData);
		List<Long> rPos = new ArrayList<>();
		List<Float> normData = new ArrayList<>();
		for(Short d : ecgData) {
			normData.add((float)d);
		}
		for(int i = 1; i < rrInterval.size()-1; i++) {
			long qrs = qrsPos.get(i+1);
			long qrsBegin = qrs - QRS_HALF_WIDTH;
			long qrsEnd = qrs + QRS_HALF_WIDTH;
			long beatBegin = qrs - Math.round(rrInterval.get(i)*2.0/5);
			long beatEnd = qrs + Math.round(rrInterval.get(i+1)*3.0/5);
			
			List<Short> temp = ecgData.subList((int)beatBegin, (int)beatEnd);
			float ave = MathUtil.shortAve(temp);
			float std = MathUtil.shortStd(temp);
			
			long ii = beatBegin;
			for(Short d : temp) {
				normData.set((int) ii, (d - ave)/std);
				ii++;
			}
			
			Pair<Integer, Float> rlt = MathUtil.floatMin(normData.subList((int)qrsBegin, (int)qrsEnd));
			float minV = rlt.second;
			int minI = rlt.first;
			
			rlt = MathUtil.floatMax(normData.subList((int)qrsBegin, (int)qrsEnd));
			float maxV = rlt.second;
			int maxI = rlt.first;
			
			if(Math.abs(maxV) > 2*Math.abs(minV)/3) {
				rPos.add(qrsBegin + maxI);
			} else {
				rlt = MathUtil.floatMin(d2.subList((int)qrsBegin, (int)qrsEnd));
				minV = rlt.second;
				minI = rlt.first;
				
				rlt = MathUtil.floatMax(d2.subList((int)qrsBegin, (int)qrsEnd));
				maxV = rlt.second;
				maxI = rlt.first;
				
				if(Math.abs(maxV) > Math.abs(minV)) {
					rPos.add(qrsBegin + maxI - 2);
				} else {
					rPos.add(qrsBegin + minI - 2);
				}
			}
			
			long rBegin = rPos.get(i-1) - 5;
			long rEnd = rPos.get(i-1) + 5;
			List<Float> tmp = new ArrayList<>();
			for(long j = rBegin; j <= rEnd; j++) {
				tmp.add((float)Math.abs(ecgData.get((int)j)));
			}
			
			rlt = MathUtil.floatMax(tmp);
			maxV = rlt.second;
			maxI = rlt.first;
			rPos.set(i-1, rBegin + maxI);			
		}	
		
		List<Long> beatBegin = new ArrayList<>();
		for(int i = 1; i < rrInterval.size()-1; i++) {
			beatBegin.add(rPos.get(i-1) - Math.round(rrInterval.get(i)*2.0/5));
		}

		if(beatBegin.get(0) < 0) {
            beatBegin.remove(0);
            rPos.remove(0);
        }
		
		Map<String, Object> map = new HashMap<>();
		map.put("RPos", rPos);
		map.put("BeatBegin", beatBegin);
		return map;
	}
	
	private static List<Float> diffFilter(List<Short> data) {
		double[] b = {1,0,-2,0,1};
		IDigitalFilter diffFilter = new FIRFilter(b);
		diffFilter.createStructure(StructType.FIR_LPF);
		
		List<Float> d2Data = new ArrayList<>();
		for(Short d : data) {
			d2Data.add((float)diffFilter.filter(d));
		}
		
		return d2Data;
	}
}
