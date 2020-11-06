package com.cmtech.android.bledeviceapp.dataproc.ecgalgorithm;

import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;
import com.cmtech.android.bledeviceapp.dataproc.ecgalgorithm.afdetector.AFEvidence.MyAFEvidence;

import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledeviceapp.dataproc.ecgalgorithm.afdetector.AFEvidence.MyAFEvidence.NON_AF;

public class EcgAFDetector implements IEcgAlgorithm{
    private static final int LIMIT_HR_TOO_LOW = 50;
    private static final int LIMIT_HR_TOO_HIGH = 100;
    private final static String VER = "0.1.3";


    @Override
    public String getVer() {
        return VER;
    }

    @Override
    public EcgReport process(BleEcgRecord record) {
        EcgProcessor ecgProc = new EcgProcessor();
        ecgProc.process(record.getEcgData(), record.getSampleRate());
        int aveHr = ecgProc.getAverageHr();
        String strHrResult;
        if(aveHr > LIMIT_HR_TOO_HIGH)
            strHrResult = "心动过速。";
        else if(aveHr < LIMIT_HR_TOO_LOW)
            strHrResult = "心动过缓。";
        else
            strHrResult = "心率正常。";

        List<Double> RR = ecgProc.getRRIntervalInMs();
        MyAFEvidence afEvi = MyAFEvidence.getInstance();
        afEvi.process(RR);
        int afe = afEvi.getAFEvidence();
        int classify = afEvi.getClassifyResult();
        //ViseLog.e("afe:" + afe + "classify:" + classify);

        StringBuilder builder = new StringBuilder();
        if(classify == MyAFEvidence.AF) {
            builder.append("提示房颤风险。");
        } else if(classify == NON_AF){
            builder.append("未发现房颤风险。");
        } else {
            builder.append("无法判断是否有房颤风险。");
        }
        builder.append("(房颤风险值：").append(afe).append(")");
        String strAFEResult = builder.toString();

        EcgReport report = new EcgReport();
        report.setVer(VER);
        report.setReportTime(new Date().getTime());
        report.setContent(strHrResult + strAFEResult);
        report.setStatus(EcgReport.DONE);
        report.setAveHr(aveHr);
        return report;
    }
}
