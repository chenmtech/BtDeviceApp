package com.cmtech.android.bledeviceapp.controller.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.bledevice.SupportedDeviceType;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.controller.activity.ScanDeviceActivity;
import com.cmtech.android.bledevicecore.model.Uuid;

import java.util.List;

/**
 * Created by bme on 2018/2/8.
 */

public class ScanDeviceAdapter extends RecyclerView.Adapter<ScanDeviceAdapter.ViewHolder> {
    private List<BluetoothLeDevice> deviceList;
    private final List<String> registeredDeviceMac;
    private final ScanDeviceActivity activity;


    static class ViewHolder extends RecyclerView.ViewHolder {
        View deviceView;
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceTypeName;

        TextView deviceStatus;
        ImageButton ibRegister;


        public ViewHolder(View itemView) {
            super(itemView);
            deviceView = itemView;
            deviceName = deviceView.findViewById(R.id.tv_scandevice_name);
            deviceAddress = deviceView.findViewById(R.id.tv_scandevice_macaddress);
            deviceTypeName = deviceView.findViewById(R.id.tv_scandevice_type);

            deviceStatus = deviceView.findViewById(R.id.tv_scandevice_status);
            ibRegister = deviceView.findViewById(R.id.ib_scandevice_register);
        }
    }

    public ScanDeviceAdapter(List<BluetoothLeDevice> deviceList, final List<String> registeredDeviceMac, ScanDeviceActivity activity) {
        this.deviceList = deviceList;
        this.registeredDeviceMac = registeredDeviceMac;
        this.activity = activity;
    }



    @Override
    public ScanDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_scandevice, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.ibRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activity != null) {
                    activity.registerDevice(deviceList.get(holder.getAdapterPosition()));
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ScanDeviceAdapter.ViewHolder holder, final int position) {
        BluetoothLeDevice device = deviceList.get(position);
        AdRecord recordUUID = device.getAdRecordStore().getRecord(AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        String supportedUUID = Uuid.longToShortString(Uuid.byteArrayToUuid(recordUUID.getData()).toString());
        holder.deviceName.setText("设备名："+device.getName());
        holder.deviceTypeName.setText("设备类型："+ SupportedDeviceType.getDeviceTypeFromUuid(supportedUUID).getDefaultNickname());
        holder.deviceAddress.setText("蓝牙地址："+device.getAddress());

        boolean status = hasRegistered(device);
        if(status) {
            holder.deviceStatus.setVisibility(View.VISIBLE);
            holder.ibRegister.setVisibility(View.GONE);
        } else {
            holder.deviceStatus.setVisibility(View.GONE);
            holder.ibRegister.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    // 设备是否已经登记过
    private boolean hasRegistered(BluetoothLeDevice device) {
        for(String ele : registeredDeviceMac) {
            if(ele.equalsIgnoreCase(device.getAddress())) {
                return true;
            }
        }
        return false;
    }
}
