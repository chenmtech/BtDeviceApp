package com.cmtech.android.bledeviceapp.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.DeviceType;

import java.util.List;

/**
 * Created by bme on 2018/2/8.
 */

public class LocalDeviceAdapter extends RecyclerView.Adapter<LocalDeviceAdapter.ViewHolder> {
    private final List<IDevice> deviceList = MyApplication.getDeviceManager().getBleDeviceList(); // 设备列表
    private final MainActivity activity; // MainActivity

    class ViewHolder extends RecyclerView.ViewHolder {
        View deviceView;
        ImageView deviceImage;
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceStatus;

        ViewHolder(View itemView) {
            super(itemView);
            deviceView = itemView;
            deviceImage = deviceView.findViewById(R.id.iv_tab_image);
            deviceName = deviceView.findViewById(R.id.tv_device_nickname);
            deviceAddress = deviceView.findViewById(R.id.tv_device_macaddress);
            deviceStatus = deviceView.findViewById(R.id.tv_device_status);
        }
    }

    public LocalDeviceAdapter(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public LocalDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_device_local, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        IDevice device = deviceList.get(position);

        String imagePath = device.getIcon();
        //ViseLog.e("device icon:" + imagePath);
        if(!TextUtils.isEmpty(imagePath)) {
            Drawable drawable = new BitmapDrawable(MyApplication.getContext().getResources(), imagePath);
            holder.deviceImage.setImageDrawable(drawable);
        } else {
            DeviceType type = DeviceType.getFromUuid(device.getUuid());
            if(type == null) return;
            Glide.with(MyApplication.getContext()).load(type.getDefaultIcon()).into(holder.deviceImage);
        }

        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());
        holder.deviceStatus.setText(device.getConnectState().getDescription());

        holder.deviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.openDevice(device);
            }
        });

        holder.deviceView.setOnLongClickListener(new View.OnLongClickListener() {
            final MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {         //设置每个菜单的点击动作
                    switch (item.getItemId()){
                        case 1:
                            activity.modifyDeviceInfo(device.getCommonInfo());
                            break;
                        case 2:
                            activity.removeRegisteredDevice(device);
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            };

            @Override
            public boolean onLongClick(View view) {
                view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        MenuItem config = menu.add(Menu.NONE, 1, 0, R.string.modify);
                        MenuItem delete = menu.add(Menu.NONE, 2, 0, R.string.delete);
                        config.setOnMenuItemClickListener(listener);            //响应点击事件
                        delete.setOnMenuItemClickListener(listener);            //响应点击事件
                    }
                });
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void update() {
        notifyDataSetChanged();
    }
}
