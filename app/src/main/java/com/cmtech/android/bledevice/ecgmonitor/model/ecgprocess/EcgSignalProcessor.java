package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgcalibrator.EcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgcalibrator.EcgCalibrator65536;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgcalibrator.IEcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgfilter.EcgPreFilterWith35HzNotch;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgfilter.IEcgFilter;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrAbnormalWarner;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.IEcgHrProcessor;
import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * EcgSignalProcessor: 心电信号处理器，包含心电信号的标定，滤波，基于QRS波检测的心率计算，以及心率记录和统计分析，心率异常报警
 * 但是不包含心电信号的记录和信号的留言。
 * Created by Chenm, 2018-12-23
 */

public class EcgSignalProcessor {
    public final static short INVALID_HR = 0; // 无效心率值
    public static final int SECOND_IN_HR_FILTER = 10;
    public static final int BAR_NUM_IN_HR_HISTOGRAM = 5;
    private final static String KEY_HR_WARNER = "hrwarner"; // EcgHrAbnormalWarner的键值
    private final static String KEY_HR_PROCESSOR = "hrrecorder"; // EcgHrRecorder的键值


    public interface IEcgSignalUpdatedListener {
        void onUpdateEcgSignal(int ecgSignal);
    }

    public interface IEcgHrValueUpdatedListener {
        void onUpdateEcgHrValue(short hr);
    }

    private IEcgSignalUpdatedListener signalListener = null; // 心电信号更新监听器

    private List<IEcgHrValueUpdatedListener> hrValueListeners = new ArrayList<>(); // 心率值更新监听器

    private IEcgCalibrator ecgCalibrateProcessor; // 标定处理器

    private IEcgFilter ecgFilter; // 滤波器

    private QrsDetector qrsDetector; // QRS波检测器，可求心率值

    private Map<String, IEcgHrProcessor> hrProcessors; // 心率处理器

    private EcgSignalProcessor(IEcgCalibrator ecgCalibrateProcessor,
                               IEcgFilter ecgFilter,
                               QrsDetector qrsDetector,
                               Map<String, IEcgHrProcessor> hrProcessors,
                               IEcgSignalProcessListener listener) {
        this.ecgCalibrateProcessor = ecgCalibrateProcessor;
        this.ecgFilter = ecgFilter;
        this.qrsDetector = qrsDetector;
        this.hrProcessors = hrProcessors;
        this.signalListener = listener;
        if(!hrValueListeners.contains(listener))
            hrValueListeners.add(listener);
    }

    // 处理Ecg信号
    public void process(int ecgSignal) {
        // 标定后滤波处理
        ecgSignal = (int) ecgFilter.filter(ecgCalibrateProcessor.process(ecgSignal));

        // 通知心电信号更新监听器
        if(signalListener != null) signalListener.onUpdateEcgSignal(ecgSignal);

        // 检测Qrs波，获取心率
        short currentHr = (short) qrsDetector.outputHR(ecgSignal);

        // 通知心率值更新监听器
        notifyEcgHrValueUpdatedListeners(currentHr);

        // 用所有的心率处理器处理心率值
        if(currentHr != INVALID_HR) {
            for(IEcgHrProcessor processor : hrProcessors.values()) {
                processor.process(currentHr);
            }
        }
    }

    public void setHrAbnormalWarner(boolean isWarn, int lowLimit, int highLimit, EcgHrAbnormalWarner.IEcgHrAbnormalListener listener) {
        EcgHrAbnormalWarner hrWarner = (EcgHrAbnormalWarner) hrProcessors.get(KEY_HR_WARNER);
        if(isWarn) {
            if(hrWarner != null) {
                hrWarner.setHrWarn(lowLimit, highLimit);
            } else {
                hrWarner = new EcgHrAbnormalWarner(lowLimit, highLimit);
                hrProcessors.put(KEY_HR_WARNER, hrWarner);
            }
            hrWarner.addEcgHrAbnormalListener(listener);
        } else {
            if(hrWarner != null) {
                hrWarner.close();
            }
            hrProcessors.remove(KEY_HR_WARNER);
        }
    }

