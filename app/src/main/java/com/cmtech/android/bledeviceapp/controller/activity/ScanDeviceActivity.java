package com.cmtech.android.bledeviceapp.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.cmtech.android.ble.callback.scan.DevNameFilterScanCallback;
import com.cmtech.android.ble.callback.scan.FilterScanCallback;
import com.cmtech.android.ble.callback.scan.IScanCallback;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.BluetoothLeDeviceStore;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.controller.adapter.ScanDeviceAdapter;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.model.BleDeviceConfig;
import com.cmtech.android.bledevicecore.model.BleDeviceUtil;
import com.cmtech.android.bledevicecore.model.Uuid;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.ble.model.adrecord.AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE;
import static com.cmtech.android.bledeviceapp.controller.activity.DeviceBasicInfoActivity.DEVICE_BASICINFO;

public class ScanDeviceActivity extends AppCompatActivity {
    private static final String TAG = "ScanDeviceActivity";

    private class ScanDeviceCallback implements IScanCallback {
        public ScanDeviceCallback() {

        }

        @Override
        public void onDeviceFound(final BluetoothLeDevice bluetoothLeDevice) {
            if(bluetoothLeDevice != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addDevice(bluetoothLeDevice);
                    }
                });

            }
        }

        @Override
        public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    srlScanDevice.setRefreshing(false);
                    Toast.makeText(ScanDeviceActivity.this, "搜索结束。", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public void onScanTimeout() {
            srlScanDevice.setRefreshing(false);
            Toast.makeText(ScanDeviceActivity.this, "请靠近设备下拉再次搜索。", Toast.LENGTH_SHORT).show();
        }
    }

    private final FilterScanCallback scanCallback = new DevNameFilterScanCallback(new ScanDeviceCallback()).setDeviceName(BleDeviceConfig.getInstance().getScanDeviceName());

    // 用于实现扫描设备的显示
    private SwipeRefreshLayout srlScanDevice;
    private ScanDeviceAdapter scanDeviceAdapter;
    private RecyclerView rvScanDevice;


    private List<BluetoothLeDevice> deviceList = new ArrayList<>();    // 设备列表

    // 已经登记的设备Mac地址列表
    private List<String> registeredDeviceMacList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);

        // 获取已登记过的设备Mac列表
        Intent intent = getIntent();
        if(intent != null) {
            registeredDeviceMacList = (List<String>) intent.getSerializableExtra("registered_device_list");
        }

        rvScanDevice = findViewById(R.id.rv_scandevice);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvScanDevice.setLayoutManager(layoutManager);
        rvScanDevice.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        scanDeviceAdapter = new ScanDeviceAdapter(deviceList, registeredDeviceMacList, this);
        rvScanDevice.setAdapter(scanDeviceAdapter);

        srlScanDevice = findViewById(R.id.srl_scandevice);
        srlScanDevice.setProgressViewOffset(true, 200, 300);
        srlScanDevice.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startScan();
            }
        });

        startScan();
        srlScanDevice.setRefreshing(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // 登记设备信息返回结果
            case 1:
                if ( resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(srlScanDevice.isRefreshing())
            srlScanDevice.setRefreshing(false);

        if(scanCallback != null) {
            BleDeviceUtil.stopScan(scanCallback);
        }
    }

    private void startScan() {
        if(!scanCallback.isScanning()) {
            Toast.makeText(ScanDeviceActivity.this, "开始搜索。", Toast.LENGTH_SHORT).show();
            deviceList.clear();
            scanDeviceAdapter.notifyDataSetChanged();
            BleDeviceUtil.startScan(scanCallback);
        }
    }

    // 添加一个新设备到发现的设备列表中
    private void addDevice(final BluetoothLeDevice device) {
        if(device == null) return;

        boolean canAdd = true;
        for(BluetoothLeDevice dv : deviceList) {
            if(dv.getAddress().equalsIgnoreCase(device.getAddress())) {
                canAdd = false;
                break;
            }
        }
        if(canAdd) {
            deviceList.add(device);
            scanDeviceAdapter.notifyItemInserted(deviceList.size()-1);
            scanDeviceAdapter.notifyDataSetChanged();
            rvScanDevice.scrollToPosition(deviceList.size()-1);
        }
    }


    // 登记一个设备
    public void registerDevice(final BluetoothLeDevice device) {
        String macAddress = device.getAddress();

        // 获取设备广播数据中的UUID的短串
        AdRecord record = device.getAdRecordStore().getRecord(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        if(record == null) return;

        String uuidShortString = Uuid.longToShortString(Uuid.byteArrayToUuid(record.getData()).toString());

        Intent intent = new Intent(ScanDeviceActivity.this, DeviceBasicInfoActivity.class);
        BleDeviceBasicInfo basicInfo = new BleDeviceBasicInfo(macAddress, "", uuidShortString, "", true, 3);
        intent.putExtra(DEVICE_BASICINFO, basicInfo);

        startActivityForResult(intent, 1);
    }

}
