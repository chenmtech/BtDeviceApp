package com.cmtech.android.bledeviceapp.adapter;

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.ble.core.BleDeviceDetailInfo;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.ble.utils.HexUtil;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.ScanActivity;
import com.cmtech.android.bledeviceapp.model.DeviceType;
import com.cmtech.android.bledeviceapp.util.ClickCheckUtil;

import java.util.List;


/**
 * ScannedDeviceAdapter : 扫描到的设备Adapter
 * Created by bme on 2018/2/8.
 */

public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ViewHolder> {
    private final List<BleDeviceDetailInfo> foundDetailInfos; // 扫描到的设备详细信息列表
    private final List<String> registeredAddresses; // 已注册设备Mac地址List
    private final ScanActivity activity; // 扫描设备的Activiy

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view; // 设备视图
        TextView name; // 设备名
        TextView address; // 设备mac地址
        TextView type; // 设备类型名
        TextView status; // 设备状态：是否已经登记

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            name = view.findViewById(R.id.tv_name_or_id);
            address = view.findViewById(R.id.tv_address);
            type = view.findViewById(R.id.tv_type);
            status = view.findViewById(R.id.tv_status);
        }
    }

    public ScanAdapter(List<BleDeviceDetailInfo> foundDetailInfos, List<String> registeredAddresses, ScanActivity activity) {
        this.foundDetailInfos = foundDetailInfos;
        this.registeredAddresses = registeredAddresses;
        this.activity = activity;
    }


    @Override
    public ScanAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_device_scan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanAdapter.ViewHolder holder, int position) {
        BleDeviceDetailInfo detailInfo = foundDetailInfos.get(position);

        AdRecord serviceUUID = detailInfo.getAdRecordStore().getRecord(AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        byte[] uuidBytes = null;
        if(serviceUUID != null) {
            uuidBytes = new byte[]{serviceUUID.getData()[13], serviceUUID.getData()[12]};
        } else {
            serviceUUID = detailInfo.getAdRecordStore().getRecord(AdRecord.BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_MORE_AVAILABLE);
            if(serviceUUID != null) {
                uuidBytes = new byte[]{serviceUUID.getData()[1], serviceUUID.getData()[0]};
            }
        }

        DeviceType type = null;
        if(uuidBytes != null) {
            type = DeviceType.getFromUuid(HexUtil.encodeHexStr(uuidBytes));
        }

        if(type == null) {
            holder.type.setText("不支持");
        } else {
            holder.type.setText(type.getDefaultName());
        }

        holder.address.setText(detailInfo.getAddress());
        holder.name.setText(detailInfo.getName());

        boolean status = isRegistered(detailInfo);
        if(status) {
            holder.status.setText(R.string.added);
        } else {
            if(type == null) {
                holder.status.setText(" ");
            } else {
                holder.status.setText("可添加");
            }
        }

        int color;
        if(!status && type != null) {
            color = Color.RED;
        } else {
            color = Color.BLACK;
        }
        holder.status.setTextColor(color);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ClickCheckUtil.isFastClick()) return;

                if(activity != null) {
                    if(!isRegistered(detailInfo)) {
                        activity.registerDevice(detailInfo);
                    } else {
                        Toast.makeText(activity, "该设备已添加，可以直接使用。", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return foundDetailInfos.size();
    }

    // 设备是否已经注册过
    private boolean isRegistered(BleDeviceDetailInfo device) {
        for(String ele : registeredAddresses) {
            if(ele.equalsIgnoreCase(device.getAddress())) {
                return true;
            }
        }
        return false;
    }
}
