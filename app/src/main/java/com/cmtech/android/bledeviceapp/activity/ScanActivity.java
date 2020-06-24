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
import android.view.Menu;
import android.view.MenuItem;

import com.cmtech.android.ble.callback.IBleScanCallback;
import com.cmtech.android.ble.core.BleDeviceDetailInfo;
import com.cmtech.android.ble.core.BleDeviceInfo;
import com.cmtech.android.ble.core.BleScanner;
import com.cmtech.android.ble.core.DeviceInfo;
import com.cmtech.android.ble.model.adrecord.AdRecord;
import com.cmtech.android.ble.utils.HexUtil;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.ScanAdapter;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledeviceapp.AppConstant.SCAN_DURATION;
import static com.cmtech.android.bledeviceapp.MyApplication.showMessageUsingLongToast;
import static com.cmtech.android.bledeviceapp.MyApplication.showMessageUsingShortToast;
import static com.cmtech.android.bledeviceapp.activity.DeviceInfoActivity.DEVICE_INFO;

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
    private static final String TAG = "ScanActivity";
    private static final ScanFilter SCAN_FILTER = null;
    //public static final String REGISTERED_DEVICE_ADDRESS_LIST = "registered_device_address_list";

    private final List<BleDeviceDetailInfo> foundDevInfos = new ArrayList<>(); // 扫描到的设备的BleDeviceDetailInfo列表
    private List<String> regAddrs = new ArrayList<>(); // 已注册的设备mac地址列表
    private SwipeRefreshLayout srlDevice;
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
                    showMessageUsingLongToast("扫描中，请稍等");
                    break;

                case CODE_BLE_CLOSED:
                    showMessageUsingLongToast("蓝牙已关闭");
                    srlDevice.setRefreshing(false);
                    BleScanner.stopScan(this);
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intent);
                    break;

                case CODE_BLE_INNER_ERROR:
                    showMessageUsingLongToast("蓝牙错误，请重启蓝牙。");
                    srlDevice.setRefreshing(false);
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
            regAddrs = (List<String>) intent.getSerializableExtra("device_address_list");
        }

        // 初始化扫描设备列表
        rvDevice = findViewById(R.id.rv_device);
        rvDevice.setLayoutManager(new LinearLayoutManager(this));
        rvDevice.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        devAdapter = new ScanAdapter(foundDevInfos, regAddrs, this);
        rvDevice.setAdapter(devAdapter);

        srlDevice = findViewById(R.id.srl_device);
        Point pt = new Point();
        getWindowManager().getDefaultDisplay().getSize(pt);
        int height = pt.y;
        srlDevice.setProgressViewOffset(true, height/2-50, height/2);
        srlDevice.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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

        foundDevInfos.clear();
        devAdapter.notifyDataSetChanged();
        BleScanner.stopScan(bleScanCallback);
        srlDevice.setRefreshing(true);
        BleScanner.startScan(SCAN_FILTER, bleScanCallback);

        mHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(srlDevice.isRefreshing())
                    srlDevice.setRefreshing(false);

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

        if(srlDevice.isRefreshing())
            srlDevice.setRefreshing(false);

        BleScanner.stopScan(bleScanCallback);
    }

    public void registerDevice(final BleDeviceDetailInfo detailInfo) {
        // 先停止扫描
        BleScanner.stopScan(bleScanCallback);
        srlDevice.setRefreshing(false);

        AdRecord serviceUUID = detailInfo.getAdRecordStore().getRecord(AdRecord.BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_MORE_AVAILABLE);
        byte[] uuidBytes = null;
        if(serviceUUID != null) {
            uuidBytes = new byte[]{serviceUUID.getData()[1], serviceUUID.getData()[0]};
        }
        if(uuidBytes == null) {
            showMessageUsingShortToast("不支持的设备类型");
            return;
        }

        String uuidShortString = HexUtil.encodeHexStr(uuidBytes);
        DeviceInfo registerInfo = new BleDeviceInfo(detailInfo.getAddress(), uuidShortString);
        Intent intent = new Intent(ScanActivity.this, DeviceInfoActivity.class);
        intent.putExtra(DEVICE_INFO, registerInfo);
        startActivityForResult(intent, 1);
    }

    private void addDevice(final BleDeviceDetailInfo device) {
        if(device == null) return;

        ViseLog.e("Find device: " + device);

        boolean isNewDevice = true;
        for(BleDeviceDetailInfo dv : foundDevInfos) {
            if(dv.getAddress().equalsIgnoreCase(device.getAddress())) {
                isNewDevice = false;
                break;
            }
        }

        if(isNewDevice) {
            foundDevInfos.add(device);
            devAdapter.notifyDataSetChanged();
        }
    }
}
