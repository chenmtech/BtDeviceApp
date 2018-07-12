package com.cmtech.android.btdeviceapp.adapter;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceType;

import java.util.List;

/**
 * Created by bme on 2018/2/8.
 */

public class BLEDeviceListAdapter extends RecyclerView.Adapter<BLEDeviceListAdapter.ViewHolder> {

    // 设备列表
    private List<IBleDeviceInterface> mDeviceList;

    // MainActivity
    MainActivity activity;

    class ViewHolder extends RecyclerView.ViewHolder {
        View deviceView;
        ImageView deviceImage;
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceStatus;
        ImageButton ibtnDelete;
        ImageButton ibtnOpen;

        MenuItem.OnMenuItemClickListener listener=new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {         //设置每个菜单的点击动作
                switch (item.getItemId()){
                    case 1:
                        //do something
                        return true;
                    case 2:
                        //do something
                    default:
                        return true;
                }
            }
        };

        public ViewHolder(View itemView) {
            super(itemView);
            deviceView = itemView;
            deviceImage = deviceView.findViewById(R.id.configured_device_image);
            deviceName = deviceView.findViewById(R.id.configured_device_nickname);
            deviceAddress = deviceView.findViewById(R.id.configured_device_address);
            deviceStatus = deviceView.findViewById(R.id.configured_device_status);
            ibtnDelete = deviceView.findViewById(R.id.configured_device_delete_btn);
            ibtnOpen = deviceView.findViewById(R.id.configured_device_open_btn);

            itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    MenuInflater inflater = activity.getMenuInflater();
                    MenuItem delete = menu.add(Menu.NONE, 1, 1, "删除");
                    MenuItem delete_much = menu.add(Menu.NONE, 2, 2, "批量删除");
                    delete.setOnMenuItemClickListener(listener);            //响应点击事件
                    delete_much.setOnMenuItemClickListener(listener);
                }
            });

        }
    }

    public BLEDeviceListAdapter(List<IBleDeviceInterface> deviceList, MainActivity activity) {

        mDeviceList = deviceList;
        this.activity = activity;
    }


    @Override
    public BLEDeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_configured_device, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.ibtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IBleDeviceInterface device = mDeviceList.get(holder.getAdapterPosition());
                activity.deleteIncludedDevice(device);
            }
        });

        /*holder.ibtnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IBleDeviceInterface device = mDeviceList.get(holder.getAdapterPosition());
                activity.openDevice(device);
            }
        });*/

        holder.deviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IBleDeviceInterface device = mDeviceList.get(holder.getAdapterPosition());
                activity.openDevice(device);
            }
        });

        /*holder.deviceView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });*/

        return holder;
    }

    @Override
    public void onBindViewHolder(BLEDeviceListAdapter.ViewHolder holder, final int position) {
        BleDevice device = (BleDevice)mDeviceList.get(position);

        String imagePath = device.getImagePath();
        if(imagePath != null && !"".equals(imagePath)) {
            Drawable drawable = new BitmapDrawable(MyApplication.getContext().getResources(), imagePath);
            holder.deviceImage.setImageDrawable(drawable);
        } else {
            Glide.with(MyApplication.getContext()).load(BleDeviceType.fromUuid(device.getUuidString()).getImage()).into(holder.deviceImage);
        }

        holder.deviceName.setText(device.getNickName());
        holder.deviceAddress.setText(device.getMacAddress());
        holder.deviceStatus.setText(device.getDeviceConnectState().getDescription());

    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }


}
