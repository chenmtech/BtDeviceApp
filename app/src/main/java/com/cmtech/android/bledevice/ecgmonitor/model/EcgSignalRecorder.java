package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendix;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.EcgMonitorConstant.ECG_FILE_DIR;

/**
 * EcgSignalRecorder: 心电记录器
 * Created by Chenm, 2018-12-27
 */

public class EcgSignalRecorder {
    public interface IEcgRecordSecondUpdatedListener {
        void onEcgRecordSecondUpdated(int second); // 更新记录的秒数
    }

    private final List<Integer> ecgSignalList = new ArrayList<>();

    private int sampleRate = 125; // 采样频率

    private EcgAppendix appendix; // 当前信号的留言

    private IEcgRecordSecondUpdatedListener listener; // 心电记录秒数更新监听器

    // 获取记录的秒数
    public int getRecordSecond() {
        return ecgSignalList.size()/sampleRate;
    }

    // 获取记录的数据个数
    public long getRecordDataNum() {
        return ecgSignalList.size();
    }

    public List<Integer> getEcgSignalList() {
        return ecgSignalList;
    }

    public EcgAppendix getAppendix() {
        return appendix;
    }

    public EcgSignalRecorder(int sampleRate) {
        this.sampleRate = sampleRate;
        appendix = EcgAppendix.createDefaultAppendix();
    }

    // 记录心电信号
    public synchronized void record(int ecgSignal) {
        ecgSignalList.add(ecgSignal);
        if(listener != null) listener.onEcgRecordSecondUpdated(getRecordSecond());
    }

    // 添加留言的内容
    public void addAppendixContent(String content) {
        appendix.appendContent(content);
    }

    public void setEcgRecordSecondUpdatedListener(IEcgRecordSecondUpdatedListener listener) {
        this.listener = listener;
    }

    public void removeEcgRecordSecondUpdatedListener() {
        listener = null;
    }
}
