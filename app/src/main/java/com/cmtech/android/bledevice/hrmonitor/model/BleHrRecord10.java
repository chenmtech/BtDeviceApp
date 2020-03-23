package com.cmtech.android.bledevice.hrmonitor.model;

import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.bmefile.DataIOUtil;
import com.vise.log.ViseLog;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.cmtech.android.bledevice.hrmonitor.model.HRMonitorDevice.INVALID_HEART_RATE;
import static com.cmtech.android.bledeviceapp.AppConstant.DIR_CACHE;
import static com.cmtech.android.bledeviceapp.model.Account.PLAT_NAME_CHAR_LEN;
import static com.cmtech.android.bledeviceapp.model.Account.USER_ID_CHAR_LEN;

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
public class BleHrRecord10 extends LitePalSupport {
    public static final int HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH = 10; // unit: s
    private static final byte[] HRR = {'H', 'R', 'R'}; // indication of heart rate record
    private static final int DEVICE_ADDRESS_CHAR_NUM = 12; // char num of device address

    private int id;
    private byte[] ver = new byte[2]; // hr record version
    private long createTime; //
    private String devAddress; //
    private String creatorPlat;
    private String creatorId;
    private List<Short> filterHrList; // list of the filtered HR
    private short hrMax;
    private short hrAve;
    private List<Integer> hrHist; // HR histogram value

    @Column(ignore = true)
    private final HrMAFilter hrMAFilter; // moving average filter
    @Column(ignore = true)
    private final List<HrHistogramElement<Integer>> hrHistogram = new ArrayList<>();
    @Column(ignore = true)
    private long hrSum;
    @Column(ignore = true)
    private long hrNum;
    @Column(ignore = true)
    private long preTime = 0;

    private BleHrRecord10() {
        createTime = 0;
        devAddress = "";
        creatorPlat = "";
        creatorId = "";
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
    }

    public String getRecordName() {
        return createTime + devAddress;
    }
    public int getId() {
        return id;
    }
    public long getCreateTime() {
        return createTime;
    }
    public String getDevAddress() {
        return devAddress;
    }
    public String getCreatorName() {
        return creatorPlat+creatorId;
    }
    public List<Short> getFilterHrList() {
        return filterHrList;
    }
    public short getHrMax() {
        return hrMax;
    }
    public short getHrAve() {
        return hrAve;
    }
    public List<HrHistogramElement<Integer>> getHrHistogram() {
        return hrHistogram;
    }
    public void createHistogram() {
        if(hrHist != null && hrHist.size() == hrHistogram.size()) {
            for (int i = 0; i < hrHistogram.size(); i++) {
                hrHistogram.get(i).histValue = hrHist.get(i);
            }
        }
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

    @Override
    public boolean save() {
        for(int i = 0; i < hrHistogram.size(); i++) {
            hrHist.set(i, hrHistogram.get(i).getHistValue());
        }

        return super.save();
    }

    // create new hr record
    public static BleHrRecord10 create(byte[] ver, String devAddress, Account creator) {
        if(creator == null) {
            throw new NullPointerException("The creator is null.");
        }
        if(DIR_CACHE == null) {
            throw new NullPointerException("The cache dir is null");
        }
        if(ver == null || ver.length != 2 || ver[0] != 0x01 || ver[1] != 0x00) return null;

        BleHrRecord10 record = new BleHrRecord10();
        record.ver[0] = 0x01;
        record.ver[1] = 0x00;
        record.createTime = new Date().getTime();
        record.devAddress = devAddress;
        record.creatorPlat = creator.getPlatName();
        record.creatorId = creator.getUserId();
        for(int i = 0; i < record.hrHistogram.size(); i++)
            record.hrHist.add(0);
        return record;
    }

    // load hr record from a file
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
                record.creatorId = DataIOUtil.readFixedString(raf, USER_ID_CHAR_LEN);
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
            DataIOUtil.writeFixedString(raf, creatorId, USER_ID_CHAR_LEN); // 写设备地址
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
    }

    @Override
    public String toString() {
        return createTime + "-" + devAddress + "-" + creatorPlat + "-" + creatorId + "-" + filterHrList + "-" + hrMax + "-" + hrAve + "-" + hrHist;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        BleHrRecord10 other = (BleHrRecord10) otherObject;
        return getRecordName().equals(other.getRecordName());
    }

    @Override
    public int hashCode() {
        return getRecordName().hashCode();
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
    public static class HrHistogramElement<T> {
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
