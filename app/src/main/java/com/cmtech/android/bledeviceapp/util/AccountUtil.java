package com.cmtech.android.bledeviceapp.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AccountUtil {
    private static final String getuser_url = "http://huawei.tighoo.com/home/GetUsers?";

    interface IGetUserInfoCallback {
        void onReceived(String userId, String name, String description);
    }

    public static void getUserInfo(String userId, final IGetUserInfoCallback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("user_id", userId);
        HttpUtils.upload(getuser_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(callback != null) {
                    callback.onReceived(null, null, null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                if(callback != null) {
                    callback.onReceived(null, null, null);
                }
            }
        });
    }
}
