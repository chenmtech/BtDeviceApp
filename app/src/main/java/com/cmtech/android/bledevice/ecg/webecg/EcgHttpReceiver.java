package com.cmtech.android.bledevice.ecg.webecg;


import android.util.Log;

import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.WebDeviceRegisterInfo;
import com.cmtech.android.bledevice.ecg.enumeration.EcgLeadType;
import com.cmtech.android.bledeviceapp.model.DeviceManager;
import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.log.ViseLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledevice.ecg.webecg.WebEcgFactory.ECGWEBMONITOR_DEVICE_TYPE;

public class EcgHttpReceiver {
    private static final String TAG = "EcgHttpReceiver";

    private static final String download_url = "http://huawei.tighoo.com/home/download?";
    private static final String device_info_url = "http://huawei.tighoo.com/home/GeReceivedDeviceInfo?";

    private static final String TYPE_DEVICE_ID = "deviceId"; // 设备ID
    private static final String TYPE_RECEIVER_ID = "receiverId"; // 接收者id
    private static final String TYPE_LAST_PACKET_ID = "id"; // 最后接收的数据包id
    private static final String TYPE_DATA_TYPE = "dataType"; // 接收的数据类型


    private EcgHttpReceiver() {
    }

    /**
     * 获取可接收的广播设备列表
     *
     * @param callback : 回调
     */
    public static void retrieveDeviceInfo(String receiverId, final IEcgDeviceInfoCallback callback) {
        Map<String, String> data = new HashMap<>();
        data.put(TYPE_RECEIVER_ID, receiverId);

        HttpUtils.upload(device_info_url, data, new Callback() {
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
                List<WebEcgDevice> deviceList = parseDevicesWithJSONObject(responseStr);
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
     *
     * @param deviceId : 广播设备ID
     * @param id       : 最后接收到的数据包时间戳
     * @param callback : 回调
     */
    public static void readDataPackets(String receiverId, String deviceId, int id, final IEcgDataPacketCallback callback) {
        Map<String, String> data = new HashMap<>();
        data.put(TYPE_DEVICE_ID, deviceId);
        data.put(TYPE_RECEIVER_ID, receiverId);
        data.put(TYPE_LAST_PACKET_ID, String.valueOf(id));
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

    private static List<WebEcgDevice> parseDevicesWithJSONObject(String jsonData) {
        List<WebEcgDevice> devices = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String deviceId = jsonObject.getString("deviceID");
                String creatorId = jsonObject.getString("open_id");
                String setting = jsonObject.getString("setting");
                JSONObject settingObj = new JSONObject(setting);
                int sampleRate = Integer.parseInt(settingObj.getString("sample_Rate"));
                int caliValue = Integer.parseInt(settingObj.getString("cali_Value"));
                int leadTypeCode = Integer.parseInt(settingObj.getString("lead_Type"));
                EcgLeadType leadType = EcgLeadType.getFromCode(leadTypeCode);
                DeviceRegisterInfo registerInfo = new WebDeviceRegisterInfo(deviceId, ECGWEBMONITOR_DEVICE_TYPE.getUuid(), creatorId);
                ViseLog.e(registerInfo + " sr=" + sampleRate + " cali=" + caliValue + " lead=" + leadType);
                WebEcgDevice device = (WebEcgDevice) DeviceManager.createDeviceIfNotExist(registerInfo);
                if (device != null) {
                    device.setName(ECGWEBMONITOR_DEVICE_TYPE.getDefaultNickname());
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

    private static long stringToTime(String timeStr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.CHINA).parse(timeStr).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /*private static String timeToString(long timeInMillis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(timeInMillis));
    }*/

    private static List<EcgDataPacket> parseDataPacketsWithJSONObject(String jsonData) {
        List<EcgDataPacket> packets = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String dataStr = jsonObject.getString("value");
                String[] dataStrs = dataStr.split(",");
                List<Integer> data = new ArrayList<>();
                for (String str : dataStrs) {
                    data.add(Integer.valueOf(str));
                }
                int id = Integer.parseInt(jsonObject.getString("id"));
                if (id != 0) {
                    EcgDataPacket packet = new EcgDataPacket(id, data);
                    ViseLog.e(packet);
                    packets.add(packet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packets;
    }

    // 获取广播设备信息回调
    public interface IEcgDeviceInfoCallback {
        void onReceived(List<WebEcgDevice> deviceList);
    }

    // 读取广播设备数据包回调
    public interface IEcgDataPacketCallback {
        void onReceived(List<EcgDataPacket> dataPacketList);
    }

    // 心电数据包
    public static class EcgDataPacket {
        private int id;
        private List<Integer> data; // 数据

        EcgDataPacket(int id, List<Integer> data) {
            this.id = id;
            this.data = data;
        }

        public int getId() {
            return id;
        }

        public List<Integer> getData() {
            return data;
        }

        @Override
        public String toString() {
            return "EcgDataPacket{" +
                    "id=" + id +
                    ", data=" + data +
                    '}';
        }
    }
}
