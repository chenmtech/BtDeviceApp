package com.cmtech.android.bledeviceapp.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.ble.core.WebDeviceCommonInfo;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.cmtech.android.bledeviceapp.model.DeviceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bme on 2018/2/8.
 */

public class WebDeviceAdapter extends RecyclerView.Adapter<WebDeviceAdapter.ViewHolder> {
    private List<IDevice> deviceList = new ArrayList<>(); // 设备列表
    private MainActivity activity; // MainActivity

    class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView image;
        TextView name;
        TextView address;
        TextView status;
        TextView broadcastName;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            image = view.findViewById(R.id.iv_tab_image);
            name = view.findViewById(R.id.tv_device_nickname);
            address = view.findViewById(R.id.tv_device_macaddress);
            status = view.findViewById(R.id.tv_device_status);
            broadcastName = view.findViewById(R.id.tv_broadcast_name);
        }
    }

    public WebDeviceAdapter(MainActivity activity) {
        this.deviceList = MyApplication.getDeviceManager().getWebDeviceList();
        this.activity = activity;
    }

    @Override
    public WebDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_device_web, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.view.setOnClickListener(new View.OnClickListener() {
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

        String imagePath = device.getIcon();
        if(!TextUtils.isEmpty(imagePath)) {
            Drawable drawable = new BitmapDrawable(MyApplication.getContext().getResources(), imagePath);
            holder.image.setImageDrawable(drawable);
        } else {
            DeviceType type = DeviceType.getFromUuid(device.getUuid());
            if(type == null) return;
            Glide.with(MyApplication.getContext()).load(type.getDefaultIcon()).into(holder.image);
        }

        holder.name.setText(device.getName());
        holder.address.setText(device.getAddress());
        holder.status.setText(device.getConnectState().getDescription());
        holder.broadcastName.setText(((WebDeviceCommonInfo)device.getCommonInfo()).getBroadcastName());
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void update() {
        this.deviceList = MyApplication.getDeviceManager().getWebDeviceList();
        notifyDataSetChanged();
    }
}
