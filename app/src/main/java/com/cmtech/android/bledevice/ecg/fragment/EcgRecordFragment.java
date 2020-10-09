package com.cmtech.android.bledevice.ecg.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecg.adapter.EcgMarkerAdapter;
import com.cmtech.android.bledevice.ecg.device.AbstractEcgDevice;
import com.cmtech.android.bledevice.ecg.enumeration.EcgAbnormal;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.activity
 * ClassName:      EcgRecordFragment
 * Description:    控制记录Ecg信号的Fragment
 * Author:         chenm
 * CreateDate:     2019-04-15 上午5:26
 * UpdateUser:     更新者
 * UpdateDate:     2019/4/15 上午5:26
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class EcgRecordFragment extends Fragment{
    public static final String TITLE = "心电记录";
    private ImageButton ibRecord; // 切换信号记录状态
    private TextView tvRecordTime; // 已记录信号时长
    private RecyclerView rvMarker; // 标记recycleview
    private EcgMarkerAdapter markerAdapter; // 标记adapter
    private AbstractEcgDevice device;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_ecg_signal_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(device == null) {
            throw new IllegalStateException("The device is null.");
        }

        tvRecordTime = view.findViewById(R.id.tv_ecg_signal_recordtime);
        setSignalSecNum(device.getRecordSecond());

        rvMarker = view.findViewById(R.id.rv_ecg_marker);
        GridLayoutManager layoutManage = new GridLayoutManager(getContext(), 2);
        rvMarker.setLayoutManager(layoutManage);
        rvMarker.setLayoutManager(layoutManage);
        List<EcgAbnormal> ecgAbnormals = new ArrayList<>();
        if(device.isLocal()) {
            for(int i = 0; i < 4; i++)
                ecgAbnormals.add(EcgAbnormal.getFromCode(i));
        } else {
            for(int i = 4; i < 8; i++)
                ecgAbnormals.add(EcgAbnormal.getFromCode(i));
        }
        //List<EcgAbnormal> ecgAbnormals = new ArrayList<>(Arrays.asList(EcgAbnormal.values()));
        markerAdapter = new EcgMarkerAdapter(ecgAbnormals, new EcgMarkerAdapter.OnMarkerClickListener() {
            @Override
            public void onMarkerClicked(EcgAbnormal marker) {
                if(device != null)
                    device.addCommentContent(DateTimeUtil.secToTimeInChinese((int)(device.getRecordDataNum() / device.getSampleRate())) + '，' + marker.getDescription() + '；');
            }
        });
        rvMarker.setAdapter(markerAdapter);

        ibRecord = view.findViewById(R.id.ib_ecg_record);
        // 根据设备的isRecord初始化Record按钮
        setRecordStatus(device.isRecord());
        ibRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                device.setRecord(!device.isRecord());
            }
        });
    }

    public void setDevice(AbstractEcgDevice device) {
        this.device = device;
    }

    public void setSignalSecNum(final int second) {
        tvRecordTime.setText(DateTimeUtil.secToTimeInChinese(second));
    }

    public void setRecordStatus(final boolean isRecord) {
        int imageId = (isRecord) ? R.mipmap.ic_start_32px : R.mipmap.ic_stop_32px;
        ibRecord.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), imageId));
        markerAdapter.setEnabled(isRecord);
    }
}
