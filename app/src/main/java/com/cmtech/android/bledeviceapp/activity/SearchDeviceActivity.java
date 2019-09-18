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

import com.cmtech.android.ble.callback.BleScanCallback;
import com.cmtech.android.ble.extend.BleDeviceBasicInfo;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.BleDeviceConstant;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.ScanDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.ble.model.adrecord.AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE;
import static com.cmtech.android.bledeviceapp.activity.DeviceBasicInfoActivity.DEVICE_BASICINFO;

/**
  *
  * ClassName:      SearchDeviceActivity
  * Description:    搜索设备Activiy
  * Author:         chenm
  * CreateDate:     2018/2/28 18:07
  * UpdateUser:     chenm
  * UpdateDate:     2019-04-23 18:07
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class SearchDeviceActivity extends AppCompatActivity {
    private static final String TAG = "SearchDeviceActivity";

    public static final String REGISTER_DEVICE_MAC_LIST = "register_device_mac_list";

    // 设备绑定状态改变的广播接收器类
    private class BleDeviceBondReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                switch (device.getBondState()) {
                    // 设备已绑定
                    case BluetoothDevice.BOND_BONDED:
                        // 登记设备
                        for (BluetoothLeDevice ele : deviceList) {
                            if(ele.getAddress().equalsIgnoreCase(device.getAddress())) {
                                startConfigBondedDevice(ele);

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

    private final ScanFilter scanFilter = new ScanFilter.Builder().setDeviceName(BleDeviceConstant.SCAN_DEVICE_NAME).build();

    private final BleScanCallback bleScanCallback = new BleScanCallback() {
        @Override
        public void onScanFinish(BluetoothLeDevice bluetoothLeDevice) {
            if(bluetoothLeDevice != null) {
                addDeviceToList(bluetoothLeDevice);
            } else {
                srlScanDevice.setRefreshing(false);

                Toast.makeText(SearchDeviceActivity.this, "搜索结束。", Toast.LENGTH_SHORT).show();
            }
        }
    }.setScanFilter(scanFilter); // 扫描回调

    private SwipeRefreshLayout srlScanDevice;

    private ScanDeviceAdapter scanDeviceAdapter;

    private RecyclerView rvScanDevice;

    private List<BluetoothLeDevice> deviceList = new ArrayList<>();

    private List<String> registedDeviceMacList = new ArrayList<>();

    private BleDeviceBondReceiver bondReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_device_register);

        setSupportActionBar(toolbar);

        // 获取已登记过的设备Mac列表
        Intent intent = getIntent();

        if(intent != null) {
            registedDeviceMacList = (List<String>) intent.getSerializableExtra(REGISTER_DEVICE_MAC_LIST);
        }

        rvScanDevice = findViewById(R.id.rv_scandevice);

        rvScanDevice.setLayoutManager(new LinearLayoutManager(this));

        rvScanDevice.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        scanDeviceAdapter = new ScanDeviceAdapter(deviceList, registedDeviceMacList, this);

        rvScanDevice.setAdapter(scanDeviceAdapter);

        srlScanDevice = findViewById(R.id.srl_scandevice);

        srlScanDevice.setProgressViewOffset(true, 250, 350);

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

                srlScanDevice.setRefreshing(true);

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

        unregisterReceiver(bondReceiver);

        if(srlScanDevice.isRefreshing())
            srlScanDevice.setRefreshing(false);

        bleScanCallback.stopScan();
    }


    public void registerDevice(final BluetoothLeDevice device) {
        // 先停止扫描
        bleScanCallback.stopScan();

        srlScanDevice.setRefreshing(false);

        if(device.getDevice().getBondState() != BluetoothDevice.BOND_BONDED) {
            device.getDevice().createBond();
        } else {
            startConfigBondedDevice(device);
        }
    }


    // 开始扫描
    private void startScan() {
        deviceList.clear();

        scanDeviceAdapter.notifyDataSetChanged();

        bleScanCallback.stopScan();

        bleScanCallback.startScan();
    }

    private void addDeviceToList(final BluetoothLeDevice device) {
        if(device == null) return;

        boolean isNewDevice = true;

        for(BluetoothLeDevice dv : deviceList) {
            if(dv.getAddress().equalsIgnoreCase(device.getAddress())) {
                isNewDevice = false;

                break;
            }
        }

        if(isNewDevice) {
            deviceList.add(device);

            scanDeviceAdapter.notifyDataSetChanged();

            rvScanDevice.scrollToPosition(deviceList.size()-1);

        }
    }

    private void startConfigBondedDevice(final BluetoothLeDevice device) {
        if(device.getDevice().getBondState() != BluetoothDevice.BOND_BONDED) {
            throw new IllegalStateException("设备未绑定");
        }

        String macAddress = device.getAddress();

        // 获取设备广播数据中的UUID的短串
        AdRecord record = device.getAdRecordStore().getRecord(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);

        if(record == null) {
            throw new IllegalStateException("设备未绑定");
        }

        String uuidShortString = UuidUtil.longToShortString(UuidUtil.byteArrayToUuid(record.getData()).toString());

        Intent intent = new Intent(SearchDeviceActivity.this, DeviceBasicInfoActivity.class);

        BleDeviceBasicInfo basicInfo = new BleDeviceBasicInfo();

        basicInfo.setMacAddress(macAddress);

        basicInfo.setUuidString(uuidShortString);

        intent.putExtra(DEVICE_BASICINFO, basicInfo);

        startActivityForResult(intent, 1);
    }

}
