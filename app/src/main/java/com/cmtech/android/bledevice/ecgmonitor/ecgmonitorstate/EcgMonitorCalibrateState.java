package com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate;

import com.cmtech.android.bledevice.ecgmonitor.EcgMonitorDevice;
import com.vise.log.ViseLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

public class EcgMonitorCalibrateState implements IEcgMonitorState {
    private EcgMonitorDevice device;
    private ArrayList<Integer> calibrationData = new ArrayList<Integer>(250);   // 用于保存标定用的数据

    public EcgMonitorCalibrateState(EcgMonitorDevice device) {
        this.device = device;
    }

    @Override
    public void start() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void stop() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void switchState() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void onCalibrateSuccess() {
        //device.stopSampleData();
        device.setState(device.getCalibratedState());
        device.start();
    }

    @Override
    public void onCalibrateFailure() {
        device.stopSampleData();
        device.setState(device.getInitialState());
    }

    @Override
    public void onProcessData(byte[] data) {
        // 单片机发过来的是LITTLE_ENDIAN的数据
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        // 单片机发过来的int是两个字节的short
        for (int i = 0; i < data.length / 2; i++) {
            int tmpData = buffer.getShort();

            // 采集1个周期的定标信号
            if (calibrationData.size() < device.getSampleRate())
                calibrationData.add(tmpData);
            else {
                // 计算1mV定标信号值
                int value1mV = calculateCalibration(calibrationData);
                device.setValue1mV(value1mV);
                calibrationData.clear();

                device.initializeEcgView();

                onCalibrateSuccess();
            }
        }
    }

    @Override
    public boolean canStart() {
        return false;
    }

    @Override
    public boolean canStop() {
        return false;
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
