package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.core.BleDataOpException;
import com.cmtech.android.bledevice.core.BleDeviceGattOperator;
import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledevice.core.BleGattElement;
import com.cmtech.android.bledevice.core.IBleDataOpCallback;
import com.vise.log.ViseLog;

import static com.cmtech.android.bledevice.core.BleDeviceConstant.CCCUUID;
import static com.cmtech.android.bledevice.core.BleDeviceConstant.MY_BASE_UUID;
import static com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice.MSG_OBTAINDATA;
import static com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice.MSG_OBTAINLEADTYPE;
import static com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice.MSG_OBTAINSAMPLERATE;
import static com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice.MSG_STARTSAMPLINGSIGNAL;

/**
 * EcgMonitorGattOperator: 心电带Gatt操作者
 * Created by bme on 2018/12/20.
 */

public class EcgMonitorGattOperator extends BleDeviceGattOperator {
    // 心电监护仪Service UUID常量
    private static final String ecgMonitorServiceUuid       = "aa40";           // 心电监护仪服务UUID:aa40
    private static final String ecgMonitorDataUuid          = "aa41";           // ECG数据特征UUID:aa41
    private static final String ecgMonitorCtrlUuid          = "aa42";           // 测量控制UUID:aa42
    private static final String ecgMonitorSampleRateUuid    = "aa44";           // 采样率UUID:aa44
    private static final String ecgMonitorLeadTypeUuid      = "aa45";           // 导联类型UUID:aa45

    // Gatt Element常量
    private static final BleGattElement ECGMONITORDATA =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, null, MY_BASE_UUID, "心电数据");
    private static final BleGattElement ECGMONITORDATACCC =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorDataUuid, CCCUUID, MY_BASE_UUID, "心电数据CCC");
    private static final BleGattElement ECGMONITORCTRL =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorCtrlUuid, null, MY_BASE_UUID, "心电Ctrl");
    private static final BleGattElement ECGMONITORSAMPLERATE =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorSampleRateUuid, null, MY_BASE_UUID, "采样率");
    private static final BleGattElement ECGMONITORLEADTYPE =
            new BleGattElement(ecgMonitorServiceUuid, ecgMonitorLeadTypeUuid, null, MY_BASE_UUID, "导联类型");

    // ECGMONITORCTRL控制常量
    private static final byte ECGMONITORCTRL_STOP =             (byte) 0x00;        // 停止采集
    private static final byte ECGMONITORCTRL_STARTSIGNAL =      (byte) 0x01;        // 启动采集Ecg信号
    private static final byte ECGMONITORCTRL_START1MV =         (byte) 0x02;        // 启动采集1mV定标

    private final EcgMonitorDevice device;

    public EcgMonitorGattOperator(EcgMonitorDevice device) {
        super(device.getCommandExecutor());
        this.device = device;
    }

    // 读采样率
    public void readSampleRate() {
        addReadCommand(ECGMONITORSAMPLERATE, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                device.sendGattMessage(MSG_OBTAINSAMPLERATE, (data[0] & 0xff) | ((data[1] << 8) & 0xff00));
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });
    }

    // 读导联类型
    public void readLeadType() {
        addReadCommand(ECGMONITORLEADTYPE, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                device.sendGattMessage(MSG_OBTAINLEADTYPE, data[0]);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });
    }

    // 启动ECG信号采集
    public void startSampleEcg() {

        IBleDataOpCallback indicationCallback = new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                device.sendGattMessage(MSG_OBTAINDATA, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        };

        // enable ECG data indication
        addIndicateCommand(ECGMONITORDATACCC, true, null, indicationCallback);

        addWriteCommand(ECGMONITORCTRL, ECGMONITORCTRL_STARTSIGNAL, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                device.sendGattMessage(MSG_STARTSAMPLINGSIGNAL, null);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        });
    }

    // 启动1mV定标信号采集
    public void startSample1mV() {
        IBleDataOpCallback indicationCallback = new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                device.sendGattMessage(MSG_OBTAINDATA, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        };

        // enable ECG data indication
        addIndicateCommand(ECGMONITORDATACCC, true, null, indicationCallback);

        addWriteCommand(ECGMONITORCTRL, ECGMONITORCTRL_START1MV, null);
    }

    // 停止数据采集
    public void stopSampleData() {
        // disable ECG data indication
        addIndicateCommand(ECGMONITORDATACCC, false, null, null);

        addWriteCommand(ECGMONITORCTRL, ECGMONITORCTRL_STOP, null);
    }

    @Override
    public boolean checkService() {
        if(BleDeviceUtil.getGattObject(device, ECGMONITORDATA) == null) return false;
        if(BleDeviceUtil.getGattObject(device, ECGMONITORCTRL) == null) return false;
        if(BleDeviceUtil.getGattObject(device, ECGMONITORSAMPLERATE) == null) return false;
        if(BleDeviceUtil.getGattObject(device, ECGMONITORLEADTYPE) == null) return false;
        if(BleDeviceUtil.getGattObject(device, ECGMONITORDATACCC) == null) return false;

        ViseLog.i("EcgMonitor Services is ok!");
        return true;
    }
}
