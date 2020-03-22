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
    private static final int DEVICE_ADDRESS_CHAR_NUM = 12; // 设备地址字符数

    private int id;
    private byte[] ver = new byte[2]; // hr record version
    private long createTime; // 创建时间
    private String devAddress; // 设备地址
    private String creatorPlat;
    private String creatorId;
    private List<Short> hrList; // 心率列表
    private short hrMax = INVALID_HEART_RATE;
    private short hrAve = INVALID_HEART_RATE;
    private List<Integer> hrHist;

    @Column(ignore = true)
    private final HrMAFilter hrMAFilter;
    @Column(ignore = true)
    private final List<HrHistogramElement<Integer>> hrHistogram = new ArrayList<>();

    private BleHrRecord10() {
        createTime = 0;
        devAddress = "";
        creatorPlat = "";
        creatorId = "";
        hrList = null;
        hrMax = 0;
        hrAve = 0;
        hrHist = null;
        hrMAFilter = new HrMAFilter(HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH);
        hrHistogram.add(new HrHistogramElement<>((short)0, (short)121, 0, "平静心率"));
        hrHistogram.add(new HrHistogramElement<>((short)122, (short)131, 0, "热身放松"));
        hrHistogram.add(new HrHistogramElement<>((short)132, (short)141, 0, "有氧燃脂"));
        hrHistogram.add(new HrHistogramElement<>((short)142, (short)152, 0, "有氧耐力"));
        hrHistogram.add(new HrHistogramElement<>((short)153, (short)162, 0, "无氧耐力"));
        hrHistogram.add(new HrHistogramElement<>((short)163, (short)1000, 0, "极限冲刺"));
    }

    public String getRecordName() {
        return devAddress + createTime;
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
    public List<Short> getHrList() {
        return hrList;
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
    public void updateHrHistogram() {
        if(hrHist != null && hrHist.size() == hrHistogram.size()) {
            for (int i = 0; i < hrHistogram.size(); i++) {
                hrHistogram.get(i).setHistValue(hrHist.get(i));
            }
        }
    }

    public boolean process(short hr, long time) {
        short fHr = hrMAFilter.process(hr, time);
        if(fHr != INVALID_HEART_RATE) {
            hrList.add(fHr);
            return true;
        }
        return false;
    }

    @Override
    public boolean save() {
        hrHist = new ArrayList<>();
        for(HrHistogramElement<Integer> ele : hrHistogram) {
            hrHist.add(ele.getHistValue());
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
        BleHrRecord10 record = null;
        if(Arrays.equals(ver, new byte[]{0x01, 0x00})) {
            record = new BleHrRecord10();
            record.ver = Arrays.copyOf(ver, 2);
        } else {
            return null;
        }
        record.createTime = new Date().getTime();
        record.devAddress = devAddress;
        record.creatorPlat = creator.getPlatName();
        record.creatorId = creator.getUserId();
        record.hrList = new ArrayList<>();
        record.hrHist = new ArrayList<>();
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
                record.hrList = new ArrayList<>();
                while (true) {
                    try {
                        record.hrList.add(raf.readShort());
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
            for(short hr : hrList) {
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
        return devAddress + "-" + createTime + "-" + creatorPlat + "-" + creatorId + "-" + hrList + "-" + hrMax + "-" + hrAve + "-" + hrHist;
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

    public class HrMAFilter {
        private final int filterWidth; // hr filter width, unit: second
        private long hrSum;
        private long hrNum;
        private long preTime = 0;

        private int sumTmp = 0;
        private int numTmp = 0;
        private int periodTmp = 0;

        HrMAFilter(int filterWidth) {
            this.filterWidth = filterWidth;
        }

        public short process(short hr, long time) {
            hrSum += hr;
            hrNum++;
            if(hrMax < hr) hrMax = hr;
            hrAve = (short)(hrSum/hrNum);

            long tmp = Math.round((time-preTime)/1000.0); // ms to second
            int interval = (tmp > filterWidth) ? filterWidth : (int)tmp;
            preTime = time;
            for(HrHistogramElement<Integer> ele : hrHistogram) {
                if(hr < ele.maxValue) {
                    ele.histValue += interval;
                    break;
                }
            }

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
            ViseLog.e("" + interval + " " + periodTmp + " " + filteredHr);
            return filteredHr;
        }
    }

    // 心率直方图的Element类
    public static class HrHistogramElement<T> {
        private final short minValue;
        private final short maxValue;
        private final String barTitle; // histogram bar title string
        private T histValue; // 直方图值

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
        public void setHistValue(T histValue) {
            this.histValue = histValue;
        }
        short getMinValue() {
            return minValue;
        }
        short getMaxValue() {
            return maxValue;
        }

    }

}
