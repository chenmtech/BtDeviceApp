package com.cmtech.android.btdevice.ecgmonitor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.cmtech.android.btdeviceapp.util.Uuid;
import com.cmtech.dsp.bmefile.BmeFile;
import com.cmtech.dsp.exception.FileException;
import com.cmtech.dsp.filter.IIRFilter;
import com.cmtech.dsp.filter.design.DCBlockDesigner;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by bme on 2018/3/13.
 */

public class EcgMonitorFragment extends DeviceFragment {
    private static final int MSG_ECGDATA = 0;
    private static final int MSG_READSAMPLERATE = 1;
    private static final int MSG_READLEADTYPE = 2;
    private static final int MSG_STARTSAMPLEECG = 3;

    ///////////////// 温湿度计Service相关的常量////////////////
    private static final String ecgMonitorServiceUuid    = "aa40";           // 心电监护仪服务UUID:aa40
    private static final String ecgMonitorDataUuid       = "aa41";           // ECG数据特征UUID:aa41
    private static final String ecgMonitorCtrlUuid       = "aa42";           // 测量控制UUID:aa42
    private static final String ecgMonitorSampleRateUuid    = "aa44";        // 采样率UUID:aa44
    private static final String ecgMonitorLeadTypeUuid      = "aa45";        // 导联类型UUID:aa45

