package com.cmtech.android.bledeviceapp.util;

import com.vise.log.ViseLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtils {
    private static OkHttpClient client = new OkHttpClient();

    public static void upload(String baseUrl, Map<String, String> data, Callback callback) {
        String url = baseUrl + convertString(data);
        Get(url, callback);
        ViseLog.e(url);
    }

    public static String upload(String url, Callback callback) {
        ViseLog.e(url);
        String result = "OK";
        try {
            Get(url, callback);
            return result;
        } catch (Exception ex) {
            return "failure:" + ex.getMessage();
        }
    }

    private static void Get(String url, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get() //请求参数
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

    private static String convertString(Map<String, String> data) {
        if (data == null || data.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry entry : data.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append("&");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return builder.toString();
    }

    public static String convertString(List list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i)).append(",");
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
            if(keyValue.length == 1)
                entity.put(keyValue[0], "");
            else
                entity.put(keyValue[0], keyValue[1]);
        }

        return entity;
    }

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void postRequest(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

}
