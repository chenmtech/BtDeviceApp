package com.cmtech.android.btdeviceapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceFragment;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;
import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;

/**
 * Created by bme on 2018/2/27.
 */

public abstract class BLEDeviceFragment extends Fragment implements IBLEDeviceFragment {
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

                close();
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
        controller = activity.findController(this);

        // 获取device
        if(controller != null) {
            device = controller.getDevice();
        }

        if(device == null || controller == null) {
            throw new IllegalStateException();
        }

        device.registerDeviceObserver(this);

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

    /////////////// IBLEDeviceObserver接口函数//////////////////////
    @Override
    public void updateDeviceInfo(final BLEDeviceModel device, final int type) {
        if(device == this.device) {
            switch (type) {
                case TYPE_MODIFY_CONNECTSTATE:
                    updateConnectState();
                    break;

                default:
                    break;
            }

        }
    }
    //////////////////////////////////////////////////////////////////////////




    ////////////////////////IDeviceFragment接口函数/////////////////////////////
    @Override
    public void updateConnectState() {
        if(device != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    tvConnectState.setText(device.getDeviceState().getDescription());
                    switch (device.getDeviceState()) {
                        case CONNECT_SUCCESS:
                            btnConnectSwitch.setImageDrawable(getResources().getDrawable(R.mipmap.ic_connect_32px));
                            btnConnectSwitch.setEnabled(true);
                            break;

                        case CONNECT_PROCESS:
                        case CONNECT_DISCONNECTING:
                            btnConnectSwitch.setImageDrawable(getResources().getDrawable(R.mipmap.ic_connecting_32px));
                            btnConnectSwitch.setEnabled(false);
                            break;

                        default:
                            btnConnectSwitch.setImageDrawable(getResources().getDrawable(R.mipmap.ic_disconnect_32px));
                            btnConnectSwitch.setEnabled(true);
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void connectDevice() {
        controller.connectDevice();
    }

    @Override
    public void disconnectDevice() {
        controller.disconnectDevice();

    }


    @Override
    public void close() {
        // 断开设备
        disconnectDevice();

        device.removeDeviceObserver(this);

        // 让观察者删除此Fragment
        activity.deleteFragment(this);
    }





}
