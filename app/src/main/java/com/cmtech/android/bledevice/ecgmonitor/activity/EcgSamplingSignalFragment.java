package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgMarkerAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgAbnormal;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.activity
 * ClassName:      EcgSamplingSignalFragment
 * Description:    控制采集Ecg信号的Fragment
 * Author:         chenm
 * CreateDate:     2019/4/15 上午5:26
 * UpdateUser:     更新者
 * UpdateDate:     2019/4/15 上午5:26
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class EcgSamplingSignalFragment extends Fragment{
    private ImageButton ibRecord; // 切换记录信号状态

    private TextView tvRecordTime; // 记录信号时长

    private RecyclerView rvMarker; // 标记recycleview

    private EcgMarkerAdapter markerAdapter; // ecg标记adapter

    private EcgMonitorDevice device;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_sample_ecgsignal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvRecordTime = view.findViewById(R.id.tv_ecg_signal_recordtime);
        setSignalSecNum(device.getEcgSignalRecordSecond());

        rvMarker = view.findViewById(R.id.rv_ecg_marker);

        LinearLayoutManager markerLayoutManager = new LinearLayoutManager(getContext());
        markerLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        rvMarker.setLayoutManager(markerLayoutManager);
        if(getContext() != null)
            rvMarker.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        List<EcgAbnormal> ecgAbnormals = new ArrayList<>(Arrays.asList(EcgAbnormal.values()));
        markerAdapter = new EcgMarkerAdapter(ecgAbnormals, new EcgMarkerAdapter.OnMarkerClickListener() {
            @Override
            public void onMarkerClicked(EcgAbnormal marker) {
                if(device != null)
                    device.addCommentContent(DateTimeUtil.secToTimeInChinese((int)(device.getEcgSignalRecordDataNum() / device.getSampleRate())) + '，' + marker.getDescription() + '；');
            }
        });

        rvMarker.setAdapter(markerAdapter);

        ibRecord = view.findViewById(R.id.ib_ecg_record);
        // 根据设备的isRecord初始化Record按钮
        setSignalRecordStatus(device.isRecordEcgSignal());
        ibRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                device.setEcgSignalRecord(!device.isRecordEcgSignal());
            }
        });
    }

    public void setSignalSecNum(final int second) {
        tvRecordTime.setText(DateTimeUtil.secToTimeInChinese(second));
    }

    public void setDevice(EcgMonitorDevice device) {
        this.device = device;
    }

    public void setSignalRecordStatus(final boolean isRecord) {
        int imageId = (isRecord) ? R.mipmap.ic_ecg_record_start : R.mipmap.ic_ecg_record_stop;

        ibRecord.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), imageId));

        markerAdapter.setEnabled(isRecord);
    }
}
