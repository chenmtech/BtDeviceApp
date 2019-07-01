package com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess.ecgcalibrator.EcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess.ecgcalibrator.EcgCalibrator65536;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess.ecgcalibrator.IEcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess.ecgfilter.EcgPreFilterWith35HzNotch;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess.ecgfilter.IEcgFilter;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess.ecghrprocess.HrAbnormalWarner;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess.ecghrprocess.HrProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgsignalprocess.ecghrprocess.IHrOperator;
import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
  *
  * ClassName:      EcgSignalProcessor
  * Description:    心电信号处理器，包含心电信号的标定，滤波，基于QRS波检测的心率计算，以及心率记录和统计分析，心率异常报警
  * Author:         chenm
  * CreateDate:     2018-12-23 08:00
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-15 08:00
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgSignalProcessor {
    public static final short INVALID_HR = 0; // 无效心率值
    public static final int HR_FILTER_TIME_IN_SECOND = 10;
    public static final int HR_HISTOGRAM_BAR_NUM = 5;
    private static final String HR_ABNORMAL_WARNER_KEY = "hr_warner"; // 心率异常报警器的String key
    private static final String HR_PROCESSOR_KEY = "hr_processor"; // 心率处理器的String key


    private IEcgCalibrator caliProcessor; // 标定处理器

    private IEcgFilter ecgFilter; // 滤波器

    private QrsDetector qrsDetector; // QRS波检测器，可求心率值

    private Map<String, IHrOperator> hrOperators; // 心率相关操作Map

    private final EcgMonitorDevice device;

    private EcgSignalProcessor(EcgMonitorDevice device,
                               IEcgCalibrator caliProcessor,
                               IEcgFilter ecgFilter,
                               QrsDetector qrsDetector,
                               Map<String, IHrOperator> hrOperators) {
        if(device == null) {
            throw new NullPointerException();
        }

        this.device = device;

        this.caliProcessor = caliProcessor;

        this.ecgFilter = ecgFilter;

        this.qrsDetector = qrsDetector;

        this.hrOperators = hrOperators;
    }

    // 处理Ecg信号
    public void process(int ecgSignal) {
        // 标定,滤波
        ecgSignal = (int) ecgFilter.filter(caliProcessor.process(ecgSignal));

        // 通知信号更新
        device.onSignalValueUpdated(ecgSignal);

        // 检测Qrs波，获取心率
        short currentHr = (short) qrsDetector.outputHR(ecgSignal);

        // 通知心率值更新
        if(currentHr != INVALID_HR) {
            device.onHrValueUpdated(currentHr);
        }

        // 心率操作
        if(currentHr != INVALID_HR) {
            for(IHrOperator operator : hrOperators.values()) {
                operator.operate(currentHr);
            }
        }
    }

    // 重置心率记录仪
    public void resetHrProcessor() {
        HrProcessor hrProcessor = (HrProcessor) hrOperators.get(HR_PROCESSOR_KEY);

        if(hrProcessor != null) {
            hrProcessor.reset();
        }
    }

    public void updateHrStatisticInfo() {
        HrProcessor hrProcessor = (HrProcessor) hrOperators.get(HR_PROCESSOR_KEY);

        if(hrProcessor != null) {
            hrProcessor.updateHrStatisticInfo();
        }
    }

    public List<Short> getHrList() {
        HrProcessor hrProcessor = (HrProcessor) hrOperators.get(HR_PROCESSOR_KEY);

        if(hrProcessor != null) {
            return hrProcessor.getHrList();
        }

        return null;
    }

    public HrProcessor getHrProcessor() {
        return (HrProcessor) hrOperators.get(HR_PROCESSOR_KEY);
    }

    public void setHrProcessor(HrProcessor hrProcessor) {
        hrOperators.put(HR_PROCESSOR_KEY, hrProcessor);
    }

    public void setHrAbnormalWarner(boolean isWarn, int lowLimit, int highLimit) {
        HrAbnormalWarner hrWarner = (HrAbnormalWarner) hrOperators.get(HR_ABNORMAL_WARNER_KEY);

        if(isWarn) {

            if(hrWarner != null) {
                hrWarner.initialize(lowLimit, highLimit);

            } else {
                hrWarner = new HrAbnormalWarner(device, lowLimit, highLimit);

                hrOperators.put(HR_ABNORMAL_WARNER_KEY, hrWarner);
            }
        } else {
            if(hrWarner != null) {
                hrWarner.close();
            }

            hrOperators.remove(HR_ABNORMAL_WARNER_KEY);
        }
    }

    public void close() {
        for(IHrOperator operator : hrOperators.values()) {
            if(operator != null)
                operator.close();
        }
    }


    // 构建者
    public static class Builder {
        private final EcgMonitorDevice device;

        private int sampleRate = 0;

        private int value1mVBeforeCalibrate = 0; // 定标前1mV对应的数值

        private int value1mVAfterCalibrate = 0; // 定标后1mV对应的数值

        private boolean hrWarnEnabled = false; // 是否使能HR警告

        private int hrLowLimit = 0; // HR异常下限

        private int hrHighLimit = 0; // HR异常上限

        public Builder(EcgMonitorDevice device) {
            this.device = device;
        }

        // 设置Ecg信号采样率
        public void setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
        }

        // 设置标定前后1mV的采样值
        public void setValue1mVCalibrate(int before, int after) {
            this.value1mVBeforeCalibrate = before;

            this.value1mVAfterCalibrate = after;
        }

        // 设置心率异常是否报警
        public void setHrWarnEnabled(boolean hrWarnEnabled) {
            this.hrWarnEnabled = hrWarnEnabled;
        }

        // 设置心率报警上下限
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

            Map<String, IHrOperator> hrOperators = new HashMap<>();

            HrProcessor hrProcessor = new HrProcessor(HR_FILTER_TIME_IN_SECOND, device);

            hrOperators.put(HR_PROCESSOR_KEY, hrProcessor);

            if(hrWarnEnabled) {
                HrAbnormalWarner hrWarner = new HrAbnormalWarner(device, hrLowLimit, hrHighLimit);

                hrOperators.put(HR_ABNORMAL_WARNER_KEY, hrWarner);
            }

            return new EcgSignalProcessor(device, ecgCalibrator, ecgFilter, qrsDetector, hrOperators);
        }
    }

}
