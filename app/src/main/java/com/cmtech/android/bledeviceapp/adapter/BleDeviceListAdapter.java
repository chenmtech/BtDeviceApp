package com.cmtech.android.bledeviceapp.adapter;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceType;
import com.vise.log.ViseLog;

import java.util.List;

/**
 * Created by bme on 2018/2/8.
 */

public class BleDeviceListAdapter extends RecyclerView.Adapter<BleDeviceListAdapter.ViewHolder> {

    // 设备列表
    private List<BleDevice> mDeviceList;

    // MainActivity
    MainActivity activity;

    private int selectItem = 0;

    Drawable defaultBackground;

    class ViewHolder extends RecyclerView.ViewHolder {
        View deviceView;
        ImageView deviceImage;
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            deviceView = itemView;
            deviceImage = deviceView.findViewById(R.id.registered_device_image);
            deviceName = deviceView.findViewById(R.id.registered_device_nickname);
            deviceAddress = deviceView.findViewById(R.id.registered_device_address);
            deviceStatus = deviceView.findViewById(R.id.registered_device_status);

        }
    }

    public BleDeviceListAdapter(List<BleDevice> deviceList, MainActivity activity) {

        mDeviceList = deviceList;
        this.activity = activity;
    }


    @Override
    public BleDeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_registered_device, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        defaultBackground = holder.deviceView.getBackground();

        holder.deviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleDevice device = mDeviceList.get(holder.getAdapterPosition());
                activity.openDevice(device);
                selectItem = holder.getAdapterPosition();
                notifyDataSetChanged();
            }
        });

        holder.deviceView.setOnLongClickListener(new View.OnLongClickListener() {
            final MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {         //设置每个菜单的点击动作
                    BleDevice device = mDeviceList.get(holder.getAdapterPosition());
                    switch (item.getItemId()){
                        case 1:
                            activity.modifyDeviceBasicInfo(device);
                            return true;
                        case 2:
                            activity.deleteDeviceFromRegisteredDeviceList(device);
                            return true;

                        default:
                            return true;
                    }
                }
            };

            @Override
            public boolean onLongClick(View view) {
                view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        MenuItem config = menu.add(Menu.NONE, 1, 0, "修改基本信息");
                        MenuItem delete = menu.add(Menu.NONE, 2, 0, "删除");
                        config.setOnMenuItemClickListener(listener);            //响应点击事件
                        delete.setOnMenuItemClickListener(listener);
                    }
                });
                return false;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(BleDeviceListAdapter.ViewHolder holder, final int position) {
        BleDevice device = mDeviceList.get(position);

        String imagePath = device.getImagePath();
        if(imagePath != null && !"".equals(imagePath)) {
            Drawable drawable = new BitmapDrawable(MyApplication.getContext().getResources(), imagePath);
            holder.deviceImage.setImageDrawable(drawable);
        } else {
            Glide.with(MyApplication.getContext()).load(BleDeviceType.fromUuid(device.getUuidString()).getImage()).into(holder.deviceImage);
        }

        holder.deviceName.setText(device.getNickName());
        holder.deviceAddress.setText(device.getMacAddress());
        holder.deviceStatus.setText(device.getStateDescription());

        if(selectItem == position) {
            holder.deviceView.setBackgroundColor(Color.parseColor("#00a0e9"));
        } else {
            holder.deviceView.setBackground(defaultBackground);
        }
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }


}
