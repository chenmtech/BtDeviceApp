package com.cmtech.android.btdeviceapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cmtech.android.ble.ViseBle;
import com.cmtech.android.ble.callback.scan.DevNameFilterScanCallback;
import com.cmtech.android.ble.model.BluetoothLeDevice;
import com.cmtech.android.ble.utils.BleUtil;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.adapter.ScanDeviceAdapter;
import com.cmtech.android.btdeviceapp.callback.ScanDeviceCallback;
import com.vise.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScanDeviceActivity extends AppCompatActivity {
    private static final String TAG = "ScanDeviceActivity";
    private static final String SCAN_DEVICE_NAME = "CM1.0";

    private ViseBle viseBle = MyApplication.getViseBle();

    // 用于实现扫描设备的显示
    private ScanDeviceAdapter scanDeviceAdapter;
    private RecyclerView rvScanedDevices;
    private List<BluetoothLeDevice> scanedDeviceList = new ArrayList<>();

    // 当前已经在配置设备列表中的设备
    private List<String> deviceMacList = new ArrayList<>();

    private Button btnCancel;
    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);

        // 获取已配置设备Mac列表
        deviceMacList =  (ArrayList<String>) getIntent()
                .getSerializableExtra("device_list");


        rvScanedDevices = (RecyclerView)findViewById(R.id.rvScanedDevices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvScanedDevices.setLayoutManager(layoutManager);
        scanDeviceAdapter = new ScanDeviceAdapter(scanedDeviceList);
        rvScanedDevices.setAdapter(scanDeviceAdapter);


        btnCancel = (Button)findViewById(R.id.device_add_cancel_btn);
        btnOk = (Button)findViewById(R.id.device_add_ok_btn);

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
                    if(hasConfigured(scanedDeviceList.get(which))) {
                        Toast.makeText(ScanDeviceActivity.this, "此设备已添加", Toast.LENGTH_LONG).show();
                    } else {
                        addToConfiguredDevice(scanDeviceAdapter.getSelectItem());
                    }
                }
            }
        });

    }

    private boolean hasConfigured(BluetoothLeDevice device) {
        for(String ele : deviceMacList) {
            if(ele.equalsIgnoreCase(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    private void addToConfiguredDevice(final int which) {
        LinearLayout layout = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_configured_device_info, null);
        String deviceName = scanedDeviceList.get(which).getName();
        final EditText editText = (EditText)layout.findViewById(R.id.cfg_device_nickname);
        editText.setText(deviceName);
        /*final AlertDialog.Builder builder = new AlertDialog.Builder(ScanDeviceActivity.this);
        builder.setTitle("设置昵称");
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String nickName = editText.getText().toString();
                String macAddress = scanedDeviceList.get(which).getAddress();
                boolean isAutoConnected = false;
                *//*MyBluetoothDevice device = new MyBluetoothDevice();
                device.setNickName(editText.getText().toString());
                device.setMacAddress(scanedDeviceList.get(which).getAddress());
                device.setAutoConnected(false);*//*
                Intent intent = new Intent();
                intent.putExtra("device_nickname", nickName);
                intent.putExtra("device_macaddress", macAddress);
                intent.putExtra("device_isautoconnect", isAutoConnected);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();*/
        Intent intent = new Intent(ScanDeviceActivity.this, ConfigureDeviceActivity.class);
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检测权限，并使能蓝牙
        checkBluetoothPermission();

        Log.d(TAG, "start to scan now.");
        viseBle.startScan(new DevNameFilterScanCallback(new ScanDeviceCallback(this)).setDeviceName(SCAN_DEVICE_NAME));
    }

    // 将扫描到的设备添加到扫描设备列表中
    public boolean addToScanedDevice(BluetoothLeDevice device) {
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
            scanDeviceAdapter.notifyItemInserted(scanedDeviceList.size()-1);
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
            else{
                enableBluetooth();
            }
        } else {
            enableBluetooth();
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
                    Toast.makeText(this, "没有蓝牙权限，程序无法运行", Toast.LENGTH_SHORT).show();
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
        switch (requestCode) {
            // 请求蓝牙结果
            case 1:
                if (resultCode == RESULT_OK) {
                    enableBluetooth();
                } else if (resultCode == RESULT_CANCELED) { // 不同意
                    Toast.makeText(this, "蓝牙未打开，程序无法运行", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            // 配置设备信息结果
            case 2:
                if ( resultCode == RESULT_OK) {
                    String deviceNickname = data.getStringExtra("device_nickname");
                    String imagePath = data.getStringExtra("device_imagepath");
                    Boolean isAutoConnect = data.getBooleanExtra("device_isautoconnect", false);
                    int which = scanDeviceAdapter.getSelectItem();
                    String macAddress = scanedDeviceList.get(which).getAddress();
                    Intent intent = new Intent();
                    intent.putExtra("device_nickname", deviceNickname);
                    intent.putExtra("device_macaddress", macAddress);

                    File fromFile = FileUtil.getFile(imagePath);
                    File toFile = FileUtil.getFile(getExternalFilesDir("images"), macAddress, ".jpg");
                    try {
                        FileUtil.copyFile(fromFile, toFile);
                        imagePath = toFile.getCanonicalPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    intent.putExtra("device_imagepath", imagePath);

                    intent.putExtra("device_isautoconnect", isAutoConnect);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}
