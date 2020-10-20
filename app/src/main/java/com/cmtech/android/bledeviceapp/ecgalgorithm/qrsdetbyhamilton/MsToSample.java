package com.cmtech.android.bledeviceapp.ecgalgorithm.qrsdetbyhamilton;

public class MsToSample {
	private MsToSample() {
		
	}

	public static int get(int ms, int sampleRate) {
		double msPerSample =  1000.0/sampleRate;
		return (int)Math.round(ms/msPerSample);
	}
	
	public static int get(int ms, double msPerSample) {
		return (int)Math.round(ms/msPerSample);
	}
}
