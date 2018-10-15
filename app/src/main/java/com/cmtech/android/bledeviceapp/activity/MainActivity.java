package com.cmtech.android.bledeviceapp.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cmtech.android.bledevice.SupportedDeviceType;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.BleDeviceListAdapter;
import com.cmtech.android.bledeviceapp.model.FragmentAndTabLayoutManager;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceAbstractFactory;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.model.BleDeviceController;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;
import com.cmtech.android.bledevicecore.model.BleDeviceUtil;
import com.cmtech.android.bledevicecore.model.IBleDeviceActivity;
import com.cmtech.android.bledevicecore.model.IBleDeviceStateObserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *  MainActivity: 主界面
 *  Created by bme on 2018/2/19.
 */
public class MainActivity extends AppCompatActivity implements IBleDeviceStateObserver, IBleDeviceActivity {
    private final static int REQUESTCODE_REGISTERDEVICE = 1;     // 登记设备返回码
    private final static int REQUESTCODE_MODIFYDEVICE = 2;       // 修改设备返回码
    private final static int REQUESTCODE_ENABLEBLUETOOTH = 3;    // 使能蓝牙

    // 已登记的设备列表
    private final List<BleDevice> registeredDeviceList = new ArrayList<>();

    // 已打开的设备控制器列表
    private final List<BleDeviceController> openedControllerList = new LinkedList<>();

    // 显示已登记设备列表的Adapter和RecyclerView
    private BleDeviceListAdapter deviceListAdapter;
    private RecyclerView deviceListRecycView;

