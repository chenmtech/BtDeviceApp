package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDeviceConfig;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgcalibrator.EcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgcalibrator.EcgCalibrator65536;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgcalibrator.IEcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgfilter.EcgPreFilterWith35HzNotch;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgfilter.IEcgFilter;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrAbnormalWarner;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.IEcgHrAbnormalObserver;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.IEcgHrProcessor;
import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * EcgSignalProcessor: 心电信号处理器，包含所有心电信号的处理
 * Created by Chenm, 2018-12-23
 */

public class EcgSignalProcessor {
    public final static int INVALID_HR = 0; // 无效心率值
    private final static String KEY_HRWARNER = "hrwarner"; // EcgHrAbnormalWarner的键值
    private final static String KEY_HRPROCESSOR = "hrprocessor"; // EcgHrProcessor的键值

    private IEcgCalibrator ecgCalibrator; // 定标器
    private IEcgFilter ecgFilter; // 滤波器
    private QrsDetector qrsDetector; // QRS波检测器，可求心率值
    private Map<String, IEcgHrProcessor> hrProcessors; // 心率处理器
    private IEcgSignalObserver signalObserver = null; // 心电信号观察者
    private List<IEcgHrValueObserver> hrValueObservers = new ArrayList<>(); // 心率值观察者

    private EcgSignalProcessor(IEcgCalibrator ecgCalibrator, IEcgFilter ecgFilter, QrsDetector qrsDetector, Map<String, IEcgHrProcessor> hrProcessors) {
        this.ecgCalibrator = ecgCalibrator;
        this.ecgFilter = ecgFilter;
        this.qrsDetector = qrsDetector;
        this.hrProcessors = hrProcessors;
    }

    // 处理Ecg信号
    public void process(int ecgSignal) {
        // 标定后滤波处理
        ecgSignal = (int) ecgFilter.filter(ecgCalibrator.process(ecgSignal));
        // 通知信号观察者
        notifySignalObserver(ecgSignal);
        // 检测Qrs波，获取心率
        int currentHr = qrsDetector.outputHR(ecgSignal);
        // 通知心率观察者
        notifyHrValueObserver(currentHr);
        // 处理心率值
        if(currentHr != INVALID_HR) {
            for(IEcgHrProcessor processor : hrProcessors.values()) {
                processor.process(currentHr);
            }
        }
    }

    // 修改配置信息
    public void changeConfiguration(EcgMonitorDeviceConfig config) {
        if(config.isWarnWhenHrAbnormal()) {
            EcgHrAbnormalWarner hrWarner = (EcgHrAbnormalWarner) hrProcessors.get(KEY_HRWARNER);
            if(hrWarner != null) {
                hrWarner.setHrWarn(config.getHrLowLimit(), config.getHrHighLimit());
            } else {
                hrProcessors.put(KEY_HRWARNER, new EcgHrAbnormalWarner(config.getHrLowLimit(), config.getHrHighLimit()));
            }
        } else {
            hrProcessors.remove(KEY_HRWARNER);
        }
    }

    // 重置HR直方图
    public void resetHrHistogram() {
        EcgHrProcessor hrHistogram = (EcgHrProcessor) hrProcessors.get(KEY_HRPROCESSOR);
        if(hrHistogram != null) {
            hrHistogram.clear();
            ViseLog.e("clear hr histogram");
        }
    }

    public EcgHrProcessor getHrProcessor() {
        return (EcgHrProcessor) hrProcessors.get(KEY_HRPROCESSOR);
    }

    public void setHrProcessor(EcgHrProcessor hrProcessor) {
        hrProcessors.put(KEY_HRPROCESSOR, hrProcessor);
    }

    // 获取HR直方图数据
    public int[] getHistogramData() {
        EcgHrProcessor hrHistogram = (EcgHrProcessor) hrProcessors.get(KEY_HRPROCESSOR);
        if(hrHistogram != null) {
            return hrHistogram.getHistgram();
        }
        return null;
    }

    public void close() {
        resetHrHistogram();
        removeSignalObserver();
        removeAllHrAbnormalObserver();
        removeAllHrValueObserver();
    }

    public void registerSignalObserver(IEcgSignalObserver signalObserver) {
        this.signalObserver = signalObserver;
    }
    private void notifySignalObserver(int ecgSignal) {
        if(signalObserver != null) {
            signalObserver.updateEcgSignal(ecgSignal);
        }
    }
    public void removeSignalObserver() {
        signalObserver = null;
    }

    public void registerHrValueObserver(IEcgHrValueObserver observer) {
        if(!hrValueObservers.contains(observer)) {
            hrValueObservers.add(observer);
        }
    }

    public void notifyHrValueObserver(int hr) {
        if(hr != INVALID_HR) {
            for (IEcgHrValueObserver observer : hrValueObservers) {
                observer.updateHrValue(hr);
            }
        }
    }
    public void removeHrValueObserver(IEcgHrValueObserver observer) {
        hrValueObservers.remove(observer);
    }
    public void removeAllHrValueObserver() {
        hrValueObservers.clear();
    }

    public void registerHrAbnormalObserver(IEcgHrAbnormalObserver observer) {
        EcgHrAbnormalWarner hrWarner = (EcgHrAbnormalWarner) hrProcessors.get(KEY_HRWARNER);
        if(hrWarner != null) {
            hrWarner.registerObserver(observer);
        }
    }

    public void removeHrAbnormalObserver(IEcgHrAbnormalObserver observer) {
        EcgHrAbnormalWarner hrWarner = (EcgHrAbnormalWarner) hrProcessors.get(KEY_HRWARNER);
        if(hrWarner != null) {
            hrWarner.removeObserver(observer);
        }
    }
    public void removeAllHrAbnormalObserver() {
        EcgHrAbnormalWarner hrWarner = (EcgHrAbnormalWarner) hrProcessors.get(KEY_HRWARNER);
        if(hrWarner != null) {
            hrWarner.removeAllObserver();
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
            hrProcessors.put(KEY_HRPROCESSOR, new EcgHrProcessor());
            ViseLog.e("重新创建了EcgHrHistogram");

            if(hrWarnEnabled) {
                hrProcessors.put(KEY_HRWARNER, new EcgHrAbnormalWarner(hrLowLimit, hrHighLimit));
            }

            return new EcgSignalProcessor(ecgCalibrator, ecgFilter, qrsDetector, hrProcessors);
        }
    }

}
