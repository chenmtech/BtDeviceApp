package com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgcalibrator.EcgCalibrator65536;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgcalibrator.IEcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgfilter.IEcgFilter;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.IEcgHrProcessor;
import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * EcgSignalProcessor: 心电信号处理器，包含需要对心电信号做的所有处理
 * Created by Chenm, 2018-12-23
 */

public class EcgSignalProcessor {
    private IEcgCalibrator ecgCalibrator;
    private IEcgFilter ecgFilter;
    private QrsDetector qrsDetector;
    private List<IEcgHrProcessor> hrProcessors = new ArrayList<>();

    private EcgSignalProcessor() {

    }

    // 构建者
    public static class Builder {

    }

}
