package com.cmtech.android.bledeviceapp.util;

import com.vise.log.ViseLog;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.Companion.parse("application/json; charset=utf-8");

    public static void requestGet(String baseUrl, Map<String, String> requestData, Callback callback) {
        String url = baseUrl + convertToString(requestData);
        requestGet(url, callback);
        ViseLog.e(url);
    }

    public static void requestGet(String url, Callback callback) {
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static Response requestGet(String baseUrl, Map<String, String> requestData) throws IOException{
        String url = baseUrl + convertToString(requestData);
        return requestGet(url);
    }

    public static Response requestGet(String url) throws IOException{
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();

        return client.newCall(request).execute();
    }

    public static void requestPost(String url, JSONObject json, Callback callback) {
        RequestBody body = RequestBody.Companion.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static Response requestPost(String url, JSONObject json) throws IOException {
        RequestBody body = RequestBody.Companion.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return client.newCall(request).execute();
    }

    private static String convertToString(Map<String, String> data) {
        if (data == null || data.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append("&");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return builder.toString();
    }

    public static String convertToString(List list) {
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
}
