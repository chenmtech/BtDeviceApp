package com.cmtech.android.bledevice.ecgmonitor.record;

import com.cmtech.android.bledevice.ecgmonitor.interfac.OnEcgRecordListener;
import com.cmtech.android.bledeviceapp.util.HttpUtils;

public class OnEcgRecordWebListener implements OnEcgRecordListener {
    private static final int TYPE_CODE_CREATE_RECORD = 0;
    private static final int TYPE_CODE_RECORD_NAME = 1;
    private static final int TYPE_CODE_CREATE_TIME = 2;
    private static final int TYPE_CODE_CREATOR_ID = 3;
    private static final int TYPE_CODE_MODIFY_TIME = 4;
    private static final int TYPE_CODE_SAMPLE_RATE = 5;
    private static final int TYPE_CODE_CALI_VALUE = 6;
    private static final int TYPE_CODE_DEVICE_ADDRESS = 7;
    private static final int TYPE_CODE_LEAD_TYPE = 8;
    private static final int TYPE_CODE_ECG_SIGNAL = 9;
    private static final int TYPE_CODE_HR_VALUE = 10;
    private static final int TYPE_CODE_START = 11;
    private static final int TYPE_CODE_STOP = 12;

    @Override
    public void onRecordCreated(String recordName) {
        String data = createDataUrlString(TYPE_CODE_CREATE_RECORD, recordName);
        HttpUtils.upload(data);
    }

    @Override
    public void onRecordNameSetup(String recordName) {
        String data = createDataUrlString(TYPE_CODE_RECORD_NAME, recordName);
        HttpUtils.upload(data);
    }

    @Override
    public void onCreateTimeUpdated(long createTime) {
        String data = createDataUrlString(TYPE_CODE_CREATE_TIME, String.valueOf(createTime));
        HttpUtils.upload(data);
    }

    @Override
    public void onCreatorUpdated(String creatorId) {
        String data = createDataUrlString(TYPE_CODE_CREATOR_ID, creatorId);
        HttpUtils.upload(data);
    }

    @Override
    public void onModifyTimeUpdated(long modifyTime) {
        String data = createDataUrlString(TYPE_CODE_MODIFY_TIME, String.valueOf(modifyTime));
        HttpUtils.upload(data);
    }

    @Override
    public void onSampleRateUpdated(int sampleRate) {
        String data = createDataUrlString(TYPE_CODE_SAMPLE_RATE, String.valueOf(sampleRate));
        HttpUtils.upload(data);
    }

    @Override
    public void onCaliValueUpdated(int caliValue) {
        String data = createDataUrlString(TYPE_CODE_CALI_VALUE, String.valueOf(caliValue));
        HttpUtils.upload(data);
    }

    @Override
    public void onDeviceAddressUpdated(String address) {
        String data = createDataUrlString(TYPE_CODE_DEVICE_ADDRESS, address);
        HttpUtils.upload(data);
    }

    @Override
    public void onLeadTypeUpdated(int leadTypeCode) {
        String data = createDataUrlString(TYPE_CODE_LEAD_TYPE, String.valueOf((byte)leadTypeCode));
        HttpUtils.upload(data);
    }

    @Override
    public void onEcgSignalUpdated(int ecgSignal) {
        String data = createDataUrlString(TYPE_CODE_ECG_SIGNAL, String.valueOf(ecgSignal));
        HttpUtils.upload(data);
    }

    @Override
    public void onHrValueUpdated(short hr) {
        String data = createDataUrlString(TYPE_CODE_HR_VALUE, String.valueOf(hr));
        HttpUtils.upload(data);
    }

    @Override
    public void onStart() {
        String data = createDataUrlString(TYPE_CODE_START, "");
        HttpUtils.upload(data);
    }

    @Override
    public void onStop() {
        String data = createDataUrlString(TYPE_CODE_STOP, "");
        HttpUtils.upload(data);
    }

    private static String createDataUrlString(int typeCode, String vStr) {
        StringBuilder builder = new StringBuilder();
        builder.append("?type = ").append(typeCode).append("&value = ").append(vStr);
        return builder.toString();
    }
}
