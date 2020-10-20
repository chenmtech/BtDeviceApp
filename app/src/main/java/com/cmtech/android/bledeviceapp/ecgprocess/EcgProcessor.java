package com.cmtech.android.bledeviceapp.ecgprocess;

import com.cmtech.android.bledeviceapp.ecgprocess.qrsdetbyhamilton.QrsDetectorWithQRSInfo;
import com.cmtech.dsp.filter.FIRFilter;
import com.cmtech.dsp.filter.IIRFilter;
import com.cmtech.dsp.filter.design.FIRDesigner;
import com.cmtech.dsp.filter.design.FilterType;
import com.cmtech.dsp.filter.design.NotchDesigner;
import com.cmtech.dsp.filter.design.WinType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EcgProcessor {
	private static final int NUM_BEFORE_R = 99;
	private static final int NUM_AFTER_R = 150;

	private Map<String, Object> result = new HashMap<>();
	private List<List<Float>> segEcgData;
	
	public EcgProcessor() {
	}
	
	public Map<String, Object> getResult() {
		return result;
	}
	
	public JSONObject getResultJson() {
	    try {
            if (result.isEmpty()) return null;
            JSONObject json = new JSONObject();
            for (Entry<String, Object> ent : result.entrySet()) {
                json.put(ent.getKey(), ent.getValue());
            }
            return json;
        } catch (JSONException ex) {
	        ex.printStackTrace();
        }
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Double> getRRIntervalInMs() {
		if(result.isEmpty()) return null;
		List<Long> RPos = (List<Long>) result.get("RPos");
		int sampleRate = (int) result.get("SampleRate");
		List<Double> RR = new ArrayList<>();
        for(int i = 1; i < RPos.size(); i++) {
        	double R1 = RPos.get(i-1)*1000.0/sampleRate;
        	double R2 = RPos.get(i)*1000.0/sampleRate;
        	RR.add(R2-R1);
        }
        return RR;
	}

	public int getAverageHr() {
	    List<Double> RR = getRRIntervalInMs();
	    if(RR == null) return 0;
        double aveRR = MathUtil.doubleAve(RR);
        return (int)(60000/aveRR);
    }

	@SuppressWarnings("unchecked")
	public void process(List<Short> ecgData, int sampleRate) {
		if(ecgData == null || ecgData.isEmpty()) {
			return;
		}
		
		// do filtering
		//IIRFilter dcBlocker = DCBlockDesigner.design(1, sampleRate);
		IIRFilter notch50 = designNotch(50, sampleRate);
		FIRFilter lpFilter = designLpFilter(sampleRate);
		List<Short> afterFilter = new ArrayList<Short>();
		for(Short d : ecgData) {
			afterFilter.add((short)Math.round(lpFilter.filter(notch50.filter(d))));
		}
		ecgData = afterFilter;
		
		// detect the QRS waves and RR interval
		Map<String, Object> qrsAndRRInterval = getQrsPosAndRRInterval(ecgData, sampleRate);
		if(qrsAndRRInterval == null) {
		    return;
        }
		
		// detect the R wave position and the begin pos of each beat
		Map<String, Object> rPosAndBeatBegin = getRPosAndBeatBegin(ecgData, qrsAndRRInterval);

		// normalize the Ecg data per beat
		List<Long> beatBegin = (List<Long>)rPosAndBeatBegin.get("BeatBegin");
		List<Float> normalizedEcgData = normalizeEcgData(ecgData, beatBegin);
		
		// cut the ecg data into the segments
		List<Long> rPos =  (List<Long>)rPosAndBeatBegin.get("RPos");
		segEcgData = getSegEcgData(normalizedEcgData, rPos, beatBegin);
		
		result = rPosAndBeatBegin;
		result.put("QrsPos", qrsAndRRInterval.get("QrsPos"));
		result.put("EcgData", ecgData);
		result.put("SegEcgData", segEcgData);
		result.put("SampleRate", sampleRate);
	}	
	
	public String getSegEcgDataString() {
		if(segEcgData == null || segEcgData.isEmpty()) return "";
		
		StringBuilder builder = new StringBuilder();
		
		for(List<Float> seg : segEcgData) {
			for(Float d : seg) {
				builder.append(String.format("%.3f", d));
				builder.append(' ');
			}
			builder.append("\r\n");
		}
		return builder.toString();
	}
	
	private List<List<Float>> getSegEcgData(List<Float> normalizedEcgData, List<Long> rPos, List<Long> beatBeginPos) {
		List<List<Float>> segEcgData = new ArrayList<>();
		
		for(int i = 0; i < beatBeginPos.size()-1; i++) {
			List<Float> oneBeat = new ArrayList<>();
			
			long begin = beatBeginPos.get(i);
			long end = beatBeginPos.get(i+1);
			long r = rPos.get(i);
			long fillBefore = 0;
			long fillAfter = 0;
			float first = 0.0f;
			float last = 0.0f;
			if(r - begin >= NUM_BEFORE_R) {
				begin = r - NUM_BEFORE_R;
			} else {
				fillBefore = NUM_BEFORE_R - (r-begin);
				first = normalizedEcgData.get((int)begin);
			}
			if(end-r > NUM_AFTER_R) {
				end = r + NUM_AFTER_R + 1;
			} else {
				fillAfter = NUM_AFTER_R - (end-r) + 1;
				last = normalizedEcgData.get((int)(end-1));
			}
			for(int j = 0; j < fillBefore; j++) {
				oneBeat.add(first);
			}
			for(long pos = begin; pos < end; pos++) {
				oneBeat.add(normalizedEcgData.get((int)pos));
			}
			for(int j = 0; j < fillAfter; j++) {
				oneBeat.add(last);
			}
			segEcgData.add(oneBeat);
			//System.out.println(oneBeat.size());
		}
		return segEcgData;
	}
	
	private Map<String, Object> getQrsPosAndRRInterval(List<Short> ecgData, int sampleRate) {
		QrsDetectorWithQRSInfo detector = new QrsDetectorWithQRSInfo(sampleRate);
		int n = 0;
		for(Short datum : ecgData) {
			detector.outputRRInterval((int)datum);
			n++;
			if(detector.firstPeakFound()) break;
		}
		for(Short datum : ecgData) {
			detector.outputRRInterval((int)datum);
		}
		
		List<Long> qrsPos = detector.getQrsPositions();
		List<Integer> rrInterval = detector.getRrIntervals();

		if(qrsPos.size() < 6)
		    return null;

		qrsPos.remove(0);
		rrInterval.remove(0);
		for(int i = 0; i < qrsPos.size(); i++) {
			long p = qrsPos.get(i)-n;
			if(p < 0) p = 0;
			qrsPos.set(i, p);
		}
		
		Map<String, Object> map = new HashMap<>();
		map.put("QrsPos", qrsPos);
		map.put("RRInterval", rrInterval);		
		
		return map;
	}
	
	private static Map<String, Object> getRPosAndBeatBegin(List<Short> ecgData, Map<String, Object> qrsAndRRInterval) {
		return RWaveDetecter.findRPosAndBeatBegin(ecgData, qrsAndRRInterval);
	}
	
	private List<Float> normalizeEcgData(List<Short> ecgData, List<Long> beatBeginPos) {
		List<Float> normalized = new ArrayList<>();
		
		for(Short d : ecgData) {
			normalized.add((float)d);
		}
		
		List<Float> oneBeat =  new ArrayList<>();
		for(int i = 0; i < beatBeginPos.size()-1; i++) {
			for(long pos = beatBeginPos.get(i); pos < beatBeginPos.get(i+1); pos++) {
				oneBeat.add(normalized.get((int)pos));
			}
			
			float ave = MathUtil.floatAve(oneBeat);
			float std = MathUtil.floatStd(oneBeat);
			
			for(int ii = 0; ii < oneBeat.size(); ii++) {
				oneBeat.set(ii, (oneBeat.get(ii)-ave)/std);
			}
			
			for(long pos = beatBeginPos.get(i),  ii = 0; pos < beatBeginPos.get(i+1); pos++, ii++) {
				normalized.set((int)pos, oneBeat.get((int)ii));
			}
			
			oneBeat.clear();
		}
		return normalized;
	}

	private FIRFilter designLpFilter(int sampleRate) {
		double[] wp = {2*Math.PI*65/sampleRate};
		double[] ws = {2*Math.PI*85/sampleRate};
		double Rp = 1;
		double As = 50;
		FilterType fType = FilterType.LOWPASS;
		WinType wType = WinType.HAMMING;
		
		FIRFilter filter = FIRDesigner.design(wp, ws, Rp, As, fType, wType);
		return filter;
	}
	
	private IIRFilter designNotch(int f0, int sampleRate) {
		IIRFilter filter = NotchDesigner.design(f0, 2, sampleRate);
		return filter;
	}
	
}
