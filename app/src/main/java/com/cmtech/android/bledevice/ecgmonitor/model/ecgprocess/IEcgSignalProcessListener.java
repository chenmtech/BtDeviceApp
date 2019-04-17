package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrAbnormalWarner;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrProcessor;

/**
 * IEcgSignalProcessListener: 与心电信号处理相关的所有监听器接口
 * Created by chenm on 2019/4/8
 *
 */

public interface IEcgSignalProcessListener extends EcgSignalProcessor.IEcgSignalUpdatedListener, EcgSignalProcessor.IEcgHrValueUpdatedListener, EcgHrAbnormalWarner.IEcgHrAbnormalListener, EcgHrProcessor.OnEcgHrInfoUpdateListener {
}
