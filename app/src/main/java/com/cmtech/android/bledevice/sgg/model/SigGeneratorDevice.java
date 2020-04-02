package com.cmtech.android.bledevice.sgg.model;

import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceInfo;
import com.cmtech.android.ble.utils.UuidUtil;
import com.vise.log.ViseLog;

import java.util.UUID;

import static com.cmtech.android.bledeviceapp.AppConstant.MY_BASE_UUID;

public class SigGeneratorDevice extends AbstractDevice {
    // 信号发生器Service UUID常量
    private static final String sigGeneratorServiceUuid       = "aa50";           // 信号发生器服务UUID:aa50
    private static final String sigGeneratorCtrlUuid          = "aa51";           // 启动/停止信号的控制UUID:aa51
    private static final String sigGeneratorTypeUuid          = "aa52";           // 信号类型UUID:aa52
    private static final String sigGeneratorMagUuid          = "aa53";           // 信号幅度UUID:aa53
    private static final String sigGeneratorFreqUuid        = "aa54";           // 信号频率UUID:aa54
    private static final String sigGeneratorBaselineUuid      = "aa55";           // 信号基线UUID:aa55

    private static final UUID sigGeneratorServiceUUID       = UuidUtil.stringToUUUID(sigGeneratorServiceUuid, MY_BASE_UUID);
    private static final UUID sigGeneratorCtrlUUID          = UuidUtil.stringToUUUID(sigGeneratorCtrlUuid, MY_BASE_UUID);
    private static final UUID sigGeneratorTypeUUID          = UuidUtil.stringToUUUID(sigGeneratorTypeUuid, MY_BASE_UUID);
    private static final UUID sigGeneratorMagUUID          = UuidUtil.stringToUUUID(sigGeneratorMagUuid, MY_BASE_UUID);
    private static final UUID sigGeneratorFreqUUID        = UuidUtil.stringToUUUID(sigGeneratorFreqUuid, MY_BASE_UUID);
    private static final UUID sigGeneratorBaselineUUID      = UuidUtil.stringToUUUID(sigGeneratorBaselineUuid, MY_BASE_UUID);

    // Gatt Element常量
    private static final BleGattElement SIGGENERATOR_CTRL =
            new BleGattElement(sigGeneratorServiceUUID, sigGeneratorCtrlUUID, null, "信号控制");
    private static final BleGattElement SIGGENERATOR_TYPE =
            new BleGattElement(sigGeneratorServiceUUID, sigGeneratorTypeUUID, null, "信号类型");
    private static final BleGattElement SIGGENERATOR_MAG =
            new BleGattElement(sigGeneratorServiceUUID, sigGeneratorMagUUID, null, "信号幅度");
    private static final BleGattElement SIGGENERATOR_FREQ =
            new BleGattElement(sigGeneratorServiceUUID, sigGeneratorFreqUUID, null, "信号频率");
    private static final BleGattElement SIGGENERATOR_BASELINE =
            new BleGattElement(sigGeneratorServiceUUID, sigGeneratorBaselineUUID, null, "信号基线");

    // ECGMONITOR_CTRL Element的控制常量
    private static final byte SIGGENERATOR_CTRL_STOP =             (byte) 0x00;        // 停止信号
    private static final byte SIGGENERATOR_CTRL_START =             (byte) 0x01;        // 启动信号

    // 构造器
    public SigGeneratorDevice(DeviceInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public boolean onConnectSuccess() {
        BleGattElement[] elements = new BleGattElement[]{SIGGENERATOR_CTRL, SIGGENERATOR_TYPE, SIGGENERATOR_MAG, SIGGENERATOR_FREQ, SIGGENERATOR_BASELINE};

        if(!((BleConnector)connector).containGattElements(elements)) {
            ViseLog.e("Signal Generator Elements are wrong.");

            //disconnect();

            return false;
        }

        return true;
    }

    @Override
    public void onConnectFailure() {

    }

    @Override
    public void onDisconnect() {

    }

    private void setSignalParameter() {

    }

    private void startGeneratingSignal() {
        ((BleConnector)connector).write(SIGGENERATOR_CTRL, SIGGENERATOR_CTRL_START, null);
    }

    private void stopGeneratingSignal() {
        ((BleConnector)connector).write(SIGGENERATOR_CTRL, SIGGENERATOR_CTRL_STOP, null);
    }
}
