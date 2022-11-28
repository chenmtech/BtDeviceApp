package com.cmtech.android.bledeviceapp.data.record;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.ALL;

import java.io.IOException;
import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.data.record
 * ClassName:      AllTypeRecord
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/10/8 上午5:27
 * UpdateUser:     更新者
 * UpdateDate:     2020/10/8 上午5:27
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class AllTypesRecord extends BasicRecord {
    private AllTypesRecord(String ver, int accountId, long createTime, String devAddress,
                           int sampleRate, int channelNum, String gain, String unit) {
        super(ALL, ver, accountId, createTime, devAddress, sampleRate, channelNum, 1, gain, unit);
    }

    @Override
    public int[] readData() throws IOException {
        throw new IOException("Error!");
    }

    @Override
    public int getDataNum() {
        throw new IllegalStateException("");
    }

    @Override
    public List<Integer> getGain() {
        throw new IllegalStateException("");
    }

    @Override
    public int getSampleRate() {
        throw new IllegalStateException("");
    }

    @Override
    public boolean isEOD() {
        throw new IllegalStateException("");
    }

    @Override
    public void seek(int pos) {
        throw new IllegalStateException("");
    }

}
