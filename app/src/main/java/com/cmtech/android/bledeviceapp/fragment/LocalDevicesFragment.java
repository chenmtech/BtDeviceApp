package com.cmtech.android.bledeviceapp.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.cmtech.android.bledeviceapp.adapter.LocalDeviceAdapter;

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
    public static final int TITLE_ID = R.string.local_device;
    private LocalDeviceAdapter localDeviceAdapter;
    private RecyclerView rvDevices;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_device_local, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化已注册设备列表
        rvDevices = view.findViewById(R.id.rv_local_device);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvDevices.setLayoutManager(layoutManager);
        rvDevices.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        localDeviceAdapter = new LocalDeviceAdapter((MainActivity) getActivity());
        rvDevices.setAdapter(localDeviceAdapter);
    }

    public void update() {
        if(localDeviceAdapter != null)
            localDeviceAdapter.update();
    }
}
