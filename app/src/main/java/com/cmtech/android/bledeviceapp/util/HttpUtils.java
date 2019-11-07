package com.cmtech.android.bledeviceapp.util;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HttpUtils {
    public static final  String  upload_url="http://huawei.tighoo.com/home/upload";

    public static void Get(String url, Callback callback) throws Exception {
        OkHttpClient client  = new OkHttpClient();
        Request request = new Request.Builder()
                .get() //请求参数
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public  static  String  upload(String data,Callback callback)
    {
        String result="OK";
        String url =  upload_url+"?data="+ data;
        try {
            Get(url, callback);
            return result;
        }catch (Exception ex)
        {
            return "failure:"+ex.getMessage();
        }
    }

    public   static   String  upload(String data)
    {
         String result =  upload(data, new Callback() {
             @Override
             public void onFailure(Call call, IOException e) {

             }

             @Override
             public void onResponse(Call call, Response response) throws IOException {

             }
        });
         return result;
    }
}
