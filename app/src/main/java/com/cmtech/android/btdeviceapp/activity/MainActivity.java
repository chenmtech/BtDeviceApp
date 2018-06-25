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
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;
import com.cmtech.android.btdeviceapp.model.BLEDeviceFragment;
import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;
import com.cmtech.android.btdeviceapp.model.BLEDevicePersistantInfo;
import com.cmtech.android.btdeviceapp.model.MainController;
import com.cmtech.android.btdeviceapp.model.MainTabFragmentManager;

import java.io.Serializable;
import java.util.List;

/**
 *  MainActivity: 主界面
 *  Created by bme on 2018/2/19.
 */
public class MainActivity extends AppCompatActivity{

    // 显示设备列表的Adapter和RecyclerView
    private BLEDeviceListAdapter deviceListAdapter;
    private RecyclerView deviceListRecycView;

    // 添加设备按钮
    private Button btnAdd;

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
    private MainController mainController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_menu_white_18dp);
        }

        mainController = new MainController(this);
        mainController.initialize();

        // 设置设备信息View
        deviceListRecycView = (RecyclerView)findViewById(R.id.rvDevices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        deviceListRecycView.setLayoutManager(layoutManager);
        deviceListRecycView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        deviceListAdapter = new BLEDeviceListAdapter(mainController.getIncludedDeviceList(), this);
        deviceListRecycView.setAdapter(deviceListAdapter);

        btnAdd = (Button)findViewById(R.id.device_add_btn);

        // 添加设备
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainController.startScanNewDevice();
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

        TabLayout tabLayout = findViewById(R.id.main_tab_layout);

        // 创建Fragment管理器
        fragmentManager = new MainTabFragmentManager(this, tabLayout, R.id.main_fragment_layout);

        updateMainLayoutVisibility();
    }

    public void startScanActivity(List<String> deviceMacList) {
        // 启动扫描Activity
        Intent intent = new Intent(MainActivity.this, ScanDeviceActivity.class);
        intent.putExtra("device_list", (Serializable) deviceMacList);

        startActivityForResult(intent, 1);
    }

    // 连接一个BLE设备：创建控制器，创建Fragment，并自动连接
    public void connectBLEDevice(BLEDeviceModel device) {
        mainController.connectBLEDevice(device);
    }

    // 从数据库中删除设备
    public void deleteBLEDevice(final BLEDeviceModel device) {
        mainController.deleteBLEDevice(device);
    }

    public void closeFragment(BLEDeviceFragment fragment) {
        mainController.closeFragment(fragment);

    }

    // 将一个Fragment添加到管理器中，并显示
    public void addFragmentToManager(BLEDeviceModel device, BLEDeviceFragment fragment) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        // 添加设备的Fragment到管理器
        fragmentManager.addFragment(device, fragment);
        updateMainLayoutVisibility();
    }

    // 显示一个Fragment
    public void showDeviceFragment(BLEDeviceFragment fragment) {
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

                    BLEDevicePersistantInfo persistantInfo = new BLEDevicePersistantInfo();
                    persistantInfo.setNickName(nickName);
                    persistantInfo.setMacAddress(macAddress);
                    persistantInfo.setUuidString(deviceUuid);
                    persistantInfo.setImagePath(imagePath);
                    persistantInfo.setAutoConnected(isAutoConnect);

                    mainController.addNewScanedDevice(persistantInfo);
                }
                break;
        }
    }

    // 登记设备的观察者
    public void registerDeviceObserver(BLEDeviceModel device) {
        device.registerDeviceObserver(deviceListAdapter);
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

        mainController.closeAllFragment();

        MyApplication.getViseBle().disconnect();
        MyApplication.getViseBle().clear();
        //android.os.Process.killProcess(android.os.Process.myPid());
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
    public BLEDeviceController getController(BLEDeviceFragment fragment) {
        return mainController.getController(fragment);
    }

}
