package com.cmtech.android.bledevice.ecg.fragment;

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

import com.cmtech.android.bledevice.ecg.adapter.EcgReceiverAdapter;
import com.cmtech.android.bledevice.ecg.device.EcgDevice;
import com.cmtech.android.bledevice.ecg.device.EcgHttpBroadcast;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;

import java.util.List;

public class EcgBroadcastFragment extends Fragment {
    public static final String TITLE = "心电广播";
    private ImageButton ibBroadcast; // 切换记录广播状态
    private RecyclerView rvReceiver; // 接收者recycleview
    private EcgReceiverAdapter receiverAdapter; // 接收者adapter
    private EcgDevice device;

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
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvReceiver.setLayoutManager(layoutManager);
        if(getContext() != null)
            rvReceiver.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
        receiverAdapter = new EcgReceiverAdapter(new EcgReceiverAdapter.OnReceiverChangedListener() {
            @Override
            public void onReceiverChanged(final EcgHttpBroadcast.Receiver receiver, boolean isChecked) {
                ViseLog.e(receiver + " Check: " + isChecked);
                if(isChecked) {
                    device.addBroadcastReceiver(receiver);
                } else {
                    device.deleteBroadcastReceiver(receiver);
                }
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

    public void setDevice(EcgDevice device) {
        this.device = device;
    }

    public void setBroadcastStatus(final boolean isBroadcast) {
        int imageId = (isBroadcast) ? R.mipmap.ic_start_48px : R.mipmap.ic_stop_48px;
        ibBroadcast.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), imageId));
        receiverAdapter.setEnabled(isBroadcast);
    }

    public void setBroadcastReceiver(final List<EcgHttpBroadcast.Receiver> receivers) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receiverAdapter.setReceivers(receivers);
            }
        });
    }

    public void updateBroadcastReceiver() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receiverAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
