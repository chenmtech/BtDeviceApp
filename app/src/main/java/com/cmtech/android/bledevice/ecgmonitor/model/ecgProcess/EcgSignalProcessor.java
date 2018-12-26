package com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDeviceConfig;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgcalibrator.EcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgcalibrator.EcgCalibrator65536;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgcalibrator.IEcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgfilter.EcgPreFilterWith35HzNotch;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgfilter.IEcgFilter;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.EcgHrBroadcaster;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.EcgHrHistogram;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.EcgHrWarner;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.IEcgHrObserver;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.IEcgHrProcessor;
import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.IEcgHrProcessor.INVALID_HR;

/**
 * EcgSignalProcessor: 心电信号处理器，包含需要对心电信号做的所有处理
 * Created by Chenm, 2018-12-23
 */

public class EcgSignalProcessor {
    private final static String KEY_HRBROADCASTER = "hrbroadcaster";
    private final static String KEY_HRWARNER = "hrwarner";
    private final static String KEY_HRHISTOGRAM = "hrhistogram";

    private IEcgCalibrator ecgCalibrator;
    private IEcgFilter ecgFilter;
    private QrsDetector qrsDetector;
    private Map<String, IEcgHrProcessor> hrProcessors = new HashMap<>();

    private List<IEcgHrObserver> hrObservers = new ArrayList<>();

    private IEcgSignalObserver signalObserver = null;


    private EcgSignalProcessor(IEcgCalibrator ecgCalibrator, IEcgFilter ecgFilter, QrsDetector qrsDetector, Map<String, IEcgHrProcessor> hrProcessors) {
        this.ecgCalibrator = ecgCalibrator;
        this.ecgFilter = ecgFilter;
        this.qrsDetector = qrsDetector;
        this.hrProcessors = hrProcessors;
    }

    public void process(int ecgSignal) {
        // 标定后滤波处理
        ecgSignal = (int) ecgFilter.filter(ecgCalibrator.process(ecgSignal));
        notifySignalObserver(ecgSignal);

        // 检测Qrs波，获取心率
        int currentHr = qrsDetector.outputHR(ecgSignal);

        if(currentHr != INVALID_HR) {
            Collection<IEcgHrProcessor> processors = this.hrProcessors.values();
            for(IEcgHrProcessor processor : processors) {
                processor.process(currentHr);
            }
        }
    }

    // 修改配置信息
    public void changeConfiguration(EcgMonitorDeviceConfig config) {
        if(config.isWarnWhenHrAbnormal()) {
            EcgHrWarner hrWarner = (EcgHrWarner) hrProcessors.get(KEY_HRWARNER);
            if(hrWarner != null) {
                hrWarner.setHrWarn(config.getHrLowLimit(), config.getHrHighLimit());
            } else {
                hrProcessors.put(KEY_HRWARNER, new EcgHrWarner(config.getHrLowLimit(), config.getHrHighLimit()));
            }
        } else {
            hrProcessors.remove(KEY_HRWARNER);
        }
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

            hrProcessors.put(KEY_HRBROADCASTER, new EcgHrBroadcaster());
            hrProcessors.put(KEY_HRHISTOGRAM, new EcgHrHistogram());
            if(hrWarnEnabled) {
                hrProcessors.put(KEY_HRWARNER, new EcgHrWarner(hrLowLimit, hrHighLimit));
            }

            return new EcgSignalProcessor(ecgCalibrator, ecgFilter, qrsDetector, hrProcessors);
        }
    }

}
