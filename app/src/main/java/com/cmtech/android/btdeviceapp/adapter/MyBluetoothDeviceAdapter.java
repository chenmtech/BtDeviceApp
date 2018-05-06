package com.cmtech.android.btdeviceapp.adapter;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IMyBluetoothDeviceObserver;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDevice;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDeviceType;

import java.util.List;

/**
 * Created by bme on 2018/2/8.
 */

public class MyBluetoothDeviceAdapter extends RecyclerView.Adapter<MyBluetoothDeviceAdapter.ViewHolder>
                                    implements IMyBluetoothDeviceObserver {

    // 设备列表
    private List<MyBluetoothDevice> mDeviceList;

    // MainActivity
    MainActivity activity;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View deviceView;
        ImageView deviceImage;
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceStatus;
        ImageButton ibtnDelete;
        ImageButton ibtnOpen;


        public ViewHolder(View itemView) {
            super(itemView);
            deviceView = itemView;
            deviceImage = deviceView.findViewById(R.id.configured_device_image);
            deviceName = deviceView.findViewById(R.id.configured_device_nickname);
            deviceAddress = deviceView.findViewById(R.id.configured_device_address);
            deviceStatus = deviceView.findViewById(R.id.configured_device_status);
            ibtnDelete = deviceView.findViewById(R.id.configured_device_delete_btn);
            ibtnOpen = deviceView.findViewById(R.id.configured_device_open_btn);
        }
    }

    public MyBluetoothDeviceAdapter(List<MyBluetoothDevice> deviceList, MainActivity activity) {

        mDeviceList = deviceList;
        this.activity = activity;

        for(MyBluetoothDevice device : mDeviceList) {
            device.registerDeviceObserver(this);
        }
    }


    @Override
    public MyBluetoothDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_configured_device, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.ibtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyBluetoothDevice device = mDeviceList.get(holder.getAdapterPosition());
                activity.deleteDevice(device);
            }
        });

        holder.ibtnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyBluetoothDevice device = mDeviceList.get(holder.getAdapterPosition());
                activity.openDevice(device);
            }
        });


        return holder;
    }

    @Override
    public void onBindViewHolder(MyBluetoothDeviceAdapter.ViewHolder holder, final int position) {
        MyBluetoothDevice device = (MyBluetoothDevice)mDeviceList.get(position);

        String imagePath = device.getImagePath();
        if(imagePath != null && !"".equals(imagePath)) {
            Drawable drawable = new BitmapDrawable(MyApplication.getContext().getResources(), imagePath);
            holder.deviceImage.setImageDrawable(drawable);
        } else {
            Glide.with(MyApplication.getContext()).load(MyBluetoothDeviceType.fromUuid(device.getUuidString()).getImage()).into(holder.deviceImage);
        }

        holder.deviceName.setText(device.getNickName());
        holder.deviceAddress.setText(device.getMacAddress());
        holder.deviceStatus.setText(device.getDeviceState().getDescription());

        if(position % 2 == 0) {
            holder.deviceView.setBackgroundColor(Color.parseColor("#ffffff"));
        } else {
            holder.deviceView.setBackgroundColor(Color.parseColor("#808080"));
        }

    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    // 作为IMyBluetoothDeviceObserver设备观察者要实现的函数
    @Override
    public void updateDeviceInfo(MyBluetoothDevice device, final int type) {
        new Handler(Looper.getMainLooper()).post((new Runnable() {
            @Override
            public void run() {
            notifyDataSetChanged();
            }
        }));

    }
}
