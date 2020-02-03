package com.cmtech.android.bledevice.hrmonitor.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cmtech.android.bledevice.hrmonitor.model.HRMonitorDevice;
import com.cmtech.android.bledevice.temphumid.adapter.TempHumidHistoryDataAdapter;
import com.cmtech.android.bledevice.temphumid.model.TempHumidData;
import com.cmtech.android.bledevice.temphumid.model.TempHumidDevice;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledeviceapp.activity.MainActivity;

import java.util.ArrayList;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.view
 * ClassName:      HRMonitorFragment
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020-02-04 06:06
 * UpdateUser:     更新者
 * UpdateDate:     2020-02-04 06:06
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HRMonitorFragment extends DeviceFragment {
    private HRMonitorDevice device;

    public HRMonitorFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (HRMonitorDevice) getDevice();

        return inflater.inflate(R.layout.fragment_hrmonitor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 打开设备
        MainActivity activity = (MainActivity) getActivity();
        device.open(activity.getNotifyService());
    }

    @Override
    public void openConfigureActivity() {

    }
}
