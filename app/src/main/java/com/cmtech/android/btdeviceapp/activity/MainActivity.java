package com.cmtech.android.btdeviceapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.adapter.BLEDeviceListAdapter;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceConnectStateObserver;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceControllerInterface;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BLEDeviceFragment;
import com.cmtech.android.btdeviceapp.model.BLEDevice;
import com.cmtech.android.btdeviceapp.model.BLEDeviceBasicInfo;
import com.cmtech.android.btdeviceapp.model.MainController;
import com.cmtech.android.btdeviceapp.model.MainTabFragmentManager;

import java.io.Serializable;
import java.util.List;

/**
 *  MainActivity: 主界面
 *  Created by bme on 2018/2/19.
 */
public class MainActivity extends AppCompatActivity implements IBLEDeviceConnectStateObserver{

    // 显示设备列表的Adapter和RecyclerView
    private BLEDeviceListAdapter deviceListAdapter;
    private RecyclerView deviceListRecycView;

    // 添加设备按钮
    private Button btnScan;

    // 侧滑界面
    private DrawerLayout mDrawerLayout;

    // 欢迎界面
    private FrameLayout mWelcomeLayout;

    // 包含设备Fragment和Tablayout的界面
    private LinearLayout mMainLayout;

    // 欢迎界面中的图像
    private ImageView welcomeImage;

    // 主界面的TabLayout和Fragment管理器
    private MainTabFragmentManager fragmentManager;

    // 所有设备的主控制器
    private final MainController mainController = new MainController(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_menu_white_18dp);
        }

        // 设置设备信息RecycleView
        deviceListRecycView = (RecyclerView)findViewById(R.id.rvDevices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        deviceListRecycView.setLayoutManager(layoutManager);
        deviceListRecycView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        deviceListAdapter = new BLEDeviceListAdapter(mainController.getAddedDeviceList(), this);
        deviceListRecycView.setAdapter(deviceListAdapter);

        // 添加(扫描)设备
        btnScan = (Button)findViewById(R.id.device_scan_btn);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanDevice();
            }
        });

        // 导航菜单设置
        NavigationView navView = (NavigationView)findViewById(R.id.nav_view);
        navView.setCheckedItem(R.id.nav_userinfo);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_userinfo:
                        Toast.makeText(MainActivity.this, "you click userinfo", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_aboutus:
                        Toast.makeText(MainActivity.this, "you click aboutus", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mWelcomeLayout = findViewById(R.id.welecome_layout);
        mMainLayout = findViewById(R.id.main_layout);

        welcomeImage = findViewById(R.id.welcome_image);
        Glide.with(this).load(R.drawable.welcome_image).into(welcomeImage);

        // 创建Fragment管理器
        TabLayout tabLayout = findViewById(R.id.main_tab_layout);
        fragmentManager = new MainTabFragmentManager(this, tabLayout, R.id.main_fragment_layout);

        // 更新主界面
        updateMainLayoutVisibility();

        // 初始化主控制器
        mainController.initialize();
    }

    // 开始扫描设备
    public void scanDevice() {
        mainController.scanDevice();
    }

    // 打开一个BLE设备：为设备创建控制器和Fragment，并自动连接
    public void openDevice(IBLEDeviceInterface device) {
        mainController.openDevice(device);
    }

    // 连接设备
    public void connectDevice(IBLEDeviceInterface device) {
        mainController.connectDevice(device);
    }

    // 断开连接
    public void disconnectDevice(IBLEDeviceInterface device) {
        mainController.disconnectDevice(device);
    }

    // 关闭设备
    public void closeDevice(IBLEDeviceInterface device) {
        mainController.closeDevice(device);
    }

    // 删除已添加设备
    public void deleteIncludedDevice(final IBLEDeviceInterface device) {
        mainController.deleteIncludedDevice(device);
    }

    // 启动扫描设备Activity
    public void startScanActivity(List<String> includedDeviceMacList) {
        Intent intent = new Intent(MainActivity.this, ScanDeviceActivity.class);
        intent.putExtra("device_list", (Serializable) includedDeviceMacList);

        startActivityForResult(intent, 1);
    }

    // 添加设备及其Fragment，并显示Fragment
    public void addFragment(IBLEDeviceInterface device, BLEDeviceFragment fragment) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        // 添加设备的Fragment到管理器
        fragmentManager.addFragment(device, fragment);
        updateMainLayoutVisibility();
    }

    // 显示一个Fragment
    public void showFragment(BLEDeviceFragment fragment) {
        openDrawer(false);
        fragmentManager.showDeviceFragment(fragment);
    }

    // 删除指定的Fragment
    public void deleteFragment(final BLEDeviceFragment fragment) {
        fragmentManager.deleteFragment(fragment);
        updateMainLayoutVisibility();
    }


    private void updateMainLayoutVisibility() {
        if(fragmentManager.size() == 0) {
            mWelcomeLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.INVISIBLE);
            setTitle(R.string.app_name);
        } else {
            mWelcomeLayout.setVisibility(View.INVISIBLE);
            mMainLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                // 添加设备返回
                if(resultCode == RESULT_OK) {
                    String nickName = data.getStringExtra("device_nickname");
                    String macAddress = data.getStringExtra("device_macaddress");
                    String deviceUuid = data.getStringExtra("device_uuid");
                    String imagePath = data.getStringExtra("device_imagepath");
                    boolean isAutoConnect = data.getBooleanExtra("device_isautoconnect", false);

                    BLEDeviceBasicInfo basicInfo = new BLEDeviceBasicInfo();
                    basicInfo.setNickName(nickName);
                    basicInfo.setMacAddress(macAddress);
                    basicInfo.setUuidString(deviceUuid);
                    basicInfo.setImagePath(imagePath);
                    basicInfo.setAutoConnected(isAutoConnect);

                    // 用基本信息创建BleDevice
                    if(mainController.createBleDeviceUsingBasicInfo(basicInfo)) {
                        // 创建成功后，将设备基本信息保存到数据库
                        basicInfo.save();
                    }
                }
                break;
        }
    }

    // 打开或关闭侧滑菜单
    private void openDrawer(boolean open) {
        if(open) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer(true);
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MyApplication.getViseBle().disconnect();
        MyApplication.getViseBle().clear();
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    @Override
    public void onBackPressed() {
        // 如果drawerLayout打开，则关闭drawerLayout；否则退出
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
            openDrawer(false);
        else
            finish();
    }

    // 从deviceControllerList中寻找Fragment对应的控制器,在Fragment的OnAttach()中会调用
    public IBLEDeviceControllerInterface getController(BLEDeviceFragment fragment) {
        return mainController.getController(fragment);
    }

    // IBLEDeviceConnectStateObserver接口函数，更新设备连接状态
    @Override
    public void updateConnectState(BLEDevice device) {
        updateDeviceListAdapter();
    }

    // 更新设备列表
    public void updateDeviceListAdapter() {
        if(deviceListAdapter != null) deviceListAdapter.notifyDataSetChanged();
    }
}
