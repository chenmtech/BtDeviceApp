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
import com.cmtech.android.btdeviceapp.interfa.IDeviceFragment;
import com.cmtech.android.btdeviceapp.interfa.IDeviceFragmentObserver;
import com.cmtech.android.btdeviceapp.model.DeviceState;
import com.cmtech.android.btdeviceapp.interfa.IConnectSuccessCallback;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDevice;

/**
 * Created by bme on 2018/2/27.
 */

public abstract class DeviceFragment extends Fragment implements IDeviceFragment {
    // 对应的设备
    protected MyBluetoothDevice device;

    // 观察者，一般为Activity
    protected IDeviceFragmentObserver observer;

    // 连接状态tv
    protected TextView tvConnectState;

    protected ImageButton btnConnectSwitch;
    protected ImageButton btnClose;

    private int times = 0;

    public DeviceFragment() {

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
                if(device == null) return;

                switch (device.getDeviceState()) {
                    case CONNECT_SUCCESS:
                    case CONNECT_PROCESS:
                        disconnectDevice();
                        break;

                    default:
                        connectDevice();
                        break;
                }
            }
        });


        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(DeviceFragment.this.getClass().getSimpleName(), "is closed.");

                close();
            }
        });


    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof IDeviceFragmentObserver)) {
            throw new IllegalStateException("context没有实现IDeviceFragmentObserver接口");
        }

        // 获得Observer
        observer = (IDeviceFragmentObserver) context;

        // 获取device
        device = observer.findDevice(this);

        if(device == null) {
            throw new IllegalStateException("fragment对应的device为空。");
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

        // 执行初始化
        executeGattInitOperation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        disconnectDevice();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        observer = null;

        //device = null;
    }

    /////////////// IMyBluetoothDeviceObserver接口函数//////////////////////
    @Override
    public void updateDeviceInfo(final MyBluetoothDevice device, final int type) {
        if(device != null && device.getFragment() == this) {
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
                            break;

                        case CONNECT_PROCESS:
                            btnConnectSwitch.setImageDrawable(getResources().getDrawable(R.mipmap.ic_connecting_32px));
                            break;

                        default:
                            btnConnectSwitch.setImageDrawable(getResources().getDrawable(R.mipmap.ic_disconnect_32px));
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void connectDevice() {
        if(device == null) return;

        DeviceState state = device.getDeviceState();

        if(state == DeviceState.CONNECT_SUCCESS || state == DeviceState.CONNECT_PROCESS) return;

        device.connect(new IConnectSuccessCallback() {
            @Override
            public void doAfterConnectSuccess(MyBluetoothDevice device) {
                executeGattInitOperation();
            }
        });
    }

    @Override
    public void disconnectDevice() {

        // 断开设备
        if(device != null) device.disconnect();
    }

    @Override
    public void close() {
        // 断开设备
        disconnectDevice();

        // 让观察者删除此Fragment
        if(observer != null) {
            observer.deleteFragment(DeviceFragment.this);
        }
    }





}
