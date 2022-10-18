package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import static com.cmtech.android.bledeviceapp.dataproc.ecgproc.afdetector.AFEvidence.MyAFEvidence.NON_AF;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_HR;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.afdetector.AFEvidence.MyAFEvidence;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.preproc.EcgPreProcessor;

import java.util.List;

public class MyEcgRhythmDetector implements IEcgRhythmDetector {
    private static final int HR_TOO_LOW_LIMIT = 50;
    private static final int HR_TOO_HIGH_LIMIT = 100;
    public static final String VER = "1.0.0";

    private static final String REPORT_PROVIDER = "康明智能";

    private final MyAFEvidence afEvidence;

    public MyEcgRhythmDetector() {
        afEvidence = new MyAFEvidence();
    }

    @Override
    public String getVer() {
        return VER;
    }

    @Override
    public EcgReport process(BleEcgRecord record) {
        EcgPreProcessor preProcessor = new EcgPreProcessor();
        //preProcessor.process(record.getEcgData(), record.getSampleRate());

        int aveHr = preProcessor.getAverageHr();
        List<Double> RR = preProcessor.getRRIntervalInMs();

        String strHrResult;
        if(aveHr == INVALID_HR) {
            strHrResult = "";
        } else {
            strHrResult = "平均心率：" + aveHr + "次/分钟，";
            if(aveHr > HR_TOO_HIGH_LIMIT)
                strHrResult += "心动过速;";
            else if(aveHr < HR_TOO_LOW_LIMIT)
                strHrResult += "心动过缓;";
            else
                strHrResult += "心率正常;";
        }

        afEvidence.process(RR);
        int afe = afEvidence.getAFEvidence();
        int classify = afEvidence.getClassifyResult();
        //ViseLog.e("afe:" + afe + "classify:" + classify);

        StringBuilder builder = new StringBuilder();
        if(classify == MyAFEvidence.AF) {
            builder.append("发现房颤风险。");
        } else if(classify == NON_AF){
            builder.append("未发现房颤风险。");
        } else {
            builder.append("由于信号质量问题，无法判断房颤风险。");
        }
        builder.append("(风险值：").append(afe).append(")");
        String strAFEResult = builder.toString();

        EcgReport report = new EcgReport();
        /*report.setVer(VER);
        report.setReportProvider(REPORT_PROVIDER);
        report.setReportTime(new Date().getTime());
        report.setReportContent(strHrResult + strAFEResult);
        report.setReportStatus(EcgReport.DONE);*/
        //report.setAveHr(aveHr);
        return report;
    }
}
