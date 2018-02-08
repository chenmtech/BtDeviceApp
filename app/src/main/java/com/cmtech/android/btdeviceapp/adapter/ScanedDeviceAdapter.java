package com.cmtech.android.btdeviceapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.btdeviceapp.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by bme on 2018/2/8.
 */

public class ScanedDeviceAdapter extends RecyclerView.Adapter<ScanedDeviceAdapter.ViewHolder> {
    private List<BluetoothLeDevice> mDeviceList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View deviceView;
        TextView deviceName;
        TextView deviceSupportedUUID;


        public ViewHolder(View itemView) {
            super(itemView);
            deviceView = itemView;
            deviceName = deviceView.findViewById(R.id.scaned_device_name);
            deviceSupportedUUID = deviceView.findViewById(R.id.scaned_device_supporteduuid);
        }
    }

    public ScanedDeviceAdapter(List<BluetoothLeDevice> deviceList) {
        mDeviceList = deviceList;
    }



    @Override
    public ScanedDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scaned_device_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ScanedDeviceAdapter.ViewHolder holder, int position) {
        BluetoothLeDevice device = (BluetoothLeDevice)mDeviceList.get(position);
        AdRecord recordUUID = device.getAdRecordStore().getRecord(AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        String supportedUUID = Arrays.toString(recordUUID.getData());
        holder.deviceName.setText(device.getName());
        holder.deviceSupportedUUID.setText(supportedUUID);
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }
}
