package com.cmtech.android.bledevice.hrmonitor.model;

import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.bmefile.DataIOUtil;

import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    private static final byte[] HRR = {'H', 'R', 'R'}; // indication of heart rate record
    private static final int DEVICE_ADDRESS_CHAR_NUM = 12; // 设备地址字符数

    private int id;
    private byte[] ver = new byte[2]; // hr record version
    private long createTime; // 创建时间
    private String devAddress; // 设备地址
    private String creatorPlat;
    private String creatorId;
    private List<Short> hrList; // 心率列表

    private BleHrRecord10() {
        createTime = 0;
        devAddress = "";
        creatorPlat = "";
        creatorId = "";
        hrList = null;
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

    public int getId() {
        return id;
    }
    public String getRecordName() {
        return devAddress + createTime;
    }
    public String getCreatorName() {
        return creatorPlat+creatorId;
    }
    public long getCreateTime() {
        return createTime;
    }
    public String getDevAddress() {
        return devAddress;
    }
    public List<Short> getHrList() {
        return hrList;
    }
    public void setHrList(List<Short> hrList) {
        this.hrList = hrList;
    }

    @Override
    public String toString() {
        return devAddress + "-" + createTime + "-" + creatorPlat + "-" + creatorId + "-" + hrList;
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
}
