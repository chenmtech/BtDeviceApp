package com.cmtech.android.bledeviceapp.ecgalgorithm;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledevice.report.EcgReport;
import com.cmtech.android.bledeviceapp.ecgalgorithm.afdetector.AFEvidence.MyAFEvidence;

import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledeviceapp.ecgalgorithm.afdetector.AFEvidence.MyAFEvidence.NON_AF;

public class EcgAFDetector implements IEcgAlgorithm{
    private final static String VER = "0.1.2";

    @Override
    public String getVer() {
        return VER;
    }

    @Override
    public EcgReport process(BleEcgRecord10 record) {
        EcgProcessor ecgProc = new EcgProcessor();
        ecgProc.process(record.getEcgData(), record.getSampleRate());
        int aveHr = ecgProc.getAverageHr();
        //ViseLog.e("aveHr:" + aveHr);

        List<Double> RR = ecgProc.getRRIntervalInMs();
        MyAFEvidence afEvi = MyAFEvidence.getInstance();
        afEvi.process(RR);
        int afe = afEvi.getAFEvidence();
        int classify = afEvi.getClassifyResult();
        //ViseLog.e("afe:" + afe + "classify:" + classify);

        StringBuilder builder = new StringBuilder();
        if(classify == MyAFEvidence.AF) {
            builder.append("提示房颤风险。如有心脏不适，请及时就医。");
        } else if(classify == NON_AF){
            builder.append("未发现房颤。");
        } else {
            builder.append("无法判断是否有房颤风险。");
        }
        builder.append("(房颤风险值：").append(afe).append(")");

        EcgReport report = new EcgReport();
        report.setVer(VER);
        report.setReportTime(new Date().getTime());
        report.setContent(builder.toString());
        report.setStatus(EcgReport.DONE);
        report.setAveHr(aveHr);
        return report;
    }
}
