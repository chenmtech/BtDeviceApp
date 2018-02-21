package com.cmtech.android.btdeviceapp.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
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
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.adapter.AddDeviceAdapter;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;
import com.cmtech.android.btdeviceapp.scan.ScanDeviceCallback;

import java.util.ArrayList;
import java.util.List;

public class AddDeviceActivity extends AppCompatActivity {
    private static final String TAG = "AddDeviceActivity";
    private static final String SCAN_DEVICE_NAME = "CM1.0";

    private ViseBle viseBle = ViseBle.getInstance();;
    private AddDeviceAdapter addDeviceAdapter;
    private RecyclerView rvScanedDevices;

    private List<BluetoothLeDevice> scanedDeviceList = new ArrayList<>();

    private List<ConfiguredDevice> configuredDevices = new ArrayList<>();

    private Button btnCancel;
    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        configuredDevices =  (ArrayList<ConfiguredDevice>) getIntent().getSerializableExtra("configured_device_list");

        rvScanedDevices = (RecyclerView)findViewById(R.id.rvScanedDevices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvScanedDevices.setLayoutManager(layoutManager);
        addDeviceAdapter = new AddDeviceAdapter(scanedDeviceList);
        rvScanedDevices.setAdapter(addDeviceAdapter);
        viseBle.init(this);

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
                int which = addDeviceAdapter.getSelectItem();
                if(which != -1) {
                    if(isConfiguredDevice(scanedDeviceList.get(which))) {
                        Toast.makeText(AddDeviceActivity.this, "此设备已添加", Toast.LENGTH_LONG).show();
                    } else {
                        addConfiguredDevice(addDeviceAdapter.getSelectItem());
                    }
                }
            }
        });

    }

    private boolean isConfiguredDevice(BluetoothLeDevice device) {
        for(ConfiguredDevice ele : configuredDevices) {
            if(ele.getMacAddress().equalsIgnoreCase(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    private void addConfiguredDevice(final int which) {
        LinearLayout layout = (LinearLayout)getLayoutInflater().inflate(R.layout.activity_set_cfg_device_info, null);
        String deviceName = scanedDeviceList.get(which).getName();
        final EditText editText = (EditText)layout.findViewById(R.id.cfg_device_nickname);
        editText.setText(deviceName);
        final AlertDialog.Builder builder = new AlertDialog.Builder(AddDeviceActivity.this);
        builder.setTitle("设置设备别名");
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ConfiguredDevice device = new ConfiguredDevice();
                device.setNickName(editText.getText().toString());
                device.setMacAddress(scanedDeviceList.get(which).getAddress());
                device.setAutoConnected(false);
                Intent intent = new Intent();
                intent.putExtra("return_device", device);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检测权限，使能蓝牙
        checkBluetoothPermission();

        Log.d(TAG, "start to scan now.");
        viseBle.startScan(new DevNameFilterScanCallback(new ScanDeviceCallback(this)).setDeviceName(SCAN_DEVICE_NAME));
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
            addDeviceAdapter.notifyItemInserted(scanedDeviceList.size()-1);
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
                    Toast.makeText(this, "没有蓝牙权限，程序无法运行", Toast.LENGTH_SHORT);
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
            Toast.makeText(this, "蓝牙未打开，程序无法运行", Toast.LENGTH_SHORT);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
