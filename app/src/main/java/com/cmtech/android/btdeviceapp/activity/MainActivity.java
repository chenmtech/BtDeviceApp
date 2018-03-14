package com.cmtech.android.btdeviceapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragmentFactory;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.adapter.MyBluetoothDeviceAdapter;
import com.cmtech.android.btdeviceapp.interfa.IDeviceFragmentObserver;
import com.cmtech.android.btdeviceapp.model.DeviceState;
import com.cmtech.android.btdeviceapp.interfa.IConnectSuccessCallback;
import com.cmtech.android.btdeviceapp.interfa.IMyBluetoothDeviceObserver;
import com.cmtech.android.btdeviceapp.model.MyBluetoothDevice;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

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


    private Button btnModify;
    private Button btnDelete;
    private Button btnAdd;
    private Button btnConnect;

    private DrawerLayout mDrawerLayout;
    private LinearLayout mWelcomeLayout;
    private LinearLayout mMainLayout;

    private ViewPager viewPager;
    private CommonTabLayout tabLayout;
    private MyPagerAdapter fragAdapter;

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
        deviceAdapter = new MyBluetoothDeviceAdapter(deviceList);
        deviceRecycView.setAdapter(deviceAdapter);

        btnModify = (Button)findViewById(R.id.device_modify_btn);
        btnDelete = (Button)findViewById(R.id.device_delete_btn);
        btnAdd = (Button)findViewById(R.id.device_add_btn);
        btnConnect = (Button)findViewById(R.id.device_connect_btn);

        // 修改设备信息
        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = deviceAdapter.getSelectItem();
                if(index == -1)
                    Toast.makeText(MainActivity.this, "请先选择一个设备", Toast.LENGTH_SHORT).show();
                else
                    modifyDeviceInfo(deviceList.get(index));
            }
        });

        // 删除设备
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = deviceAdapter.getSelectItem();
                if(index == -1)
                    Toast.makeText(MainActivity.this, "请先选择一个设备", Toast.LENGTH_SHORT).show();
                else
                    deleteDevice(deviceList.get(index));
            }
        });

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

        // 连接设备
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = deviceAdapter.getSelectItem();
                if(index == -1)
                    Toast.makeText(MainActivity.this, "请先选择一个设备", Toast.LENGTH_SHORT).show();
                else {
                    final MyBluetoothDevice device = deviceList.get(index);
                    if(device == null) return;

                    // 设备有对应的Fragment，表示曾经连接成功过
                    if (device.getFragment() != null) {
                        device.getFragment().connectDevice();
                        viewPager.setCurrentItem(fragAdapter.getPosition(device.getFragment()));
                        return;
                    }

                    // 设备没有连接过
                    DeviceState state = device.getDeviceState();
                    if (state == DeviceState.CONNECT_SUCCESS) {             // 已经连接
                        Toast.makeText(MainActivity.this, "设备已连接", Toast.LENGTH_SHORT).show();
                    } else if (state == DeviceState.CONNECT_PROCESS) {      // 连接中...
                        Toast.makeText(MainActivity.this, "设备连接中...", Toast.LENGTH_SHORT).show();
                    } else {
                        // 连接设备，连接成功后创建Fragment
                        device.connect(new IConnectSuccessCallback() {
                            @Override
                            public void doAfterConnectSuccess(final MyBluetoothDevice device) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        createFragmentForDevice(device);
                                    }
                                });
                            }
                        });
                    }
                }
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
        mWelcomeLayout = (LinearLayout)findViewById(R.id.welecome_layout);
        mMainLayout = (LinearLayout)findViewById(R.id.main_layout);

        // TabLayout和ViewPager相关设置
        viewPager = (ViewPager) findViewById(R.id.main_vp);
        fragAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragAdapter);
        tabLayout = (CommonTabLayout) findViewById(R.id.main_tab_layout);
        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mWelcomeLayout.setVisibility(View.VISIBLE);

        mMainLayout.setVisibility(View.INVISIBLE);

    }

    // 显示已连接设备
    private void createFragmentForDevice(MyBluetoothDevice device) {
        if(device == null) return;
        mDrawerLayout.closeDrawer(GravityCompat.START);

        // 创建正确的Fragment
        DeviceFragment fragment = DeviceFragmentFactory.build(device);
        device.setFragment(fragment);

        // 更新TabLayout和ViewPager
        updateTabandViewPager();

        // 翻到当前Fragment
        viewPager.setCurrentItem(fragAdapter.getPosition(fragment));
    }



    // 更新TabLayout和ViewPager
    private void updateTabandViewPager() {
        // 获取已创建Fragment的设备的TabEntity、Title和DeviceFragment
        ArrayList<CustomTabEntity> tabEntities = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<DeviceFragment> fragments = new ArrayList<>();
        for(MyBluetoothDevice dev : deviceList) {
            if(dev.hasFragment()) {
                tabEntities.add(dev.getTabEntity());
                titles.add(dev.getNickName());
                fragments.add(dev.getFragment());
            }
        }
        // 通知Adapter更新
        fragAdapter.updateData(titles, fragments);

        // 设置tabLayout的TabEntity
        // 没有Tab的时候不能设置，这个CommonTabLayout的问题
        if(tabEntities != null && tabEntities.size() != 0) {
            tabLayout.setTabData(tabEntities);
            tabLayout.notifyDataSetChanged();
            mWelcomeLayout.setVisibility(View.INVISIBLE);
            mMainLayout.setVisibility(View.VISIBLE);
        } else {
            mWelcomeLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.INVISIBLE);
        }
    }

    // 修改设备信息
    private void modifyDeviceInfo(final MyBluetoothDevice device) {
        if(device == null) return;

        LinearLayout layout = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_configured_device_info, null);
        String deviceName = device.getNickName();
        final EditText editText = (EditText)layout.findViewById(R.id.cfg_device_nickname);
        editText.setText(deviceName);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("修改设备信息");
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                device.setNickName(editText.getText().toString());
                device.save();
                updateTabandViewPager();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    // 删除设备
    private void deleteDevice(final MyBluetoothDevice device) {
        if(device == null) return;

        LinearLayout layout = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_configured_device_info, null);
        String deviceName = device.getNickName();
        final EditText editText = (EditText)layout.findViewById(R.id.cfg_device_nickname);
        editText.setText(deviceName);
        editText.setEnabled(false);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("确定删除该设备吗？");
        builder.setView(layout);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                // 添加设备返回
                if(resultCode == RESULT_OK) {
                    String nickName = data.getStringExtra("device_nickname");
                    String macAddress = data.getStringExtra("device_macaddress");
                    boolean isAutoConnect = data.getBooleanExtra("device_isautoconnect", false);
                    //暂时设置一个图标，以后增加这个功能
                    int icon = R.mipmap.ic_tablet_mac_black_48dp;

                    MyBluetoothDevice device = new MyBluetoothDevice();
                    device.setNickName(nickName);
                    device.setMacAddress(macAddress);
                    device.setAutoConnected(isAutoConnect);
                    device.setIcon(icon);

                    // 保存到数据库
                    device.save();
                    // 添加到设备列表
                    deviceList.add(device);
                    // 添加deviceAdapter作为观察者
                    device.registerDeviceObserver(deviceAdapter);
                    // 通知观察者
                    device.notifyDeviceObservers(IMyBluetoothDeviceObserver.TYPE_ADDED);
                }
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

    // 只有从FragmentStatePagerAdapter继承才能正常关闭Fragment
    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        private List<String> nickNames;
        private List<DeviceFragment> fragments;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void updateData(List<String> nickNames, List<DeviceFragment> fragments) {
            this.nickNames = nickNames;
            this.fragments = fragments;
            notifyDataSetChanged();
        }

        public int getPosition(DeviceFragment fragment) {
            return fragments.indexOf(fragment);
        }

        @Override
        public int getCount() {
            return (nickNames == null) ? 0 : nickNames.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return nickNames.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }


        // 一定要重载这个函数
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }


    ///////////////////IDeviceFragmentObserver接口函数///////////////////////

    // 寻找Fragment对应的设备
    @Override
    public MyBluetoothDevice findDevice(DeviceFragment fragment) {
        for(MyBluetoothDevice device : deviceList) {
            if(device.getFragment() == fragment) {
                return device;
            }
        }
        return null;
    }

    // 删除Fragment
    @Override
    public void delete(DeviceFragment fragment) {
        MyBluetoothDevice device = findDevice(fragment);
        if(device == null) return;

        device.removeDeviceObserver(fragment);
        device.setFragment(null);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                updateTabandViewPager();
            }
        });
    }
    ////////////////////////////////////////////////////////////////////////
}
