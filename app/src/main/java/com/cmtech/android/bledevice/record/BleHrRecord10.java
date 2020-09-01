package com.cmtech.android.bledevice.record;

import com.cmtech.android.bledeviceapp.model.Account;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.annotation.Column;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.hrm.model.HrmDevice.INVALID_HEART_RATE;
import static com.cmtech.android.bledevice.record.RecordType.HR;

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
public class BleHrRecord10 extends BasicRecord implements Serializable {
    public static final int HR_MOVE_AVERAGE_FILTER_TIME_SPAN = 10; // unit: s

    private List<Short> hrList; // filtered HR list
    private List<Integer> hrHist; // HR histogram value
    private short hrMax; // HR max
    private short hrAve; // HR average
    private int recordSecond; // unit: s

    @Column(ignore = true)
    private transient final HrMAFilter hrMAFilter = new HrMAFilter(HR_MOVE_AVERAGE_FILTER_TIME_SPAN);; // moving average filter
    @Column(ignore = true)
    private final List<HrHistogramBar<Integer>> hrHistogram = new ArrayList<>();
    @Column(ignore = true)
    private transient long hrSum;
    @Column(ignore = true)
    private transient long hrNum;
    @Column(ignore = true)
    private transient long preTime = 0;

    private BleHrRecord10() {
        super(HR);
        initialize();
    }

    private BleHrRecord10(long createTime, String devAddress, Account creator, String note) {
        super(HR, "1.0", createTime, devAddress, creator, note, true);
        initialize();
    }

    private BleHrRecord10(JSONObject json) throws JSONException{
        super(json, false);
        initialize();
        if(json.has("recordSecond"))
            this.recordSecond = json.getInt("recordSecond");
    }

    private void initialize() {
        hrList = new ArrayList<>();
        hrMax = INVALID_HEART_RATE;
        hrAve = INVALID_HEART_RATE;
        hrHist = new ArrayList<>();
        hrHistogram.add(new HrHistogramBar<>((short)0, (short)121, "平静心率", 0));
        hrHistogram.add(new HrHistogramBar<>((short)122, (short)131, "热身放松", 0));
        hrHistogram.add(new HrHistogramBar<>((short)132, (short)141, "有氧燃脂", 0));
        hrHistogram.add(new HrHistogramBar<>((short)142, (short)152, "有氧耐力", 0));
        hrHistogram.add(new HrHistogramBar<>((short)153, (short)162, "无氧耐力", 0));
        hrHistogram.add(new HrHistogramBar<>((short)163, (short)1000, "极限冲刺", 0));
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = super.toJson();
        json.put("hrList", RecordUtil.listToString(hrList));
        json.put("hrMax", hrMax);
        json.put("hrAve", hrAve);
        json.put("hrHist", RecordUtil.listToString(hrHist));
        json.put("recordSecond", recordSecond);
        return json;
    }

    @Override
    public boolean fromJson(JSONObject json) throws JSONException{
        if(json == null) {
            return false;
        }

        String hrListStr = json.getString("hrList");
        hrList.clear();
        String[] strings = hrListStr.split(",");
        for(String str : strings) {
            hrList.add(Short.parseShort(str));
        }

        hrMax = (short)json.getInt("hrMax");
        hrAve = (short)json.getInt("hrAve");

        String hrHistStr = json.getString("hrHist");
        hrHist.clear();
        strings = hrHistStr.split(",");
        for(String str : strings) {
            hrHist.add(Integer.parseInt(str));
        }

        recordSecond = json.getInt("recordSecond");
        createHistogramFromHrHist();

        return save();
    }

    @Override
    public boolean noData() {
        return hrList.isEmpty();
    }

    public List<Short> getHrList() {
        return hrList;
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
    public void setRecordSecond(int recordSecond) {
        this.recordSecond = recordSecond;
    }
    public boolean createHistogramFromHrHist() {
        if(hrHist != null && hrHist.size() == hrHistogram.size()) {
            for (int i = 0; i < hrHistogram.size(); i++) {
                hrHistogram.get(i).setValue(hrHist.get(i));
            }
            return true;
        } else {
            return false;
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
            interval = (tmp > HR_MOVE_AVERAGE_FILTER_TIME_SPAN) ? HR_MOVE_AVERAGE_FILTER_TIME_SPAN : (int)tmp;
        }
        preTime = time;
        for(HrHistogramBar<Integer> bar : hrHistogram) {
            if(hr < bar.getMaxValue()) {
                bar.setValue(bar.getValue() + interval);
                break;
            }
        }

        short fHr = hrMAFilter.process(hr, interval);

        boolean fHrUpdated = (fHr != INVALID_HEART_RATE);
        if(fHrUpdated)
            hrList.add(fHr);
        return fHrUpdated;
    }

    @Override
    public String toString() {
        return super.toString() + "-" + hrList + "-" + hrMax + "-" + hrAve + "-" + hrHist;
    }

}
