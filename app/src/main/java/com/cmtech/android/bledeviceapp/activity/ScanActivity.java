package com.cmtech.android.bledeviceapp.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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
    private static final int RC_REGISTER_DEVICE = 1; // 登记设备返回码
    private static final int RC_OPEN_POSITION_FUNCTION = 2; // 开启位置信息功能返回码

    private final List<BleDeviceDetailInfo> scannedDevInfos = new ArrayList<>(); // 扫描到的设备的BleDeviceDetailInfo列表
    private List<String> registeredDevAddrs = new ArrayList<>(); // 已注册的设备mac地址列表
    private LinearLayout llSearchProgress;
    private ScanAdapter devAdapter;
    private RecyclerView rvDevice;
    private final Handler mHandle = new Handler(Looper.getMainLooper());

    private volatile boolean isScanning = false;

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
                    llSearchProgress.setVisibility(View.GONE);
                    BleScanner.stopScan(this);
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intent);
                    break;

                case CODE_BLE_INNER_ERROR:
                    Toast.makeText(ScanActivity.this, R.string.scan_failed_ble_inner_error, Toast.LENGTH_SHORT).show();
                    llSearchProgress.setVisibility(View.GONE);
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

        llSearchProgress = findViewById(R.id.ll_search_progress);

        // 初始化扫描设备列表
        rvDevice = findViewById(R.id.rv_device);
        rvDevice.setLayoutManager(new LinearLayoutManager(this));
        rvDevice.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        devAdapter = new ScanAdapter(scannedDevInfos, registeredDevAddrs, this);
        rvDevice.setAdapter(devAdapter);
        rvDevice.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //用来标记是否正在向下滑动
            private boolean isSlidingDownward = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                // 当不滑动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取第一个完全显示的itemPosition
                    int firstItemPosition = manager.findFirstCompletelyVisibleItemPosition();

                    ViseLog.e(firstItemPosition + " " + isSlidingDownward);

                    // 判断是否滑动到了第一个item，并且是向下滑动
                    if (firstItemPosition < 0 || (firstItemPosition == 0 && isSlidingDownward)) {
                        //重新扫描
                        startScan();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 小于0表示向下滑动
                isSlidingDownward = dy <= 0;
            }
        });

        startScan();
    }

    // 开始扫描
    private void startScan() {
        if(isScanning) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            LocationManager alm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            if (!alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)){
                Toast.makeText(this, "添加新设备需要您先开启位置服务。", Toast.LENGTH_SHORT).show();
                Intent openPosIntent = new Intent();
                openPosIntent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                openPosIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(openPosIntent, RC_OPEN_POSITION_FUNCTION);
                return;
            }
        }

        mHandle.removeCallbacksAndMessages(null);

        scannedDevInfos.clear();
        devAdapter.notifyDataSetChanged();
        BleScanner.stopScan(bleScanCallback);
        llSearchProgress.setVisibility(View.VISIBLE);
        BleScanner.startScan(SCAN_FILTER, bleScanCallback);
        isScanning = true;

        mHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                llSearchProgress.setVisibility(View.GONE);
                BleScanner.stopScan(bleScanCallback);
                isScanning = false;
            }
        }, SCAN_DURATION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;

            case R.id.item_refresh:
                startScan();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_REGISTER_DEVICE: // 登记设备返回
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;

            case RC_OPEN_POSITION_FUNCTION: // 开启GPS位置信息返回
                if (resultCode == RESULT_OK) {
                    startScan();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandle.removeCallbacksAndMessages(null);

        llSearchProgress.setVisibility(View.GONE);

        BleScanner.stopScan(bleScanCallback);
    }

    public void registerDevice(final BleDeviceDetailInfo detailInfo) {
        AdRecord serviceUUID = detailInfo.getAdRecordStore().getRecord(AdRecord.BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_MORE_AVAILABLE);
        if(serviceUUID != null) {
            byte[] uuidBytes = new byte[]{serviceUUID.getData()[1], serviceUUID.getData()[0]};
            String uuidShortString = HexUtil.encodeHexStr(uuidBytes);
            DeviceCommonInfo registerInfo = new BleDeviceCommonInfo(detailInfo.getAddress(), uuidShortString);
            Intent intent = new Intent(ScanActivity.this, DeviceInfoActivity.class);
            intent.putExtra(DEVICE_INFO, registerInfo);
            startActivityForResult(intent, RC_REGISTER_DEVICE);
        }

        // 先停止扫描
        BleScanner.stopScan(bleScanCallback);
        llSearchProgress.setVisibility(View.GONE);
        isScanning = false;
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
