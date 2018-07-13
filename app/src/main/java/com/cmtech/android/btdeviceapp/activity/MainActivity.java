package com.cmtech.android.btdeviceapp.activity;

import android.content.Intent;
import android.graphics.Color;
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
import android.view.Menu;
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
import com.cmtech.android.btdeviceapp.adapter.BleDeviceListAdapter;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceConnectStateObserver;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceControllerInterface;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;
import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;
import com.cmtech.android.btdeviceapp.model.MainController;
import com.cmtech.android.btdeviceapp.model.MainTabFragmentManager;

import java.io.Serializable;
import java.util.List;

import static com.cmtech.android.btdeviceapp.model.BleDeviceConnectState.CONNECT_SUCCESS;

/**
 *  MainActivity: 主界面
 *  Created by bme on 2018/2/19.
 */
public class MainActivity extends AppCompatActivity implements IBleDeviceConnectStateObserver {

    // 显示设备列表的Adapter和RecyclerView
    private BleDeviceListAdapter deviceListAdapter;
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

    private MenuItem menuConnect;
    private MenuItem menuClose;

    private IBleDeviceInterface modifyDevice;

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
        deviceListAdapter = new BleDeviceListAdapter(mainController.getAddedDeviceList(), this);
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
        //tab可滚动
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        //tab的字体选择器,默认黑色,选择时红色
        tabLayout.setTabTextColors(Color.BLACK, Color.BLUE);
        //tab的下划线颜色,默认是粉红色
        tabLayout.setSelectedTabIndicatorColor(Color.BLUE);
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
    public void openDevice(IBleDeviceInterface device) {
        mainController.openDevice(device);
    }

    // 连接设备
    public void connectDevice(IBleDeviceInterface device) {
        mainController.connectDevice(device);
    }

    // 断开连接
    public void disconnectDevice(IBleDeviceInterface device) {
        mainController.disconnectDevice(device);
    }

    // 关闭设备
    public void closeDevice(IBleDeviceInterface device) {
        mainController.closeDevice(device);
    }

    // 删除已添加设备
    public void deleteIncludedDevice(final IBleDeviceInterface device) {
        mainController.deleteIncludedDevice(device);
    }

    // 修改设备信息 
    public void modifyDeviceInfo(final IBleDeviceInterface device) {
        modifyDevice = device;
        Intent intent = new Intent(this, ConfigureDeviceActivity.class);
        intent.putExtra("device_nickname", device.getNickName());
        intent.putExtra("device_macaddress", device.getMacAddress());
        intent.putExtra("device_uuid", device.getUuidString());
        intent.putExtra("device_imagepath", device.getImagePath());
        intent.putExtra("device_isautoconnect", device.isAutoConnected());

        startActivityForResult(intent, 2);
    }

    // 启动扫描设备Activity
    public void startScanActivity(List<String> includedDeviceMacList) {
        Intent intent = new Intent(MainActivity.this, ScanDeviceActivity.class);
        intent.putExtra("device_list", (Serializable) includedDeviceMacList);

        startActivityForResult(intent, 1);
    }

