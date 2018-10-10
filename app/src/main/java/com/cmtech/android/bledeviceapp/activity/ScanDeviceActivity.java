package com.cmtech.android.bledeviceapp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cmtech.android.ble.ViseBle;
import com.cmtech.android.ble.callback.scan.DevNameFilterScanCallback;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.ScanDeviceAdapter;
import com.cmtech.android.bledeviceapp.callback.ScanDeviceCallback;
import com.cmtech.android.bledeviceapp.util.Uuid;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.ble.model.adrecord.AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE;

public class ScanDeviceActivity extends AppCompatActivity {
    private static final String TAG = "ScanDeviceActivity";
    private static final String DEFAULT_DEVICE_NAME = "CM1.0";

    private ViseBle viseBle = MyApplication.getViseBle();

    // 用于实现扫描设备的显示
    private ScanDeviceAdapter scanDeviceAdapter;
    private RecyclerView rvScanDevice;
    private List<BluetoothLeDevice> foundDeviceList = new ArrayList<>();    // 扫描到的设备列表
    private List<Boolean> foundDeviceStatus = new ArrayList<>();            // 扫描到的设备状态列表，是否登记

    // 已经登记的设备Mac地址列表
    private List<String> registeredDeviceMacList = new ArrayList<>();

    private Button btnCancel;
    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);

        // 获取已登记过的设备Mac列表
        registeredDeviceMacList =  (ArrayList<String>) getIntent()
                .getSerializableExtra("registered_device_list");


        rvScanDevice = findViewById(R.id.rvScanDevice);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvScanDevice.setLayoutManager(layoutManager);
        rvScanDevice.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        scanDeviceAdapter = new ScanDeviceAdapter(foundDeviceList, foundDeviceStatus);
        rvScanDevice.setAdapter(scanDeviceAdapter);


        btnCancel = findViewById(R.id.device_register_cancel_btn);
        btnOk = findViewById(R.id.device_register_ok_btn);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int which = scanDeviceAdapter.getSelectItem();
                if(which != -1) {
                    BluetoothLeDevice device = foundDeviceList.get(which);

                    if(hasRegistered(device)) {
                        Toast.makeText(ScanDeviceActivity.this, "此设备已登记！", Toast.LENGTH_LONG).show();
                    } else {
                        registerDevice(device);
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "start to scan now.");
        viseBle.startScan(new DevNameFilterScanCallback(new ScanDeviceCallback(this)).setDeviceName(DEFAULT_DEVICE_NAME));
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

    // 添加一个新设备到发现的设备列表中
    public void addOneNewDeviceToFoundDeviceList(BluetoothLeDevice device) {
        if(device == null) return;

        boolean canAdd = true;
        for(BluetoothLeDevice dv : foundDeviceList) {
            if(dv.getAddress().equalsIgnoreCase(device.getAddress())) {
                canAdd = false;
                break;
            }
        }
        if(canAdd) {
            foundDeviceList.add(device);
            foundDeviceStatus.add(hasRegistered(device));
            scanDeviceAdapter.notifyItemInserted(foundDeviceList.size()-1);
            rvScanDevice.scrollToPosition(foundDeviceList.size()-1);
        }
        return;
    }


    // 设备是否已经登记过
    private boolean hasRegistered(BluetoothLeDevice device) {
        for(String ele : registeredDeviceMacList) {
            if(ele.equalsIgnoreCase(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    // 登记一个设备
    private void registerDevice(final BluetoothLeDevice device) {
        String macAddress = device.getAddress();

        // 获取设备广播数据中的UUID的短串
        AdRecord record = device.getAdRecordStore().getRecord(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        if(record == null) return;

        String uuidShortString = Uuid.longToShortString(Uuid.byteArrayToUuid(record.getData()).toString());

        Intent intent = new Intent(ScanDeviceActivity.this, DeviceBasicInfoActivity.class);
        BleDeviceBasicInfo basicInfo = new BleDeviceBasicInfo(macAddress, "", uuidShortString, "", true);
        intent.putExtra("devicebasicinfo", basicInfo);

        startActivityForResult(intent, 1);
    }

}
