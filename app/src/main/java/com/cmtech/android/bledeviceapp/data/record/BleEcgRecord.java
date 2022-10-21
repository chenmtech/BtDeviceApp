package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.ECG;
import static com.cmtech.android.bledeviceapp.data.report.EcgReport.HR_TOO_HIGH_LIMIT;
import static com.cmtech.android.bledeviceapp.data.report.EcgReport.HR_TOO_LOW_LIMIT;
import static com.cmtech.android.bledeviceapp.global.AppConstant.AF_LABEL;
import static com.cmtech.android.bledeviceapp.global.AppConstant.ALL_RHYTHM_LABEL;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_HR;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_LABEL;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_POS;
import static com.cmtech.android.bledeviceapp.global.AppConstant.NOISE_LABEL;
import static com.cmtech.android.bledeviceapp.global.AppConstant.NSR_LABEL;
import static com.cmtech.android.bledeviceapp.global.AppConstant.OTHER_LABEL;
import static com.cmtech.android.bledeviceapp.global.AppConstant.RHYTHM_LABEL_MAP;
import static com.cmtech.android.bledeviceapp.util.DateTimeUtil.INVALID_TIME;

import android.content.Context;
import androidx.annotation.NonNull;
import com.cmtech.android.bledeviceapp.data.report.EcgReport;
import com.cmtech.android.bledeviceapp.dataproc.ecgproc.EcgRhythmDetectItem;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.interfac.IWebResponseCallback;
import com.cmtech.android.bledeviceapp.util.ListStringUtil;
import com.cmtech.android.bledeviceapp.util.MathUtil;
import com.cmtech.android.bledeviceapp.util.UploadDownloadFileUtil;
import com.vise.log.ViseLog;
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
public class BleEcgRecord extends BasicRecord implements ISignalRecord, IDiagnosable, Serializable {
    //-----------------------------------------常量
    // 记录每个信号数据所需的字节数，因为用的是short类型数据，所以是2个字节
    private static final int BYTES_PER_DATUM = 2;

    //------------------------------------------实例变量，这些变量值会保存到本地和远程数据库中
    // 采样率
    private int sampleRate = 0;

    // 1mV标定值
    private int caliValue = 0;

    // 导联码
    private int leadTypeCode = 0;

    // 平均心率值
    private int aveHr = INVALID_HR;

    // 心电采集时设备连接断开时的信号记录位置值列表
    private final List<Integer> breakPos = new ArrayList<>();

    // 心电采集时设备连接断开时的信号记录时刻点列表
    private final List<Long> breakTime = new ArrayList<>();

    // 每一次心律检测的条目起始时刻列表
    private final List<Long> rhythmItemStartTime = new ArrayList<>();

    // 每一次心律检测的条目标记列表
    private final List<Integer> rhythmItemLabel = new ArrayList<>();

    //------------------------------------------实例变量，这些变量值不会保存到本地和远程数据库中
    // 采集时设备连接是否断开
    @Column(ignore = true)
    private boolean interrupt = false;

    // 心率值列表
    @Column(ignore = true)
    private final List<Integer> hrList = new ArrayList<>();

    //--------------------------------------------------构造器
    /**
     * 由RecordFactory工厂类通过反射调用它来创建对象
     * @param ver 记录版本号
     * @param createTime 创建时间
     * @param devAddress 创建设备的蓝牙地址
     * @param creatorId 创建者的ID号
     */
    private BleEcgRecord(String ver, long createTime, String devAddress, int creatorId) {
        super(ECG, ver, createTime, devAddress, creatorId);
    }

    //-----------------------------------------------------------公有实例方法
    // 创建并打开信号文件
    public void createSigFile() {
        super.createSigFile(BYTES_PER_DATUM);
    }

