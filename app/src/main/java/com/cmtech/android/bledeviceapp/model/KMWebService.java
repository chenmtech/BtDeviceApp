package com.cmtech.android.bledeviceapp.model;

import com.cmtech.android.bledevice.hrm.model.BleEcgRecord10;
import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.log.ViseLog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledeviceapp.AppConstant.KMURL;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.model
 * ClassName:      KMWebService
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/14 上午10:51
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/14 上午10:51
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class KMWebService {
    public static void signUp(String platName, String platId, Callback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("cmd", "signUp");
        data.put("platName", platName);
        data.put("platId", platId);
        HttpUtils.requestGet(KMURL + "Account?", data, callback);
    }

    public static void findRecord(int recordTypeCode, long createTime, String devAddress, Callback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("recordTypeCode", String.valueOf(recordTypeCode));
        data.put("createTime", String.valueOf(createTime));
        data.put("devAddress", devAddress);
        HttpUtils.requestGet(KMURL + "Record?", data, callback);
    }

    public static void uploadRecord(String platName, String platId, BleEcgRecord10 record, Callback callback) {
        JSONObject json = record.toJson();
        try {
            json.put("platName", platName);
            json.put("platId", platId);
            HttpUtils.requestPost(KMURL + "Record?", json, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
