package com.cmtech.android.bledevice.ecgmonitor.device;

import android.util.Log;
import android.util.Pair;

import com.cmtech.android.bledeviceapp.util.HttpUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class EcgRecordWebBroadcaster {
    private static final String TAG = "EcgRecordWebBroadcaster";
    public static final  String  upload_url = "http://huawei.tighoo.com/home/upload?";

    private static final int TYPE_CODE_START_CMD = 0; // 启动命令
    private static final int TYPE_CODE_STOP_CMD = 1; // 停止命令
    private static final int TYPE_CODE_BROADCAST_ID = 2; // 广播ID
    private static final int TYPE_CODE_DEVICE_ID = 3; // 设备ID
    private static final int TYPE_CODE_CREATOR_ID = 4; // 创建者ID
    private static final int TYPE_CODE_SAMPLE_RATE = 5; // 采样率
    private static final int TYPE_CODE_CALI_VALUE = 6; // 标定值
    private static final int TYPE_CODE_LEAD_TYPE = 7; // 导联类型
    private static final int TYPE_CODE_ECG_SIGNAL = 8; // 心电信号
    private static final int TYPE_CODE_HR_VALUE = 9; // 心率值
    private static final int TYPE_CODE_COMMENTER_ID = 10; // 留言人ID
    private static final int TYPE_CODE_COMMENT_CONTENT = 11; // 留言内容
    private static final int TYPE_CODE_RECEIVER_ID = 12; // 接收者ID
    private static final int TYPE_CODE_DATA = 13;

    private static final String INVALID_BROADCAST_ID = ""; // 无效广播ID

    private String broadcastId;
    private final String deviceId;
    private final String creatorId;
    private final int sampleRate;
    private final int caliValue;
    private final int leadTypeCode;
    private final List<Integer> ecgBuffer; // 心电缓存
    private final List<Short> hrBuffer; // 心率缓存
    private volatile boolean sending;

    public EcgRecordWebBroadcaster(String deviceId, String creatorId, int sampleRate, int caliValue, int leadTypeCode) {
        this.broadcastId = INVALID_BROADCAST_ID;
        this.deviceId = deviceId;
        this.creatorId = creatorId;
        this.sampleRate = sampleRate;
        this.caliValue = caliValue;
        this.leadTypeCode = leadTypeCode;
        this.ecgBuffer = new ArrayList<>();
        this.hrBuffer = new ArrayList<>();
        sending = false;
    }

    // 启动广播
    public void start() {
        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_START_CMD, ""));
        data.add(new Pair<>(TYPE_CODE_DEVICE_ID, deviceId));
        data.add(new Pair<>(TYPE_CODE_CREATOR_ID, creatorId));
        data.add(new Pair<>(TYPE_CODE_SAMPLE_RATE, String.valueOf(sampleRate)));
        data.add(new Pair<>(TYPE_CODE_CALI_VALUE, String.valueOf(caliValue)));
        data.add(new Pair<>(TYPE_CODE_LEAD_TYPE, String.valueOf(leadTypeCode)));
        String urlData = createDataUrlString(data);
        HttpUtils.upload(upload_url + urlData, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "broadcast start fail.");
                broadcastId = INVALID_BROADCAST_ID;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "broadcast started.");
                // 这里解析Response中的broadcastId，暂时用deviceId代替
                broadcastId = deviceId;
            }
        });
    }

    // 停止广播
    public void stop() {
        if(broadcastId.equals(INVALID_BROADCAST_ID)) return;

        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_STOP_CMD, ""));
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(upload_url + dataUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "broadcast stop fail.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "broadcast stopped.");
                broadcastId = INVALID_BROADCAST_ID;
            }
        });
    }

    // 发送心电信号
    public void sendEcgSignal(int ecgSignal) {
        if(broadcastId.equals(INVALID_BROADCAST_ID)) return;

        ecgBuffer.add(ecgSignal);
        send();
    }

    // 发送心率值
    public void sendHrValue(short hr) {
        if(broadcastId.equals(INVALID_BROADCAST_ID)) return;

        hrBuffer.add(hr);
        send();
    }

    private void send() {
        if(ecgBuffer.size() >= sampleRate && !sending) {
            sending = true;
            String ecgStr = ConvertString(ecgBuffer);
            String hrStr = ConvertString(hrBuffer);
            String sendData = "type=" + TYPE_CODE_DATA + "&deviceId=" + broadcastId +
                    "&data="+hrStr+";"+ecgStr;
            HttpUtils.upload(upload_url + sendData, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    sending = false;
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    sending = false;
                    ecgBuffer.clear();
                    hrBuffer.clear();
                }
            });
        }
    }

    // 发送一条留言
    public void sendComment(String commenterId, String content) {
        if(broadcastId.equals(INVALID_BROADCAST_ID)) return;

        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        data.add(new Pair<>(TYPE_CODE_COMMENTER_ID, commenterId));
        data.add(new Pair<>(TYPE_CODE_COMMENT_CONTENT, content));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(upload_url + dataUrl);
    }

    // 添加一个接收者
    public void addReceiver(String receiverId) {
        if(broadcastId.equals(INVALID_BROADCAST_ID)) return;

        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        data.add(new Pair<>(TYPE_CODE_RECEIVER_ID, receiverId));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(upload_url + dataUrl);
    }

    private static String createDataUrlString(List<Pair<Integer, String>> data) {
        if(data == null || data.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < data.size(); i++) {
            Pair<Integer, String> ele = data.get(i);
            if(i != 0) builder.append("&");
            builder.append(ele.first).append("=").append(ele.second);
        }
        String rlt = builder.toString();
        Log.e("EcgRecordWebBroadcaster", "DataUrlString = " + rlt);
        return rlt;
    }

    private  static String ConvertString(List list)
    {
        if(list == null || list.isEmpty())
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<list.size();i++)
        {
            sb.append(list.get(i)+",");
        }
        return sb.toString();
    }
}
