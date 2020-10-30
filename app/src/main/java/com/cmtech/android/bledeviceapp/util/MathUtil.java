package com.cmtech.android.bledeviceapp.util;

import android.util.Pair;

import java.util.List;

public class MathUtil {
	private MathUtil() {
		
	}

    //均值
    public static double doubleAve(List<Double> x) {
        int m = x.size();
        double sum = 0;
        for(Double d : x) {
            sum += d;
        }
        return sum/m;
    }
	
    //均值
    public static float floatAve(List<Float> x) {
        int m = x.size();
        float sum = 0;
        for(Float d : x) {
            sum += d;
        }
        return sum/m;
    }

    //short均值,返回float
    public static float shortAve(List<Short> x) {
        int m = x.size();
        float sum = 0;
        for(Short d : x) {
            sum += d;
        }
        return sum/m;
    }
	
    //标准差σ=sqrt(s^2)
    public static float floatStd(List<Float> x) {
        float ave = floatAve(x);//求平均值
        float var = 0;
        for(Float d : x) {
            var += (d-ave)*(d-ave);
        }
        return (float)Math.sqrt(var/(x.size()-1));
    }

    //标准差σ=sqrt(s^2)
    public static double doubleStd(List<Double> x) {
        double ave = doubleAve(x);//求平均值
        double var = 0;
        for(double d : x) {
            var += (d-ave)*(d-ave);
        }
        return Math.sqrt(var/(x.size()-1));
    }
	
    //标准差σ=sqrt(s^2)
    public static float shortStd(List<Short> x) {
	    float ave = shortAve(x);
        float var = 0;

        for(Short d : x) {
            var += (d-ave)*(d-ave);
        }

        return (float)Math.sqrt(var/(x.size()-1));
    }
	 
	 public static Pair<Integer, Float> floatMin(List<Float> x) {
		 float minV = Float.MAX_VALUE;
		 int minI = -1;
		 for(int i = 0; i < x.size(); i++) {
			 if(x.get(i) < minV) {
				 minV = x.get(i);
				 minI = i;
			 }
		 }
		 return new Pair<>(minI, minV);
	 }
	 
	 public static Pair<Integer, Float> floatMax(List<Float> x) {
		 float maxV = Float.MIN_VALUE;
		 int maxI = -1;
		 for(int i = 0; i < x.size(); i++) {
			 if(x.get(i) > maxV) {
				 maxV = x.get(i);
				 maxI = i;
			 }
		 }
		 return new Pair<>(maxI, maxV);
	 }

}
