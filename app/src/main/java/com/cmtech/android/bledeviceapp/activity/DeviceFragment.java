package com.cmtech.android.bledeviceapp.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;

/**
 * DeviceFragment：设备Fragment
 * Created by bme on 2018/2/27.
 */

public abstract class DeviceFragment extends Fragment{
    private static final String TAG = "DeviceFragment";
    private static final String ARG_ADDRESS = "address";

    private IDevice device; // 设备

    protected DeviceFragment() {
    }

    public static DeviceFragment create(String address, Class<? extends DeviceFragment> fragClass) {
        try {
            DeviceFragment fragment = fragClass.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString(ARG_ADDRESS, address);
            fragment.setArguments(bundle);
            return fragment;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public IDevice getDevice() {
        return device;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof MainActivity)) {
            throw new IllegalArgumentException("The context is not a instance of MainActivity.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get device using address
        Bundle bundle = getArguments();
        if(bundle == null) throw new IllegalStateException();
        device = MyApplication.getDeviceManager().findDevice(bundle.getString(ARG_ADDRESS));
        if(device == null) {
            Toast.makeText(getContext(), "设备不存在，打开失败。", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新状态
        updateState();

        // 注册通用监听器
        MainActivity activity = (MainActivity) getActivity();
        if(activity != null) {
            device.addCommonListener(activity);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // 切换状态
    public void switchState() {
        device.switchState();
    }

    // 更新状态
    public void updateState() {
        // isAdded()用来判断Fragment是否与Activity关联，如果关联了，才能更新状态信息
        if(device != null && isAdded()) {

        }
    }

    // 关闭
    public void close() {
        if(device != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.close_device)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            device.close();

                            if(getActivity() != null) {
                                ((MainActivity) getActivity()).removeFragment(DeviceFragment.this);
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null).show();

        }
    }

    // 打开配置Activity
    public abstract void openConfigureActivity();

}
