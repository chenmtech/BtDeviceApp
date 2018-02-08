package com.cmtech.android.btdeviceapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.cmtech.android.ble.ViseBle;
import com.cmtech.android.ble.callback.scan.ScanCallback;
import com.cmtech.android.ble.callback.scan.SingleFilterScanCallback;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.utils.BleUtil;
import com.cmtech.android.btdeviceapp.adapter.ScanedDeviceAdapter;
import com.cmtech.android.btdeviceapp.scan.ScanDeviceCallback;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String SCAN_DEVICE_NAME = "CM1.0";

    private ViseBle viseBle;
    private ScanedDeviceAdapter scanedDeviceAdapter;
    private RecyclerView rvScanedDevices;
    private List<BluetoothLeDevice> scanedDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvScanedDevices = (RecyclerView)findViewById(R.id.rvScanedDevices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvScanedDevices.setLayoutManager(layoutManager);
        scanedDeviceAdapter = new ScanedDeviceAdapter(scanedDeviceList);
        rvScanedDevices.setAdapter(scanedDeviceAdapter);

        viseBle = ViseBle.getInstance();
        viseBle.init(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检测权限
        checkBluetoothPermission();
        // 使能蓝牙
        enableBluetooth();

        Log.d(TAG, "start to scan now");
        viseBle.startScan(new SingleFilterScanCallback(new ScanDeviceCallback(this)).setDeviceName(SCAN_DEVICE_NAME));
    }

    public boolean addScanedDevice(BluetoothLeDevice device) {
        if(device == null) return false;

        boolean canAdd = true;
        for(BluetoothLeDevice dv : scanedDeviceList) {
            if(dv.getAddress().equalsIgnoreCase(device.getAddress())) {
                canAdd = false;
                break;
            }
        }
        if(canAdd) {
            scanedDeviceList.add(device);
            scanedDeviceAdapter.notifyItemInserted(scanedDeviceList.size()-1);
            rvScanedDevices.scrollToPosition(scanedDeviceList.size()-1);
        }
        return canAdd;
    }

    /**
     * 检查蓝牙权限
     */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //校验是否已具有模糊定位权限
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // 同意获得所需权限
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableBluetooth();
                } else {
                    // 不同意获得权限
                    finish();
                }
                return;
            }
        }
    }

    private void enableBluetooth() {
        if (!BleUtil.isBleEnable(this)) {
            BleUtil.enableBluetooth(this, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                // 同意了，再使能一次
                enableBluetooth();
            }
        } else if (resultCode == RESULT_CANCELED) { // 不同意
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
