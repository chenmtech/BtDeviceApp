package com.cmtech.android.bledeviceapp.dataproc.ecgproc.preproc;

import android.util.Pair;

import com.cmtech.android.bledeviceapp.util.MathUtil;
import com.cmtech.dsp.filter.FIRFilter;
import com.cmtech.dsp.filter.IDigitalFilter;
import com.cmtech.dsp.filter.structure.StructType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RWaveDetecter {
    private static final int QRS_WIDTH_MS = 200; // QRS波宽度，unit:毫秒
    private static final float MAX_PEAK_MIN_PEAK_RATIO = 1.0f/3; // 可接受的正峰值与负峰值之比

    @SuppressWarnings("unchecked")
    public static Map<String, Object> findRWaveAndBeatBeginPos(List<Short> ecgData, Map<String, Object> qrsAndRRInterval, int sampleRate) {
        int halfQrsWidth = QRS_WIDTH_MS*sampleRate/1000/2; // QRS波一半宽度点数

        List<Long> qrsPos = (List<Long>) qrsAndRRInterval.get("QrsPos"); // QRS波位置
        List<Integer> rrInterval = (List<Integer>) qrsAndRRInterval.get("RRInterval"); // 初步的RR间隔
        if(rrInterval.size() != qrsPos.size()-1) return null; //RR间隔的个数必须是QRS波个数-1

        List<Float> delta2= diffFilter(ecgData); // 信号二阶差分

        List<Long> rPos = new ArrayList<>(); // 存放R波位置

        for(int i = 1; i < qrsPos.size()-1; i++) {
            long qrs = qrsPos.get(i);

            long beatBegin = qrs - Math.round(rrInterval.get(i-1)*2.0/5);
            long qrsBegin = qrs - halfQrsWidth;
            long minTmp = Math.min(beatBegin, qrsBegin);
            if(minTmp < 0) continue;
            beatBegin = minTmp;

            long beatEnd = qrs + Math.round(rrInterval.get(i)*3.0/5);
            long qrsEnd = qrs + halfQrsWidth;
            long maxTmp = Math.max(beatEnd, qrsEnd);
            if(maxTmp >= ecgData.size()) continue;
            beatEnd = maxTmp;

            // 截取当前一次心跳的数据
            List<Float> beat = new ArrayList<>();
            for(Short d : ecgData.subList((int)beatBegin, (int)beatEnd)) {
                beat.add((float)d);
            }

            // 归一化处理
            float ave = MathUtil.floatAve(beat);
            float std = MathUtil.floatStd(beat);
            for(int i1 = 0; i1 < beat.size(); i1++) {
                beat.set(i1, (beat.get(i1)-ave)/std);
            }

            // 把beat缩小为QRS波的范围
            beat = beat.subList((int)(qrsBegin-beatBegin), (int)(qrsEnd-beatBegin));

            // 搜索beat的最小值和最大值
            Pair<Integer, Float> rlt = MathUtil.floatMin(beat);
            float minV = rlt.second;
            int minI = rlt.first;

            rlt = MathUtil.floatMax(beat);
            float maxV = rlt.second;
            int maxI = rlt.first;


            if(maxI < minI && Math.abs(maxV) > MAX_PEAK_MIN_PEAK_RATIO*Math.abs(minV)) {
                rPos.add(qrsBegin + maxI);
                System.out.println("MaxV peak > "+ MAX_PEAK_MIN_PEAK_RATIO + " minV peak");
            } else {
                System.out.println("MaxV peak <= "+ MAX_PEAK_MIN_PEAK_RATIO + " minV peak");
                List<Float> qrsDelta2 = delta2.subList((int)qrsBegin, (int)qrsEnd); // QRS波的二阶差分数据

                rlt = MathUtil.floatMin(qrsDelta2);
                minV = rlt.second;
                minI = rlt.first;

                rlt = MathUtil.floatMax(qrsDelta2);
                maxV = rlt.second;
                maxI = rlt.first;

                long qrsPosTmp;
                if(Math.abs(maxV) > Math.abs(minV)) {
                    qrsPosTmp = qrsBegin + maxI;
                } else {
                    qrsPosTmp = qrsBegin + minI;
                }

                // 最后在qrsPosTmp附近搜索峰值
                long rBegin = qrsPosTmp - 5;
                long rEnd = qrsPosTmp + 5;
                List<Float> tmpList = new ArrayList<>();
                for(long j = rBegin; j <= rEnd; j++) {
                    tmpList.add((float)Math.abs(ecgData.get((int)j)));
                }

                rlt = MathUtil.floatMax(tmpList);
                rPos.add(rBegin + rlt.first);
            }
        }

        // 求新的RR间隔
        List<Integer> newRRInterval = new ArrayList<>();
        for(int i = 1; i < rPos.size(); i++) {
            newRRInterval.add((int)(rPos.get(i)-rPos.get(i-1)));
        }

        // 求每次心跳开始位置
        List<Long> beatBegin = new ArrayList<>();
        for(int i = 1; i < rPos.size(); i++) {
            beatBegin.add(rPos.get(i) - Math.round(newRRInterval.get(i-1)*2.0/5));
        }

        // 打包
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

        // 考虑滤波产生2个数的移位，矫正它
        d2Data.remove(0);
        d2Data.remove(0);
        float tmp = d2Data.get(d2Data.size()-1);
        d2Data.add(tmp);
        d2Data.add(tmp);

        return d2Data;
    }
}
