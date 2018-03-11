package com.cmtech.android.btdeviceapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.common.ConnectState;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.DeviceMirrorPool;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDevice;

/**
 * Created by bme on 2018/2/27.
 */

public abstract class DeviceFragment extends Fragment implements IDeviceFragment {
    protected MyBluetoothDevice device;
    protected IDeviceFragmentObserver fragmentListener;

    protected TextView tvConnectState;
    protected Button btnDisconnect;
    protected Button btnClose;

    /*public interface IDeviceFragmentObserver {
        // 用Fragment找到相应的Device
        MyBluetoothDevice findDeviceFromFragment(DeviceFragment fragment);
        // 关闭Fragment及其相应的Device
        void closeFragmentAndDevice(DeviceFragment fragment);
    }*/

    public DeviceFragment() {

    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvConnectState = view.findViewById(R.id.device_connect_state_tv);
        btnDisconnect = view.findViewById(R.id.device_disconnect_btn);
        btnClose = view.findViewById(R.id.device_close_btn);

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(device == null) return;
                ConnectState state = device.getConnectState();
                if(state == ConnectState.CONNECT_SUCCESS || state == ConnectState.CONNECT_PROCESS)

                    device.disconnect();
                else
                    connectDevice();
            }
        });


        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DeviceFragment", DeviceFragment.this.getClass().getSimpleName() + "is closed.");

                if(fragmentListener != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            fragmentListener.closeFragmentAndDevice(DeviceFragment.this);
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof IDeviceFragmentObserver)) {
            throw new IllegalStateException("context没有实现IDeviceFragmentListener接口");
        }

        // 获取listener
        fragmentListener = (IDeviceFragmentObserver) context;
        // 获取device
        device = fragmentListener.findDeviceFromFragment(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateConnectState();
    }


    protected void updateConnectState() {
        if(device != null) {
            tvConnectState.setText(device.getConnectStateString());
            if(device.getConnectState() == ConnectState.CONNECT_SUCCESS
                    || device.getConnectState() == ConnectState.CONNECT_PROCESS)
                btnDisconnect.setText("断开");
            else
                btnDisconnect.setText("连接");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        fragmentListener = null;

        if(device != null) {
            device.disconnect();
        }

        device = null;
    }

    public void connectDevice() {
        if(device == null) return;
        ConnectState state = device.getConnectState();
        if(state == ConnectState.CONNECT_SUCCESS || state == ConnectState.CONNECT_PROCESS) return;
        device.connect(new IConnectCallback() {
            @Override
            public void onConnectSuccess(DeviceMirror deviceMirror) {
                DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();
                if(deviceMirrorPool.isContainDevice(deviceMirror)) {
                    device.setDeviceMirror(deviceMirror);
                    device.setConnectState(ConnectState.CONNECT_SUCCESS);
                    initProcess();
                }
            }

            @Override
            public void onConnectFailure(BleException exception) {
                device.setConnectState(ConnectState.CONNECT_FAILURE);
            }

            @Override
            public void onDisconnect(boolean isActive) {
                device.setConnectState(ConnectState.CONNECT_DISCONNECT);
            }
        });
    }

    public abstract void initProcess();

}
