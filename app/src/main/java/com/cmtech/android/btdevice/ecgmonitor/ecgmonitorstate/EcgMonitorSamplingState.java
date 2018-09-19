package com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate;

import android.os.Message;

import com.cmtech.android.btdevice.ecgmonitor.EcgMonitorDevice;
import com.cmtech.dsp.exception.FileException;
import com.vise.log.ViseLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EcgMonitorSamplingState implements IEcgMonitorState {
    private EcgMonitorDevice device;

    public EcgMonitorSamplingState(EcgMonitorDevice device) {
        this.device = device;
    }

    @Override
    public void start() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void stop() {
        device.stopSampleData();
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
