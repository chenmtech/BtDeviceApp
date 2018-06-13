package com.cmtech.android.btdevice.temphumid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.model.BluetoothGattElement;
import com.cmtech.android.btdeviceapp.model.GattSerialExecutor;
import com.cmtech.android.btdeviceapp.util.ByteUtil;
import com.cmtech.android.btdeviceapp.util.Uuid;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Created by bme on 2018/2/27.
 */

public class TempHumidFragment extends DeviceFragment {
    private static final int MSG_TEMPHUMIDDATA = 0;
    private static final int MSG_TEMPHUMIDCTRL = 1;
    private static final int MSG_TEMPHUMIDPERIOD = 2;
    private static final int MSG_TEMPHUMIDHISTORYDATA = 3;
    private static final int MSG_TIMERCTRL = 4;

    ///////////////// 温湿度计Service相关的常量////////////////
    private static final String tempHumidServiceUuid    = "aa60";           // 温湿度计服务UUID:aa60
    private static final String tempHumidDataUuid       = "aa61";           // 温湿度数据特征UUID:aa61
    private static final String tempHumidCtrlUuid       = "aa62";           // 测量控制UUID:aa62
    private static final String tempHumidPeriodUuid     = "aa63";           // 采样周期UUID:aa63
    private static final String tempHumidHistoryTimeUuid  = "aa64";         // 历史数据采集的时间UUID
    private static final String tempHumidHistoryDataUuid = "aa65";          // 历史数据UUID


