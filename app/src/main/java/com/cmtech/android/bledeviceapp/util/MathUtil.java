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

    //int均值,返回float
    public static float intAve(List<Integer> x) {
        int m = x.size();
        float sum = 0;
        for(Integer d : x) {
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

    public static Pair<Integer, Float> floatMax(float[] x) {
        float maxV = Float.MIN_VALUE;
        int maxI = -1;
        for(int i = 0; i < x.length; i++) {
            if(x[i] > maxV) {
                maxV = x[i];
                maxI = i;
            }
        }
        return new Pair<>(maxI, maxV);
    }

    // 寻找List<Integer>中从begin到end之间的最大值及出现最大值的位置
    public static Pair<Integer, Integer> intMax(List<Integer> x, int begin, int end) {
        int maxV = Integer.MIN_VALUE;
        int maxI = -1;
        for(int i = begin; i < end; i++) {
            int value = x.get(i);
            if(value > maxV) {
                maxV = value;
                maxI = i;
            }
        }
        return new Pair<>(maxI, maxV);
    }

    public static Pair<Integer, Integer> intMin(List<Integer> x, int begin, int end) {
        int minV = Integer.MAX_VALUE;
        int minI = -1;
        for(int i = begin; i < end; i++) {
            if(x.get(i) < minV) {
                minV = x.get(i);
                minI = i;
            }
        }
        return new Pair<>(minI, minV);
    }

    // 寻找List<Integer>中从begin到end之间的最大值及出现最大值的位置
    // 如果连续出现几个最大值，则返回出现最大值的中间位置
    public static Pair<Integer, Integer> intMax1(List<Integer> x, int begin, int end) {
        int maxV = Integer.MIN_VALUE;
        int maxI = -1;
        for(int i = begin; i < end; i++) {
            int value = x.get(i);
            if(value > maxV) {
                maxV = value;
                maxI = i;
            }
        }
        int num = 0;
        for(int i = maxI+1; i < end; i++) {
            int value = x.get(i);
            if(value == maxV) {
                num++;
            } else {
                break;
            }
        }
        return new Pair<>(maxI+num/2, maxV);
    }
}
