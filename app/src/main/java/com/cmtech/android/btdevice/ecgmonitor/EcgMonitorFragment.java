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
import android.widget.TextView;

import com.cmtech.android.ble.callback.IBleCallback;
import com.cmtech.android.ble.core.BluetoothGattChannel;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.model.BluetoothGattElement;
import com.cmtech.android.btdeviceapp.model.GattSerialExecutor;
import com.cmtech.android.btdeviceapp.util.Uuid;
import com.cmtech.dsp.filter.IIRFilter;
import com.cmtech.dsp.filter.design.DCBlockDesigner;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by bme on 2018/3/13.
 */

public class EcgMonitorFragment extends DeviceFragment {
    private static final int MSG_ECGDATA = 0;
    private static final int MSG_ECGSAMPLERATE = 1;
    private static final int MSG_ECGLEADTYPE = 2;

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


    private TextView tvEcgSampleRate;
    private TextView tvEcgLeadType;
    private EcgWaveView ecgView;

    private final IIRFilter dcBlock = DCBlockDesigner.design(0.06, 250);   // 隔直滤波器

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ECGDATA) {
                if(msg.obj != null) {
                    byte[] data = (byte[]) msg.obj;

                    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
                    for(int i = 0; i < data.length/2; i++) {
                        //int filtered = (int)dcBlock.filter((double)buffer.getShort());
                        int filtered = buffer.getShort();
                        ecgView.addData(filtered);
                    }
                }
            } else if(msg.what ==  MSG_ECGSAMPLERATE) {
                sampleRate = msg.arg1;
                tvEcgSampleRate.setText(""+sampleRate);
                int xRes = 2;
                int yRes = 15;
                ecgView.setRes(xRes, yRes);
                // 25mm/s的走纸速度代表每mm的小格为0.04秒
                // 每秒采样sampleRate个数据，每个数据xRes个像素，即每秒sampleRate*xRes个像素，所以每小格包含0.04*sampleRate*xRes个像素
                // 每小格的像素个数
                ecgView.setGridWidth((int)(0.04*sampleRate*xRes));
                ecgView.setZeroLocation(0.5);
                ecgView.startShow();

            } else if(msg.what == MSG_ECGLEADTYPE) {
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
        ecgView = (EcgWaveView)view.findViewById(R.id.ecg_view);

        //dcBlock.createStructure(StructType.IIR_DCBLOCK);
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
                msg.what = MSG_ECGSAMPLERATE;
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
                msg.what = MSG_ECGLEADTYPE;
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

        // 启动ECG数据采集
/*        serialExecutor.addWriteCommand(ECGMONITORCTRL, new byte[]{0x01}, new IBleCallback() {
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
        });*/

        serialExecutor.start();
    }

}
