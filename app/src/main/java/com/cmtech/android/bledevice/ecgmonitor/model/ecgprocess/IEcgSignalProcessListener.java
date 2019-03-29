package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrAbnormalWarner;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrRecorder;

/**
 * IEcgSignalProcessListener: 与心电信号处理相关的所有监听器接口
 */

public interface IEcgSignalProcessListener extends EcgSignalProcessor.IEcgSignalUpdatedListener, EcgSignalProcessor.IEcgHrValueUpdatedListener, EcgHrAbnormalWarner.IEcgHrAbnormalListener, EcgHrRecorder.IEcgHrInfoUpdatedListener {
}
