package com.cmtech.android.bledevice.ecg.process.signal;

import com.cmtech.android.bledevice.ecg.interfac.IEcgDevice;
import com.cmtech.android.bledevice.ecg.process.signal.calibrator.EcgCalibrator65536;
import com.cmtech.android.bledevice.ecg.process.signal.calibrator.IEcgCalibrator;
import com.cmtech.android.bledevice.ecg.process.signal.filter.EcgPreFilterWith35HzNotch;
import com.cmtech.android.bledevice.ecg.process.signal.filter.IEcgFilter;
import com.cmtech.android.bledevice.ecg.process.hr.HrAbnormalProcessor;
import com.cmtech.android.bledevice.ecg.process.hr.HrStatisticProcessor;
import com.cmtech.android.bledevice.ecg.process.hr.IHrProcessor;
import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.cmtech.android.bledevice.ecg.process.signal.calibrator.IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION;


/**
  *
  * ClassName:      EcgSignalProcessor
  * Description:    心电信号处理器，包含心电信号的标定，预滤波，基于QRS波检测的心率计算，以及心率记录和统计分析，心率异常报警
  * Author:         chenm
  * CreateDate:     2018-12-23 08:00
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-15 08:00
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgSignalProcessor {
    public static final short INVALID_HR = 0; // 无效心率值
    public static final int HR_FILTER_SECOND = 10;
    public static final int HR_HISTOGRAM_BAR_NUM = 5;
    private static final String HR_ABNORMAL_PROCESSOR_KEY = "hr_abnormal_processor"; // 心率异常处理器的String key
    private static final String HR_STATISTICS_PROCESSOR_KEY = "hr_statistics_processor"; // 心率统计处理器的String key

    private final IEcgDevice device; // 设备
    private final IEcgCalibrator ecgCalibrator; // 标定处理器
    private final IEcgFilter ecgFilter; // 滤波器
    private QrsDetector qrsDetector; // QRS波检测器，可求心率值
    private final Map<String, IHrProcessor> hrProcessorMap; // 心率处理相关操作Map

    public EcgSignalProcessor(IEcgDevice device, boolean needFilter) {
        if(device == null) {
            throw new IllegalArgumentException("EcgDevice is null");
        }

        this.device = device;
        if(needFilter) {
            ecgCalibrator = new EcgCalibrator65536(this.device.getValue1mV());
            ecgFilter = new EcgPreFilterWith35HzNotch(this.device.getSampleRate());
        } else {
            ecgCalibrator = null;
            ecgFilter = null;
        }
        qrsDetector = new QrsDetector(this.device.getSampleRate(), STANDARD_VALUE_1MV_AFTER_CALIBRATION);

        hrProcessorMap = new ConcurrentHashMap<>();
        HrStatisticProcessor hrStatisticProcessor = new HrStatisticProcessor(HR_FILTER_SECOND, this.device);
        hrProcessorMap.put(HR_STATISTICS_PROCESSOR_KEY, hrStatisticProcessor);
        if(this.device.getConfig().warnWhenHrAbnormal()) {
            HrAbnormalProcessor hrAbnormalProcessor = new HrAbnormalProcessor(this.device);
            hrProcessorMap.put(HR_ABNORMAL_PROCESSOR_KEY, hrAbnormalProcessor);
        }
    }

    // 重置，不会重置心率统计处理器
    public void reset() {
        if(ecgCalibrator != null)
            ecgCalibrator.reset(device.getValue1mV(), STANDARD_VALUE_1MV_AFTER_CALIBRATION);
        if(ecgFilter != null)
            ecgFilter.reset(device.getSampleRate());
        qrsDetector = new QrsDetector(device.getSampleRate(), STANDARD_VALUE_1MV_AFTER_CALIBRATION);
        resetHrAbnormalProcessor();
    }

    public void resetHrAbnormalProcessor() {
        HrAbnormalProcessor hrAbnormalProcessor = (HrAbnormalProcessor) hrProcessorMap.get(HR_ABNORMAL_PROCESSOR_KEY);

        if (device.getConfig().warnWhenHrAbnormal()) {
            if (hrAbnormalProcessor != null) {
                hrAbnormalProcessor.reset();
            } else {
                hrAbnormalProcessor = new HrAbnormalProcessor(device);
                hrProcessorMap.put(HR_ABNORMAL_PROCESSOR_KEY, hrAbnormalProcessor);
            }
        } else {
            hrProcessorMap.remove(HR_ABNORMAL_PROCESSOR_KEY);
        }
    }

    // 处理Ecg信号
    public void process(int ecgSignal) {
        if(ecgFilter != null && ecgCalibrator != null)
            ecgSignal = (int) ecgFilter.filter(ecgCalibrator.calibrate(ecgSignal)); // 标定,滤波
        device.updateSignalValue(ecgSignal); // 更新信号
        short currentHr = (short) qrsDetector.outputHR(ecgSignal); // 检测Qrs波，获取心率
        // 心率值更新处理
        if(currentHr != INVALID_HR) {
            device.updateHrValue(currentHr);

            for (IHrProcessor operator : hrProcessorMap.values()) {
                operator.process(currentHr);
            }
        }
    }

    public void updateHrStatisticInfo() {
        HrStatisticProcessor hrStatisticProcessor = (HrStatisticProcessor) hrProcessorMap.get(HR_STATISTICS_PROCESSOR_KEY);

        if(hrStatisticProcessor != null) {
            hrStatisticProcessor.updateHrStatisticInfo();
        }
    }
}
