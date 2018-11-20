package com.cmtech.android.bledevice.ecgmonitor.model.state;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.vise.log.ViseLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EcgMonitorSampleState implements IEcgMonitorState {
    private EcgMonitorDevice device;

    public EcgMonitorSampleState(EcgMonitorDevice device) {
        this.device = device;
    }

    @Override
    public void start() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void stop() {
        device.stopSampleData();

        device.getHandler().removeCallbacksAndMessages(null);

        device.setState(device.getCalibratedState());
    }

    @Override
    public void switchState() {
        stop();
    }

    @Override
    public void onCalibrateSuccess() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void onCalibrateFailure() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void onProcessData(byte[] data) {
        // 单片机发过来的是LITTLE_ENDIAN的数据
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        // 单片机发过来的int是两个字节的short
        for(int i = 0; i < data.length/2; i++) {
            int tmpData = buffer.getShort();

            device.processOneEcgData(tmpData);
        }
    }

    @Override
    public boolean canStart() {
        return false;
    }

    @Override
    public boolean canStop() {
        return true;
    }
}
