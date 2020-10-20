package com.cmtech.android.bledeviceapp.ecgalgorithm.qrsdetbyhamilton;

public class MAverageFilter {
	private final int windowWidth;
	private final int[] data;
	
	private long sum = 0 ;
	private int ptr = 0 ;

	public MAverageFilter(int sampleRate) {
		int MS80 = MsToSample.get(80, sampleRate);
		int MS120 = MsToSample.get(120, sampleRate);
		windowWidth = MS120;
		data = new int[windowWidth];
		
		initialize();
	
	}
	
	public int getLength() {
		return windowWidth;
	}
	
	public void initialize() {
		for(ptr = 0; ptr < windowWidth ; ++ptr)
			data[ptr] = 0 ;
		sum = 0 ;
		ptr = 0 ;
		
		filter(0);
	}
	
	/*****************************************************************************
	* mvwint() implements a moving window integrator.  Actually, mvwint() averages
	* the signal values over the last WINDOW_WIDTH samples.
	*****************************************************************************/

	public int filter(int datum)
	{
		int output;
		
		sum += datum ;
		sum -= data[ptr] ;
		data[ptr] = datum ;
		if(++ptr == windowWidth)
			ptr = 0 ;
		
		if((sum / windowWidth) > 32000)
			output = 32000 ;
		else
			output = (int) (sum / windowWidth) ;
		return(output) ;
	}
}
