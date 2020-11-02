package com.cmtech.android.bledeviceapp.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanFilter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleScanCallback;
import com.cmtech.android.ble.core.BleDeviceCommonInfo;
import com.cmtech.android.ble.core.BleDeviceDetailInfo;
import com.cmtech.android.ble.core.BleScanner;
import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.ble.utils.HexUtil;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.ScanAdapter;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledeviceapp.activity.DeviceInfoActivity.DEVICE_INFO;
import static com.cmtech.android.bledeviceapp.global.AppConstant.SCAN_DURATION;

/**
  *
  * ClassName:      ScanActivity
  * Description:    扫描添加设备Activiy
  * Author:         chenm
  * CreateDate:     2018/2/28 18:07
  * UpdateUser:     chenm
  * UpdateDate:     2019-04-23 18:07
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class ScanActivity extends AppCompatActivity {
    private static final ScanFilter SCAN_FILTER = null;

    private final List<BleDeviceDetailInfo> scannedDevInfos = new ArrayList<>(); // 扫描到的设备的BleDeviceDetailInfo列表
    private List<String> registeredDevAddrs = new ArrayList<>(); // 已注册的设备mac地址列表
    private ProgressBar pbScan;
    private ScanAdapter devAdapter;
    private RecyclerView rvDevice;
    private Handler mHandle = new Handler(Looper.getMainLooper());

    // 扫描回调
    private final IBleScanCallback bleScanCallback = new IBleScanCallback() {
        @Override
        public void onDeviceFound(final BleDeviceDetailInfo bleDeviceDetailInfo) {
            mHandle.post(new Runnable() {
                @Override
                public void run() {
                    addDevice(bleDeviceDetailInfo);
                }
            });
        }

        @Override
        public void onScanFailed(int errorCode) {
            switch (errorCode) {
                case CODE_ALREADY_STARTED:
                    Toast.makeText(ScanActivity.this, R.string.scan_failed_already_started, Toast.LENGTH_SHORT).show();
                    break;

                case CODE_BLE_CLOSED:
                    Toast.makeText(ScanActivity.this, R.string.scan_failed_bt_closed, Toast.LENGTH_SHORT).show();
                    pbScan.setVisibility(View.GONE);
                    BleScanner.stopScan(this);
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intent);
                    break;

                case CODE_BLE_INNER_ERROR:
                    Toast.makeText(ScanActivity.this, R.string.scan_failed_ble_inner_error, Toast.LENGTH_SHORT).show();
                    pbScan.setVisibility(View.GONE);
                    BleScanner.stopScan(this);
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

        // 获取已注册设备地址列表
        Intent intent = getIntent();
        if(intent != null) {
            registeredDevAddrs = (List<String>) intent.getSerializableExtra("device_address_list");
        }

        pbScan = findViewById(R.id.pb_scan_device);

        // 初始化扫描设备列表
        rvDevice = findViewById(R.id.rv_device);
        rvDevice.setLayoutManager(new LinearLayoutManager(this));
        rvDevice.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        devAdapter = new ScanAdapter(scannedDevInfos, registeredDevAddrs, this);
        rvDevice.setAdapter(devAdapter);
        rvDevice.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == devAdapter.getItemCount()-1) {
                    startScan();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if(layoutManager != null)
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }
        });

        startScan();
    }

    // 开始扫描
    private void startScan() {
        mHandle.removeCallbacksAndMessages(null);

        scannedDevInfos.clear();
        devAdapter.notifyDataSetChanged();
        BleScanner.stopScan(bleScanCallback);
        pbScan.setVisibility(View.VISIBLE);
        BleScanner.startScan(SCAN_FILTER, bleScanCallback);

        mHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                pbScan.setVisibility(View.GONE);

                BleScanner.stopScan(bleScanCallback);
            }
        }, SCAN_DURATION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
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

        mHandle.removeCallbacksAndMessages(null);

        pbScan.setVisibility(View.GONE);

        BleScanner.stopScan(bleScanCallback);
    }

    public void registerDevice(final BleDeviceDetailInfo detailInfo) {
        // 先停止扫描
        BleScanner.stopScan(bleScanCallback);
        pbScan.setVisibility(View.GONE);

        AdRecord serviceUUID = detailInfo.getAdRecordStore().getRecord(AdRecord.BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_MORE_AVAILABLE);
        if(serviceUUID != null) {
            byte[] uuidBytes = new byte[]{serviceUUID.getData()[1], serviceUUID.getData()[0]};
            String uuidShortString = HexUtil.encodeHexStr(uuidBytes);
            DeviceCommonInfo registerInfo = new BleDeviceCommonInfo(detailInfo.getAddress(), uuidShortString);
            Intent intent = new Intent(ScanActivity.this, DeviceInfoActivity.class);
            intent.putExtra(DEVICE_INFO, registerInfo);
            startActivityForResult(intent, 1);
        }
    }

    private void addDevice(final BleDeviceDetailInfo device) {
        if(device == null) return;

        ViseLog.e("Find device: " + device);

        boolean isNewDevice = true;
        for(BleDeviceDetailInfo dv : scannedDevInfos) {
            if(dv.getAddress().equalsIgnoreCase(device.getAddress())) {
                isNewDevice = false;
                break;
            }
        }

        if(isNewDevice) {
            scannedDevInfos.add(device);
            devAdapter.notifyDataSetChanged();
        }
    }
}
