package com.cmtech.android.btdeviceapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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

import com.cmtech.android.ble.callback.IConnectCallback;
import com.cmtech.android.ble.common.ConnectState;
import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.ble.core.DeviceMirrorPool;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragment;
import com.cmtech.android.btdeviceapp.fragment.DeviceFragmentFactory;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.adapter.ConfiguredDeviceAdapter;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *  MainActivity: 主界面，主要数据存放区
 */
public class MainActivity extends AppCompatActivity implements DeviceFragment.IDeviceFragmentListener{
    private static MainActivity activity;

    // 已配置的设备列表
    List<ConfiguredDevice> configuredDeviceList = new ArrayList<>();

    // 已打开的设备列表
    //List<OpenedDevice> openedDeviceList = new ArrayList<>();


    private ConfiguredDeviceAdapter configuredDeviceAdapter;
    private RecyclerView rvConfiguredDevices;


    private Button btnModify;
    private Button btnDelete;
    private Button btnAdd;
    private Button btnConnect;

    private DrawerLayout mDrawerLayout;

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
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        // 获取已配置设备信息
        configuredDeviceList = DataSupport.findAll(ConfiguredDevice.class);

        // 暂时先任意设置一个图标
        for(ConfiguredDevice device : configuredDeviceList) {
            device.setIcon(R.mipmap.ic_tablet_mac_black_48dp);
        }

        // 设置已配置设备信息
        rvConfiguredDevices = (RecyclerView)findViewById(R.id.rvConfiguredDevices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvConfiguredDevices.setLayoutManager(layoutManager);
        configuredDeviceAdapter = new ConfiguredDeviceAdapter(this, configuredDeviceList);
        rvConfiguredDevices.setAdapter(configuredDeviceAdapter);

        btnModify = (Button)findViewById(R.id.device_modify_btn);
        btnDelete = (Button)findViewById(R.id.device_delete_btn);
        btnAdd = (Button)findViewById(R.id.device_add_btn);
        btnConnect = (Button)findViewById(R.id.device_connect_btn);

        // 修改设备信息
        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int which = configuredDeviceAdapter.getSelectItem();
                if(which != -1)
                    modifyConfiguredDeviceInfo(configuredDeviceList.get(which));
            }
        });

        // 删除设备
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int which = configuredDeviceAdapter.getSelectItem();
                if(which != -1)
                    deleteConfiguredDevice(configuredDeviceList.get(which));
            }
        });

        // 添加设备
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> configuredDeviceMacList = new ArrayList<>();
                for(ConfiguredDevice device : configuredDeviceList) {
                    configuredDeviceMacList.add(device.getMacAddress());
                }
                Intent intent = new Intent(MainActivity.this, ScanDeviceActivity.class);
                intent.putExtra("configured_device_list", (Serializable) configuredDeviceMacList);
                startActivityForResult(intent, 1);
            }
        });

        // 连接设备
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int which = configuredDeviceAdapter.getSelectItem();
                if(which != -1) {
                    final ConfiguredDevice device = configuredDeviceList.get(which);
                    device.connect(new IConnectCallback() {
                        @Override
                        public void onConnectSuccess(DeviceMirror deviceMirror) {
                            DeviceMirrorPool deviceMirrorPool = MyApplication.getViseBle().getDeviceMirrorPool();
                            if(deviceMirrorPool.isContainDevice(deviceMirror)) {
                                device.setDeviceMirror(deviceMirror);
                                device.setConnectState(ConnectState.CONNECT_SUCCESS);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        openConnectedDevice(device);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onConnectFailure(BleException exception) {
                            device.setConnectState(ConnectState.CONNECT_FAILURE);
                        }

                        @Override
                        public void onDisconnect(boolean isActive) {
                            device.setConnectState(ConnectState.CONNECT_DISCONNECT);
                        }
                    });
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

        // TabLayout相关设置
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

        activity = this;
    }

    public static MainActivity getActivity() {return activity;}

    // 打开已连接设备
    public void openConnectedDevice(ConfiguredDevice device) {
        if(device == null) return;
        mDrawerLayout.closeDrawer(GravityCompat.START);

        // 创建正确的Fragment
        DeviceFragment fragment = DeviceFragmentFactory.build(device);
        device.setFragment(fragment);

        // 更新TabLayout和ViewPager
        updateTabandViewPager();

        // 翻到最后一页
        viewPager.setCurrentItem(fragAdapter.getCount()-1);
    }

    // 更新TabLayout和ViewPager
    private void updateTabandViewPager() {
        // 获取已打开的设备的TabEntity、Title和DeviceFragment
        ArrayList<CustomTabEntity> tabEntities = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<DeviceFragment> fragments = new ArrayList<>();
        for(ConfiguredDevice dev : configuredDeviceList) {
            if(dev.isOpen()) {
                tabEntities.add(dev.getTabEntity());
                titles.add(dev.getNickName());
                fragments.add(dev.getFragment());
            }
        }
        // 通知Adapter更新
        fragAdapter.updateData(titles, fragments);
        // 设置tabLayout的TabEntity
        tabLayout.setTabData(tabEntities);
        // 翻到最后一页
        //viewPager.setCurrentItem(tabLayout.getTabCount()-1);
    }

    // 关闭已连接设备
    public void closeConnectedDevice(ConfiguredDevice device) {
        if(device == null) return;
        configuredDeviceList.remove(device);
        /*fragAdapter.notifyDataSetChanged();
        if(fragAdapter.getCount() >= 1) {
            viewPager.setCurrentItem(fragAdapter.getCount() - 1);
            tabLayout.getTabAt(fragAdapter.getCount() - 1).select();
        }*/
    }

    // 修改已配置设备信息
    private void modifyConfiguredDeviceInfo(final ConfiguredDevice device) {
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
                device.save();
                device.setNickName(editText.getText().toString());
                tabLayout.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    // 删除已配置设备
    private void deleteConfiguredDevice(final ConfiguredDevice device) {
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
                int index = configuredDeviceList.indexOf(device);
                configuredDeviceList.remove(index);
                device.notifyDeviceObservers(ConfiguredDevice.TYPE_DELETE);
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
                // 添加ConfiguredDevice
                if(resultCode == RESULT_OK) {
                    String nickName = data.getStringExtra("device_nickname");
                    String macAddress = data.getStringExtra("device_macaddress");
                    boolean isAutoConnect = data.getBooleanExtra("device_isautoconnect", false);

                    ConfiguredDevice device = new ConfiguredDevice();
                    device.setNickName(nickName);
                    device.setMacAddress(macAddress);
                    device.setAutoConnected(isAutoConnect);

                    device.save();
                    configuredDeviceList.add(device);
                    device.registerDeviceObserver(configuredDeviceAdapter);
                    device.notifyDeviceObservers(ConfiguredDevice.TYPE_ADD);
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
        MyApplication.getViseBle().disconnect();
        MyApplication.getViseBle().clear();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public ConfiguredDevice findDeviceFromFragment(DeviceFragment fragment) {
        for(ConfiguredDevice device : configuredDeviceList) {
            if(device.getFragment() == fragment) {
                return device;
            }
        }
        return null;
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
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
    }
}
