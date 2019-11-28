package com.cmtech.android.bledevice.ecgmonitor.device;


import android.util.Log;

import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.log.ViseLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

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

    private static final String upload_url = "http://huawei.tighoo.com/home/upload?";
    private static final String getuser_url = "http://huawei.tighoo.com/home/GetUsers?";

    private static final String TYPE_USER_ID = "open_id"; // 用户ID
    private static final String TYPE_DEVICE_ID = "deviceId"; // 设备ID
    private static final String TYPE_SAMPLE_RATE = "SR"; // 采样率
    private static final String TYPE_CALI_VALUE = "CALI"; // 标定值
    private static final String TYPE_LEAD_TYPE = "LEAD"; // 导联类型
    private static final String TYPE_DATA = "data"; // 数据
    private static final String TYPE_RECEIVER_ID = "receiverId"; // 接收者ID

    private boolean isStopped;
    private final String userId; // 用户ID
    private final String deviceId; // 设备ID
    private final int sampleRate; // 采样率
    private final int caliValue; // 标定值
    private final int leadTypeCode; // 导联类型码
    private final LinkedBlockingQueue<Integer> ecgBuffer;
    private final List<Short> hrBuffer; // 心率缓存
    private volatile boolean waiting; // 是否在等待数据响应

    public interface IGetReceiversCallback {
        void onReceived(List<Account> accounts);
    }

    public interface IAddReceiverCallback {
        void onReceived(String responseStr);
    }

    public interface IDeleteReceiverCallback {
        void onReceived(String responseStr);
    }

    public EcgHttpBroadcast(String userId, String deviceId, int sampleRate, int caliValue, int leadTypeCode) {
        isStopped = true;
        this.userId = userId;
        this.deviceId = deviceId;
        this.sampleRate = sampleRate;
        this.caliValue = caliValue;
        this.leadTypeCode = leadTypeCode;
        this.ecgBuffer = new LinkedBlockingQueue<>();
        this.hrBuffer = new ArrayList<>();
        this.waiting = false;
    }

    /**
     * 广播是否已停止
     * @return : 是否已停止
     */
    private boolean isStop() {
        return isStopped;
    }

    /**
     * 启动广播，这里上传广播相关参数。服务器端接收后，如果允许广播，应该在Response中返回广播ID
     */
    public void start() {
        if(!isStop()) return; // 不能重复启动

        Map<String, String> data = new HashMap<>();
        data.put(TYPE_USER_ID, userId);
        data.put(TYPE_DEVICE_ID, deviceId);
        data.put(TYPE_SAMPLE_RATE, String.valueOf(sampleRate));
        data.put(TYPE_CALI_VALUE, String.valueOf(caliValue));
        data.put(TYPE_LEAD_TYPE, String.valueOf(leadTypeCode));
        HttpUtils.upload(upload_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "broadcast start fail.");
                isStopped = true;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                ViseLog.e("start: " + responseStr);

                isStopped = false;
                Log.e(TAG, "broadcast started.");
            }
        });
    }

    /**
     * 停止广播
     */
    public void stop() {
        if(isStop()) return;
        isStopped = true;
    }

    /**
     * 发送心电信号
     * @param ecgSignal ：心电数据
     */
    public synchronized void sendEcgSignal(int ecgSignal) {
        if(isStop()) return;
        ecgBuffer.add(ecgSignal);
        if(!waiting && ecgBuffer.size() >= sampleRate) {
            waiting = true;
            sendData();
        }
    }

    /**
     * 发送心率值
     * @param hr ：心率值
     */
    public synchronized void sendHrValue(short hr) {
        if(isStop()) return;
        hrBuffer.add(hr);
    }

    private void sendData() {
        Map<String, String> data = new HashMap<>();
        data.put(TYPE_USER_ID, userId);
        data.put(TYPE_DEVICE_ID,deviceId);

        StringBuilder sb = new StringBuilder();
        while (sb.length() < 1600) {
            Integer n = ecgBuffer.poll();
            if(n == null) break;
            sb.append(n).append(",");
        }
        String ecgStr = sb.toString();

        data.put(TYPE_DATA, HttpUtils.convertString(hrBuffer)+ ";"+ ecgStr);
        hrBuffer.clear();
        HttpUtils.upload(upload_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ViseLog.e(e.getMessage());
                synchronized (EcgHttpBroadcast.this) {
                    waiting = false;
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                ViseLog.e("send data: " + responseStr);
                synchronized (EcgHttpBroadcast.this) {
                    waiting = false;
                }
            }
        });
    }

    /**
     * 添加一个可接收该广播的接收者
     * @param receiverId ：接收者ID
     */
    public void addReceiver(String receiverId, final IAddReceiverCallback callback) {
        if(isStop()) return;
        Map<String, String> data = new HashMap<>();
        data.put(TYPE_USER_ID, userId);
        data.put(TYPE_DEVICE_ID, deviceId);
        data.put(TYPE_RECEIVER_ID, receiverId);

        HttpUtils.upload(upload_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(callback != null) {
                    callback.onReceived(null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                ViseLog.e("addReceiver: " + responseStr);

                if(callback != null)
                    callback.onReceived(responseStr);
            }
        });
    }

    /**
     * 删除一个可接收该广播的接收者
     * @param receiverId ：接收者ID
     */
    public void deleteReceiver(String receiverId, final IDeleteReceiverCallback callback) {
        if(isStop()) return;

        Map<String, String> data = new HashMap<>();
        data.put(TYPE_USER_ID, userId);
        data.put(TYPE_DEVICE_ID, deviceId);
        data.put(TYPE_RECEIVER_ID, receiverId);

        HttpUtils.upload(upload_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(callback != null) {
                    callback.onReceived(null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                ViseLog.e("deleteReceiver: " + responseStr);
                if(callback != null) {
                    callback.onReceived(responseStr);
                }
            }
        });
    }

    public void getUsers(final IGetReceiversCallback callback) {
        if(isStop()) return;

        Map<String, String> data = new HashMap<>();
        data.put(TYPE_USER_ID, userId);
        data.put(TYPE_DEVICE_ID, deviceId);
        HttpUtils.upload(getuser_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "get users fail.");
                if(callback != null) {
                    callback.onReceived(null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                ViseLog.e("getUsers: " + responseStr);

                if(callback != null) {
                    callback.onReceived(parseReceivers(responseStr));
                }
            }
        });
    }

    private static List<Account> parseReceivers(String jsonData) {
        List<Account> receivers = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String huaweiId = jsonObject.getString("open_id");
                String name = jsonObject.getString("displayName");
                Account account = new Account();
                account.setName(name);
                account.setHuaweiId(huaweiId);
                receivers.add(account);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return receivers;
    }
}
