package com.cmtech.android.bledevice.siggenerator.model;

import android.content.Context;

import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.ble.extend.BleDeviceBasicInfo;
import com.cmtech.android.ble.extend.BleGattElement;
import com.vise.log.ViseLog;

import static com.cmtech.android.bledeviceapp.BleDeviceConstant.MY_BASE_UUID;

public class SigGeneratorDevice extends BleDevice {
    // 信号发生器Service UUID常量
    private static final String sigGeneratorServiceUuid       = "aa50";           // 信号发生器服务UUID:aa50
    private static final String sigGeneratorCtrlUuid          = "aa51";           // 启动/停止信号的控制UUID:aa51
    private static final String sigGeneratorTypeUuid          = "aa52";           // 信号类型UUID:aa52
    private static final String sigGeneratorMagUuid          = "aa53";           // 信号幅度UUID:aa53
    private static final String sigGeneratorFreqUuid        = "aa54";           // 信号频率UUID:aa54
    private static final String sigGeneratorBaselineUuid      = "aa55";           // 信号基线UUID:aa55

    // Gatt Element常量
    private static final BleGattElement SIGGENERATOR_CTRL =
            new BleGattElement(sigGeneratorServiceUuid, sigGeneratorCtrlUuid, null, MY_BASE_UUID, "信号控制");
    private static final BleGattElement SIGGENERATOR_TYPE =
            new BleGattElement(sigGeneratorServiceUuid, sigGeneratorTypeUuid, null, MY_BASE_UUID, "信号类型");
    private static final BleGattElement SIGGENERATOR_MAG =
            new BleGattElement(sigGeneratorServiceUuid, sigGeneratorMagUuid, null, MY_BASE_UUID, "信号幅度");
    private static final BleGattElement SIGGENERATOR_FREQ =
            new BleGattElement(sigGeneratorServiceUuid, sigGeneratorFreqUuid, null, MY_BASE_UUID, "信号频率");
    private static final BleGattElement SIGGENERATOR_BASELINE =
            new BleGattElement(sigGeneratorServiceUuid, sigGeneratorBaselineUuid, null, MY_BASE_UUID, "信号基线");

    // ECGMONITOR_CTRL Element的控制常量
    private static final byte SIGGENERATOR_CTRL_STOP =             (byte) 0x00;        // 停止信号
    private static final byte SIGGENERATOR_CTRL_START =             (byte) 0x01;        // 启动信号

    // 构造器
    public SigGeneratorDevice(Context context, BleDeviceBasicInfo basicInfo) {
        super(context, basicInfo);

    }

    @Override
    protected boolean executeAfterConnectSuccess() {
        BleGattElement[] elements = new BleGattElement[]{SIGGENERATOR_CTRL, SIGGENERATOR_TYPE, SIGGENERATOR_MAG, SIGGENERATOR_FREQ, SIGGENERATOR_BASELINE};

        if(!containGattElements(elements)) {
            ViseLog.e("Signal Generator Elements are wrong.");

            //startDisconnection();

            return false;
        }

        return true;
    }

    @Override
    protected void executeAfterConnectFailure() {

    }

    @Override
    protected void executeAfterDisconnect() {

    }

    private void setSignalParameter() {

    }

    private void startGeneratingSignal() {
        write(SIGGENERATOR_CTRL, SIGGENERATOR_CTRL_START, null);
    }

    private void stopGeneratingSignal() {
        write(SIGGENERATOR_CTRL, SIGGENERATOR_CTRL_STOP, null);
    }
}
