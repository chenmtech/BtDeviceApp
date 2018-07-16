package com.cmtech.android.btdeviceapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import com.cmtech.android.btdeviceapp.interfa.BleDeviceAbstractFactory;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceConnectStateObserver;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceControllerInterface;
import com.cmtech.android.btdeviceapp.interfa.IBleDeviceInterface;
import com.cmtech.android.btdeviceapp.model.BleDeviceFragment;
import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.cmtech.android.btdeviceapp.model.BleDeviceBasicInfo;
import com.cmtech.android.btdeviceapp.model.FragmentAndTabLayoutManager;

import org.litepal.LitePal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.btdeviceapp.model.BleDeviceConnectState.CONNECT_SUCCESS;

/**
 *  MainActivity: 主界面
 *  Created by bme on 2018/2/19.
 */
public class MainActivity extends AppCompatActivity implements IBleDeviceConnectStateObserver {
    // 已登记的设备列表
    private final List<IBleDeviceInterface> registeredDeviceList = new ArrayList<>();

    // 已打开的设备控制器列表
    private final List<IBleDeviceControllerInterface> openedControllerList = new LinkedList<>();

    // 显示已登记设备列表的Adapter和RecyclerView
    private BleDeviceListAdapter deviceListAdapter;
    private RecyclerView deviceListRecycView;

    // 登记设备按钮
    private Button btnRegister;

    // 侧滑界面
    private DrawerLayout mDrawerLayout;

    // 欢迎界面
    private FrameLayout mWelcomeLayout;

    // 包含设备Fragment和Tablayout的界面
    private LinearLayout mMainLayout;

    // 欢迎界面中的图像
    private ImageView welcomeImage;

    // 主界面的TabLayout和Fragment管理器
    private FragmentAndTabLayoutManager fragmentManager;

    private MenuItem menuConnect;
    private MenuItem menuClose;


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
        deviceListAdapter = new BleDeviceListAdapter(registeredDeviceList, this);
        deviceListRecycView.setAdapter(deviceListAdapter);


        // 登记新设备
        btnRegister = (Button)findViewById(R.id.device_register_btn);
        btnRegister.setOnClickListener(new View.OnClickListener() {
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
        fragmentManager = new FragmentAndTabLayoutManager(getSupportFragmentManager(), tabLayout, R.id.main_fragment_layout);
        fragmentManager.setOnFragmentChangedListener(new FragmentAndTabLayoutManager.OnFragmentChangedListener() {
            @Override
            public void onFragmentchanged() {
                updateToolBar((BleDeviceFragment) fragmentManager.getCurrentFragment());
            }
        });

        // 更新主界面
        updateMainLayoutVisibility();

        // 初始化设备
        initializeBleDevice();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                // 登记设备返回
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
                    if(createBleDeviceUsingBasicInfo(basicInfo)) {
                        // 创建成功后，将设备基本信息保存到数据库
                        basicInfo.save();
                    }
                }
                break;
            case 2:
                // 修改设备信息返回
                if ( resultCode == RESULT_OK) {
                    String deviceNickname = data.getStringExtra("device_nickname");
                    String macAddress = data.getStringExtra("device_macaddress");
                    String deviceUuid = data.getStringExtra("device_uuid");
                    String imagePath = data.getStringExtra("device_imagepath");
                    Boolean isAutoConnect = data.getBooleanExtra("device_isautoconnect", false);
                    IBleDeviceInterface device = null;
                    for(IBleDeviceInterface ele : registeredDeviceList) {
                        if(macAddress.equalsIgnoreCase(ele.getMacAddress())) {
                            device = ele;
                            break;
                        }
                    }
                    if(device != null) {
                        BleDeviceBasicInfo basicInfo = device.getBasicInfo();
                        basicInfo.setNickName(deviceNickname);
                        basicInfo.setMacAddress(macAddress);
                        basicInfo.setUuidString(deviceUuid);
                        basicInfo.setImagePath(imagePath);
                        basicInfo.setAutoConnected(isAutoConnect);
                        basicInfo.save();
                        deviceListAdapter.notifyDataSetChanged();
                    }
                }
                break;
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
                    //fragment.disconnectDevice();
                    //fragment.closeDevice();
                    closeDevice(fragment.getDevice());
                } else {
                    finish();
                }
                break;

