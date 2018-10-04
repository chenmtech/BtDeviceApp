package com.cmtech.android.bledeviceapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import com.cmtech.android.ble.utils.BleUtil;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.ScanDeviceAdapter;
import com.cmtech.android.bledeviceapp.callback.ScanDeviceCallback;
import com.cmtech.android.bledeviceapp.util.Uuid;

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
    private List<BluetoothLeDevice> foundDeviceList = new ArrayList<>();

    // 已经登记过的设备Mac地址列表
    private List<String> registeredDeviceMacList = new ArrayList<>();

    private Button btnCancel;
    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);

        // 获取已登记过的设备Mac列表
        registeredDeviceMacList =  (ArrayList<String>) getIntent()
                .getSerializableExtra("device_list");


        rvScanDevice = findViewById(R.id.rvScanDevice);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvScanDevice.setLayoutManager(layoutManager);
        rvScanDevice.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        scanDeviceAdapter = new ScanDeviceAdapter(foundDeviceList);
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
                        Toast.makeText(ScanDeviceActivity.this, "此设备之前已登记！", Toast.LENGTH_LONG).show();
                    } else {
                        addDevice(device);
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

    // 此设备是否已经登记过
    private boolean hasRegistered(BluetoothLeDevice device) {
        for(String ele : registeredDeviceMacList) {
            if(ele.equalsIgnoreCase(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    private void addDevice(final BluetoothLeDevice device) {
        String macAddress = device.getAddress();

        // 获取设备广播数据中的UUID的短串
        AdRecord record = device.getAdRecordStore().getRecord(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        if(record == null) return;

        String uuidShortString = Uuid.longToShortString(Uuid.byteArrayToUuid(record.getData()).toString());

        Intent intent = new Intent(ScanDeviceActivity.this, DeviceBasicInfoActivity.class);
        intent.putExtra("device_nickname", "");
        intent.putExtra("device_macaddress", macAddress);
        intent.putExtra("device_uuid", uuidShortString);
        intent.putExtra("device_imagepath", "");
        intent.putExtra("device_isautoconnect", false);

        startActivityForResult(intent, 1);
    }



    // 将扫描到的一个设备添加到扫描设备列表中
    public boolean addToScanedDevice(BluetoothLeDevice device) {
        if(device == null) return false;

        boolean canAdd = true;
        for(BluetoothLeDevice dv : foundDeviceList) {
            if(dv.getAddress().equalsIgnoreCase(device.getAddress())) {
                canAdd = false;
                break;
            }
        }
        if(canAdd) {
            foundDeviceList.add(device);
            scanDeviceAdapter.notifyItemInserted(foundDeviceList.size()-1);
            rvScanDevice.scrollToPosition(foundDeviceList.size()-1);
        }
        return canAdd;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // 配置设备信息结果
            case 1:
                if ( resultCode == RESULT_OK) {
                    /*String deviceNickname = data.getStringExtra("device_nickname");
                    String macAddress = data.getStringExtra("device_macaddress");
                    String deviceUuid = data.getStringExtra("device_uuid");
                    String imagePath = data.getStringExtra("device_imagepath");
                    Boolean isAutoConnect = data.getBooleanExtra("device_isautoconnect", false);
                    int which = scanDeviceAdapter.getSelectItem();

                    Intent intent = new Intent();
                    intent.putExtra("device_nickname", deviceNickname);
                    intent.putExtra("device_macaddress", macAddress);
                    intent.putExtra("device_uuid", deviceUuid);
                    intent.putExtra("device_imagepath", imagePath);
                    intent.putExtra("device_isautoconnect", isAutoConnect);*/
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}
