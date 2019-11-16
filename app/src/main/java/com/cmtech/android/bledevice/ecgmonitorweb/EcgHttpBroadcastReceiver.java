package com.cmtech.android.bledevice.ecgmonitorweb;

import android.util.Log;
import android.util.Pair;

import com.cmtech.android.bledeviceapp.util.HttpUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class EcgHttpBroadcastReceiver {
    private static final String TAG = "EcgBroadcastReceiver";

    private static final int TYPE_CODE_RETRIEVE_BROADCAST_ID_LIST_CMD = 0; // 获取广播ID列表命令
    private static final int TYPE_CODE_RETRIEVE_BROADCAST_INFO_CMD = 1; // 获取广播信息命令
    private static final int TYPE_CODE_READ_BROADCAST_DATA_PACKET_CMD = 2; // 读取广播数据包命令
    private static final int TYPE_CODE_RECEIVER_ID = 3; // 接收者ID
    private static final int TYPE_CODE_BROADCAST_ID = 4; // 广播ID
    private static final int TYPE_CODE_LAST_DATA_PACKET_ID = 5; // 上次接收的数据包ID

    private static final String download_url = "http://huawei.tighoo.com/home/query?count=1";

    // 获取广播ID列表回调
    public interface IEcgBroadcastIdListCallback {
        void onReceived(List<String> broadcastIdList);
    }

    // 获取广播信息回调
    public interface IEcgBroadcastInfoCallback {
        void onReceived(String broadcastId, String deviceId, String creatorId, int sampleRate, int caliValue, int leadTypeCode);
    }

    // 读取广播数据包回调
    public interface IEcgBroadcastDataPacketCallback {
        void onReceived(List<EcgDataPacket> dataList);
    }

    // 心电数据包
    public static class EcgDataPacket {
        private String id;
        private List<Integer> data;

        EcgDataPacket(String id, List<Integer> data) {
            this.id = id;
            this.data = data;
        }

        public String getId() {
            return id;
        }

        public List<Integer> getData() {
            return data;
        }
    }

    private EcgHttpBroadcastReceiver() {
    }

    /**
     * 获取接收者可接收的广播ID列表
     * @param receiverId : 接收者ID
     * @param callback : 回调
     */
    public static void retrieveBroadcastIdList(String receiverId, final IEcgBroadcastIdListCallback callback) {
        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_RETRIEVE_BROADCAST_ID_LIST_CMD, ""));
        data.add(new Pair<>(TYPE_CODE_RECEIVER_ID, receiverId));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(download_url + dataUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
                if(callback != null) {
                    callback.onReceived(null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                // 这里用responseStr解析出broadcastIdList
                List<String> broadcastIdList = new ArrayList<>();

                if(callback != null) {
                    callback.onReceived(broadcastIdList);
                }
            }
        });
    }

    /**
     * 获取某广播的相关信息参数
     * @param broadcastId ： 广播ID
     * @param callback : 回调
     */
    public static void retrieveBroadcastInfo(String broadcastId, final IEcgBroadcastInfoCallback callback) {
        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_RETRIEVE_BROADCAST_INFO_CMD, ""));
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(download_url + dataUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
                if(callback != null) {
                    callback.onReceived(null, null, null, 0, 0, 0);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                // 这里用responseStr解析出广播的信息

                if(callback != null) {
                    callback.onReceived(null, null, null, 0, 0, 0);
                }
            }
        });
    }

    // 读取broadcastId广播从lastDataPackId代表的数据包之后的数据包

    /**
     * 读取广播数据包
     * @param broadcastId : 广播ID
     * @param lastDataPackId : 上一次接收到的最后一个数据包ID
     * @param callback : 回调
     */
    public static void readDataPackets(String broadcastId, String lastDataPackId, final IEcgBroadcastDataPacketCallback callback) {
        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_READ_BROADCAST_DATA_PACKET_CMD, ""));
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        data.add(new Pair<>(TYPE_CODE_LAST_DATA_PACKET_ID, lastDataPackId));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(download_url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
                if(callback != null) {
                    callback.onReceived(null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                int begin = responseStr.lastIndexOf(';');
                int end = responseStr.lastIndexOf(',');
                String subStr = responseStr.substring(begin+1, end);
                String[] strArr = subStr.split(",");
                List<Integer> data = new ArrayList<>();
                for (int i = 0; i < strArr.length; i++) {
                    data.add(Integer.parseInt(strArr[i]));
                }
                //Log.e(TAG, data.toString());
                // 这里用responseStr解析出数据包List<EcgDataPacket>
                List<EcgDataPacket> dataPackets = new ArrayList<>();
                dataPackets.add(new EcgDataPacket("1", data));

                if(callback != null) {
                    callback.onReceived(dataPackets);
                }
            }
        });
    }

    /*public static void readDataPackets(String broadcastId, String lastDataPackId, final IEcgBroadcastDataPacketCallback callback) {
        List<Pair<Integer, String>> data = new ArrayList<>();
        data.add(new Pair<>(TYPE_CODE_READ_BROADCAST_DATA_PACKET_CMD, ""));
        data.add(new Pair<>(TYPE_CODE_BROADCAST_ID, broadcastId));
        data.add(new Pair<>(TYPE_CODE_LAST_DATA_PACKET_ID, lastDataPackId));
        String dataUrl = createDataUrlString(data);
        HttpUtils.upload(download_url + dataUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
                if(callback != null) {
                    callback.onReceived(null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                // 这里用responseStr解析出数据包List<EcgDataPacket>
                List<EcgDataPacket> dataPackets = new ArrayList<>();

                if(callback != null) {
                    callback.onReceived(dataPackets);
                }
            }
        });
    }*/

    private static String createDataUrlString(List<Pair<Integer, String>> data) {
        if(data == null || data.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < data.size(); i++) {
            Pair<Integer, String> ele = data.get(i);
            if(i != 0) builder.append("&");
            builder.append(ele.first).append("=").append(ele.second);
        }
        String rlt = builder.toString();
        Log.e(TAG, "DataUrlString = " + rlt);
        return rlt;
    }
}
