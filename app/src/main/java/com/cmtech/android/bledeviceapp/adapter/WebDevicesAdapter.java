package com.cmtech.android.bledeviceapp.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.ble.core.WebDeviceRegisterInfo;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.cmtech.android.bledeviceapp.model.DeviceManager;
import com.cmtech.android.bledeviceapp.model.DeviceType;
import com.cmtech.android.bledeviceapp.util.UserUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bme on 2018/2/8.
 */

public class WebDevicesAdapter extends RecyclerView.Adapter<WebDevicesAdapter.ViewHolder> {
    private List<IDevice> deviceList = new ArrayList<>(); // 设备列表
    private MainActivity activity; // MainActivity

    class ViewHolder extends RecyclerView.ViewHolder {
        View deviceView;
        ImageView deviceImage;
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceStatus;
        TextView broadcastName;

        ViewHolder(View itemView) {
            super(itemView);
            deviceView = itemView;
            deviceImage = deviceView.findViewById(R.id.iv_tab_image);
            deviceName = deviceView.findViewById(R.id.tv_device_nickname);
            deviceAddress = deviceView.findViewById(R.id.tv_device_macaddress);
            deviceStatus = deviceView.findViewById(R.id.tv_device_status);
            broadcastName = deviceView.findViewById(R.id.tv_broadcast_name);
        }
    }

    public WebDevicesAdapter(MainActivity activity) {
        for(IDevice device : DeviceManager.getDeviceList()) {
            if(!device.isLocal()) {
                this.deviceList.add(device);
            }
        }
        this.activity = activity;
    }

    @Override
    public WebDevicesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_web_device, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.deviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IDevice device = deviceList.get(holder.getAdapterPosition());
                activity.openDevice(device);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        IDevice device = deviceList.get(position);

        String imagePath = device.getImagePath();
        if(!TextUtils.isEmpty(imagePath)) {
            Drawable drawable = new BitmapDrawable(MyApplication.getContext().getResources(), imagePath);
            holder.deviceImage.setImageDrawable(drawable);
        } else {
            DeviceType type = DeviceType.getFromUuid(device.getUuidString());
            if(type == null) return;
            Glide.with(MyApplication.getContext()).load(type.getDefaultImageId()).into(holder.deviceImage);
        }

        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());
        holder.deviceStatus.setText(device.getState().getDescription());
        holder.broadcastName.setText(((WebDeviceRegisterInfo)device.getRegisterInfo()).getBroadcastName());
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void update() {
        this.deviceList.clear();
        for(IDevice device : DeviceManager.getDeviceList()) {
            if(!device.isLocal()) {
                this.deviceList.add(device);
            }
        }
        notifyDataSetChanged();
    }
}
