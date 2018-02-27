package com.cmtech.android.btdeviceapp.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.ble.common.ConnectState;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import java.util.List;

/**
 * Created by bme on 2018/2/8.
 */

public class ConfiguredDeviceAdapter extends RecyclerView.Adapter<ConfiguredDeviceAdapter.ViewHolder>
                                    implements ConfiguredDevice.IConnectStateObersver{
    private Activity activity;

    private List<ConfiguredDevice> mDeviceList;

    private int selectItem = -1;

    Drawable defaultBackground;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View deviceView;
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceStatus;


        public ViewHolder(View itemView) {
            super(itemView);
            deviceView = itemView;
            deviceName = deviceView.findViewById(R.id.configured_device_nickname);
            deviceAddress = deviceView.findViewById(R.id.configured_device_address);
            deviceStatus = deviceView.findViewById(R.id.configured_device_status);

        }
    }

    public ConfiguredDeviceAdapter(Activity activity, List<ConfiguredDevice> deviceList) {
        this.activity = activity;

        mDeviceList = deviceList;
        for(ConfiguredDevice device : mDeviceList) {
            device.registerConnectStateObserver(this);
        }
    }


    @Override
    public ConfiguredDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.configured_device_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        defaultBackground = holder.deviceView.getBackground();

        holder.deviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectItem = holder.getAdapterPosition();
                notifyDataSetChanged();
            }
        });


        return holder;
    }

    @Override
    public void onBindViewHolder(ConfiguredDeviceAdapter.ViewHolder holder, final int position) {
        ConfiguredDevice device = (ConfiguredDevice)mDeviceList.get(position);
        holder.deviceName.setText(device.getNickName());
        holder.deviceAddress.setText(device.getMacAddress());
        String connectState = "等待连接";
        switch (device.getConnectState()) {
            case CONNECT_INIT:
                connectState = "等待连接";
                break;
            case CONNECT_PROCESS:
                connectState = "连接中...";
                break;
            case CONNECT_DISCONNECT:
                connectState = "连接断开";
                break;
            case CONNECT_FAILURE:
                connectState = "连接错误";
                break;
            case CONNECT_SUCCESS:
                connectState = "已连接";
                break;
            default:
                break;
        }
        holder.deviceStatus.setText(connectState);


        if(selectItem == position) {
            holder.deviceView.setBackgroundColor(Color.BLUE);
        } else {
            holder.deviceView.setBackground(defaultBackground);
        }

    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }


    public int getSelectItem() {return selectItem;}


    public void setSelectItem(int selectItem) {
        if(selectItem >= 0 && selectItem < mDeviceList.size())
            this.selectItem = selectItem;
        else
            this.selectItem = -1;
    }

    @Override
    public void updateConnectState(ConfiguredDevice device, ConnectState state) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }
}
