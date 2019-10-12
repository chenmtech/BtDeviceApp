package com.cmtech.android.bledeviceapp.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleScanCallback;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.ble.core.BleScanner;
import com.cmtech.android.ble.core.BleDeviceDetailInfo;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.BleDeviceConstant;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.ScannedDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.ble.model.adrecord.AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE;
import static com.cmtech.android.bledeviceapp.activity.RegisterActivity.DEVICE_REGISTER_INFO;

/**
  *
  * ClassName:      ScanActivity
  * Description:    扫描Activiy
  * Author:         chenm
  * CreateDate:     2018/2/28 18:07
  * UpdateUser:     chenm
  * UpdateDate:     2019-04-23 18:07
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class ScanActivity extends AppCompatActivity {
    private static final String TAG = "ScanActivity";
    private static final ScanFilter SCAN_FILTER_DEVICE_NAME = new ScanFilter.Builder().setDeviceName(BleDeviceConstant.SCAN_DEVICE_NAME).build();
    public static final String REGISTERED_DEVICE_MAC_LIST = "registered_device_mac_list";

    private final List<BleDeviceDetailInfo> scannedDeviceDetailInfoList = new ArrayList<>(); // 扫描到的设备的BleDeviceDetailInfo列表
    private List<String> registeredDeviceMacList = new ArrayList<>(); // 已注册的设备mac地址列表
    private SwipeRefreshLayout srlScanDevice;
    private ScannedDeviceAdapter scannedDeviceAdapter;
    private RecyclerView rvScanDevice;

    // 设备绑定状态广播接收器
    private final BroadcastReceiver bondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    for (BleDeviceDetailInfo detailInfo : scannedDeviceDetailInfoList) {
                        if(detailInfo.getAddress().equalsIgnoreCase(device.getAddress())) {
                            registerBondedDevice(detailInfo);
                            break;
                        }
                    }
                }
            }
        }
    };

    // 扫描回调
    private final IBleScanCallback bleScanCallback = new IBleScanCallback() {
        @Override
        public void onDeviceFound(BleDeviceDetailInfo bleDeviceDetailInfo) {
            addDeviceDetailInfoToList(bleDeviceDetailInfo);
        }

        @Override
        public void onScanFailed(int errorCode) {
            switch (errorCode) {
                case SCAN_FAILED_ALREADY_STARTED:
                    Toast.makeText(ScanActivity.this, "正在扫描中。", Toast.LENGTH_LONG).show();
                    break;

                case SCAN_FAILED_BLE_CLOSED:
                    Toast.makeText(ScanActivity.this, "蓝牙已关闭。", Toast.LENGTH_LONG).show();
                    srlScanDevice.setRefreshing(false);
                    break;

                case SCAN_FAILED_BLE_ERROR:
                    srlScanDevice.setRefreshing(false);
                    BleScanner.stopScan(this);
                    Toast.makeText(ScanActivity.this, "蓝牙错误，必须重启蓝牙。", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_device_scan);
        setSupportActionBar(toolbar);

        // 获取已注册设备Mac列表
        Intent intent = getIntent();
        if(intent != null) {
            registeredDeviceMacList = (List<String>) intent.getSerializableExtra(REGISTERED_DEVICE_MAC_LIST);
        }

        // 初始化扫描设备列表
        rvScanDevice = findViewById(R.id.rv_scandevice);
        rvScanDevice.setLayoutManager(new LinearLayoutManager(this));
        rvScanDevice.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        scannedDeviceAdapter = new ScannedDeviceAdapter(scannedDeviceDetailInfoList, registeredDeviceMacList, this);
        rvScanDevice.setAdapter(scannedDeviceAdapter);

        srlScanDevice = findViewById(R.id.srl_scandevice);
        srlScanDevice.setProgressViewOffset(true, 300, 400);
        srlScanDevice.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startScan();
            }
        });

        IntentFilter bondIntent = new IntentFilter();
        bondIntent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bondStateReceiver, bondIntent);

        startScan();
    }

    // 开始扫描
    private void startScan() {
        scannedDeviceDetailInfoList.clear();
        scannedDeviceAdapter.notifyDataSetChanged();
        BleScanner.stopScan(bleScanCallback);
        BleScanner.startScan(SCAN_FILTER_DEVICE_NAME, bleScanCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

            case R.id.scan_device:
                startScan();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: // 设备登记返回码
                if (resultCode == RESULT_OK) {
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

        unregisterReceiver(bondStateReceiver);

        if(srlScanDevice.isRefreshing())
            srlScanDevice.setRefreshing(false);

        BleScanner.stopScan(bleScanCallback);
    }

    public void registerDevice(final BleDeviceDetailInfo device) {
        // 先停止扫描
        BleScanner.stopScan(bleScanCallback);
        srlScanDevice.setRefreshing(false);

        if(device.getDevice().getBondState() != BluetoothDevice.BOND_BONDED) {
            device.getDevice().createBond();
        } else {
            registerBondedDevice(device);
        }
    }

    private void addDeviceDetailInfoToList(final BleDeviceDetailInfo device) {
        if(device == null) return;
        boolean isNewDevice = true;
        for(BleDeviceDetailInfo dv : scannedDeviceDetailInfoList) {
            if(dv.getAddress().equalsIgnoreCase(device.getAddress())) {
                isNewDevice = false;
                break;
            }
        }

        if(isNewDevice) {
            scannedDeviceDetailInfoList.add(device);
            scannedDeviceAdapter.notifyDataSetChanged();
            rvScanDevice.scrollToPosition(scannedDeviceDetailInfoList.size()-1);
        }
    }

    private void registerBondedDevice(final BleDeviceDetailInfo device) {
        if(device.getDevice().getBondState() != BluetoothDevice.BOND_BONDED) {
            Toast.makeText(this, "设备未绑定，无法注册。", Toast.LENGTH_SHORT).show();
        }
        // 获取设备广播数据中的UUID的短串
        AdRecord record = device.getAdRecordStore().getRecord(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        if(record == null) {
            Toast.makeText(this, "获取设备UUID信息错误，无法注册。", Toast.LENGTH_SHORT).show();
            return;
        }

        String uuidShortString = UuidUtil.longToShortString(UuidUtil.byteArrayToUuid(record.getData()).toString());
        Intent intent = new Intent(ScanActivity.this, RegisterActivity.class);
        BleDeviceRegisterInfo registerInfo = new BleDeviceRegisterInfo();
        registerInfo.setMacAddress(device.getAddress());
        registerInfo.setUuidString(uuidShortString);
        intent.putExtra(DEVICE_REGISTER_INFO, registerInfo);
        startActivityForResult(intent, 1);
    }

}