    // 打开信号文件
    public void openSigFile() {
        super.openSigFile(BYTES_PER_DATUM);
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setCaliValue(int caliValue) {
        this.caliValue = caliValue;
    }

    public void setLeadTypeCode(int leadTypeCode) {
        this.leadTypeCode = leadTypeCode;
    }

    public int getAveHr() {
        return aveHr;
    }

/*    public void setAveHr(int aveHr) {
        this.aveHr = aveHr;
    }*/

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    // 添加一个心率值，并立刻计算平均心率
    public void addHRValue(int bpm) {
        hrList.add(bpm);
        this.aveHr = (int)MathUtil.intAve(hrList);
    }

    /**
     * 获取记录的信号文件中当前位置上的数据的采集时刻点
     * @return 时刻点，单位ms
     */
    public long getTimeAtCurrentPosition() {
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
     * 获取记录的信号文件中pos位置上的数据的采集时刻点
     * 为了正确获取数据的采集时刻，必须用到breakPos和breakTime
     * @param pos 数据位置
     * @return 时刻，单位ms
     */
    private long getTimeAtPosition(int pos) {
        if(pos < 0) return INVALID_TIME;

        int startPos;
        long startTime;
        if(breakPos.isEmpty()) {
            startPos = 0;
            startTime = getCreateTime();
        } else {
            int i;
            for (i = 0; i < breakPos.size(); i++) {
                if (breakPos.get(i) > pos) break;
            }
            if(i==0) return INVALID_TIME;
            startPos = breakPos.get(i-1);
            startTime = breakTime.get(i-1);
        }
        return startTime + (pos - startPos)*1000L/sampleRate;
    }

    // 获取指定时间点对应的数据位置

    /**
     * 获取指定采集时刻time对应于记录的信号文件中的数据位置pos
     * 为了正确获取数据位置，必须用到breakPos和breakTime
     * @param time 采集时刻点
     * @return 信号文件中的数据位置
     */
    public int getPositionAtTime(long time) {
        if(time < 0) return INVALID_POS;

        int startPos;
        long startTime;
        if(breakPos.isEmpty()) {
            startPos = 0;
            startTime = getCreateTime();
        } else {
            if(time < breakTime.get(0)) {
                return 0;
            }

            int i;
            for (i = 0; i < breakTime.size(); i++) {
                if (breakTime.get(i) > time) break;
            }
            startPos = breakPos.get(i-1);
            startTime = breakTime.get(i-1);
        }
        return (int) (startPos + (time - startTime)*sampleRate/1000);
    }

    /**
     * 获取指定采集时刻time,记录中的信号的诊断标签
     * @param time 采集时刻点
     * @return 信号在该时刻的诊断标签
     */
    public int getLabelAtTime(long time) {
        if(rhythmItemLabel.isEmpty()) return INVALID_LABEL;

        int i;
        for(i = 0; i < rhythmItemStartTime.size(); i++) {
            if(rhythmItemStartTime.get(i) > time) break;
        }
        if(i == 0) return INVALID_LABEL;
        return rhythmItemLabel.get(i-1);
    }

    public int getPreItemPositionFromCurrentPosition(int label) {
        long curTime = getTimeAtCurrentPosition();
        ViseLog.e("curTime:"+curTime);
        if(curTime == INVALID_TIME) return INVALID_POS;

        if(rhythmItemStartTime.isEmpty()) return INVALID_POS;

        int i;
        for(i = 0; i < rhythmItemStartTime.size(); i++) {
            if(rhythmItemStartTime.get(i) > curTime) break;
        }
        ViseLog.e("i:"+i);
        if(i-2 < 0) return INVALID_POS;

        if(label == INVALID_LABEL)
            return getPositionAtTime(rhythmItemStartTime.get(i-2));

        if(label == ALL_RHYTHM_LABEL) {
            for (int j = i - 2; j >= 0; j--) {
                if(rhythmItemLabel.get(j) != NSR_LABEL && rhythmItemLabel.get(j) != NOISE_LABEL) {
                    return getPositionAtTime(rhythmItemStartTime.get(j));
                }
            }
        }
        return INVALID_POS;
    }

    public int getNextItemPositionFromCurrentPosition(int label) {
        long curTime = getTimeAtCurrentPosition();
        if(curTime == INVALID_TIME) return INVALID_POS;

        if(rhythmItemStartTime.isEmpty()) return INVALID_POS;

        int i;
        for(i = 0; i < rhythmItemStartTime.size(); i++) {
            if(rhythmItemStartTime.get(i) > curTime) break;
        }

        if(i == rhythmItemStartTime.size()) return INVALID_POS;

        if(label == INVALID_LABEL)
            return getPositionAtTime(rhythmItemStartTime.get(i));

        if(label == ALL_RHYTHM_LABEL) {
            for (int j = i; j < rhythmItemLabel.size(); j++) {
                if(rhythmItemLabel.get(j) != NSR_LABEL && rhythmItemLabel.get(j) != NOISE_LABEL) {
                    return getPositionAtTime(rhythmItemStartTime.get(j));
                }
            }
        }
        return INVALID_POS;
    }

    /**
     * 处理一个ECG信号值
     * @param ecg ECG信号
     * @return 是否正常处理
     */
    public boolean record(short ecg) {
        boolean success = false;
        try {
            if(sigFile != null) {
                sigFile.writeShort(ecg);
                if (interrupt) {
                    breakPos.add(sigFile.size() - 1);
                    breakTime.add(new Date().getTime());
                    interrupt = false;
                }
                success = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    /**
     * 添加一个心律异常检测条目
     * @param item 一条检测项
     */
    public void addRhythmItem(EcgRhythmDetectItem item) {
        int label = item.getLabel();

        // 如果前一个标记和当前的条目标记一样，就放弃添加
        if(!rhythmItemLabel.isEmpty() && rhythmItemLabel.get(rhythmItemLabel.size()-1) == label) {
            return;
        }

        long startTime = item.getStartTime();
        // 防止条目起始时间比记录创建时间还要早
        if(startTime < getCreateTime()) {
            startTime = getCreateTime();
        }

        rhythmItemStartTime.add(startTime);
        rhythmItemLabel.add(label);
    }

    //-------------------------------------------------实现ISignalRecord方法
    @Override
    public int getCaliValue() {
        return caliValue;
    }

    @Override
    public int getSampleRate() {
        return sampleRate;
    }

    @Override
    public int readData() throws IOException {
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
        sampleRate = json.getInt("sampleRate");
        caliValue = json.getInt("caliValue");
        leadTypeCode = json.getInt("leadTypeCode");
        aveHr = json.getInt("aveHr");
        ListStringUtil.stringToList(json.getString("breakPos"), breakPos, Integer.class);
        ListStringUtil.stringToList(json.getString("breakTime"), breakTime, Long.class);
        ListStringUtil.stringToList(json.getString("rhythmItemStartTime"), rhythmItemStartTime, Long.class);
        ListStringUtil.stringToList(json.getString("rhythmItemLabel"), rhythmItemLabel, Integer.class);
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("sampleRate", sampleRate);
        json.put("caliValue", caliValue);
        json.put("leadTypeCode", leadTypeCode);
        json.put("aveHr", aveHr);
        json.put("breakPos", ListStringUtil.listToString(breakPos));
        json.put("breakTime", ListStringUtil.listToString(breakTime));
        json.put("rhythmItemStartTime", ListStringUtil.listToString(rhythmItemStartTime));
        json.put("rhythmItemLabel", ListStringUtil.listToString(rhythmItemLabel));
        return json;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + sampleRate + "-" + caliValue + "-" + leadTypeCode +
                "-" + breakPos + "-" + breakTime + "-" + rhythmItemStartTime + "-" + rhythmItemLabel;
    }

    @Override
    public void download(Context context, ICodeCallback callback) {
        File file = FileUtil.getFile(BasicRecord.SIG_FILE_PATH, getSigFileName());
        // 如果本地不存在信号文件
        if(!file.exists()) {
            // 如果远程存在信号文件
            if(UploadDownloadFileUtil.isFileExist("ECG", getSigFileName())) {
                // 下载远程信号文件
                UploadDownloadFileUtil.downloadFile(context, "ECG", getSigFileName(), BasicRecord.SIG_FILE_PATH, new ICodeCallback() {
                    @Override
                    public void onFinish(int code) {
                        if(code==RETURN_CODE_SUCCESS) {
                            BleEcgRecord.super.download(context, callback);
                        } else {
                            callback.onFinish(RETURN_CODE_DOWNLOAD_ERR);
                        }
                    }
                });
            } else {
                callback.onFinish(RETURN_CODE_DOWNLOAD_ERR);
            }
        }
        // 如果本地存在信号文件，则调用基类方法从数据库下载记录信息
        else {
            super.download(context, callback);
        }
    }

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
                    public void onFinish(int code) {
                        if (code == RETURN_CODE_SUCCESS) {
                            BleEcgRecord.super.upload(context, callback);
                        } else {
                            callback.onFinish(RETURN_CODE_DOWNLOAD_ERR);
                        }
                    }
                });
            }
            // 如果远程存在文件，则调用基类方法上传记录信息
            else {
                super.upload(context, callback);
            }
        } else {
            callback.onFinish(RETURN_CODE_UPLOAD_ERR);
        }
    }

