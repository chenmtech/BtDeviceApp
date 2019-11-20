package com.cmtech.android.bledevice.ecgmonitor.fragment;

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

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgReceiverAdapter;
import com.cmtech.android.bledevice.ecgmonitor.device.EcgMonitorDevice;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.User;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;

public class EcgBroadcastFragment extends Fragment {
    public static final String TITLE = "信号广播";
    private ImageButton ibBroadcast; // 切换记录广播状态
    private RecyclerView rvReceiver; // 接收者recycleview
    private EcgReceiverAdapter receiverAdapter; // 接收者adapter
    private List<User> receivers;
    private EcgMonitorDevice device;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_ecg_signal_broadcast, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(device == null) {
            throw new IllegalStateException("The device is null.");
        }

        rvReceiver = view.findViewById(R.id.rv_ecg_signal_receiver);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvReceiver.setLayoutManager(layoutManager);
        if(getContext() != null)
            rvReceiver.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        initReceivers();
        receiverAdapter = new EcgReceiverAdapter(receivers, new EcgReceiverAdapter.OnReceiverChangeListener() {
            @Override
            public void onReceiverChanged(User receiver, boolean isChecked) {
                ViseLog.e(receiver + " Check: " + isChecked);

            }
        });
        rvReceiver.setAdapter(receiverAdapter);

        ibBroadcast = view.findViewById(R.id.ib_ecg_broadcast);
        // 根据设备的isBroadcast初始化Broadcast按钮
        setBroadcastStatus(device.isBroadcast());
        ibBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                device.setBroadcast(!device.isBroadcast());
            }
        });
    }

    private void initReceivers() {
        receivers = new ArrayList<>();
        User user = new User();
        user.setPhone("101");
        user.setName("陈医生");
        receivers.add(user);
        user = new User();
        user.setPhone("102");
        user.setName("方医生");
        receivers.add(user);
        user = new User();
        user.setPhone("103");
        user.setName("范医生");
        receivers.add(user);
        user = new User();
        user.setPhone("104");
        user.setName("林医生");
        receivers.add(user);
    }

    public void setDevice(EcgMonitorDevice device) {
        this.device = device;
    }

    public void setBroadcastStatus(final boolean isBroadcast) {
        int imageId = (isBroadcast) ? R.mipmap.ic_start_48px : R.mipmap.ic_stop_48px;
        ibBroadcast.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), imageId));
        receiverAdapter.setEnabled(isBroadcast);
    }
}