    // 登记设备按钮
    //private Button btnRegister;

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

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 创建ToolBar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置设备信息RecycleView
        deviceListRecycView = findViewById(R.id.rvDevices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        deviceListRecycView.setLayoutManager(layoutManager);
        deviceListRecycView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        deviceListAdapter = new BleDeviceListAdapter(registeredDeviceList, this);
        deviceListRecycView.setAdapter(deviceListAdapter);


        // 导航菜单设置
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setCheckedItem(R.id.nav_registerdevice);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_registerdevice:
                        registerNewDevice();
                        return true;
                    case R.id.nav_exit:
                        finish();
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
        fragmentManager = new FragmentAndTabLayoutManager(getSupportFragmentManager(), tabLayout, R.id.main_fragment_layout);
        fragmentManager.setOnFragmentChangedListener(new FragmentAndTabLayoutManager.OnFragmentChangedListener() {
            @Override
            public void onFragmentchanged() {
                updateToolBar(((BleDeviceFragment) fragmentManager.getCurrentFragment()).getDevice());
            }
        });

        // 更新主界面
        updateMainLayoutVisibility();

        // 检查蓝牙权限，并使能蓝牙
        checkBluetoothPermissionAndEnableBT();

        // 初始化设备
        initializeBleDevice();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // 同意获得所需权限
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableBluetooth();
                } else {
                    // 不同意获得蓝牙权限
                    Toast.makeText(this, "没有蓝牙权限，程序无法运行", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUESTCODE_REGISTERDEVICE:
                // 登记设备返回
                if(resultCode == RESULT_OK) {
                    BleDeviceBasicInfo basicInfo = (BleDeviceBasicInfo) data.getSerializableExtra("devicebasicinfo");
                    // 用基本信息创建BleDevice
                    if(createBleDeviceUsingBasicInfo(basicInfo)) {
                        // 创建成功后，将设备基本信息保存到数据库中
                        basicInfo.save();
                    }
                }
                break;
            case REQUESTCODE_MODIFYDEVICE:
                // 修改设备信息返回
                if ( resultCode == RESULT_OK) {
                    BleDeviceBasicInfo basicInfo = (BleDeviceBasicInfo) data.getSerializableExtra("devicebasicinfo");
                    String macAddress = basicInfo.getMacAddress();
                    BleDevice device = null;
                    for(BleDevice ele : registeredDeviceList) {
                        if(macAddress.equalsIgnoreCase(ele.getMacAddress())) {
                            device = ele;
                            break;
                        }
                    }
                    if(device != null) {
                        basicInfo.save();
                        device.setBasicInfo(basicInfo);
                        deviceListAdapter.notifyDataSetChanged();
                    }
                }
                break;
            case REQUESTCODE_ENABLEBLUETOOTH:
                if (resultCode == RESULT_OK) {
                    enableBluetooth();
                } else if (resultCode == RESULT_CANCELED) { // 不同意
                    Toast.makeText(this, "蓝牙未打开，程序无法运行", Toast.LENGTH_SHORT).show();
                    finish();
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

            case R.id.toolbar_connectswitch:
                fragment = (BleDeviceFragment)fragmentManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.switchState();
                }
                break;

            case R.id.toolbar_close:
                fragment = (BleDeviceFragment)fragmentManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.closeDevice();
                } else {
                    finish();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for(BleDevice device : registeredDeviceList) {
            if(device != null)
                device.removeDeviceStateObserver(this);
        }

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

    // IBleDeviceStateObserver接口函数，更新设备状态
    @Override
    public void updateDeviceState(final BleDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 更新设备列表Adapter
                updateDeviceListAdapter();

                // 更新设备的Fragment
                BleDeviceFragment deviceFrag = getFragment(device);
                if(deviceFrag != null) deviceFrag.updateDeviceState(device);

                // 更新Activity的ToolBar
                BleDeviceFragment currentFrag = (BleDeviceFragment)fragmentManager.getCurrentFragment();
                if(currentFrag != null && deviceFrag == currentFrag) {
                    updateToolBar(currentFrag.getDevice());
                }
            }
        });

    }

    // 关闭设备
    @Override
    public void closeDevice(BleDeviceFragment fragment) {
        if(fragment == null) return;

        BleDeviceController controller = getController(fragment);
        if(controller == null) return;

        openedControllerList.remove(controller);
        deleteFragment(fragment);
    }

    // 从deviceControllerList中寻找Fragment对应的控制器
    @Override
    public BleDeviceController getController(BleDeviceFragment fragment) {
        for(BleDeviceController controller : openedControllerList) {
            if(controller.getFragment().equals(fragment)) {
                return controller;
            }
        }
        return null;
    }


    ////////////////////////////////////////////////////////////

    // 启动一个BLE设备：为设备创建控制器和Fragment，并自动连接
    public void openDevice(BleDevice device) {
        if(device == null) return;

        BleDeviceFragment fragment = getFragment(device);
        if(fragment != null) {
            // 已经打开了，只要显示Fragment，并开始连接
            showFragment(fragment);
            fragment.openDevice();
        } else {
            BleDeviceAbstractFactory factory = BleDeviceAbstractFactory.getBLEDeviceFactory(device);
            if(factory == null) return;

            BleDeviceController deviceController = factory.createController(device);
            openedControllerList.add(deviceController);
            openFragment(deviceController.getFragment(), device.getImagePath(), device.getNickName());
        }
    }


    // 删除一个已登记设备
    public void deleteDeviceFromRegisteredDeviceList(final BleDevice device) {
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

    // 修改设备基本信息 
    public void modifyDeviceBasicInfo(final BleDevice device) {
        Intent intent = new Intent(this, DeviceBasicInfoActivity.class);
        intent.putExtra("devicebasicinfo", device.getBasicInfo());

        startActivityForResult(intent, REQUESTCODE_MODIFYDEVICE);
    }

    // 更新主Layout的可视性
    private void updateMainLayoutVisibility() {
        if(fragmentManager.size() == 0) {
            mWelcomeLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.INVISIBLE);

            toolbar.setTitleTextColor(Color.BLACK);
            setTitle("物联网蓝牙终端App");
            toolbar.setLogo(null);
            if(menuConnect != null) menuConnect.setIcon(getResources().getDrawable(R.mipmap.ic_disconnect_24px));

        } else {
            mWelcomeLayout.setVisibility(View.INVISIBLE);
            mMainLayout.setVisibility(View.VISIBLE);
        }
    }

    // 从数据库中获取已登记的设备基本信息列表
    private void initializeBleDevice() {
        // 从数据库获取设备信息，并构造相应的BLEDevice
        List<BleDeviceBasicInfo> basicInfoList = BleDeviceBasicInfo.findAllFromPreference();
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
        BleDevice device = factory.createBleDevice(basicInfo);

        if(device != null) {
            // 将设备添加到设备列表
            registeredDeviceList.add(device);
            // 添加Activity作为设备状态的观察者
            device.registerDeviceStateObserver(this);
            // 通知观察者
            device.notifyDeviceStateObservers();
        }
        return true;
    }


    // 打开或关闭侧滑菜单
    private void openDrawer(boolean open) {
        if(open) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    // 产生已登记的设备Mac地址字符串列表
    private List<String> getRegisteredDeviceMacAddressList() {
        List<String> deviceMacList = new ArrayList<>();
        for(BleDevice device : registeredDeviceList) {
            deviceMacList.add(device.getMacAddress());
        }
        return deviceMacList;
    }

    // 登记新设备
    private void registerNewDevice() {
        startScanDevice();
    }

    // 开始扫描设备
    private void startScanDevice() {
        List<String> deviceMacList = getRegisteredDeviceMacAddressList();
        startScanActivity(deviceMacList);
    }

    // 启动扫描设备Activity
    private void startScanActivity(List<String> registeredDeviceMacList) {
        Intent intent = new Intent(MainActivity.this, ScanDeviceActivity.class);
        intent.putExtra("registered_device_list", (Serializable) registeredDeviceMacList);

        startActivityForResult(intent, REQUESTCODE_REGISTERDEVICE);

    }

    // 从deviceControllerList中寻找Fragment对应的控制器
    private BleDeviceController getController(BleDevice device) {
        for(BleDeviceController controller : openedControllerList) {
            if(device.equals(controller.getDevice())) {
                return controller;
            }
        }
        return null;
    }

    // 获取设备对应的Fragment
    private BleDeviceFragment getFragment(BleDevice device) {
        BleDeviceController controller = getController(device);
        return (controller != null) ? controller.getFragment() : null;
    }

    // 打开Fragment：将Fragment加入Manager，并显示
    private void openFragment(BleDeviceFragment fragment, String tabImagePath, String tabText) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        // 添加设备的Fragment到管理器
        fragmentManager.addFragment(fragment, tabImagePath, tabText);
        updateMainLayoutVisibility();
    }

    // 显示Fragment
    private void showFragment(BleDeviceFragment fragment) {
        openDrawer(false);
        fragmentManager.showFragment(fragment);
    }

    // 删除Fragment
    private void deleteFragment(BleDeviceFragment fragment) {
        fragmentManager.deleteFragment(fragment);
        updateMainLayoutVisibility();
    }

    // 更新设备列表
    private void updateDeviceListAdapter() {
        if(deviceListAdapter != null) deviceListAdapter.notifyDataSetChanged();
    }

    // 更新工具条
    private void updateToolBar(BleDevice device) {
        if(device == null) return;

        // 更新连接菜单
        if(device.canDisconnect()) {
            menuConnect.setEnabled(true);
            menuConnect.setTitle("断开");
            menuConnect.setIcon(getResources().getDrawable(R.mipmap.ic_connect_24px));
        }
        else if(device.canConnect()) {
            menuConnect.setEnabled(true);
            menuConnect.setTitle("连接");
            menuConnect.setIcon(getResources().getDrawable(R.mipmap.ic_disconnect_24px));
        }
        else {
            menuConnect.setEnabled(false);
            //menuConnect.setIcon(getResources().getDrawable(R.mipmap.ic_connecting_24px));
            menuConnect.setIcon(getResources().getDrawable(R.drawable.connectingdrawable));
            AnimationDrawable connectingDrawable = (AnimationDrawable) menuConnect.getIcon();
            if(!connectingDrawable.isRunning())
                connectingDrawable.start();
        }

        // 更新关闭菜单
        menuClose.setEnabled(device.canClose());

        // 更新工具条图标
        Drawable drawable;
        String imagePath = device.getImagePath();
        if(imagePath != null && !"".equals(imagePath)) {
            drawable = new BitmapDrawable(MyApplication.getContext().getResources(), device.getImagePath());
        } else {
            drawable = MyApplication.getContext().getResources().getDrawable(SupportedDeviceType.getDeviceTypeFromUuid(device.getUuidString()).getDefaultImage());
        }
        toolbar.setLogo(drawable);

        // 更新工具条title
        toolbar.setTitle(device.getStateDescription());

    }

    /**
     * 检查蓝牙权限,并使能蓝牙
     */
    private void checkBluetoothPermissionAndEnableBT() {
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

    // 使能蓝牙
    private void enableBluetooth() {
        if (!BleDeviceUtil.isBleEnable(this)) {
            BleDeviceUtil.enableBluetooth(this, REQUESTCODE_ENABLEBLUETOOTH);
        }
    }
}