    @Override
    public void setReport(EcgReport report) {
        report.setReportContent(createReportContent());
        super.setReport(report);
    }

    // 创建诊断报告内容，返回字符串
    private String createReportContent() {
        String strHrResult;
        if(aveHr == INVALID_HR) {
            strHrResult = "";
        } else {
            strHrResult = "平均心率" + aveHr + "次/分钟,";
            if(aveHr > HR_TOO_HIGH_LIMIT)
                strHrResult += "过速;";
            else if(aveHr < HR_TOO_LOW_LIMIT)
                strHrResult += "过缓;";
            else
                strHrResult += "正常;";
        }

        int af_times = 0;
        int other_times = 0;
        for(int ll : rhythmItemLabel) {
            if(ll == AF_LABEL)
                af_times++;
            else if(ll == OTHER_LABEL) {
                other_times++;
            }
        }

        String strRhythmResult = "";
        if(af_times == 0 && other_times == 0) {
            strRhythmResult = "未发现心律异常;";
        } else {
            if(af_times != 0) {
                strRhythmResult += RHYTHM_LABEL_MAP.get(AF_LABEL)+af_times+"次；";
            }
            if(other_times != 0) {
                strRhythmResult += RHYTHM_LABEL_MAP.get(OTHER_LABEL)+other_times+"次；";
            }
        }
        return strHrResult+strRhythmResult;
    }

}
