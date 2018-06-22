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
import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragmentFactory;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.adapter.MyBluetoothDeviceAdapter;
import com.cmtech.android.btdeviceapp.interfa.IDeviceFragmentObserver;
import com.cmtech.android.btdeviceapp.model.BLEDeviceController;
import com.cmtech.android.btdeviceapp.model.MainTabFragmentManager;
import com.cmtech.android.btdeviceapp.interfa.IMyBluetoothDeviceObserver;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDevice;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *  MainActivity: 主界面，主要数据存放区，需要实现IDeviceFragmentObserver
 *  Created by bme on 2018/2/19.
 */
public class MainActivity extends AppCompatActivity implements IDeviceFragmentObserver {

    // 设备列表
    List<MyBluetoothDevice> deviceList = new ArrayList<>();

    // 显示设备列表的Adapter和RecyclerView
    private MyBluetoothDeviceAdapter deviceAdapter;
    private RecyclerView deviceRecycView;

    private Button btnAdd;

    private DrawerLayout mDrawerLayout;
    private FrameLayout mWelcomeLayout;
    private LinearLayout mMainLayout;

    private ImageView welcomeImage;

    // 主界面的TabLayout和Fragment管理器
    private MainTabFragmentManager fragmentManager;

    private BLEDeviceController deviceController;

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

        // 从数据库获取设备信息
        deviceList = DataSupport.findAll(MyBluetoothDevice.class);

        // 设置设备信息View
        deviceRecycView = (RecyclerView)findViewById(R.id.rvDevices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        deviceRecycView.setLayoutManager(layoutManager);
        deviceRecycView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        deviceAdapter = new MyBluetoothDeviceAdapter(deviceList, this);
        deviceRecycView.setAdapter(deviceAdapter);

        btnAdd = (Button)findViewById(R.id.device_add_btn);

        // 添加设备
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 产生设备Mac地址字符串列表，防止多次添加同一个设备
                List<String> deviceMacList = new ArrayList<>();
                for(MyBluetoothDevice device : deviceList) {
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

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mWelcomeLayout = (FrameLayout)findViewById(R.id.welecome_layout);
        mMainLayout = (LinearLayout)findViewById(R.id.main_layout);

        welcomeImage = (ImageView)findViewById(R.id.welcome_image);
        Glide.with(this).load(R.drawable.welcome_image).into(welcomeImage);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tab_layout);

        // 创建Fragment管理器
        fragmentManager = new MainTabFragmentManager(this, tabLayout, R.id.main_fragment_layout);

        setMainLayoutVisibility();

    }

    // 打开设备
    public void openDevice(MyBluetoothDevice device) {
        if(device == null) return;

        deviceController = new BLEDeviceController(device, this);

    }

    public DeviceFragment createFragmentForDevice(MyBluetoothDevice device) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        // 设备有对应的Fragment，表示曾经连接成功过
        if (device.hasFragment()) {
            fragmentManager.showDeviceFragment(device);
            //device.getFragment().connectDevice();

        } else {
            DeviceFragment fragment = DeviceFragmentFactory.build(device);
            device.setFragment(fragment);

            // 添加设备的Fragment到管理器
            fragmentManager.addDeviceFragment(device);
            setMainLayoutVisibility();
        }
        return device.getFragment();
    }

    // 删除设备
    public void deleteDevice(final MyBluetoothDevice device) {
        if(device == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("确定删除该设备吗？");
        builder.setMessage(device.getMacAddress()+'\n'+device.getNickName());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                device.delete();
                deviceList.remove(device);
                device.notifyDeviceObservers(IMyBluetoothDeviceObserver.TYPE_DELETED);
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

                    MyBluetoothDevice device = new MyBluetoothDevice();
                    device.setNickName(nickName);
                    device.setMacAddress(macAddress);
                    device.setUuidString(deviceUuid);
                    device.setImagePath(imagePath);
                    device.setAutoConnected(isAutoConnect);

                    // 保存到数据库
                    device.save();
                    // 添加到设备列表
                    deviceList.add(device);
                    // 添加deviceAdapter作为观察者
                    device.registerDeviceObserver(deviceAdapter);
                    // 通知观察者
                    device.notifyDeviceObservers(IMyBluetoothDeviceObserver.TYPE_ADDED);
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
        if(deviceList != null && deviceList.size() != 0) {
            for(MyBluetoothDevice device : deviceList) {
                device.disconnect();
            }
        }

        MyApplication.getViseBle().disconnect();
        MyApplication.getViseBle().clear();
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    @Override
    public void onBackPressed() {
        // 如果drawerLayout打开，则关闭drawerLayout；否则退出
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else
            finish();
    }

    ///////////////////IDeviceFragmentObserver接口函数///////////////////////

    // 从deviceList中寻找Fragment对应的设备
    @Override
    public MyBluetoothDevice findDevice(DeviceFragment fragment) {
        for(MyBluetoothDevice device : deviceList) {
            if(device.getFragment() == fragment) {
                return device;
            }
        }
        return null;
    }

    // 从deviceList中寻找Fragment对应的设备
    @Override
    public BLEDeviceController findController(DeviceFragment fragment) {
        return deviceController;
    }

    // 删除指定的Fragment
    @Override
    public void deleteFragment(final DeviceFragment fragment) {
        MyBluetoothDevice device = findDevice(fragment);
        if(device != null)
            device.setFragment(null);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                fragmentManager.deleteFragment(fragment);
                setMainLayoutVisibility();
            }
        });
    }
    ////////////////////////////////////////////////////////////////////////
}
