package com.cmtech.android.bledeviceapp.global;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.ALL;
import static com.cmtech.android.bledeviceapp.data.record.RecordType.ECG;
import static com.cmtech.android.bledeviceapp.data.record.RecordType.EEG;
import static com.cmtech.android.bledeviceapp.data.record.RecordType.HR;
import static com.cmtech.android.bledeviceapp.data.record.RecordType.PPG;
import static com.cmtech.android.bledeviceapp.data.record.RecordType.PTT;
import static com.cmtech.android.bledeviceapp.data.record.RecordType.THERMO;

import android.os.Environment;

import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledevice.eeg.model.EegFactory;
import com.cmtech.android.bledevice.hrm.model.HrmFactory;
import com.cmtech.android.bledevice.ppg.model.PpgFactory;
import com.cmtech.android.bledevice.ptt.model.PttFactory;
import com.cmtech.android.bledevice.thermo.model.ThermoFactory;
import com.cmtech.android.bledevice.thm.model.ThmFactory;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.RecordType;
import com.cmtech.android.bledeviceapp.model.DeviceType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * AppConstant: App constant
 * Created by bme on 2018/3/1.
 */

public class AppConstant {
    public static final int INVALID_ID = -1;
    public static final int INVALID_POS = -1;
    public static final long INVALID_TIME = -1;

    // 无效心率值
    public static final short INVALID_HR = -1;
    public static final String MY_BASE_UUID = "0a20XXXX-cce5-4025-a156-38ea833f6ef8"; // my base UUID
    public static final String STANDARD_BLE_UUID = "0000XXXX-0000-1000-8000-00805F9B34FB"; // standard BLE UUID
    public static final UUID CCC_UUID = UuidUtil.stringToUUID("2902", STANDARD_BLE_UUID); // client characteristic config UUID
    public static final int RECONNECT_INTERVAL = 6000; // reconnect interval, unit: millisecond
    public static final int SCAN_DURATION = 20000; // scan duration, unit: millisecond
    public static final File DIR_IMAGE = MyApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES); // image file directory
    public static final File DIR_DOC = MyApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS); // documents file directory
    public static final File DIR_CACHE = MyApplication.getContext().getExternalCacheDir(); // file cache directory
    public static final int SPLASH_ACTIVITY_COUNT_DOWN_SECOND = 3; // count down second in splash activity
    public static final String KMIC_STORE_URI = "https://decathlon.tmall.com/shop/view_shop.htm?spm=a21bo.2017.201863-1.d2.6dd211d9AzJgBt&user_number_id=352469034&pvid=067004f4-d493-413a-a4f7-003e62637549&pos=2&brandId=44506&acm=03014.1003.1.765824&scm=1007.13143.56636.100200300000000";
    public static final List<DeviceType> SUPPORT_DEVICE_TYPES = new ArrayList<DeviceType>(){
        {
            add(HrmFactory.HRM_DEVICE_TYPE);
            add(ThermoFactory.THERMO_DEVICE_TYPE);
            add(ThmFactory.THM_DEVICE_TYPE);
            add(EegFactory.EEG_DEVICE_TYPE);
            add(PpgFactory.PPG_DEVICE_TYPE);
            add(PttFactory.PTT_DEVICE_TYPE);
        }
    }; // supported device types

    // supported record types
    public static final RecordType[] SUPPORT_RECORD_TYPES = new RecordType[]{ALL, HR, ECG, THERMO, EEG, PPG, PTT};

    public static final String QQ_PLAT_NAME = QQ.NAME;
    public static final String WX_PLAT_NAME = Wechat.NAME;
    public static final String PHONE_PLAT_NAME = "PH";
    public static final Map<String, Integer> SUPPORT_LOGIN_PLATFORM = new HashMap<String, Integer>() {
        {
            put(QQ_PLAT_NAME, R.mipmap.ic_qq);
            put(WX_PLAT_NAME, R.mipmap.ic_wechat);
            put(PHONE_PLAT_NAME, R.mipmap.ic_user);
        }
    }; // supported login platform

    //public static final String KMIC_URL = "http://203.195.137.198/BtDeviceWebApp/";
    public static final String KMIC_URL = "http://kmic.xyz/BtDeviceWebApp/";

}