    public static final BluetoothGattElement TEMPHUMIDDATA =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidDataUuid, null);

    public static final BluetoothGattElement TEMPHUMIDCTRL =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidCtrlUuid, null);

    public static final BluetoothGattElement TEMPHUMIDPERIOD =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidPeriodUuid, null);

    public static final BluetoothGattElement TEMPHUMIDDATACCC =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidDataUuid, Uuid.CCCUUID);

    public static final BluetoothGattElement TEMPHUMIDHISTORYTIME =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidHistoryTimeUuid, null);

    public static final BluetoothGattElement TEMPHUMIDHISTORYDATA =
            new BluetoothGattElement(tempHumidServiceUuid, tempHumidHistoryDataUuid, null);
    ////////////////////////////////////////////////////////


    //////////////// 定时服务相关常量 /////////////////////////
    private static final String timerServiceUuid          = "aa70";
    private static final String timerCurTimeUuid          = "aa71";         // 当前时间
    private static final String timerCtrlUuid              = "aa72";        // 定时控制
    private static final String timerPeriodUuid             = "aa73";       // 定时周期

    public static final BluetoothGattElement TIMERCURTIME =
            new BluetoothGattElement(timerServiceUuid, timerCurTimeUuid, null);

    public static final BluetoothGattElement TIMERCTRL =
            new BluetoothGattElement(timerServiceUuid, timerCtrlUuid, null);

    public static final BluetoothGattElement TIMERPERIOD =
            new BluetoothGattElement(timerServiceUuid, timerPeriodUuid, null);
    ////////////////////////////////////////////////////////

    private TextView tvTempData;
    private TextView tvHumidData;
    private TextView tvHeadIndex;

    private RecyclerView rvHistoryData;
    private TempHumidHistoryDataAdapter historyDataAdapter;
    private List<TempHumidData> dataList = new ArrayList<>();

    private Button btnReadHistoryData;

    // 设备是否启动定时采集服务
    private boolean isStartTimerService = false;
    // 设备中的当前时间
    private Calendar deviceCurTime;

    private View rootView;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_TEMPHUMIDDATA) {
                if(msg.obj != null) {
                    TempHumidData data = (TempHumidData) msg.obj;
                    tvHumidData.setText( ""+data.getHumid() );
                    tvTempData.setText(String.format("%.1f", data.getTemp()));
                    float heatindex = computeHeatIndex(data.getTemp(), data.getHumid());
                    tvHeadIndex.setText(String.format("%.1f", heatindex));
                }
            } else if(msg.what == MSG_TEMPHUMIDHISTORYDATA) {
                if(msg.obj != null) {
                    dataList.add((TempHumidData) msg.obj);
                    historyDataAdapter.notifyItemInserted(dataList.size()-1);
                    rvHistoryData.scrollToPosition(0);
                    //tvHeadIndex.setText(newData.toString());
                }
            }
        }
    };

    public TempHumidFragment() {

    }

    public static TempHumidFragment newInstance() {
        return new TempHumidFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("TempHumidFragment", "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_temphumid, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTempData = (TextView)view.findViewById(R.id.tv_temp_data);
        tvHumidData = (TextView)view.findViewById(R.id.tv_humid_data);
        tvHeadIndex = (TextView)view.findViewById(R.id.tv_heat_index);

        rvHistoryData = (RecyclerView)view.findViewById(R.id.rv_history_temphumid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MyApplication.getContext());
        rvHistoryData.setLayoutManager(layoutManager);
        historyDataAdapter = new TempHumidHistoryDataAdapter(dataList);
        rvHistoryData.setAdapter(historyDataAdapter);

        btnReadHistoryData = (Button)view.findViewById(R.id.btn_read_history_temphumid);
        btnReadHistoryData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateHistoryData(device.getSerialExecutor());
            }
        });
    }


    @Override
    public synchronized void executeGattInitOperation() {
        final GattSerialExecutor serialExecutor = device.getSerialExecutor();
        if(serialExecutor == null) return;
        serialExecutor.start();

        Log.d("FragmentThread", ""+Thread.currentThread().getId());

        Object thermoData = serialExecutor.getGattObject(TEMPHUMIDDATA);
        Object thermoControl = serialExecutor.getGattObject(TEMPHUMIDCTRL);
        Object thermoPeriod = serialExecutor.getGattObject(TEMPHUMIDPERIOD);
        if(thermoData == null || thermoControl == null || thermoPeriod == null) {
            Log.d("TempHumidFragment", "can't find Gatt object of this element on the device.");
            return;
        }

/*        // 先读一次温湿度
        serialExecutor.addReadCommand(TEMPHUMIDDATA, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "first write period: " + HexUtil.encodeHexStr(data));
                Message msg = new Message();
                msg.what = MSG_TEMPHUMIDDATA;
                msg.obj = new TempHumidData(new Date(), data);
                handler.sendMessage(msg);
                Log.d("Thread", "Read Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });*/

        // 设置采样周期: 设置的值以100ms为单位
        int period = 2000;
        serialExecutor.addWriteCommand(TEMPHUMIDPERIOD, new byte[]{(byte)(period/100)}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "second write period: " + HexUtil.encodeHexStr(data));
                handler.sendEmptyMessage(MSG_TEMPHUMIDPERIOD);
                Log.d("Thread", "Period Write Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });

        // 温湿度数据Notify回调
        IBleCallback notifyCallback = new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Message msg = new Message();
                msg.what = MSG_TEMPHUMIDDATA;
                msg.obj = new TempHumidData(Calendar.getInstance(), data);
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };

        // enable温湿度notify
        serialExecutor.addNotifyCommand(TEMPHUMIDDATACCC, true, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("Thread", "Notify Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {

            }
        }, notifyCallback);


        // 启动温湿度采集
        serialExecutor.addWriteCommand(TEMPHUMIDCTRL, new byte[]{0x01}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                //Log.d("THERMOPERIOD", "second write period: " + HexUtil.encodeHexStr(data));
                //handler.sendEmptyMessage(MSG_TEMPHUMIDCTRL);
                Log.d("Thread", "Control Write Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });

        // 读取设备是否已经开启定时采集服务
        if(serialExecutor.getGattObject(TIMERCTRL) != null) {
            serialExecutor.addReadCommand(TIMERCTRL, new IBleCallback() {
                @Override
                public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                    isStartTimerService = (data[0] == 1) ? true : false;
                    if (isStartTimerService) {
                        readDeviceCurTime(serialExecutor);  // 开启了定时服务，就先读取设备的当前时间
                    } else {
                        startTimerService(serialExecutor);  // 没有开启定时服务（比如刚换电池），就先开启定时服务
                    }
                }

                @Override
                public void onFailure(BleException exception) {

                }
            });
        }
    }

    // 读设备中的当前时间
    private void readDeviceCurTime(final GattSerialExecutor serialExecutor) {
        // 读取设备当前时间
        if(serialExecutor.getGattObject(TIMERCURTIME) != null) {
            serialExecutor.addReadCommand(TIMERCURTIME, new IBleCallback() {
                @Override
                public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {

                    Calendar now = Calendar.getInstance();
                    Calendar devicenow;

                    Calendar devicetoday = (Calendar) now.clone();
                    devicetoday.set(Calendar.HOUR_OF_DAY, data[0]);
                    devicetoday.set(Calendar.MINUTE, data[1]);

                    Calendar deviceyest = (Calendar) devicetoday.clone();
                    deviceyest.add(Calendar.DAY_OF_MONTH, -1);

                    Calendar devicetomm = (Calendar) devicetoday.clone();
                    devicetomm.add(Calendar.DAY_OF_MONTH, 1);

                    long difftoday = Math.abs(now.getTimeInMillis()-devicetoday.getTimeInMillis());
                    long diffyest = Math.abs(now.getTimeInMillis()-deviceyest.getTimeInMillis());
                    long difftomm = Math.abs(now.getTimeInMillis()-devicetomm.getTimeInMillis());
                    if(difftoday < Math.min(diffyest, difftomm))
                        devicenow = devicetoday;
                    else if(diffyest < Math.min(difftoday, difftomm))
                        devicenow = deviceyest;
                    else
                        devicenow = devicetomm;

                    Log.d("TempHumidFragment", "now=" + DateFormat.getDateTimeInstance().format(now.getTime()) );
                    Log.d("TempHumidFragment", "devicenow=" + DateFormat.getDateTimeInstance().format(devicenow.getTime()));

                    deviceCurTime = devicenow;
                    updateHistoryData(serialExecutor);
                }

                @Override
                public void onFailure(BleException exception) {
                    deviceCurTime = null;
                }
            });
        }
    }

    private void updateHistoryData(GattSerialExecutor serialExecutor) {
        Calendar time = (Calendar)deviceCurTime.clone();
        for(int i = 0; i < 5; i++) {
            readHistoryDataAtTime(serialExecutor, time);
            time.add(Calendar.MINUTE, -30);
        }
    }

    private void readHistoryDataAtTime(GattSerialExecutor serialExecutor, final Calendar attime) {
        final Calendar backuptime = (Calendar) attime.clone();
        final byte[] time = {(byte)attime.get(Calendar.HOUR_OF_DAY), (byte)attime.get(Calendar.MINUTE)};
        // 写历史数据时间
        serialExecutor.addWriteCommand(TEMPHUMIDHISTORYTIME, time, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
            }

            @Override
            public void onFailure(BleException exception) {
                //Log.d("THERMOCONTROL", exception.toString());
            }
        });

        // 读取历史数据
        serialExecutor.addReadCommand(TEMPHUMIDHISTORYDATA, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Message msg = new Message();
                msg.what = MSG_TEMPHUMIDHISTORYDATA;
                msg.obj = new TempHumidData(backuptime, data);
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    private void startTimerService(GattSerialExecutor serialExecutor) {

    }

    private float computeHeatIndex(float t, float rh) {
        t = t*1.8f+32.0f;
        float index = (float)((16.923 + (0.185212 * t) + (5.37941 * rh) - (0.100254 * t * rh) +
                (0.00941695 * (t * t)) + (0.00728898 * (rh * rh)) +
                (0.000345372 * (t * t * rh)) - (0.000814971 * (t * rh * rh)) +
                (0.0000102102 * (t * t * rh * rh)) - (0.000038646 * (t * t * t)) + (0.0000291583 *
                (rh * rh * rh)) + (0.00000142721 * (t * t * t * rh)) +
                (0.000000197483 * (t * rh * rh * rh)) - (0.0000000218429 * (t * t * t * rh * rh)) +
                0.000000000843296 * (t * t * rh * rh * rh)) -
                (0.0000000000481975 * (t * t * t * rh * rh * rh)));
        index = (index-32)/1.8f;
        return index;
    }
}
