package com.cmtech.android.bledeviceapp.model;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.ble.core.BleDeviceType;
import com.cmtech.android.ble.core.BleDeviceDetailInfo;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.ScanActivity;

import java.util.List;


/**
 * ScannedDeviceAdapter : 扫描到的设备Adapter
 * Created by bme on 2018/2/8.
 */

public class ScannedDeviceAdapter extends RecyclerView.Adapter<ScannedDeviceAdapter.ViewHolder> {
    private final List<BleDeviceDetailInfo> deviceList; // 扫描到的设备列表
    private final List<String> registedMacList; // 已登记设备Mac List
    private final ScanActivity activity; // 扫描设备的Activiy

    static class ViewHolder extends RecyclerView.ViewHolder {
        View deviceView; // 设备视图
        TextView deviceName; // 设备名
        TextView deviceAddress; // 设备mac地址
        TextView deviceTypeName; // 设备类型名
        TextView deviceStatus; // 设备状态：是否已经登记

        ViewHolder(View itemView) {
            super(itemView);
            deviceView = itemView;
            deviceName = deviceView.findViewById(R.id.tv_scandevice_name);
            deviceAddress = deviceView.findViewById(R.id.tv_scandevice_macaddress);
            deviceTypeName = deviceView.findViewById(R.id.tv_scandevice_type);
            deviceStatus = deviceView.findViewById(R.id.tv_device_name);
        }
    }

    public ScannedDeviceAdapter(List<BleDeviceDetailInfo> deviceList, List<String> registedMacList, ScanActivity activity) {
        this.deviceList = deviceList;
        this.registedMacList = registedMacList;
        this.activity = activity;
    }


    @Override
    public ScannedDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_scan_device, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.deviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activity != null) {
                    BleDeviceDetailInfo device = deviceList.get(holder.getAdapterPosition());
                    if(!registered(device)) {
                        activity.registerDevice(device);
                    }
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ScannedDeviceAdapter.ViewHolder holder, final int position) {
        BleDeviceDetailInfo device = deviceList.get(position);

        AdRecord recordUUID = device.getAdRecordStore().getRecord(AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        String supportedUUID = UuidUtil.longToShortString(UuidUtil.byteArrayToUuid(recordUUID.getData()).toString());
        holder.deviceTypeName.setText(String.format("设备类型：%s", BleDeviceType.getFromUuid(supportedUUID).getDefaultNickname()));

        holder.deviceAddress.setText(String.format("设备地址：%s", device.getAddress()));

        holder.deviceName.setText(String.format("设备名：%s", device.getName()));

        boolean status = registered(device);
        TextPaint paint = holder.deviceStatus.getPaint();
        if(status) {
            holder.deviceStatus.setText("已登记");
            paint.setFlags(paint.getFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.deviceStatus.setText("未登记");
            paint.setFlags(paint.getFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    // 设备是否已经注册过
    private boolean registered(BleDeviceDetailInfo device) {
        for(String ele : registedMacList) {
            if(ele.equalsIgnoreCase(device.getAddress())) {
                return true;
            }
        }
        return false;
    }
}
