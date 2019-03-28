package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDeviceConfig;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgcalibrateprocess.EcgCalibrateProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgcalibrateprocess.EcgCalibrateProcessor65536;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgcalibrateprocess.IEcgCalibrateProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgfilter.EcgPreFilterWith35HzNotch;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgfilter.IEcgFilter;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrAbnormalWarner;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrProcessor;
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

    private final static String KEY_HR_WARNER = "hrwarner"; // EcgHrAbnormalWarner的键值
    private final static String KEY_HR_PROCESSOR = "hrprocessor"; // EcgHrProcessor的键值

    private static final int HR_FILTER_RANGE_IN_SECOND = 10;
    private static final int HR_HISTOGRAM_BAR_NUM = 5;

    public interface IEcgSignalUpdatedListener {
        void onEcgSignalUpdated(int ecgSignal);
    }

    public interface IEcgHrValueUpdatedListener {
        void onEcgHrValueUpdated(int hr);
    }

    private IEcgCalibrateProcessor ecgCalibrateProcessor; // 定标处理器

    private IEcgFilter ecgFilter; // 滤波器

    private QrsDetector qrsDetector; // QRS波检测器，可求心率值

    private Map<String, IEcgHrProcessor> hrProcessors; // 心率处理器

    private IEcgSignalUpdatedListener signalListener = null; // 心电信号监听器

    private List<IEcgHrValueUpdatedListener> hrValueListenerArray = new ArrayList<>(); // 心率值监听器数组

    private EcgSignalProcessor(IEcgCalibrateProcessor ecgCalibrateProcessor, IEcgFilter ecgFilter, QrsDetector qrsDetector, Map<String, IEcgHrProcessor> hrProcessors) {
        this.ecgCalibrateProcessor = ecgCalibrateProcessor;
        this.ecgFilter = ecgFilter;
        this.qrsDetector = qrsDetector;
        this.hrProcessors = hrProcessors;
    }

    // 处理Ecg信号
    public void process(int ecgSignal) {
        // 标定后滤波处理
        ecgSignal = (int) ecgFilter.filter(ecgCalibrateProcessor.process(ecgSignal));

        // 通知信号监听器
        if(signalListener != null) signalListener.onEcgSignalUpdated(ecgSignal);

        // 检测Qrs波，获取心率
        int currentHr = qrsDetector.outputHR(ecgSignal);

        // 通知心率值监听器
        notifyEcgHrValueUpdatedListener(currentHr);

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
            EcgHrAbnormalWarner hrWarner = (EcgHrAbnormalWarner) hrProcessors.get(KEY_HR_WARNER);
            if(hrWarner != null) {
                hrWarner.setHrWarn(config.getHrLowLimit(), config.getHrHighLimit());
            } else {
                hrProcessors.put(KEY_HR_WARNER, new EcgHrAbnormalWarner(config.getHrLowLimit(), config.getHrHighLimit()));
            }
        } else {
            hrProcessors.remove(KEY_HR_WARNER);
        }
    }

    // 重置HR处理器
    public void resetHrProcessor() {
        EcgHrProcessor hrProcessor = (EcgHrProcessor) hrProcessors.get(KEY_HR_PROCESSOR);
        if(hrProcessor != null) {
            hrProcessor.clear();
            ViseLog.e("clear hr processor");
        }
    }

    public void updateHrStatistics() {
        EcgHrProcessor hrProcessor = (EcgHrProcessor) hrProcessors.get(KEY_HR_PROCESSOR);
        if(hrProcessor != null) {
            hrProcessor.updateHrStatistics(HR_FILTER_RANGE_IN_SECOND, HR_HISTOGRAM_BAR_NUM);
        }
    }

    public List<Integer> getHrList() {
        EcgHrProcessor hrProcessor = (EcgHrProcessor) hrProcessors.get(KEY_HR_PROCESSOR);
        if(hrProcessor != null) {
            return hrProcessor.getHrList();
        }
        return null;
    }

    public void setHrList(List<Integer> hrList) {
        EcgHrProcessor hrProcessor = (EcgHrProcessor) hrProcessors.get(KEY_HR_PROCESSOR);
        if(hrProcessor != null) {
            hrProcessor.setHrList(hrList);
        }
    }

    public void close() {
        //resetHrProcessor();
        removeEcgSignalUpdatedListener();
        removeAllHrAbnormalListeners();
        removeEcgHrStatisticsListener();
        removeAllEcgHrValueUpdatedListener();
    }

    public void setEcgSignalUpdatedListener(IEcgSignalUpdatedListener signalListener) {
        this.signalListener = signalListener;
    }

    private void removeEcgSignalUpdatedListener() {
        signalListener = null;
    }

    public void addEcgHrValueUpdatedListener(IEcgHrValueUpdatedListener listener) {
        if(!hrValueListenerArray.contains(listener)) {
            hrValueListenerArray.add(listener);
        }
    }

    private void notifyEcgHrValueUpdatedListener(int hr) {
        if(hr != INVALID_HR) {
            for (IEcgHrValueUpdatedListener listener : hrValueListenerArray) {
                listener.onEcgHrValueUpdated(hr);
            }
        }
    }

    private void removeAllEcgHrValueUpdatedListener() {
        hrValueListenerArray.clear();
    }

    public void addEcgHrAbnormalListener(EcgHrAbnormalWarner.IEcgHrAbnormalListener listener) {
        EcgHrAbnormalWarner hrWarner = (EcgHrAbnormalWarner) hrProcessors.get(KEY_HR_WARNER);
        if(hrWarner != null) {
            hrWarner.addEcgHrAbnormalListener(listener);
        }
    }

    private void removeAllHrAbnormalListeners() {
        EcgHrAbnormalWarner hrWarner = (EcgHrAbnormalWarner) hrProcessors.get(KEY_HR_WARNER);
        if(hrWarner != null) {
            hrWarner.removeAllListeners();
        }
    }

    public void setEcgHrStatisticsListener(EcgHrProcessor.IEcgHrStatisticsUpdatedListener listener) {
        EcgHrProcessor hrProcessor = (EcgHrProcessor) hrProcessors.get(KEY_HR_PROCESSOR);
        if(hrProcessor != null) {
            hrProcessor.setEcgHrStatisticsUpdatedListener(listener);
        }
    }

    public void removeEcgHrStatisticsListener() {
        EcgHrProcessor hrProcessor = (EcgHrProcessor) hrProcessors.get(KEY_HR_PROCESSOR);
        if(hrProcessor != null) {
            hrProcessor.removeEcgHrStatisticsUpdatedListener();
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
            IEcgCalibrateProcessor ecgCalibrator;
            if(value1mVAfterCalibrate == 65536) {
                ecgCalibrator = new EcgCalibrateProcessor65536(value1mVBeforeCalibrate);
            } else {
                ecgCalibrator = new EcgCalibrateProcessor(value1mVBeforeCalibrate, value1mVAfterCalibrate);
            }

            IEcgFilter ecgFilter = new EcgPreFilterWith35HzNotch(sampleRate);

            QrsDetector qrsDetector = new QrsDetector(sampleRate, value1mVAfterCalibrate);

            Map<String, IEcgHrProcessor> hrProcessors = new HashMap<>();
            hrProcessors.put(KEY_HR_PROCESSOR, new EcgHrProcessor());

            if(hrWarnEnabled) {
                hrProcessors.put(KEY_HR_WARNER, new EcgHrAbnormalWarner(hrLowLimit, hrHighLimit));
            }

            return new EcgSignalProcessor(ecgCalibrator, ecgFilter, qrsDetector, hrProcessors);
        }
    }

}
