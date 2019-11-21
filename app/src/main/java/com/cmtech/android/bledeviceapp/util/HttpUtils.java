package com.cmtech.android.bledeviceapp.util;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtils {

    public static String open_id = "";

    public static void Get(String url, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get() //请求参数
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void upload(String baseUrl, Map<String, String> data) {
        upload(baseUrl, data, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });

    }

    public static void upload(String baseUrl, Map<String, String> data, Callback callback) {
        data.put("open_id", open_id);
        String dataUrlString = createDataUrlString(data);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get() //请求参数
                .url(baseUrl + dataUrlString)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static String upload(String data, Callback callback) {
        String result = "OK";
        String url = data;
        try {
            Get(url, callback);
            return result;
        } catch (Exception ex) {
            return "failure:" + ex.getMessage();
        }
    }

    public static String upload(String data) {
        String result = upload(data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
        return result;
    }

    public static String createDataUrlString(Map<String, String> data) {
        if (data == null || data.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry entry : data.entrySet()) {
            if (!isFirst) {
                builder.append("&");
            } else {
                isFirst = false;
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        String rlt = builder.toString();
        Log.e("EcgHttpBroadcast", "DataUrlString = " + rlt);
        return rlt;
    }

    public static String ConvertString(List list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i) + ",");
        }
        return sb.toString();
    }

    public static Map<String, String> parseUrl(String url) {
        Map<String, String> entity = new HashMap<>();
        if (url == null) {
            return entity;
        }
        url = url.trim();
        if (url.equals("")) {
            return entity;
        }
        String[] urlParts = url.split("\\?");
        //  entity.baseUrl = urlParts[0];
        //没有参数
        if (urlParts.length == 1) {
            return entity;
        }
        //有参数
        String[] params = urlParts[1].split("&");

        for (String param : params) {
            String[] keyValue = param.split("=");
            entity.put(keyValue[0], keyValue[1]);
        }

        return entity;
    }

}
