package com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate;

import com.cmtech.android.btdevice.ecgmonitor.EcgMonitorDevice;
import com.cmtech.dsp.bmefile.BmeFileDataType;
import com.cmtech.dsp.bmefile.BmeFileHead10;
import com.cmtech.dsp.bmefile.BmeFileHeadFactory;
import com.cmtech.dsp.exception.FileException;
import com.cmtech.dsp.filter.design.DCBlockDesigner;
import com.cmtech.dsp.filter.design.NotchDesigner;
import com.cmtech.dsp.filter.structure.StructType;
import com.vise.log.ViseLog;

import java.nio.ByteOrder;

public class EcgMonitorCalibratedState implements IEcgMonitorState {
    private EcgMonitorDevice device;

    public EcgMonitorCalibratedState(EcgMonitorDevice device) {
        this.device = device;
    }

    @Override
    public void start() {

        // 启动采样心电信号
        device.setState(device.getSamplingState());
        device.startSampleEcg();
    }

    @Override
    public void stop() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void switchState() {
        start();
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
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public boolean canStart() {
        return true;
    }

    @Override
    public boolean canStop() {
        return false;
    }
}
