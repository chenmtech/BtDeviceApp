package com.cmtech.android.bledeviceapp.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.LocalDevicesAdapter;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.activity
 * ClassName:      LocalDevicesFragment
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2019-11-26 02:33
 * UpdateUser:     更新者
 * UpdateDate:     2019-11-26 02:33
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class LocalDevicesFragment extends Fragment {
    private LocalDevicesAdapter localDevicesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_local_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化已注册设备列表
        RecyclerView rvDevices = view.findViewById(R.id.rv_local_device);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvDevices.setLayoutManager(layoutManager);
        rvDevices.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        localDevicesAdapter = new LocalDevicesAdapter((MainActivity) getActivity());
        rvDevices.setAdapter(localDevicesAdapter);
    }

    public void update() {
        localDevicesAdapter.update();
    }
}
