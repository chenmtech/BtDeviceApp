package com.cmtech.android.bledevice.ecgwebmonitor;

import android.util.Log;
import android.util.Pair;

import com.cmtech.android.bledeviceapp.util.HttpUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class EcgRecordWebReceiver {
    private static final String TAG = "EcgRecordWebReceiver";

    private static final int TYPE_CODE_RETRIEVE_BROADCASTS_CMD = 0; // 获取广播列表命令
    private static final int TYPE_CODE_READ_BROADCAST_INFO_CMD = 1; // 获取广播信息命令
    private static final int TYPE_CODE_READ_DATA_CMD = 2; // 读取广播数据命令
    private static final int TYPE_CODE_RECEIVER_ID = 3; // 接收者ID
    private static final int TYPE_CODE_BROADCAST_ID = 4; // 广播ID
    private static final int TYPE_CODE_LAST_DATA_ID = 5; // 上次接收数据ID

    private static final String download_url = "http://huawei.tighoo.com/home/download?";
    private final String receiverId;
    private IEcgRecordWebListener listener;

    public interface IEcgRecordWebListener {
        void onBroadcastIdListRetrievd(List<String> broadcastIdList);
        void onBroadcastInfoRead(String broadcastId, String deviceId, String creatorId, int sampleRate, int caliValue, int leadTypeCode);
        void onDataReceived(List<EcgDataPacket> dataList);
    }

    private static class EcgDataPacket {
        private String dataId;
        private List<Integer> ecgData;

        EcgDataPacket(String dataId, List<Integer> ecgData) {
            this.dataId = dataId;
            this.ecgData = ecgData;
        }

        public String getDataId() {
            return dataId;
        }

        public List<Integer> getEcgData() {
            return ecgData;
        }
    }

    public EcgRecordWebReceiver(String receiverId) {
        this.receiverId = receiverId;
    }

    public IEcgRecordWebListener getListener() {
        return listener;
    }

    public void setListener(IEcgRecordWebListener listener) {
        this.listener = listener;
    }

    public void retrieveBroadcastIdList() {
        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_RETRIEVE_BROADCASTS_CMD, ""));
        data.add(new Pair<>(TYPE_CODE_RECEIVER_ID, receiverId));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(download_url + dataUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                // 这里解析出broadcastIdList


                if(listener != null) {
                    listener.onBroadcastIdListRetrievd(new ArrayList<String>());
                }
            }
        });
    }

    public void readBroadcastInfo(String broadcastId) {
        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_READ_BROADCAST_INFO_CMD, ""));
        data.add(new Pair<>(TYPE_CODE_RECEIVER_ID, receiverId));
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(download_url + dataUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                // 这里解析出广播的参数


                if(listener != null) {
                    listener.onBroadcastInfoRead(null, null, null, 0, 0, 0);
                }
            }
        });
    }

    public void receiveData(String broadcastId, String lastDataId) {
        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_READ_DATA_CMD, ""));
        data.add(new Pair<>(TYPE_CODE_RECEIVER_ID, receiverId));
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        data.add(new Pair<>(TYPE_CODE_LAST_DATA_ID, lastDataId));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(download_url + dataUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                // 这里解析出数据包List<EcgDataPacket>


                if(listener != null) {
                    listener.onDataReceived(new ArrayList<EcgDataPacket>());
                }
            }
        });
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
