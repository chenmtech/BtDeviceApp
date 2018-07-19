package com.cmtech.android.btdeviceapp.model;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceController;
import com.cmtech.android.btdeviceapp.interfa.IBleDevice;

/**
 * Created by bme on 2018/2/27.
 */

public abstract class BleDeviceFragment extends Fragment{
    // MainActivity
    protected MainActivity activity;

    // 对应的控制器接口
    protected IBleDeviceController controller;

    // 对应的设备接口
    protected BleDevice device;

    // 设备连接状态tv
    //protected TextView tvConnectState;

    // 切换设备连接状态开关
    //protected ImageButton btnSwitchConnectState;

    // 关闭设备开关
    //protected ImageButton btnClose;

    public BleDeviceFragment() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*tvConnectState = view.findViewById(R.id.device_connect_state_tv);

        btnSwitchConnectState = view.findViewById(R.id.device_connectswitch_btn);
        btnSwitchConnectState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchDeviceConnectState();
            }
        });


        btnClose = view.findViewById(R.id.device_close_btn);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeDevice();
            }
        });*/
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
            device = (BleDevice) controller.getDevice();
        }

        if(device == null || controller == null) {
            throw new IllegalStateException();
        }
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

        controller.closeDevice();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // 获取设备
    public IBleDevice getDevice() {
        return device;
    }

    // 连接设备
    public void connectDevice() {
        if(canConnect())
            controller.connectDevice();
    }

    // 断开设备
    public void disconnectDevice() {
        if(canDisconnect())
            controller.disconnectDevice();
    }

    // 关闭设备
    // 为什么这里不是调用controller.closeDevice()，而是调用activity.closeDevice(fragment)???
    // 因为关闭一个带Fragment的设备，除了要关闭设备本身以外，还要销毁它的Fragment，并将设备的控制器从控制器列表中删除
    // 这些动作需要调用activity.closeDevice才能完成
    // 关闭设备的动作会在销毁Fragment时触发onDestroy()，那里会调用controller.closeDevice()来关闭设备
    public void closeDevice() {
        if(canClose())
            activity.closeDevice(this);
    }

    // 切换设备的连接状态
    public void switchDeviceConnectState() {
        controller.switchDeviceConnectState();
    }

    // 更新设备连接状态
    public void updateConnectState(final BleDevice device) {
        // isAdded()用来判断Fragment是否与Activity关联，如果关联了，才能更新状态信息
        if(device == this.device && isAdded()) {
            updateConnectState();
        }
    }

    private boolean canConnect() {
        return device.canConnect();
    }

    public boolean canDisconnect() {
        return device.canDisconnect();
    }

    public boolean canClose() {
        return device.canClose();
    }

    private void updateConnectState() {
        /*tvConnectState.setText(device.getDeviceConnectState().getDescription());
        switch (device.getDeviceConnectState()) {
            case CONNECT_SUCCESS:
                setImageButton(btnSwitchConnectState, R.mipmap.ic_connect_32px, true);
                break;

            case CONNECT_DISCONNECTING:
            case CONNECT_CONNECTING:
                setImageButton(btnSwitchConnectState, R.mipmap.ic_connecting_32px, false);
                break;

            default:
                setImageButton(btnSwitchConnectState, R.mipmap.ic_disconnect_32px, true);
                break;
        }*/
    }

    private void setImageButton(ImageButton btn, int imageId, boolean enable) {
        btn.setImageDrawable(getResources().getDrawable(imageId));
        btn.setEnabled(enable);
    }
    //////////////////////////////////////////////////////////////////////////

}
