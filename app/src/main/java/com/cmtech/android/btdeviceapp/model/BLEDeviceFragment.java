package com.cmtech.android.btdeviceapp.model;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceConnectStateObserver;

/**
 * Created by bme on 2018/2/27.
 */

public abstract class BLEDeviceFragment extends Fragment implements IBLEDeviceConnectStateObserver {
    // MainActivity
    protected MainActivity activity;

    // 对应的控制器
    protected BLEDeviceController controller;

    // 对应的设备
    protected BLEDeviceModel device;

    // 连接状态tv
    protected TextView tvConnectState;

    protected ImageButton btnConnectSwitch;
    protected ImageButton btnClose;

    public BLEDeviceFragment() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvConnectState = view.findViewById(R.id.device_connect_state_tv);
        btnConnectSwitch = view.findViewById(R.id.device_connectswitch_btn);
        btnClose = view.findViewById(R.id.device_close_btn);

        btnConnectSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.switchDevice();
            }
        });


        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(BLEDeviceFragment.this.getClass().getSimpleName(), "is closed.");

                closeDevice();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof MainActivity)) {
            throw new IllegalStateException("context不是MainActivity");
        }

        // 获得Activity
        activity = (MainActivity) context;

        // 获取controller
        controller = activity.getController(this);

        // 获取device
        if(controller != null) {
            device = controller.getDevice();
        }

        if(device == null || controller == null) {
            throw new IllegalStateException();
        }

        device.registerConnectStateObserver(this);
    }

    public BLEDeviceModel getDevice() {
        return device;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 更新连接状态
        updateConnectState();

    }

    @Override
    public void onStart() {
        super.onStart();

        // 连接设备
        connectDevice();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        disconnectDevice();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



    public void connectDevice() {
        controller.connectDevice();
    }


    public void disconnectDevice() {
        controller.disconnectDevice();

    }

    public void closeDevice() {
        activity.closeDevice(device);
    }


    private void setImageButton(ImageButton btn, int imageId, boolean enable) {
        btn.setImageDrawable(getResources().getDrawable(imageId));
        btn.setEnabled(enable);
    }

    /////////////// IBLEDeviceConnectStateObserver接口函数//////////////////////
    @Override
    public void updateConnectState(final BLEDeviceModel device) {
        if(device == this.device) {
            updateConnectState();
        }
    }

    private void updateConnectState() {
        tvConnectState.setText(device.getDeviceState().getDescription());
        switch (device.getDeviceState()) {
            case CONNECT_SUCCESS:
                setImageButton(btnConnectSwitch, R.mipmap.ic_connect_32px, true);
                break;

            case CONNECT_DISCONNECTING:
            case CONNECT_CONNECTING:
                setImageButton(btnConnectSwitch, R.mipmap.ic_connecting_32px, false);
                break;

            default:
                setImageButton(btnConnectSwitch, R.mipmap.ic_disconnect_32px, true);
                break;
        }
    }
    //////////////////////////////////////////////////////////////////////////

}
