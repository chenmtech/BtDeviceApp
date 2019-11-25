package com.cmtech.android.bledevice.ecgmonitorweb;


import android.util.Log;

import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgLeadType;
import com.cmtech.android.bledeviceapp.model.DeviceManager;
import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.log.ViseLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private static final String TYPE_LAST_PACKET_TIME = "time"; // 最后接收的数据包时间戳
    private static final String TYPE_DATA_TYPE = "dataType";


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

        @Override
        public String toString() {
            return "EcgDataPacket{" +
                    "timeStamp=" + timeToString(timeStamp) +
                    ", data=" + data +
                    '}';
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
        String urlStr = device_info_url + HttpUtils.createDataUrlString(data);

        HttpUtils.upload(urlStr, new Callback() {
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

                ViseLog.e("retrieveDeviceInfo: " + responseStr);

                // 这里用responseStr解析出IDevice列表
                List<WebEcgMonitorDevice> deviceList = parseDevicesWithJSONObject(responseStr);
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
     * @param lastPackTime : 最后接收到的数据包时间戳
     * @param callback          : 回调
     */
    public static void readDataPackets(String deviceId, long lastPackTime, final IEcgDataPacketCallback callback) {
        Map<String, String> data = new HashMap<>();
        data.put(TYPE_DEVICE_ID, deviceId);
        data.put(TYPE_RECEIVER_ID, HttpUtils.open_id);
        data.put(TYPE_LAST_PACKET_TIME, timeToString(lastPackTime));
        data.put(TYPE_DATA_TYPE, String.valueOf(1));

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
                ViseLog.e("readDataPackets: " + responseStr);

                List<EcgDataPacket> dataPackets = parseDataPacketsWithJSONObject(responseStr);

                if (callback != null) {
                    callback.onReceived(dataPackets);
                }
            }
        });
    }

    private static List<WebEcgMonitorDevice> parseDevicesWithJSONObject(String jsonData) {
        List<WebEcgMonitorDevice> devices = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String deviceId = jsonObject.getString("deviceID");
                int sampleRate = Integer.parseInt(jsonObject.getString("sample_Rate"));
                int caliValue = Integer.parseInt(jsonObject.getString("cali_Value"));
                int leadTypeCode = Integer.parseInt(jsonObject.getString("lead_Type"));
                EcgLeadType leadType = EcgLeadType.getFromCode(leadTypeCode);
                DeviceRegisterInfo registerInfo = new DeviceRegisterInfo(deviceId, "ab40");
                ViseLog.e(registerInfo);
                WebEcgMonitorDevice device = (WebEcgMonitorDevice) DeviceManager.createDeviceIfNotExist(registerInfo);
                if(device != null) {
                    device.getRegisterInfo().setName("心电广播");
                    device.setSampleRate(sampleRate);
                    device.setValue1mV(caliValue);
                    device.setLeadType(leadType);
                    devices.add(device);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return devices;
    }


    private static String timeToString(long timeInMillis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(timeInMillis));
    }

    private static long stringToTime(String timeStr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.CHINA).parse(timeStr).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static List<EcgDataPacket> parseDataPacketsWithJSONObject(String jsonData) {
        List<EcgDataPacket> packets = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String dataStr = jsonObject.getString("value");
                String[] dataStrs = dataStr.split(",");
                List<Integer> data = new ArrayList<>();
                for(String str : dataStrs) {
                    data.add(Integer.valueOf(str));
                }
                String dateStr = jsonObject.getString("creationDate");
                long time = stringToTime(dateStr);
                if(time != 0) {
                    EcgDataPacket packet = new EcgDataPacket(time, data);
                    ViseLog.e(packet);
                    packets.add(new EcgDataPacket(time, data));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packets;
    }
}
