package com.cmtech.android.bledeviceapp.dataproc.ecgproc.preproc;

import com.cmtech.android.bledeviceapp.dataproc.ecgproc.preproc.qrsdetbyhamilton.QrsDetectorWithQRSInfo;
import com.cmtech.android.bledeviceapp.util.MathUtil;
import com.cmtech.dsp.filter.FIRFilter;
import com.cmtech.dsp.filter.IIRFilter;
import com.cmtech.dsp.filter.design.FIRDesigner;
import com.cmtech.dsp.filter.design.FilterType;
import com.cmtech.dsp.filter.design.NotchDesigner;
import com.cmtech.dsp.filter.design.WinType;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_HR;

public class EcgPreProcessor {
	private static final int NUM_BEFORE_R = 99;
	private static final int NUM_AFTER_R = 150;

	private int sampleRate;
	private Map<String, Object> processResult = new HashMap<>();
	
	public EcgPreProcessor() {
	}
	
	public Map<String, Object> getProcessResult() {
		return processResult;
	}
	
	public JSONObject getResultJson() {
	    try {
            if (processResult.isEmpty()) return null;
            JSONObject json = new JSONObject();
            for (Entry<String, Object> ent : processResult.entrySet()) {
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
		if(processResult.isEmpty()) return null;
		List<Long> RPos = (List<Long>) processResult.get("RPos");
		int sampleRate = (int) processResult.get("SampleRate");
		List<Double> RRList = new ArrayList<>();
        for(int i = 1; i < RPos.size(); i++) {
            double rr = (RPos.get(i)-RPos.get(i-1))*1000.0/sampleRate;
        	RRList.add(rr);
        }
        return RRList;
	}

	public int getAverageHr() {
	    List<Double> RR = getRRIntervalInMs();
	    if(RR == null) return INVALID_HR;
        double aveRR = MathUtil.doubleAve(RR);
        return (int)Math.round(60000/aveRR);
    }

    public double getHrStdInMs() {
        List<Double> RR = getRRIntervalInMs();
        if(RR == null) return 0;
        return MathUtil.doubleStd(RR);
    }

	@SuppressWarnings("unchecked")
	public void process(List<Short> ecgData, int sampleRate) {
	    processResult.clear();

		if(ecgData == null || ecgData.isEmpty()) {
			return;
		}

		this.sampleRate = sampleRate;
		
		// 滤波
		//IIRFilter dcBlocker = DCBlockDesigner.design(1, sampleRate);
		IIRFilter notch50 = designNotch(50);
		FIRFilter lpFilter = designLpFilter();
		List<Short> afterFilter = new ArrayList<Short>();
		for(Short d : ecgData) {
			afterFilter.add((short)Math.round(lpFilter.filter(notch50.filter(d))));
		}
		ecgData = afterFilter;
		
		// 检测QRS波和RR间隔
		Map<String, Object> qrsAndRRInterval = getQrsPosAndRRInterval(ecgData);
		if(qrsAndRRInterval == null || qrsAndRRInterval.isEmpty()) {
		    return;
        }
        ViseLog.e(qrsAndRRInterval);
		
		// 检测R波和每次心跳开始位置
		Map<String, Object> rPosAndBeatBegin = getRWaveAndBeatBeginPos(ecgData, qrsAndRRInterval);
        ViseLog.e(rPosAndBeatBegin);

		// 对ECG分割为单次心跳数据，并归一化
		List<Long> beatBegin = (List<Long>)rPosAndBeatBegin.get("BeatBegin");
		List<Float> normalizedEcgData = normalizeEcgData(ecgData, beatBegin);
		List<Long> rPos =  (List<Long>)rPosAndBeatBegin.get("RPos");
        List<List<Float>> segEcgData = segmentEcgData(normalizedEcgData, rPos, beatBegin);

		// 打包处理结果
		processResult = rPosAndBeatBegin;
		processResult.put("QrsPos", qrsAndRRInterval.get("QrsPos"));
		processResult.put("EcgData", ecgData);
		processResult.put("SegEcgData", segEcgData);
		processResult.put("SampleRate", sampleRate);
	}	
	
	public String getSegEcgDataString() {
		if(processResult.isEmpty()) return "";
        List<List<Float>> segEcgData = (List<List<Float>>) processResult.get("SegEcgData");
		if(segEcgData == null || segEcgData.isEmpty()) return "";
		
		StringBuilder builder = new StringBuilder();
		
		for(List<Float> seg : segEcgData) {
			for(Float d : seg) {
				builder.append(String.format(Locale.getDefault(), "%.3f", d));
				builder.append(' ');
			}
			builder.append("\r\n");
		}
		return builder.toString();
	}
	
	private static List<List<Float>> segmentEcgData(List<Float> normalizedEcgData, List<Long> rPos, List<Long> beatBeginPos) {
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
	
	private Map<String, Object> getQrsPosAndRRInterval(List<Short> ecgData) {
		QrsDetectorWithQRSInfo qrsDetector = new QrsDetectorWithQRSInfo(sampleRate);
		int n = 0;
		for(Short datum : ecgData) {
			qrsDetector.outputRRInterval((int)datum);
			n++;
			if(qrsDetector.firstPeakFound()) break;
		}
		for(Short datum : ecgData) {
			qrsDetector.outputRRInterval((int)datum);
		}
		
		List<Long> qrsPos = qrsDetector.getQrsPositions();
		List<Integer> rrInterval = qrsDetector.getRRIntervals();

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
	
	private static Map<String, Object> getRWaveAndBeatBeginPos(List<Short> ecgData, Map<String, Object> qrsAndRRInterval) {
		return RWaveDetecter.findRWaveAndBeatBeginPos(ecgData, qrsAndRRInterval);
	}
	
	private static List<Float> normalizeEcgData(List<Short> ecgData, List<Long> beatBeginPos) {
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

	private FIRFilter designLpFilter() {
		double[] wp = {2*Math.PI*65/sampleRate};
		double[] ws = {2*Math.PI*85/sampleRate};
		double Rp = 1;
		double As = 50;
		FilterType fType = FilterType.LOWPASS;
		WinType wType = WinType.HAMMING;

        return FIRDesigner.design(wp, ws, Rp, As, fType, wType);
	}
	
	private static IIRFilter designNotch(int f0) {
        return NotchDesigner.design(f0, 2);
	}
	
}