    public static final BluetoothGattElement ECGMONITORDATA =
            new BluetoothGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, null);

    public static final BluetoothGattElement ECGMONITORCTRL =
            new BluetoothGattElement(ecgMonitorServiceUuid, ecgMonitorCtrlUuid, null);

    public static final BluetoothGattElement ECGMONITORDATACCC =
            new BluetoothGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, Uuid.CCCUUID);

    public static final BluetoothGattElement ECGMONITORSAMPLERATE =
            new BluetoothGattElement(ecgMonitorServiceUuid, ecgMonitorSampleRateUuid, null);

    public static final BluetoothGattElement ECGMONITORLEADTYPE =
            new BluetoothGattElement(ecgMonitorServiceUuid, ecgMonitorLeadTypeUuid, null);
    ////////////////////////////////////////////////////////

    private int sampleRate = 125;
    private int leadType = 0;
    private boolean isCalibration = false;
    private ArrayList<Integer> calibrationData = new ArrayList<Integer>(200);


    private TextView tvEcgSampleRate;
    private TextView tvEcgLeadType;
    private TextView tvEcg1mV;
    private WaveView ecgView;
    private Button btnEcgStartandStop;
    private CheckBox cbEcgRecord;

    private boolean isStart = false;


    private int viewGridWidth = 10;               // 设置ECG View中的每小格有10个像素点
    // 下面两个参数可用来计算View中的xRes和yRes
    private float viewXGridTime = 0.04f;          // 设置ECG View中的横向每小格代表0.04秒，即25格/s，这是标准的ECG走纸速度
    private float viewYGridmV = 0.1f;             // 设置ECG View中的纵向每小格代表0.1mV

    private final IIRFilter dcBlock = DCBlockDesigner.design(0.06, 250);   // 隔直滤波器

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ECGDATA) {
                if(msg.obj != null) {
                    int tmpData = 0;
                    byte[] data = (byte[]) msg.obj;

                    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
                    for(int i = 0; i < data.length/2; i++) {
                        tmpData = buffer.getShort();
                        if(isCalibration) {
                            // 采集1个周期的定标信号
                            if(calibrationData.size() < sampleRate)
                                calibrationData.add(tmpData);
                            else {
                                int value1mV = calculateCalibration(calibrationData);
                                calibrationData.clear();
                                tvEcg1mV.setText(""+value1mV);
                                int xRes = Math.round(viewGridWidth/(viewXGridTime *sampleRate));   // 计算横向分辨率
                                float yRes = value1mV*viewYGridmV/viewGridWidth;                     // 计算纵向分辨率
                                ecgView.setRes(xRes, yRes);
                                ecgView.setGridWidth(viewGridWidth);
                                ecgView.setZeroLocation(0.5);
                                ecgView.startShow();
                                Message msg1 = new Message();
                                msg1.what = MSG_STARTSAMPLEECG;
                                handler.sendMessage(msg1);
                            }
                        } else {
                            //tmpData = (int)dcBlock.filter(tmpData);
                            ecgView.addData(tmpData);
                        }
                    }
                }
            } else if(msg.what == MSG_READSAMPLERATE) {
                sampleRate = msg.arg1;
                tvEcgSampleRate.setText(""+sampleRate);


            } else if(msg.what == MSG_READLEADTYPE) {
                leadType = msg.arg1;
                switch (leadType) {
                    case 0:
                        tvEcgLeadType.setText("I");
                        break;
                    case 1:
                        tvEcgLeadType.setText("II");
                        break;
                    case 2:
                        tvEcgLeadType.setText("III");
                        break;
                }
            } else if(msg.what == MSG_STARTSAMPLEECG) {
                btnEcgStartandStop.setClickable(true);
                controlSampleEcg();
            }
        }
    };

    public EcgMonitorFragment() {

    }

    public static EcgMonitorFragment newInstance() {
        return new EcgMonitorFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ecgmonitor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEcgSampleRate = (TextView)view.findViewById(R.id.tv_ecg_samplerate);
        tvEcgLeadType = (TextView)view.findViewById(R.id.tv_ecg_leadtype);
        tvEcg1mV = (TextView)view.findViewById(R.id.tv_ecg_1mv);
        ecgView = (WaveView)view.findViewById(R.id.ecg_view);
        btnEcgStartandStop = view.findViewById(R.id.btn_ecg_startandstop);
        cbEcgRecord = view.findViewById(R.id.cb_ecg_record);

        //dcBlock.createStructure(StructType.IIR_DCBLOCK);

        btnEcgStartandStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlSampleEcg();
            }
        });

        cbEcgRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                File toFile = FileUtil.getFile(MyApplication.getContext().getExternalFilesDir("ecgSignal"), "chenm.bme");
                String fileName = toFile.getAbsolutePath();
                try {
                    BmeFile bmeFile = BmeFile.createBmeFile(fileName);
                    bmeFile.writeData(1.2345);
                    bmeFile.close();
                } catch (FileException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    @Override
    public synchronized void executeGattInitOperation() {
        GattSerialExecutor serialExecutor = device.getSerialExecutor();

        if(serialExecutor == null) return;

        Log.d("FragmentThread", ""+Thread.currentThread().getId());


        Object ecgData = serialExecutor.getGattObject(ECGMONITORDATA);
        Object ecgControl = serialExecutor.getGattObject(ECGMONITORCTRL);
        Object ecgSampleRate = serialExecutor.getGattObject(ECGMONITORSAMPLERATE);
        Object ecgLeadType = serialExecutor.getGattObject(ECGMONITORLEADTYPE);

        if(ecgData == null || ecgControl == null || ecgSampleRate == null || ecgLeadType == null) {
            Log.d("EcgMonitorFragment", "can't find Gatt object of this element on the device.");
            return;
        }

        // 读采样率命令
        serialExecutor.addReadCommand(ECGMONITORSAMPLERATE, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Message msg = new Message();
                msg.what = MSG_READSAMPLERATE;
                msg.arg1 = (data[0] & 0xff) | ((data[1] << 8) & 0xff00);
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });

        // 读导联类型命令
        serialExecutor.addReadCommand(ECGMONITORLEADTYPE, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Message msg = new Message();
                msg.what = MSG_READLEADTYPE;
                msg.arg1 = data[0];
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });

        IBleCallback notifyCallback = new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Message msg = new Message();
                msg.what = MSG_ECGDATA;
                msg.obj = data;
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        };

        // enable ECG 数据notify
        serialExecutor.addNotifyCommand(ECGMONITORDATACCC, true, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("Thread", "Notify Callback Thread: "+Thread.currentThread().getId());
            }

            @Override
            public void onFailure(BleException exception) {

            }
        }, notifyCallback);


        // 启动1mV数据采集
        isCalibration = true;
        serialExecutor.addWriteCommand(ECGMONITORCTRL, new byte[]{0x02}, new IBleCallback() {
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

        serialExecutor.start();
    }

    private synchronized void controlSampleEcg() {
        if(!isStart) {
            startSampleEcg();
            btnEcgStartandStop.setText("停止");
            isStart = true;
        } else {
            stopSampleEcg();
            btnEcgStartandStop.setText("开始");
            isStart = false;
        }
    }

    private synchronized void startSampleEcg() {
        GattSerialExecutor serialExecutor = device.getSerialExecutor();

        if(serialExecutor == null) return;

        // 启动ECG数据采集
        serialExecutor.addWriteCommand(ECGMONITORCTRL, new byte[]{0x01}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                ecgView.clearView();
                isCalibration = false;
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    private synchronized void stopSampleEcg() {
        GattSerialExecutor serialExecutor = device.getSerialExecutor();

        if(serialExecutor == null) return;

        // 停止ECG数据采集
        serialExecutor.addWriteCommand(ECGMONITORCTRL, new byte[]{0x00}, new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {

            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    private int calculateCalibration(ArrayList<Integer> data) {
        Integer[] arr = data.toArray(new Integer[0]);
        Arrays.sort(arr);

        int len = (arr.length-10)/2;
        int sum1 = 0;
        int sum2 = 0;
        for(int i = 0; i < len; i++) {
            sum1 += arr[i];
            sum2 += arr[arr.length-i-1];
        }
        return (sum2-sum1)/2/len;
    }

}
