package com.cmtech.android.bledevice.ecgmonitor.device;

import android.util.Log;
import android.util.Pair;

import com.cmtech.android.bledeviceapp.util.HttpUtils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Callback;

public class EcgRecordWebBroadcaster {
    private static final int TYPE_CODE_CREATE_CMD = 0;
    private static final int TYPE_CODE_STOP_CMD = 1;
    private static final int TYPE_CODE_BROADCAST_ID = 2;
    private static final int TYPE_CODE_DEVICE_ID = 3;
    private static final int TYPE_CODE_CREATOR_ID = 4;
    private static final int TYPE_CODE_SAMPLE_RATE = 5;
    private static final int TYPE_CODE_CALI_VALUE = 6;
    private static final int TYPE_CODE_LEAD_TYPE = 7;
    private static final int TYPE_CODE_ECG_SIGNAL = 8;
    private static final int TYPE_CODE_HR_VALUE = 9;
    private static final int TYPE_CODE_COMMENTER_ID = 10;
    private static final int TYPE_CODE_COMMENT_CONTENT = 11;
    private static final int TYPE_CODE_RECEIVER_ID = 12;

    private static final String INVALID_BROADCAST_ID = ""; // 无效广播ID

    private EcgRecordWebBroadcaster() {
    }

    // 创建一个心电记录广播
    public static void create(String deviceId, String creatorId, int sampleRate, int caliValue, int leadTypeCode, Callback callback) {
        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_CREATE_CMD, ""));
        data.add(new Pair<>(TYPE_CODE_DEVICE_ID, deviceId));
        data.add(new Pair<>(TYPE_CODE_CREATOR_ID, creatorId));
        data.add(new Pair<>(TYPE_CODE_SAMPLE_RATE, String.valueOf(sampleRate)));
        data.add(new Pair<>(TYPE_CODE_CALI_VALUE, String.valueOf(caliValue)));
        data.add(new Pair<>(TYPE_CODE_LEAD_TYPE, String.valueOf(leadTypeCode)));
        String urlData = createDataUrlString(data);
        HttpUtils.upload(urlData, callback);
    }

    // 停止广播
    public static void stop(String broadcastId) {
        if(broadcastId.equals(INVALID_BROADCAST_ID)) return;

        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_STOP_CMD, ""));
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(dataUrl);
    }

    // 发送心电信号
    public static void sendEcgSignal(String broadcastId, int ecgSignal) {
        if(broadcastId.equals(INVALID_BROADCAST_ID)) return;

        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        data.add(new Pair<>(TYPE_CODE_ECG_SIGNAL, String.valueOf(ecgSignal)));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(dataUrl);
    }

    // 发送心率值
    public static void sendHrValue(String broadcastId, short hr) {
        if(broadcastId.equals(INVALID_BROADCAST_ID)) return;

        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        data.add(new Pair<>(TYPE_CODE_HR_VALUE, String.valueOf(hr)));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(dataUrl);
    }

    // 发送一条留言
    public static void sendComment(String broadcastId, String commenterId, String content) {
        if(broadcastId.equals(INVALID_BROADCAST_ID)) return;

        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        data.add(new Pair<>(TYPE_CODE_COMMENTER_ID, commenterId));
        data.add(new Pair<>(TYPE_CODE_COMMENT_CONTENT, content));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(dataUrl);
    }

    // 添加一个接收者
    public static void addReceiver(String broadcastId, String receiverId) {
        if(broadcastId.equals(INVALID_BROADCAST_ID)) return;

        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        data.add(new Pair<>(TYPE_CODE_RECEIVER_ID, receiverId));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(dataUrl);
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
}
