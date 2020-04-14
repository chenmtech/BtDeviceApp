package com.cmtech.android.bledeviceapp.model;

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
        data.put("platName", platName);
        data.put("platId", platId);
        HttpUtils.requestGet(KMURL + "SignUp?", data, callback);
    }

    public static Map<String, Object> parseSignUpJsonResponse(String jsonStr) {
        Map<String, Object> map = new HashMap<>();
        try {
            JSONObject json = new JSONObject(jsonStr);
            boolean isSuccess = Boolean.parseBoolean(json.getString("isSuccess"));
            String errStr = json.getString("errStr");
            map.put("isSuccess", isSuccess);
            map.put("errStr", errStr);
            return map;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
