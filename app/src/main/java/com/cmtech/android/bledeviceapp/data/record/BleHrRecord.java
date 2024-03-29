package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.HR;
import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_HR;

import androidx.annotation.NonNull;

import com.cmtech.android.bledeviceapp.dataproc.hrproc.HRProcessor;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.ListStringUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      BleHrRecord10
 * Description:    心率记录
 * Author:         chenm
 * CreateDate:     2020/3/20 下午3:17
 * UpdateUser:     chenm
 * UpdateDate:     2020/9/1 下午3:17
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleHrRecord extends BasicRecord implements Serializable {
    public static final int HR_MA_FILTER_SPAN = 10; // unit: second

    private final List<Short> hrList = new ArrayList<>(); // filtered HR list
    private final List<Integer> hrHist = new ArrayList<>(); // HR histogram value
    private short hrMax = INVALID_HR; // HR max
    private short hrAve = INVALID_HR; // HR average

    @Column(ignore = true)
    private transient final HrMAFilter hrMAFilter = new HrMAFilter(HR_MA_FILTER_SPAN); // moving average filter
    @Column(ignore = true)
    private final List<HrHistogramBar<Integer>> hrHistogram = new ArrayList<>();
    @Column(ignore = true)
    private transient long hrSum = 0;
    @Column(ignore = true)
    private transient long hrNum = 0;
    @Column(ignore = true)
    private transient long preTime = 0;

    private BleHrRecord(String ver, int accountId, long createTime, String devAddress,
                        int sampleRate, int channelNum, String gain, String unit) {
        super(HR, ver, accountId, createTime, devAddress, sampleRate, channelNum, 2, gain, unit);
        initHrHistogram();
    }

    private void initHrHistogram() {
        hrHistogram.add(new HrHistogramBar<>((short)0, (short)121, "平静心率", 0));
        hrHistogram.add(new HrHistogramBar<>((short)122, (short)131, "热身放松", 0));
        hrHistogram.add(new HrHistogramBar<>((short)132, (short)141, "有氧燃脂", 0));
        hrHistogram.add(new HrHistogramBar<>((short)142, (short)152, "有氧耐力", 0));
        hrHistogram.add(new HrHistogramBar<>((short)153, (short)162, "无氧耐力", 0));
        hrHistogram.add(new HrHistogramBar<>((short)163, (short)1000, "极限冲刺", 0));
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
        hrList.clear();
        ListStringUtil.stringToList(json.getString("hrList"), hrList, Short.class);
        hrMax = (short)json.getInt("hrMax");
        hrAve = (short)json.getInt("hrAve");
        ListStringUtil.stringToList(json.getString("hrHist"), hrHist, Integer.class);

        createHistogramFromHrHist();
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("hrList", ListStringUtil.listToString(hrList));
        json.put("hrMax", hrMax);
        json.put("hrAve", hrAve);
        json.put("hrHist", ListStringUtil.listToString(hrHist));
        return json;
    }

    @Override
    public boolean noSignalFile() {
        return hrList.isEmpty();
    }

    public List<Short> getHrList() {
        return hrList;
    }

    public short calculateHRVInMs() {
        return HRProcessor.calculateHRVInMs(hrList);
    }

    public int calculateCalories(Account account) {
        if(account == null) {
            throw new IllegalStateException();
        }

        return HRProcessor.calculateCalories(hrAve, getSigLen(), account);
    }

    public short getHrMax() {
        return hrMax;
    }
    public short getHrAve() {
        return hrAve;
    }
    public List<Integer> getHrHist() {
        return hrHist;
    }
    public List<HrHistogramBar<Integer>> getHrHistogram() {
        return hrHistogram;
    }
    public void createHistogramFromHrHist() {
        if(hrHist != null && hrHist.size() == hrHistogram.size()) {
            for (int i = 0; i < hrHistogram.size(); i++) {
                hrHistogram.get(i).setValue(hrHist.get(i));
            }
        }
    }

    public boolean record(short hr, long time) {
        if(hrMax < hr) hrMax = hr;
        hrSum += hr;
        hrNum++;
        hrAve = (short)(hrSum/hrNum);

        int interval = 0;
        if(preTime == 0) { // the first HR
            interval = 2; // default HR sent period in HRM
        } else {
            long tmp = Math.round((time-preTime)/1000.0); // ms to second
            interval = (tmp > HR_MA_FILTER_SPAN) ? HR_MA_FILTER_SPAN : (int)tmp;
        }
        preTime = time;
        for(HrHistogramBar<Integer> bar : hrHistogram) {
            if(hr < bar.getMaxValue()) {
                bar.setValue(bar.getValue() + interval);
                break;
            }
        }

        short fHr = hrMAFilter.process(hr, interval);

        boolean fHrUpdated = (fHr != INVALID_HR);
        if(fHrUpdated)
            hrList.add(fHr);
        return fHrUpdated;
    }

    @Override
    public int[] readData() throws IOException {
        throw new IOException("Error!");
    }

    @Override
    public int getDataNum() {
        throw new IllegalStateException("");
    }

    @Override
    public List<Integer> getGain() {
        throw new IllegalStateException("");
    }

    @Override
    public int getSampleRate() {
        throw new IllegalStateException("");
    }

    @Override
    public boolean isEOD() {
        throw new IllegalStateException("");
    }

    @Override
    public void seek(int pos) {
        throw new IllegalStateException("");
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "-" + hrList + "-" + hrMax + "-" + hrAve + "-" + hrHist;
    }

}
