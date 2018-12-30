package com.cmtech.android.bledeviceapp.activity;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.cmtech.android.ble.callback.scan.DevNameFilterScanCallback;
import com.cmtech.android.ble.callback.scan.IScanCallback;
import com.cmtech.android.ble.callback.scan.ScanCallback;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.BluetoothLeDeviceStore;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.ScanDeviceAdapter;
import com.cmtech.android.bledevice.core.BleDeviceBasicInfo;
import com.cmtech.android.bledevice.core.BleDeviceUtil;
import com.cmtech.android.bledevice.core.UuidUtil;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.ble.model.adrecord.AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE;
import static com.cmtech.android.bledeviceapp.activity.DeviceBasicInfoActivity.DEVICE_BASICINFO;
import static com.cmtech.android.bledevice.core.BleDeviceConstant.SCAN_DEVICE_NAME;

/**
 *  ScanDeviceActivity: 扫描设备界面
 *  Created by bme on 2018/2/28.
 */

public class ScanDeviceActivity extends AppCompatActivity {
    private static final String TAG = "ScanDeviceActivity";
    public static final String REGISTERD_DEVICE_MACLIST = "registered_device_mac_list";

    // 扫描设备回调类
    private class ScanDeviceCallback implements IScanCallback {
        ScanDeviceCallback() {

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

    // 设备绑定动作广播接收器类
    private class BleDeviceBondReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                //当设备的连接状态改变
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    // 设备已绑定
                    case BluetoothDevice.BOND_BONDED:
                        // 连接设备
                        for (BluetoothLeDevice leDevice : deviceList) {
                            if(leDevice.getAddress().equalsIgnoreCase(device.getAddress())) {
                                registerBondedDevice(leDevice);
                                break;
                            }
                        }
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        break;
                    case BluetoothDevice.BOND_NONE:
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private final ScanCallback scanCallback = new DevNameFilterScanCallback(new ScanDeviceCallback()).setDeviceName(SCAN_DEVICE_NAME); // 扫描回调
    private SwipeRefreshLayout srlScanDevice; // 用于显示扫描到的设备
    private ScanDeviceAdapter scanDeviceAdapter;
    private RecyclerView rvScanDevice;
    private List<BluetoothLeDevice> deviceList = new ArrayList<>();    // 扫描到的设备列表
    private List<String> registeredDeviceMacList = new ArrayList<>(); // 已登记的设备Mac地址列表
    private BleDeviceBondReceiver bondReceiver; // 绑定接收器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);

        // 获取已登记过的设备Mac列表
        Intent intent = getIntent();
        if(intent != null) {
            registeredDeviceMacList = (List<String>) intent.getSerializableExtra(REGISTERD_DEVICE_MACLIST);
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

        IntentFilter bondIntent = new IntentFilter();
        bondIntent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bondReceiver = new BleDeviceBondReceiver();
        registerReceiver(bondReceiver, bondIntent);

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

        unregisterReceiver(bondReceiver);

        if(srlScanDevice.isRefreshing())
            srlScanDevice.setRefreshing(false);

        BleDeviceUtil.stopScan(scanCallback);
    }

    // 开始扫描
    private void startScan() {
        if(!scanCallback.isScanning()) {
            Toast.makeText(ScanDeviceActivity.this, "开始搜索。", Toast.LENGTH_SHORT).show();
            deviceList.clear();
            scanDeviceAdapter.notifyDataSetChanged();
            BleDeviceUtil.startScan(scanCallback);
        }
    }

    // 添加一个设备到发现的设备列表中
    private void addDevice(final BluetoothLeDevice device) {
        if(device == null) return;

        boolean newDevice = true;
        for(BluetoothLeDevice dv : deviceList) {
            if(dv.getAddress().equalsIgnoreCase(device.getAddress())) {
                newDevice = false;
                break;
            }
        }
        if(newDevice) {
            deviceList.add(device);
            scanDeviceAdapter.notifyItemInserted(deviceList.size()-1);
            scanDeviceAdapter.notifyDataSetChanged();
            rvScanDevice.scrollToPosition(deviceList.size()-1);
        }
    }

    // 登记一个设备
    public void registerDevice(final BluetoothLeDevice device) {
        // 先停止扫描
        if(scanCallback.isScanning()) {
            BleDeviceUtil.stopScan(scanCallback);
            srlScanDevice.setRefreshing(false);
        }

        if(device.getDevice().getBondState() != BluetoothDevice.BOND_BONDED) {
            Toast.makeText(ScanDeviceActivity.this, "请输入设备密码绑定设备。", Toast.LENGTH_SHORT).show();
            device.getDevice().createBond();
        } else {
            registerBondedDevice(device);
        }
    }

    // 登记已经绑定的设备
    private void registerBondedDevice(final BluetoothLeDevice device) {
        if(device.getDevice().getBondState() != BluetoothDevice.BOND_BONDED) {
            throw new IllegalStateException("设备未绑定");
        }

        String macAddress = device.getAddress();

        // 获取设备广播数据中的UUID的短串
        AdRecord record = device.getAdRecordStore().getRecord(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        if(record == null) return;

        String uuidShortString = UuidUtil.longToShortString(UuidUtil.byteArrayToUuid(record.getData()).toString());

        Intent intent = new Intent(ScanDeviceActivity.this, DeviceBasicInfoActivity.class);
        BleDeviceBasicInfo basicInfo = new BleDeviceBasicInfo();
        basicInfo.setMacAddress(macAddress);
        basicInfo.setUuidString(uuidShortString);
        intent.putExtra(DEVICE_BASICINFO, basicInfo);
        startActivityForResult(intent, 1);
    }

}
