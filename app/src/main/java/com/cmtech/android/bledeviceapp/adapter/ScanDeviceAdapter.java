package com.cmtech.android.bledeviceapp.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.ble.utils.HexUtil;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.Uuid;
import com.cmtech.android.bledevicecore.model.BleDeviceType;

import java.util.List;

/**
 * Created by bme on 2018/2/8.
 */

public class ScanDeviceAdapter extends RecyclerView.Adapter<ScanDeviceAdapter.ViewHolder> {
    private List<BluetoothLeDevice> mDeviceList;
    private List<Boolean> mDeviceStatus;

    private int selectItem = -1;

    Drawable defaultBackground;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View deviceView;
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceTypeName;
        TextView deviceStatus;


        public ViewHolder(View itemView) {
            super(itemView);
            deviceView = itemView;
            deviceName = deviceView.findViewById(R.id.scaned_device_name);
            deviceAddress = deviceView.findViewById(R.id.scaned_device_address);
            deviceTypeName = deviceView.findViewById(R.id.scaned_device_typename);
            deviceStatus = deviceView.findViewById(R.id.scaned_device_status);
        }
    }

    public ScanDeviceAdapter(List<BluetoothLeDevice> deviceList, List<Boolean> deviceStatus) {
        mDeviceList = deviceList;
        mDeviceStatus = deviceStatus;
    }



    @Override
    public ScanDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_scan_device, parent, false);
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
    public void onBindViewHolder(ScanDeviceAdapter.ViewHolder holder, final int position) {
        BluetoothLeDevice device = mDeviceList.get(position);
        AdRecord recordUUID = device.getAdRecordStore().getRecord(AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        String supportedUUID = Uuid.longToShortString(Uuid.byteArrayToUuid(recordUUID.getData()).toString());
        holder.deviceName.setText("设备名："+device.getName());
        holder.deviceTypeName.setText("设备类型："+ BleDeviceType.fromUuid(supportedUUID).getName());
        holder.deviceAddress.setText("蓝牙地址："+device.getAddress());

        Boolean status = mDeviceStatus.get(position);
        if(status)
            holder.deviceStatus.setText("已登记");
        else
            holder.deviceStatus.setText("未登记");

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

    public int getSelectItem() {
        return selectItem;
    }

    public void setSelectItem(int selectItem) {
        if(selectItem >= 0 && selectItem < mDeviceList.size())
            this.selectItem = selectItem;
        else
            this.selectItem = -1;
    }
}