    // 添加设备及其Fragment，并显示Fragment
    public void addFragment(IBleDeviceInterface device, BleDeviceFragment fragment) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        // 添加设备的Fragment到管理器
        fragmentManager.addFragment(device, fragment);
        updateMainLayoutVisibility();
    }

    // 显示一个Fragment
    public void showFragment(BleDeviceFragment fragment) {
        openDrawer(false);
        fragmentManager.showDeviceFragment(fragment);
    }

    // 删除指定的Fragment
    public void deleteFragment(final BleDeviceFragment fragment) {
        fragmentManager.deleteFragment(fragment);
        updateMainLayoutVisibility();
    }


    private void updateMainLayoutVisibility() {
        if(fragmentManager.size() == 0) {
            mWelcomeLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.INVISIBLE);
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitleTextColor(Color.BLACK);
            setTitle("陈天乐，你好！");

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

                    BleDeviceBasicInfo basicInfo = new BleDeviceBasicInfo();
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
            case 2:
                if ( resultCode == RESULT_OK) {
                    String deviceNickname = data.getStringExtra("device_nickname");
                    String deviceUuid = data.getStringExtra("device_uuid");
                    String imagePath = data.getStringExtra("device_imagepath");
                    Boolean isAutoConnect = data.getBooleanExtra("device_isautoconnect", false);
                    if(modifyDevice == null) return;
                    BleDeviceBasicInfo basicInfo = modifyDevice.getBasicInfo();
                    basicInfo.setNickName(deviceNickname);
                    basicInfo.setImagePath(imagePath);
                    basicInfo.setAutoConnected(isAutoConnect);
                    basicInfo.save();
                    deviceListAdapter.notifyDataSetChanged();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainactivity_menu, menu);
        menuConnect = menu.findItem(R.id.toolbar_connectswitch);
        menuClose = menu.findItem(R.id.toolbar_close);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BleDeviceFragment fragment;
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer(true);
                break;

            case R.id.toolbar_close:
                fragment = (BleDeviceFragment)fragmentManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.disconnectDevice();
                    fragment.closeDevice();
                } else {
                    finish();
                }
                break;

            case R.id.toolbar_connectswitch:
                fragment = (BleDeviceFragment)fragmentManager.getCurrentFragment();
                if(fragment != null) {
                    BleDevice device = (BleDevice) fragment.getDevice();
                    if(device.canDisconnect())
                        fragment.disconnectDevice();
                    else if(device.canConnect())
                        fragment.connectDevice();
                }
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
    public IBleDeviceControllerInterface getController(BleDeviceFragment fragment) {
        return mainController.getController(fragment);
    }

    // IBLEDeviceConnectStateObserver接口函数，更新设备连接状态
    @Override
    public void updateConnectState(BleDevice device) {
        // 更新设备列表
        updateDeviceListAdapter();

        BleDeviceFragment deviceFrag = mainController.getFragmentForDevice(device);
        BleDeviceFragment currentFrag = (BleDeviceFragment)fragmentManager.getCurrentFragment();

        // 更新设备的Fragment
        if(deviceFrag != null) deviceFrag.updateConnectState(device);

        // 更新Activity的ToolBar
        if(currentFrag != null && deviceFrag == currentFrag) {
            updateToolBar(currentFrag);
        }
    }

    // 更新设备列表
    public void updateDeviceListAdapter() {
        if(deviceListAdapter != null) deviceListAdapter.notifyDataSetChanged();
    }


    public void updateToolBar(BleDeviceFragment fragment) {
        BleDevice device = (BleDevice) fragment.getDevice();
        if(device == null) return;

        // 更新工具条菜单
        if(device.canDisconnect()) {
            menuConnect.setEnabled(true);
            menuConnect.setTitle("断开");
        }
        else if(device.canConnect()) {
            menuConnect.setEnabled(true);
            menuConnect.setTitle("连接");
        }
        else {
            menuConnect.setEnabled(false);
        }

        // 更新Activity的工具条图标
        /*Drawable drawable = null;

        String imagePath = device.getImagePath();
        if(imagePath != null && !"".equals(imagePath)) {
            drawable = new BitmapDrawable(MyApplication.getContext().getResources(), device.getImagePath());
        } else {
            drawable = MyApplication.getContext().getResources().getDrawable(BleDeviceType.fromUuid(device.getUuidString()).getImage());
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(drawable);*/

        setTitle(fragment.getDevice().getNickName() + " " + device.getDeviceConnectState().getDescription());
        Toolbar toolbar = findViewById(R.id.toolbar);
        if(device.getDeviceConnectState() == CONNECT_SUCCESS)
            toolbar.setTitleTextColor(Color.RED);
        else
            toolbar.setTitleTextColor(Color.GRAY);
    }
}
