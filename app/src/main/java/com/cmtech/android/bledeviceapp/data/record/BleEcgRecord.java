package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.AnnotationConstant.ANNOTATION_DESCRIPTION_MAP;
import static com.cmtech.android.bledeviceapp.data.record.AnnotationConstant.ANN_AFIB_SYMBOL;
import static com.cmtech.android.bledeviceapp.data.record.AnnotationConstant.INVALID_ANN_SYMBOL;
import static com.cmtech.android.bledeviceapp.data.record.RecordType.ECG;
import static com.cmtech.android.bledeviceapp.data.report.EcgReport.HR_TOO_HIGH_LIMIT;
import static com.cmtech.android.bledeviceapp.data.report.EcgReport.HR_TOO_LOW_LIMIT;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_HR;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_POS;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_TIME;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.data.report.EcgReport;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.IEcgRealTimeRhythmDetector;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.util.ListStringUtil;
import com.cmtech.android.bledeviceapp.util.MathUtil;
import com.cmtech.android.bledeviceapp.util.UploadDownloadFileUtil;
import com.vise.utils.file.FileUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
 * ClassName:      BleEcgRecord
 * Description:    心电信号记录类
 * Author:         chenm
 * CreateDate:     2020/3/28 上午7:11
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/28 上午7:11
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleEcgRecord extends BasicRecord implements IDiagnosable, Serializable {
    //-----------------------------------------常量

    //------------------------------------------实例变量，这些变量值会保存到本地和远程数据库中
    // 心电导联代码
    private int leadTypeCode = 0;

    // 平均心率值
    private int aveHr = INVALID_HR;

    // 分段信号开始位置列表，用样本序号来表示位置
    private final List<Integer> segPoses = new ArrayList<>();

    // 每一段信号开始时刻
    private final List<Long> segTimes = new ArrayList<>();

    // 注解位置列表，注解位置用样本序号来表示
    private final List<Integer> annPoses = new ArrayList<>();

    // 注解符号列表，用来表示注解的类型及备用信息
    private final List<String> annSymbols = new ArrayList<>();

    //------------------------------------------实例变量，这些变量值不会保存到本地或远程数据库中
    // 采集时设备连接是否断开
    @Column(ignore = true)
    private boolean interrupt = false;

    // 心率值列表，用于计算平均心率
    @Column(ignore = true)
    private final List<Integer> hrList = new ArrayList<>();

    //--------------------------------------------------构造器
    /**
     * 由RecordFactory工厂类通过反射调用它来创建对象
     * @param ver 记录版本号
     * @param createTime 创建时间
     * @param devAddress 创建设备的蓝牙地址
     * @param accountId 创建者的ID号
     */
    private BleEcgRecord(String ver, int accountId, long createTime, String devAddress,
                         int sampleRate, int channelNum, String gain, String unit) {
        super(ECG, ver, accountId, createTime, devAddress, sampleRate, channelNum, 2, gain, unit);
    }

    //-----------------------------------------------------------公有实例方法

    public void setLeadTypeCode(int leadTypeCode) {
        this.leadTypeCode = leadTypeCode;
    }

    public int getAveHr() {
        return aveHr;
    }

/*    public void setAveHr(int aveHr) {
        this.aveHr = aveHr;
    }*/

    // 添加一个心率值，并立刻计算平均心率
    public void addOneHr(int bpm) {
        hrList.add(bpm);
        this.aveHr = (int)MathUtil.intAve(hrList);
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    public List<String> getAnnSymbols() {
        return annSymbols;
    }

    /**
     * 记录一个ECG信号值，主要是将其保存到信号文件中，另外要处理设备断开操作
     * @param ecg 要记录的一个ECG信号数据
     * @return 是否正常处理
     */
    public int record(short[] ecg) {
        int pos = INVALID_POS;
        try {
            if(sigFile != null) {
                sigFile.writeShort(ecg);
                if (interrupt) {
                    segPoses.add(sigFile.size() - 1);
                    segTimes.add(new Date().getTime());
                    interrupt = false;
                }
                pos = sigFile.size() - 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pos;
    }

    /**
     * 生成诊断报告
     * @param rhythmDetector 心律检测器
     */
    public void createReport(IEcgRealTimeRhythmDetector rhythmDetector) {
        String strHrResult;
        // 先生成心率的诊断内容
        if(aveHr == INVALID_HR) {
            strHrResult = "";
        } else {
            if(aveHr > HR_TOO_HIGH_LIMIT)
                strHrResult = "过速";
            else if(aveHr < HR_TOO_LOW_LIMIT)
                strHrResult = "过缓";
            else
                strHrResult = "正常";
            strHrResult = "平均心率" + strHrResult + ":" + aveHr + "次/分钟;";
        }

        // 再生成心律的内容
        int AFIB_times = 0;
        int other_times = 0;
        for(String symbol : annSymbols) {
            String type = symbol.substring(0,1);
            if(type.equals("+")) {
                type = symbol.substring(1);
                if(type.equals("(AFIB")) {
                    AFIB_times++;
                } else if(!type.equals("(N")) {
                    other_times++;
                }
            }
        }

        String strRhythmResult = "";
        if(AFIB_times == 0 && other_times == 0) {
            strRhythmResult = "未发现心律异常;";
        } else {
            if(AFIB_times != 0) {
                strRhythmResult += ANNOTATION_DESCRIPTION_MAP.get(ANN_AFIB_SYMBOL)+AFIB_times+"次;";
            }
            if(other_times != 0) {
                strRhythmResult += "其他心律异常"+other_times+"次;";
            }
        }
        String reportContent = strHrResult+strRhythmResult;
        setReport(new EcgReport(rhythmDetector.getVer(), rhythmDetector.getProvider(),
                new Date().getTime(), reportContent, EcgReport.DONE));
    }

    /**
     * 添加一条注解项
     * @param ann 注解
     */
    public void addAnnotation(SignalAnnotation ann) {
        String symbol = ann.getSymbol();

        // 如果前一个标记和当前的条目标记一样，就放弃添加
        if(!annSymbols.isEmpty() && annSymbols.get(annSymbols.size()-1).equals(symbol)) {
            return;
        }

        int pos = ann.getPos();
        annPoses.add(pos);
        annSymbols.add(symbol);
    }

    /**
     * 获取记录的信号文件中当前位置上的数据的采集时刻
     * @return 数据采集时刻，单位ms
     */
    public long getTimeAtCurPos() {
        if(sigFile == null)
            return INVALID_TIME;
        try {
            return getTimeAtPosition(sigFile.getCurrentPos());
        } catch (IOException e) {
            e.printStackTrace();
            return INVALID_TIME;
        }
    }

    /**
     * 获取记录的信号文件中pos位置上的数据的采集时刻
     * 为了正确获取的数据采集时刻，必须用到breakPos和breakTime
     * @param pos 信号文件中的数据位置
     * @return 采集时刻，单位ms
     */
    private long getTimeAtPosition(int pos) {
        if(pos < 0) return INVALID_TIME;

        int segPos;
        long segTime;
        if(segPoses.isEmpty()) {
            segPos = 0;
            segTime = getCreateTime();
        } else {
            int i;
            for (i = 0; i < segPoses.size(); i++) {
                if (segPoses.get(i) > pos) break;
            }
            if(i==0) return INVALID_TIME;
            segPos = segPoses.get(i-1);
            segTime = segTimes.get(i-1);
        }
        return segTime + (pos - segPos)*1000L/getSampleRate();
    }

    public String getAnnSymbolAtCurPos() {
        if(annSymbols.isEmpty() || sigFile == null) return INVALID_ANN_SYMBOL;

        try {
            int curPos = sigFile.getCurrentPos();
            int i;
            for(i = 0; i < annPoses.size(); i++) {
                if(annPoses.get(i) > curPos) break;
            }
            if(i == 0) return INVALID_ANN_SYMBOL;
            return annSymbols.get(i-1);
        } catch (IOException e) {
            e.printStackTrace();
            return INVALID_ANN_SYMBOL;
        }
    }

    /**
     * 从信号文件当前数据位置向前寻找具有指定符号的注解项的数据位置
     * 当annSymbol==""，则寻找任意注解；
     * 当annSymbol为其他值时，则寻找指定注解。
     * @param annSymbol 要寻找的注解符号。
     * @return 前一个具有指定注解的数据位置
     */
    public int getPreAnnPosFromCurPos(String annSymbol) {
        if(annSymbols.isEmpty() || sigFile == null) return INVALID_POS;

        try {
            int curPos = sigFile.getCurrentPos();
            int i;
            for(i = 0; i < annPoses.size(); i++) {
                if(annPoses.get(i) > curPos) break;
            }
            if(i-2 < 0) return INVALID_POS;
            if(annSymbol.equals(""))
                return annPoses.get(i-2);

            // 返回前一个具有指定注解符号的数据位置
            for (int j = i - 2; j >= 0; j--) {
                if(annSymbols.get(j).contains(annSymbol))
                    return annPoses.get(j);
            }
            return INVALID_POS;
        } catch (IOException e) {
            e.printStackTrace();
            return INVALID_POS;
        }
    }

    /**
     * 从信号文件当前数据位置向后寻找具有指定符号的注解项的数据位置
     * 当annSymbol==""，则寻找任意注解；
     * 当annSymbol为其他值时，则寻找指定注解。
     * @param annSymbol 要寻找的注解符号。
     * @return 后一个具有指定注解的数据位置
     */
    public int getNextAnnPosFromCurPos(String annSymbol) {
        if(annSymbols.isEmpty() || sigFile == null) return INVALID_POS;

        try {
            int curPos = sigFile.getCurrentPos();
            int i;
            for(i = 0; i < annPoses.size(); i++) {
                if(annPoses.get(i) > curPos) break;
            }
            if(i == annPoses.size()) return INVALID_POS;

            if(annSymbol.equals(""))
                return annPoses.get(i);

            // 返回前一个具有指定注解符号的数据位置
            for (int j = i; j < annSymbols.size(); j++) {
                if(annSymbols.get(j).contains(annSymbol))
                    return annPoses.get(j);
            }
            return INVALID_POS;
        } catch (IOException e) {
            e.printStackTrace();
            return INVALID_POS;
        }
    }

    // 从信号文件中读取一个数据
    @Override
    public int[] readData() throws IOException {
        if(sigFile == null) throw new IOException();
        return sigFile.readShort();
    }

    //-------------------------------------------------实现IDiagnosable方法
    @Override
    public void remoteDiagnose(Context context, IWebResponseCallback callback) {
        //new ReportAsyncTask(context, callback).execute(this);
    }

    @Override
    public void localDiagnose() {
/*        IEcgRhythmDetector algorithm = new MyEcgRhythmDetector();
        EcgReport rtnReport = algorithm.process(this);
        setReportVer(rtnReport.getVer());
        setReportProvider(rtnReport.getReportProvider());
        setReportTime(rtnReport.getReportTime());
        setReportContent(rtnReport.getReportContent());
        setReportStatus(rtnReport.getReportStatus());
        setAveHr(rtnReport.getAveHr());
        setNeedUpload(true);
        save();*/
    }

    //-------------------------------------------------覆写基类BasicRecord的方法
    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
        leadTypeCode = json.getInt("leadTypeCode");
        aveHr = json.getInt("aveHr");
        ListStringUtil.stringToList(json.getString("segPoses"), segPoses, Integer.class);
        ListStringUtil.stringToList(json.getString("segTimes"), segTimes, Long.class);
        ListStringUtil.stringToList(json.getString("annPoses"), annPoses, Integer.class);
        ListStringUtil.stringToStrList(json.getString("annSymbols"), annSymbols);
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("leadTypeCode", leadTypeCode);
        json.put("aveHr", aveHr);
        json.put("segPoses", ListStringUtil.listToString(segPoses));
        json.put("segTimes", ListStringUtil.listToString(segTimes));
        json.put("annPoses", ListStringUtil.listToString(annPoses));
        json.put("annSymbols", ListStringUtil.strListToString(annSymbols));
        return json;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + leadTypeCode +
                "-" + segPoses + "-" + segTimes + "-" + annPoses + "-" + annSymbols;
    }

    /**
     * 从远程下载该记录的所有信息，包括信号文件
     * @param context 上下文
     * @param callback 下载后要执行的回调
     */
    @Override
    public void download(Context context, String showStr, ICodeCallback callback) {
        File file = FileUtil.getFile(BasicRecord.SIG_FILE_PATH, getSigFileName());
        // 如果本地不存在信号文件
        if(!file.exists()) {
            // 如果远程存在信号文件
            if(UploadDownloadFileUtil.isFileExist("ECG", getSigFileName())) {
                // 下载远程信号文件
                UploadDownloadFileUtil.downloadFile(context, "ECG", getSigFileName(), BasicRecord.SIG_FILE_PATH, new ICodeCallback() {
                    @Override
                    public void onFinish(int code, String msg) {
                        if(code== RCODE_SUCCESS) {
                            BleEcgRecord.super.download(context, showStr, callback);
                        } else {
                            if(callback != null)
                                callback.onFinish(code, msg);
                        }
                    }
                });
            } else {
                if(callback != null)
                    callback.onFinish(RCODE_DATA_ERR, "记录已损坏错误");
            }
        }
        // 如果本地存在信号文件，则调用基类方法从数据库读取记录信息
        else {
            super.download(context, showStr, callback);
        }
    }

    /**
     * 上传该记录的所有信息，包括信号文件
     * @param context 上下文
     * @param callback 上传后要执行的回调
     */
    @Override
    public void upload(Context context, ICodeCallback callback) {
        File sigFile = FileUtil.getFile(BasicRecord.SIG_FILE_PATH, getSigFileName());
        // 如果本地存在文件
        if(sigFile.exists()) {
            // 如果远程不存在文件
            if(!UploadDownloadFileUtil.isFileExist("ECG", getSigFileName())) {
                // 上传信号文件
                UploadDownloadFileUtil.uploadFile(context, "ECG", sigFile, new ICodeCallback() {
                    @Override
                    public void onFinish(int code, String msg) {
                        if (code == RCODE_SUCCESS) {
                            BleEcgRecord.super.upload(context, callback);
                        } else if(callback!=null){
                            callback.onFinish(code, msg);
                        }
                    }
                });
            }
            // 如果远程存在文件，则调用基类方法上传记录信息
            else {
                super.upload(context, callback);
            }
        } else {
            callback.onFinish(RCODE_DATA_ERR, "记录已损坏");
        }
    }

    //-------------------------------------------------私有方法

}
