package com.cmtech.android.bledeviceapp.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanFilter;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import com.cmtech.android.ble.callback.IBleScanCallback;
import com.cmtech.android.ble.core.BleDeviceDetailInfo;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.ble.core.BleScanner;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.BleDeviceConstant;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.ScannedDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.ble.model.adrecord.AdRecord.BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE;
import static com.cmtech.android.bledeviceapp.MyApplication.showLongToastMessage;
import static com.cmtech.android.bledeviceapp.MyApplication.showShortToastMessage;
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
    private static final ScanFilter SCAN_FILTER_WITH_DEVICE_NAME = new ScanFilter.Builder().setDeviceName(BleDeviceConstant.SCAN_DEVICE_NAME).build();
    public static final String REGISTERED_DEVICE_MAC_LIST = "registered_device_mac_list";

    private final List<BleDeviceDetailInfo> scannedDeviceDetailInfoList = new ArrayList<>(); // 扫描到的设备的BleDeviceDetailInfo列表
    private List<String> registeredDeviceMacList = new ArrayList<>(); // 已注册的设备mac地址列表
    private SwipeRefreshLayout srlScanDevice;
    private ScannedDeviceAdapter scannedDeviceAdapter;
    private RecyclerView rvScanDevice;
    private Handler mHandle = new Handler(Looper.getMainLooper());

    // 扫描回调
    private final IBleScanCallback bleScanCallback = new IBleScanCallback() {
        @Override
        public void onDeviceFound(BleDeviceDetailInfo bleDeviceDetailInfo) {
            addDeviceDetailInfoToList(bleDeviceDetailInfo);
        }

        @Override
        public void onScanFailed(int errorCode) {
            switch (errorCode) {
                case CODE_ALREADY_STARTED:
                    showLongToastMessage("扫描进行中，不能重复扫描。");
                    break;

                case CODE_BLE_CLOSED:
                    showLongToastMessage("蓝牙已关闭，无法扫描，请打开蓝牙。");
                    srlScanDevice.setRefreshing(false);
                    BleScanner.stopScan(this);
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intent);
                    break;

                case CODE_BLE_INNER_ERROR:
                    srlScanDevice.setRefreshing(false);
                    BleScanner.stopScan(this);
                    showLongToastMessage("蓝牙内部错误，必须重启蓝牙。");
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
        Display display = getWindowManager().getDefaultDisplay();
        Point pt = new Point();
        display.getSize(pt);
        int height = pt.y;
        srlScanDevice.setProgressViewOffset(true, height/2-50, height/2);
        srlScanDevice.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startScan();
            }
        });

        startScan();
    }

    // 开始扫描
    private void startScan() {
        mHandle.removeCallbacksAndMessages(null);

        scannedDeviceDetailInfoList.clear();
        scannedDeviceAdapter.notifyDataSetChanged();
        BleScanner.stopScan(bleScanCallback);
        srlScanDevice.setRefreshing(true);
        BleScanner.startScan(SCAN_FILTER_WITH_DEVICE_NAME, bleScanCallback);

        mHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(srlScanDevice.isRefreshing())
                    srlScanDevice.setRefreshing(false);

                BleScanner.stopScan(bleScanCallback);
            }
        }, 10000);
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

        mHandle.removeCallbacksAndMessages(null);

        if(srlScanDevice.isRefreshing())
            srlScanDevice.setRefreshing(false);

        BleScanner.stopScan(bleScanCallback);
    }

    public void registerDevice(final BleDeviceDetailInfo device) {
        // 先停止扫描
        BleScanner.stopScan(bleScanCallback);
        srlScanDevice.setRefreshing(false);

        // 获取设备广播数据中的UUID的短串
        AdRecord record = device.getAdRecordStore().getRecord(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE);
        if(record == null) {
            showShortToastMessage("获取设备UUID信息错误，无法注册。");
            return;
        }

        String uuidShortString = UuidUtil.longToShortString(UuidUtil.byteArrayToUuid(record.getData()).toString());
        BleDeviceRegisterInfo registerInfo = new BleDeviceRegisterInfo(device.getAddress(), uuidShortString);
        Intent intent = new Intent(ScanActivity.this, RegisterActivity.class);
        intent.putExtra(DEVICE_REGISTER_INFO, registerInfo);
        startActivityForResult(intent, 1);
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
}
