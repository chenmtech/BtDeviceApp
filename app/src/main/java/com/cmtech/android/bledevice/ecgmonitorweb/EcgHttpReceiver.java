package com.cmtech.android.bledevice.ecgmonitorweb;


import android.util.Log;

import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledeviceapp.util.HttpUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class EcgHttpReceiver {
    private static final String TAG = "EcgHttpReceiver";

    private static final String download_url = "http://huawei.tighoo.com/home/download?";
    private static final String device_info_url = "http://huawei.tighoo.com/home/GeReceivedDeviceInfo?";

    private static final String TYPE_DEVICE_ID = "deviceId"; // 设备ID
    private static final String TYPE_RECEIVER_ID = "receiverId";
    private static final String TYPE_LAST_PACKET_TIME_STAMP = "LPTS"; // 最后接收的数据包时间戳


    // 获取广播设备信息回调
    public interface IEcgDeviceInfoCallback {
        void onReceived(List<WebEcgMonitorDevice> deviceList);
    }

    // 读取广播设备数据包回调
    public interface IEcgDataPacketCallback {
        void onReceived(List<EcgDataPacket> dataPacketList);
    }

    // 心电数据包
    public static class EcgDataPacket {
        private long timeStamp; // 数据包的时间戳
        private List<Integer> data; // 数据

        EcgDataPacket(long timeStamp, List<Integer> data) {
            this.timeStamp = timeStamp;
            this.data = data;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public List<Integer> getData() {
            return data;
        }
    }

    private EcgHttpReceiver() {
    }

    /**
     * 获取可接收的广播设备列表
     * 服务器端应该将该接收者可接收的广播设备ID以List<String>格式返回
     * 注意：接收者ID就是HttpUtils中的open_id, 并在调用HttpUtils.upload时自动加入，所以这里就不用添加了。下同
     * @param callback : 回调
     */
    public static void retrieveDeviceInfo(final IEcgDeviceInfoCallback callback) {
        Map<String, String> data = new HashMap<>();
        data.put(TYPE_RECEIVER_ID, HttpUtils.open_id);
        String dataStr = HttpUtils.createDataUrlString(data);
        HttpUtils.upload(device_info_url + dataStr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
                if (callback != null) {
                    callback.onReceived(null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                // 这里用responseStr解析出IDevice列表
                List<WebEcgMonitorDevice> deviceList = new ArrayList<>();

                if (callback != null) {
                    callback.onReceived(deviceList);
                }
            }
        });
    }

    /**
     * 读取广播数据包
     * 服务器端应该找到比lastPackTimeStamp更晚出现的数据包，以List<EcgDataPacket>的格式返回数据包
     * 如果满足条件的数据包太多，则最多返回两个最晚出现的数据包
     * @param deviceId          : 广播设备ID
     * @param lastPackTimeStamp : 最后接收到的数据包时间戳
     * @param callback          : 回调
     */
    public static void readDataPackets(String deviceId, long lastPackTimeStamp, final IEcgDataPacketCallback callback) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(TYPE_DEVICE_ID, deviceId);
        data.put(TYPE_LAST_PACKET_TIME_STAMP, String.valueOf(lastPackTimeStamp));
        HttpUtils.upload(download_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
                if (callback != null) {
                    callback.onReceived(null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                // 这里用responseStr解析出数据包List<EcgDataPacket>

                /*int begin = responseStr.lastIndexOf(';');
                int end = responseStr.lastIndexOf(',');
                String subStr = responseStr.substring(begin+1, end);
                String[] strArr = subStr.split(",");
                List<Integer> data = new ArrayList<>();
                for (int i = 0; i < strArr.length; i++) {
                    data.add(Integer.parseInt(strArr[i]));
                }
                Log.e(TAG, data.toString());*/

                List<EcgDataPacket> dataPackets = new ArrayList<>();
                dataPackets.add(new EcgDataPacket(0, new ArrayList<Integer>()));

                if (callback != null) {
                    callback.onReceived(dataPackets);
                }
            }
        });
    }

}
