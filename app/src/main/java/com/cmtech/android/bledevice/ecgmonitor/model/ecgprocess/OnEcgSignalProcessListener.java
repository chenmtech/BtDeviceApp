package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrAbnormalWarner;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess.EcgHrProcessor;

/**
 * OnEcgSignalProcessListener: 与心电信号处理相关的所有监听器接口
 * Created by chenm on 2019/4/8
 *
 */

public interface OnEcgSignalProcessListener extends EcgSignalProcessor.OnEcgSignalUpdateListener, EcgSignalProcessor.OnEcgHrValueUpdateListener, EcgHrAbnormalWarner.IEcgHrAbnormalListener, EcgHrProcessor.OnEcgHrInfoUpdateListener {
}
