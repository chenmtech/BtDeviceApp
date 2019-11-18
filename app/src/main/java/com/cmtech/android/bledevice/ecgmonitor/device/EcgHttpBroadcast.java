package com.cmtech.android.bledevice.ecgmonitor.device;

import android.util.Log;

import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.log.ViseLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 *
 * ClassName:      EcgHttpBroadcast
 * Description:    心电Http广播
 * Author:         chenm
 * CreateDate:     2019/11/16 上午4:52
 * UpdateUser:     chenm
 * UpdateDate:     2019/11/16 上午4:52
 * UpdateRemark:   优化代码
 * Version:        1.0
 */

public class EcgHttpBroadcast {
    private static final String TAG = "EcgHttpBroadcast";
    public static final  String  upload_url = "http://huawei.tighoo.com/home/upload?";

    private static final String TYPE_START_CMD = "START"; // 启动命令
    private static final String TYPE_STOP_CMD = "STOP"; // 停止命令
    private static final String TYPE_BROADCAST_ID = "BRID"; // 广播ID
    private static final String TYPE_DEVICE_ID = "DEID"; // 设备ID
    private static final String TYPE_CREATOR_ID = "CRID"; // 创建者ID
    private static final String TYPE_SAMPLE_RATE = "SR"; // 采样率
    private static final String TYPE_CALI_VALUE = "CALI"; // 标定值
    private static final String TYPE_LEAD_TYPE = "LEAD"; // 导联类型
    private static final String TYPE_ECG_SIGNAL = "ECG"; // 心电信号
    private static final String TYPE_HR_VALUE = "HR"; // 心率值
    private static final String TYPE_COMMENTER_ID = "COID"; // 留言人ID
    private static final String TYPE_COMMENT_CONTENT = "CONT"; // 留言内容
    private static final String TYPE_RECEIVER_ID = "REID"; // 接收者ID

    private static final int TYPE_CODE_DATA = 0;
    private static final String INVALID_BROADCAST_ID = ""; // 无效广播ID

    private String broadcastId; // 广播ID, 在调用start后，由服务器返回。现在暂时用deviceId代替。
    private final String deviceId; // 设备ID
    private final String creatorId; // 创建人ID
    private final int sampleRate; // 采样率
    private final int caliValue; // 标定值
    private final int leadTypeCode; // 导联类型码
    private final List<Integer> ecgBuffer; // 心电缓存
    private final List<Short> hrBuffer; // 心率缓存
    private volatile boolean waitingDataResponse; // 是否在等待数据响应

    public EcgHttpBroadcast(String deviceId, String creatorId, int sampleRate, int caliValue, int leadTypeCode) {
        this.broadcastId = INVALID_BROADCAST_ID;
        this.deviceId = deviceId;
        this.creatorId = creatorId;
        this.sampleRate = sampleRate;
        this.caliValue = caliValue;
        this.leadTypeCode = leadTypeCode;
        this.ecgBuffer = new ArrayList<>();
        this.hrBuffer = new ArrayList<>();
        waitingDataResponse = false;
    }

    /**
     * 广播是否已停止
     * @return
     */
    private boolean isStop() {
        return broadcastId.equals(INVALID_BROADCAST_ID);
    }

    /**
     * 启动广播，这里上传广播相关参数。服务器端接收后，如果允许广播，应该在Response中返回广播ID
     */
    public void start() {
        if(!isStop()) return; // 不能重复启动

        Map<String, String> data = new HashMap<>();
        data.put(TYPE_START_CMD, "");
        data.put(TYPE_DEVICE_ID, deviceId);
        data.put(TYPE_CREATOR_ID, creatorId);
        data.put(TYPE_SAMPLE_RATE, String.valueOf(sampleRate));
        data.put(TYPE_CALI_VALUE, String.valueOf(caliValue));
        data.put(TYPE_LEAD_TYPE, String.valueOf(leadTypeCode));
        String urlData = createDataUrlString(data);
        HttpUtils.upload(upload_url + urlData, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "broadcast start fail.");
                broadcastId = INVALID_BROADCAST_ID;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                // 这里解析出broadcastId，暂时用deviceId代替
                broadcastId = deviceId;
                Log.e(TAG, "broadcast started: " + broadcastId);
            }
        });
    }

    /**
     * 停止广播
     */
    public void stop() {
        if(isStop()) return;

        Map<String, String> data = new HashMap<>();
        data.put(TYPE_STOP_CMD, "");
        data.put(TYPE_BROADCAST_ID, broadcastId);
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

    /**
     * 发送心电信号
     * @param ecgSignal
     */
    public void sendEcgSignal(int ecgSignal) {
        if(isStop()) return;

        ecgBuffer.add(ecgSignal);
        send();
    }

    /**
     * 发送心率值
     * @param hr
     */
    public void sendHrValue(short hr) {
        if(isStop()) return;

        hrBuffer.add(hr);
        send();
    }

    private void send() {
        if(ecgBuffer.size() >= sampleRate && !waitingDataResponse) {
            waitingDataResponse = true;
            String ecgStr = ConvertString(ecgBuffer);
            String hrStr = ConvertString(hrBuffer);
            String sendData = "type=" + TYPE_CODE_DATA + "&deviceId=" + broadcastId +
                    "&data="+hrStr+";"+ecgStr;
            HttpUtils.upload(upload_url + sendData, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ViseLog.e(TAG, "Send data fail.");
                    waitingDataResponse = false;
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    ViseLog.e("Send data success.");
                    waitingDataResponse = false;
                    ecgBuffer.clear();
                    hrBuffer.clear();
                }
            });
        }
    }

    /**
     * 发送一条留言
     * @param commenterId : 留言人ID
     * @param content : 留言内容
     */
    public void sendComment(String commenterId, String content) {
        if(isStop()) return;

        Map<String, String> data = new HashMap<>();
        data.put(TYPE_BROADCAST_ID, broadcastId);
        data.put(TYPE_COMMENTER_ID, commenterId);
        data.put(TYPE_COMMENT_CONTENT, content);
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(upload_url + dataUrl);
    }

    /**
     * 添加一个可接收该广播的接收者
     * @param receiverId ：接收者ID
     */
    public void addReceiver(String receiverId) {
        if(isStop()) return;

        Map<String, String> data = new HashMap<>();
        data.put(TYPE_BROADCAST_ID, broadcastId);
        data.put(TYPE_RECEIVER_ID, receiverId);
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(upload_url + dataUrl);
    }

    private static String createDataUrlString(Map<String, String> data) {
        if(data == null || data.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for(Map.Entry entry : data.entrySet()) {
            if(!isFirst) {
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
