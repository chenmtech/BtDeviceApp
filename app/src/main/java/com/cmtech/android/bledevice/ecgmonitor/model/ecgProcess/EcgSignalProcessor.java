package com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgcalibrator.EcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgcalibrator.EcgCalibrator65536;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgcalibrator.IEcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgfilter.EcgPreFilterWith35HzNotch;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecgfilter.IEcgFilter;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.EcgHrHistogram;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgProcess.ecghrprocess.EcgHrWarner;
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

    private EcgSignalProcessor(IEcgCalibrator ecgCalibrator, IEcgFilter ecgFilter, QrsDetector qrsDetector, List<IEcgHrProcessor> hrProcessors) {

    }

    // 构建者
    public static class Builder {
        private IEcgCalibrator ecgCalibrator;
        private IEcgFilter ecgFilter;
        private QrsDetector qrsDetector;
        private List<IEcgHrProcessor> hrProcessors = new ArrayList<>();

        private int sampleRate = 0;
        private int value1mVBeforeCalibrate = 0; // 定标之前1mV对应的数值
        private int value1mVAfterCalibrate = 0; // 定标之后1mV对应的数值
        private boolean hrWarn = false;
        private int hrLowLimit = 0;
        private int hrHighLimit = 0;

        public Builder() {

        }

        public void setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
        }

        public void setValue1mVBeforeCalibrate(int value1mVBeforeCalibrate) {
            this.value1mVBeforeCalibrate = value1mVBeforeCalibrate;
        }

        public void setValue1mVAfterCalibrate(int value1mVAfterCalibrate) {
            this.value1mVAfterCalibrate = value1mVAfterCalibrate;
        }

        public void setHrWarn(boolean hrWarn) {
            this.hrWarn = hrWarn;
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

            List<IEcgHrProcessor> hrProcessors = new ArrayList<>();

            hrProcessors.add(new EcgHrHistogram());

            if(hrWarn) {
                hrProcessors.add(new EcgHrWarner(hrLowLimit, hrHighLimit));
            }

            return new EcgSignalProcessor(ecgCalibrator, ecgFilter, qrsDetector, hrProcessors);
        }

    }

}