            case R.id.toolbar_connectswitch:
                fragment = (BleDeviceFragment)fragmentManager.getCurrentFragment();
                if(fragment != null) {
                    BleDevice device = (BleDevice) fragment.getDevice();
                    if(device != null) {
                        if (device.canDisconnect())
                            fragment.disconnectDevice();
                        else if (device.canConnect())
                            fragment.connectDevice();
                    }
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

    // IBLEDeviceConnectStateObserver接口函数，更新设备连接状态
    @Override
    public void updateConnectState(BleDevice device) {
        // 更新设备列表
        updateDeviceListAdapter();

        BleDeviceFragment deviceFrag = getFragmentForDevice(device);
        BleDeviceFragment currentFrag = (BleDeviceFragment)fragmentManager.getCurrentFragment();

        // 更新设备的Fragment
        if(deviceFrag != null) deviceFrag.updateConnectState(device);

        // 更新Activity的ToolBar
        if(currentFrag != null && deviceFrag == currentFrag) {
            updateToolBar(currentFrag);
        }
    }
    ////////////////////////////////////////////////////////////



    // 从数据库中获取已登记的设备基本信息列表
    private void initializeBleDevice() {
        // 从数据库获取设备信息，并构造相应的BLEDevice
        List<BleDeviceBasicInfo> basicInfoList = LitePal.findAll(BleDeviceBasicInfo.class);
        if(basicInfoList != null && !basicInfoList.isEmpty()) {
            for(BleDeviceBasicInfo basicInfo : basicInfoList) {
                createBleDeviceUsingBasicInfo(basicInfo);
            }
        }
    }

    // 根据设备基本信息创建一个新的设备，并添加到设备列表中
    private boolean createBleDeviceUsingBasicInfo(BleDeviceBasicInfo basicInfo) {
        // 获取相应的抽象工厂
        BleDeviceAbstractFactory factory = BleDeviceAbstractFactory.getBLEDeviceFactory(basicInfo);
        if(factory == null) return false;
        // 用工厂创建BleDevice
        IBleDeviceInterface device = factory.createBleDevice(basicInfo);

        if(device != null) {
            // 将设备添加到设备列表
            registeredDeviceList.add(device);
            // 添加Activity作为设备连接状态的观察者
            device.registerConnectStateObserver(this);
            // 通知观察者
            device.notifyConnectStateObservers();
        }
        return true;
    }

    // 产生已登记的设备Mac地址字符串列表字符串
    private List<String> getRegisteredDeviceMacAddressList() {
        List<String> deviceMacList = new ArrayList<>();
        for(IBleDeviceInterface device : registeredDeviceList) {
            deviceMacList.add(device.getMacAddress());
        }
        return deviceMacList;
    }

    // 开始扫描设备
    private void scanDevice() {
        List<String> deviceMacList = getRegisteredDeviceMacAddressList();
        startScanActivity(deviceMacList);
    }


    // 从deviceControllerList中寻找Fragment对应的控制器
    public IBleDeviceControllerInterface getController(BleDeviceFragment fragment) {
        for(IBleDeviceControllerInterface controller : openedControllerList) {
            if(controller.getFragment().equals(fragment)) {
                return controller;
            }
        }
        return null;
    }

    // 打开一个BLE设备：为设备创建控制器和Fragment，并自动连接
    public void openDevice(IBleDeviceInterface device) {
        if(device == null) return;

        BleDeviceFragment fragment = getFragmentForDevice(device);
        if(fragment != null) {
            // 已经打开了，只要显示Fragment，并开始连接
            showFragment(fragment);
            fragment.connectDevice();
        } else {
            BleDeviceAbstractFactory factory = BleDeviceAbstractFactory.getBLEDeviceFactory(device.getBasicInfo());
            if(factory == null) return;
            IBleDeviceControllerInterface deviceController = factory.createController(device, this);
            openedControllerList.add(deviceController);
            addFragment(deviceController.getFragment(), device.getImagePath(), device.getNickName());
        }
    }

    // 连接设备
    public void connectDevice(IBleDeviceInterface device) {
        if(device == null) return;

        BleDeviceFragment fragment = getFragmentForDevice(device);
        if(fragment != null) {
            fragment.connectDevice();
        }
    }

    // 断开连接
    public void disconnectDevice(IBleDeviceInterface device) {
        if(device == null) return;

        BleDeviceFragment fragment = getFragmentForDevice(device);
        if(fragment != null) {
            fragment.disconnectDevice();
        }
    }

    // 关闭设备
    public void closeDevice(IBleDeviceInterface device) {
        if(device == null) return;

        IBleDeviceControllerInterface controller = getController(device);
        if(controller == null) return;
        BleDeviceFragment fragment = controller.getFragment();
        if(fragment != null) {
            //controller.closeDevice();
            openedControllerList.remove(controller);
            deleteFragment(fragment);
        }
        //MyApplication.getViseBle().clear();
    }

    // 删除已添加设备
    public void deleteRegisteredDevice(final IBleDeviceInterface device) {
        if(device == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定删除该设备吗？");
        builder.setMessage(device.getMacAddress()+'\n'+device.getNickName());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                device.getBasicInfo().delete();
                registeredDeviceList.remove(device);
                updateDeviceListAdapter();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    // 修改已登记设备信息 
    public void modifyRegisteredDeviceInfo(final IBleDeviceInterface device) {
        Intent intent = new Intent(this, DeviceBasicInfoActivity.class);
        intent.putExtra("device_nickname", device.getNickName());
        intent.putExtra("device_macaddress", device.getMacAddress());
        intent.putExtra("device_uuid", device.getUuidString());
        intent.putExtra("device_imagepath", device.getImagePath());
        intent.putExtra("device_isautoconnect", device.isAutoConnected());

        startActivityForResult(intent, 2);
    }

    // 启动扫描设备Activity
    private void startScanActivity(List<String> registeredDeviceMacList) {
        Intent intent = new Intent(MainActivity.this, ScanDeviceActivity.class);
        intent.putExtra("device_list", (Serializable) registeredDeviceMacList);

        startActivityForResult(intent, 1);
    }

    // 从deviceControllerList中寻找Fragment对应的控制器
    private IBleDeviceControllerInterface getController(IBleDeviceInterface device) {
        for(IBleDeviceControllerInterface controller : openedControllerList) {
            if(device.equals(controller.getDevice())) {
                return controller;
            }
        }
        return null;
    }

    // 获取设备对应的Fragment
    private BleDeviceFragment getFragmentForDevice(IBleDeviceInterface device) {
        IBleDeviceControllerInterface controller = getController(device);
        return (controller != null) ? controller.getFragment() : null;
    }

    // 添加设备及其Fragment，并显示Fragment
    private void addFragment(BleDeviceFragment fragment, String tabImagePath, String tabText) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        // 添加设备的Fragment到管理器
        fragmentManager.addFragment(fragment, tabImagePath, tabText);
        updateMainLayoutVisibility();
    }

    // 显示一个Fragment
    private void showFragment(BleDeviceFragment fragment) {
        openDrawer(false);
        fragmentManager.showFragment(fragment);
    }

    // 删除指定的Fragment
    private void deleteFragment(BleDeviceFragment fragment) {
        fragmentManager.deleteFragment(fragment);
        updateMainLayoutVisibility();
    }


    private void updateMainLayoutVisibility() {
        if(fragmentManager.size() == 0) {
            mWelcomeLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.INVISIBLE);
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitleTextColor(Color.BLACK);
            setTitle("物联网蓝牙终端App");

        } else {
            mWelcomeLayout.setVisibility(View.INVISIBLE);
            mMainLayout.setVisibility(View.VISIBLE);
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


    // 更新设备列表
    private void updateDeviceListAdapter() {
        if(deviceListAdapter != null) deviceListAdapter.notifyDataSetChanged();
    }


    private void updateToolBar(BleDeviceFragment fragment) {
        if(fragment == null || fragment.getDevice() == null) return;

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
