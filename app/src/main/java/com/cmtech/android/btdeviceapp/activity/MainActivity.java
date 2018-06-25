package com.cmtech.android.btdeviceapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.cmtech.android.btdeviceapp.model.BLEDeviceFragment;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.adapter.BLEDeviceListAdapter;
import com.cmtech.android.btdeviceapp.interfa.BLEDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;
import com.cmtech.android.btdeviceapp.model.BLEDevicePersistantInfo;
import com.cmtech.android.btdeviceapp.model.MainTabFragmentManager;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceObserver;
import com.cmtech.android.btdeviceapp.model.BLEDeviceModel;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *  MainActivity: 主界面
 *  Created by bme on 2018/2/19.
 */
public class MainActivity extends AppCompatActivity{

    // 设备列表
    private List<BLEDeviceModel> deviceList = new ArrayList<>();

    // 设备控制器列表
    private List<BLEDeviceController> deviceControllerList = new LinkedList<>();

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

        // 从数据库获取设备信息，并构造相应的BLEDevice
        List<BLEDevicePersistantInfo> persistantInfoList = DataSupport.findAll(BLEDevicePersistantInfo.class);
        if(persistantInfoList != null && !persistantInfoList.isEmpty()) {
            for(BLEDevicePersistantInfo info : persistantInfoList) {
                BLEDeviceAbstractFactory factory = BLEDeviceAbstractFactory.getBLEDeviceFactory(info);
                deviceList.add(factory.createDevice(info));
            }
        }

        // 设置设备信息View
        deviceListRecycView = (RecyclerView)findViewById(R.id.rvDevices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        deviceListRecycView.setLayoutManager(layoutManager);
        deviceListRecycView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        deviceListAdapter = new BLEDeviceListAdapter(deviceList, this);
        deviceListRecycView.setAdapter(deviceListAdapter);

        btnAdd = (Button)findViewById(R.id.device_add_btn);

        // 添加设备
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 产生设备Mac地址字符串列表，防止多次添加同一个设备
                List<String> deviceMacList = new ArrayList<>();
                for(BLEDeviceModel device : deviceList) {
                    deviceMacList.add(device.getMacAddress());
                }

                // 启动扫描Activity
                Intent intent = new Intent(MainActivity.this, ScanDeviceActivity.class);
                intent.putExtra("device_list", (Serializable) deviceMacList);

                startActivityForResult(intent, 1);
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

        setMainLayoutVisibility();

    }

    // 打开设备
    public void openDevice(BLEDeviceModel device) {
        if(device == null) return;

        boolean isContained = false;
        for(BLEDeviceController controller : deviceControllerList) {
            if(device.equals(controller.getDevice())) {
                isContained = true;
            }
        }
        if(!isContained) {
            BLEDeviceAbstractFactory factory = BLEDeviceAbstractFactory.getBLEDeviceFactory(device);
            BLEDeviceController deviceController = factory.createController(device, this);
            deviceControllerList.add(deviceController);
        }
    }

    public void addFragmentToManager(BLEDeviceController controller) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        // 添加设备的Fragment到管理器
        fragmentManager.addDeviceFragment(controller);
        setMainLayoutVisibility();
    }

    // 删除设备
    public void deleteDevice(final BLEDeviceModel device) {
        if(device == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("确定删除该设备吗？");
        builder.setMessage(device.getMacAddress()+'\n'+device.getNickName());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                device.delete();
                deviceList.remove(device);
                device.notifyDeviceObservers(IBLEDeviceObserver.TYPE_DELETED);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }


    private void setMainLayoutVisibility() {
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

                    // 保存到数据库
                    persistantInfo.save();
                    // 添加到设备列表
                    BLEDeviceModel device = BLEDeviceAbstractFactory.getBLEDeviceFactory(persistantInfo).createDevice(persistantInfo);
                    deviceList.add(device);
                    // 添加deviceAdapter作为观察者
                    device.registerDeviceObserver(deviceListAdapter);
                    // 通知观察者
                    device.notifyDeviceObservers(IBLEDeviceObserver.TYPE_ADDED);
                }
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(deviceList != null && !deviceList.isEmpty()) {
            for(BLEDeviceModel device : deviceList) {
                device.disconnect();
            }
        }

        MyApplication.getViseBle().disconnect();
        MyApplication.getViseBle().clear();
        //android.os.Process.killProcess(android.os.Process.myPid());
    }


    @Override
    public void onBackPressed() {
        // 如果drawerLayout打开，则关闭drawerLayout；否则退出
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else
            finish();
    }


    // 从deviceControllerList中寻找Fragment对应的控制器
    public BLEDeviceController getController(BLEDeviceFragment fragment) {
        for(BLEDeviceController controller : deviceControllerList) {
            if(controller.getFragment().equals(fragment)) {
                return controller;
            }
        }
        return null;
    }

    // 删除指定的Fragment
    public void deleteFragment(final BLEDeviceFragment fragment) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                fragmentManager.deleteFragment(fragment);
                setMainLayoutVisibility();
            }
        });
    }
}
