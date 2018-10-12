package com.cmtech.android.bledevicecore.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceConnectedState implements IBleDeviceState {
    private BleDevice device;

    public BleDeviceConnectedState(BleDevice device) {
        this.device = device;
    }

    @Override
    public void open() {
        ViseLog.i("action wrong");
    }

    @Override
    public void close() {
        device.setClosing(true);
        disconnect();
    }

    @Override
    public void scan() {
        ViseLog.i("action wrong");
    }

    @Override
    public void disconnect() {
        // 防止接收不到断开连接的回调，而无法执行onDeviceDisconnect()，所以1秒后自动执行。
        device.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                device.processDisconnect();
                if(!device.isClosing())
                    device.setState(device.getDisconnectState());
                else {
                    device.setState(device.getCloseState());
                    device.setClosing(false);
                }
            }
        }, 1000);

        MyApplication.getViseBle().getDeviceMirrorPool().disconnect(device.getBluetoothLeDevice());
        device.setState(device.getDisconnectingState());
    }

    @Override
    public void switchState() {
        disconnect();
    }

    @Override
    public void onDeviceScanSuccess() {
        ViseLog.i("callback wrong");
    }

    @Override
    public void onDeviceScanFailure() {
        ViseLog.i("callback wrong");
    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {
        ViseLog.i("callback wrong");
    }

    @Override
    public void onDeviceConnectFailure() {
        device.getHandler().removeCallbacksAndMessages(null);
        device.setState(device.getDisconnectState());
        device.processConnectFailure();
    }

    @Override
    public void onDeviceConnectTimeout() {
        device.getHandler().removeCallbacksAndMessages(null);
        device.setState(device.getDisconnectState());
        device.processConnectFailure();
    }

    @Override
    public void onDeviceDisconnect() {
        device.getHandler().removeCallbacksAndMessages(null);
        if(!device.isClosing())
            device.setState(device.getDisconnectState());
        else {
            device.setState(device.getCloseState());
            device.setClosing(false);
        }
        device.processDisconnect();
    }

    @Override
    public String getStateDescription() {
        return "连接成功";
    }

    @Override
    public boolean canConnect() {
        return false;
    }

    @Override
    public boolean canDisconnect() {
        return true;
    }

    @Override
    public boolean canClose() {
        return true;
    }
}