    // 重置心率记录仪
    public void resetHrRecorder() {
        EcgHrProcessor hrRecorder = (EcgHrProcessor) hrProcessors.get(KEY_HR_PROCESSOR);
        if(hrRecorder != null) {
            hrRecorder.reset();
        }
    }

    public void updateHrInfo() {
        EcgHrProcessor hrProcessor = (EcgHrProcessor) hrProcessors.get(KEY_HR_PROCESSOR);
        if(hrProcessor != null) {
            hrProcessor.updateHrInfo();
        }
    }

    public List<Short> getHrList() {
        EcgHrProcessor hrRecorder = (EcgHrProcessor) hrProcessors.get(KEY_HR_PROCESSOR);
        if(hrRecorder != null) {
            return hrRecorder.getHrList();
        }
        return null;
    }

    public EcgHrProcessor getHrProcessor() {
        return (EcgHrProcessor) hrProcessors.get(KEY_HR_PROCESSOR);
    }

    public void setHrProcessor(EcgHrProcessor hrProcessor) {
        hrProcessors.put(KEY_HR_PROCESSOR, hrProcessor);
    }

    public void close() {
        signalListener = null;

        hrValueListeners.clear();

        EcgHrProcessor hrRecorder = (EcgHrProcessor) hrProcessors.get(KEY_HR_PROCESSOR);
        if(hrRecorder != null) {
            hrRecorder.close();
        }

        EcgHrAbnormalWarner hrWarner = (EcgHrAbnormalWarner) hrProcessors.get(KEY_HR_WARNER);
        if(hrWarner != null) {
            hrWarner.close();
        }
    }

    private void notifyEcgHrValueUpdatedListeners(short hr) {
        if(hr != INVALID_HR) {
            for (IEcgHrValueUpdatedListener listener : hrValueListeners) {
                listener.onUpdateEcgHrValue(hr);
            }
        }
    }

    // 构建者
    public static class Builder {
        private int sampleRate = 0;
        private int value1mVBeforeCalibrate = 0; // 定标之前1mV对应的数值
        private int value1mVAfterCalibrate = 0; // 定标之后1mV对应的数值
        private boolean hrWarnEnabled = false; // 是否使能HR警告
        private int hrLowLimit = 0; // HR异常下限
        private int hrHighLimit = 0; // HR异常上限
        private IEcgSignalProcessListener listener;

        public Builder() {

        }

        public void setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
        }

        public void setValue1mVCalibrate(int before, int after) {
            this.value1mVBeforeCalibrate = before;
            this.value1mVAfterCalibrate = after;
        }

        public void setHrWarnEnabled(boolean hrWarnEnabled) {
            this.hrWarnEnabled = hrWarnEnabled;
        }

        public void setHrWarnLimit(int low, int high) {
            hrLowLimit = low;
            hrHighLimit = high;
        }

        public void setEcgSignalProcessListener(IEcgSignalProcessListener listener) {
            this.listener = listener;
        }

        public EcgSignalProcessor build() {
            IEcgCalibrator ecgCalibrator;
            if(value1mVAfterCalibrate == 65536) {
                ecgCalibrator = new EcgCalibrator65536(value1mVBeforeCalibrate);
            } else {
                ecgCalibrator = new EcgCalibrator(value1mVBeforeCalibrate, value1mVAfterCalibrate);
            }

            IEcgFilter ecgFilter = new EcgPreFilterWith35HzNotch(sampleRate);

            QrsDetector qrsDetector = new QrsDetector(sampleRate, value1mVAfterCalibrate);

            Map<String, IEcgHrProcessor> hrProcessors = new HashMap<>();

            EcgHrProcessor hrRecorder = new EcgHrProcessor(SECOND_IN_HR_FILTER, listener);
            hrProcessors.put(KEY_HR_PROCESSOR, hrRecorder);

            if(hrWarnEnabled) {
                EcgHrAbnormalWarner hrWarner = new EcgHrAbnormalWarner(hrLowLimit, hrHighLimit);
                hrWarner.addEcgHrAbnormalListener(listener);
                hrProcessors.put(KEY_HR_WARNER, hrWarner);
            }

            return new EcgSignalProcessor(ecgCalibrator, ecgFilter, qrsDetector, hrProcessors, listener);
        }
    }

}
