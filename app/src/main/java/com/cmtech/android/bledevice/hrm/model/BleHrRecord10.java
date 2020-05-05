package com.cmtech.android.bledevice.hrm.model;

import com.cmtech.android.bledevice.common.AbstractRecord;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.annotation.Column;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledevice.common.RecordType.HR;
import static com.cmtech.android.bledevice.hrm.model.HrmDevice.INVALID_HEART_RATE;
import static com.cmtech.android.bledevice.hrm.model.RecordWebAsyncTask.DOWNLOAD_NUM_PER_TIME;
import static com.cmtech.android.bledeviceapp.AppConstant.DIR_CACHE;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      BleHrFile
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/20 下午3:17
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/20 下午3:17
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleHrRecord10 extends AbstractRecord implements Serializable {
    public static final int HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH = 10; // unit: s
    private static final byte[] HRR = {'H', 'R', 'R'}; // indication of heart rate record
    private static final int DEVICE_ADDRESS_CHAR_NUM = 12; // char num of device address

    private List<Short> filterHrList; // list of the filtered HR
    private short hrMax;
    private short hrAve;
    private List<Integer> hrHist; // HR histogram value
    private int recordSecond; // unit: s

    @Column(ignore = true)
    private transient final HrMAFilter hrMAFilter; // moving average filter
    @Column(ignore = true)
    private final List<HrHistogramElement<Integer>> hrHistogram = new ArrayList<>();
    @Column(ignore = true)
    private transient long hrSum;
    @Column(ignore = true)
    private transient long hrNum;
    @Column(ignore = true)
    private transient long preTime = 0;

    public BleHrRecord10(long createTime, String devAddress, Account creator) {
        super(HR, "1.0", createTime, devAddress, creator);
        filterHrList = new ArrayList<>();
        hrMax = INVALID_HEART_RATE;
        hrAve = INVALID_HEART_RATE;
        hrHist = new ArrayList<>();
        hrMAFilter = new HrMAFilter(HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH);
        hrHistogram.add(new HrHistogramElement<>((short)0, (short)121, 0, "平静心率"));
        hrHistogram.add(new HrHistogramElement<>((short)122, (short)131, 0, "热身放松"));
        hrHistogram.add(new HrHistogramElement<>((short)132, (short)141, 0, "有氧燃脂"));
        hrHistogram.add(new HrHistogramElement<>((short)142, (short)152, 0, "有氧耐力"));
        hrHistogram.add(new HrHistogramElement<>((short)153, (short)162, 0, "无氧耐力"));
        hrHistogram.add(new HrHistogramElement<>((short)163, (short)1000, 0, "极限冲刺"));
        recordSecond = 0;
    }

    @Override
    public String getDesc() {
        int time = (getRecordSecond() <= 60) ? 1 : getRecordSecond()/60;
        return "时长约"+time+"分钟";
    }

    public List<Short> getFilterHrList() {
        return filterHrList;
    }
    public void setFilterHrList(List<Short> filterHrList) {
        this.filterHrList = filterHrList;
    }
    public short getHrMax() {
        return hrMax;
    }
    public void setHrMax(short hrMax) {
        this.hrMax = hrMax;
    }
    public short getHrAve() {
        return hrAve;
    }
    public void setHrAve(short hrAve) {
        this.hrAve = hrAve;
    }
    public List<Integer> getHrHist() {
        return hrHist;
    }
    public void setHrHist(List<Integer> hrHist) {
        this.hrHist = hrHist;
    }
    public List<HrHistogramElement<Integer>> getHrHistogram() {
        return hrHistogram;
    }
    public int getRecordSecond() {
        return recordSecond;
    }
    public void setRecordSecond(int recordSecond) {
        this.recordSecond = recordSecond;
    }
    public void createHistogram() {
        if(hrHist != null && hrHist.size() == hrHistogram.size()) {
            for (int i = 0; i < hrHistogram.size(); i++) {
                hrHistogram.get(i).histValue = hrHist.get(i);
            }
        }
    }

    public boolean isDataEmpty() {
        return filterHrList.isEmpty();
    }

    public boolean process(short hr, long time) {
        if(hrMax < hr) hrMax = hr;
        hrSum += hr;
        hrNum++;
        hrAve = (short)(hrSum/hrNum);

        long tmp;
        if(preTime == 0) tmp = 2; // the first HR
        else tmp = Math.round((time-preTime)/1000.0); // ms to second
        int interval = (tmp > HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH) ? HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH : (int)tmp;
        preTime = time;
        for(HrHistogramElement<Integer> ele : hrHistogram) {
            if(hr < ele.maxValue) {
                ele.histValue += interval;
                break;
            }
        }

        short fHr = hrMAFilter.process(hr, interval);
        if(fHr != INVALID_HEART_RATE) {
            filterHrList.add(fHr);
            return true;
        }
        return false;
    }

    public static BleHrRecord10 createFromJson(JSONObject json) {
        if(json == null) {
            throw new NullPointerException("The json is null.");
        }

        try {
            String devAddress = json.getString("devAddress");
            long createTime = json.getLong("createTime");
            Account account = AccountManager.getAccount();
            int recordSecond = json.getInt("recordSecond");

            BleHrRecord10 newRecord = new BleHrRecord10(createTime, devAddress, account);
            newRecord.setRecordSecond(recordSecond);
            return newRecord;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<BleHrRecord10> createFromLocalDb(Account creator, long fromTime, int num) {
        return LitePal.select("createTime, devAddress, creatorPlat, creatorId, recordSecond")
                .where("creatorPlat = ? and creatorId = ? and createTime < ?", creator.getPlatName(), creator.getPlatId(), ""+fromTime)
                .order("createTime desc").limit(num).find(BleHrRecord10.class);
    }

    public JSONObject toJson() {
        JSONObject jsonObject = super.toJson();
        if(jsonObject == null) return null;
        try {
            jsonObject.put("recordTypeCode", getTypeCode());
            StringBuilder builder = new StringBuilder();
            for(Short ele : filterHrList) {
                builder.append(ele).append(',');
            }
            jsonObject.put("filterHrList", builder.toString());
            jsonObject.put("hrMax", hrMax);
            jsonObject.put("hrAve", hrAve);
            builder = new StringBuilder();
            for(Integer ele : hrHist) {
                builder.append(ele).append(',');
            }
            jsonObject.put("hrHist", builder.toString());
            jsonObject.put("recordSecond", recordSecond);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean setDataFromJson(JSONObject json) {
        if(json == null) {
            throw new NullPointerException("The json is null.");
        }

        try {
            String filterHrListStr = json.getString("filterHrList");
            List<Short> filterHrList = new ArrayList<>();
            String[] strings = filterHrListStr.split(",");
            for(String str : strings) {
                filterHrList.add(Short.parseShort(str));
            }
            this.filterHrList = filterHrList;
            hrMax = (short)json.getInt("hrMax");
            hrAve = (short)json.getInt("hrAve");
            String hrHistStr = json.getString("hrHist");
            List<Integer> hrHist = new ArrayList<>();
            String[] strings1 = hrHistStr.split(",");
            for(String str : strings1) {
                hrHist.add(Integer.parseInt(str));
            }
            this.hrHist = hrHist;
            recordSecond = json.getInt("recordSecond");
            createHistogram();
            return save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

   /* // load hr record from a file
    public static BleHrRecord10 load(String fileName) {
        File file = new File(fileName);
        RandomAccessFile raf = null;
        try {
            if(file.exists() && file.renameTo(file)) {
                raf = new RandomAccessFile(file, "r");
                // read indication
                byte[] hrr = new byte[3];
                raf.readFully(hrr);
                if(!Arrays.equals(hrr, HRR)) {
                    return null;
                }
                // read hr record version
                byte[] ver = new byte[2];
                raf.readFully(ver);
                if(!Arrays.equals(ver, new byte[]{0x01, 0x00})) {
                    return null;
                }
                BleHrRecord10 record = new BleHrRecord10();
                record.createTime = raf.readLong();
                record.devAddress = DataIOUtil.readFixedString(raf, DEVICE_ADDRESS_CHAR_NUM);
                record.creatorPlat = DataIOUtil.readFixedString(raf, PLAT_NAME_CHAR_LEN);
                record.creatorId = DataIOUtil.readFixedString(raf, PLAT_ID_CHAR_LEN);
                // 读心率信息
                record.filterHrList = new ArrayList<>();
                while (true) {
                    try {
                        record.filterHrList.add(raf.readShort());
                    } catch (IOException e) {
                        return record;
                    }
                }
            } else {
                throw new IOException("The hr record file can't be opened.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if(raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // save hr record as a file
    public boolean save(String fileName) {
        File file = new File(fileName);
        RandomAccessFile raf = null;
        try {
            if(file.exists()) file.delete();
            if(!file.createNewFile()) {
                throw new IOException();
            }
            raf = new RandomAccessFile(file, "rw");
            raf.write(HRR); // write HRR indication
            raf.write(ver); // write hr record version
            raf.writeLong(createTime); // 写创建时间
            DataIOUtil.writeFixedString(raf, devAddress, DEVICE_ADDRESS_CHAR_NUM); // 写设备地址
            DataIOUtil.writeFixedString(raf, creatorPlat, PLAT_NAME_CHAR_LEN); // 写设备地址
            DataIOUtil.writeFixedString(raf, creatorId, PLAT_ID_CHAR_LEN); // 写设备地址
            // 写心率信息
            for(short hr : filterHrList) {
                raf.writeShort(hr);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            if(file.exists()) file.delete();
            return false;
        } finally {
            try {
                if(raf != null)
                    raf.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }*/

    @Override
    public String toString() {
        return super.toString() + "-" + filterHrList + "-" + hrMax + "-" + hrAve + "-" + hrHist;
    }

    // moving average filter
    public static class HrMAFilter {
        private final int filterWidth; // hr filter width, unit: second

        private int sumTmp = 0;
        private int numTmp = 0;
        private int periodTmp = 0;

        HrMAFilter(int filterWidth) {
            this.filterWidth = filterWidth;
        }

        public short process(short hr, int interval) {
            short filteredHr = INVALID_HEART_RATE;
            sumTmp += hr;
            numTmp++;
            periodTmp += interval;
            if(periodTmp >= filterWidth) {
                filteredHr = (short)(sumTmp / numTmp);
                periodTmp -= filterWidth;
                sumTmp = 0;
                numTmp = 0;
            }
            ViseLog.e(interval + " " + periodTmp + " " + filteredHr);
            return filteredHr;
        }
    }

    // HR histogram element
    public static class HrHistogramElement<T> implements Serializable{
        private final short minValue;
        private final short maxValue;
        private final String barTitle; // histogram bar title string
        private T histValue; // histogram value

        HrHistogramElement(short minValue, short maxValue, T histValue, String barTitle) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.histValue = histValue; // unit: s
            this.barTitle = barTitle;
        }

        public String getBarTitle() {
            return barTitle;
        }
        public T getHistValue() {
            return histValue;
        }
        short getMinValue() {
            return minValue;
        }
        short getMaxValue() {
            return maxValue;
        }
    }

}
