package com.cmtech.android.btdeviceapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
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

import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.adapter.ConfiguredDeviceAdapter;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;
import com.cmtech.android.btdevice.common.DeviceFragmentPagerAdapter;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *  MainActivity: 主界面，主要数据存放区
 */
public class MainActivity extends AppCompatActivity {
    private static MainActivity activity;

    // 已配置的设备列表
    List<ConfiguredDevice> configuredDeviceList = new ArrayList<>();

    // 已打开的设备列表
    List<ConfiguredDevice> openedDeviceList = new ArrayList<>();


    private ConfiguredDeviceAdapter configuredDeviceAdapter;
    private RecyclerView rvConfiguredDevices;


    private Button btnModify;
    private Button btnDelete;
    private Button btnAdd;
    private Button btnConnect;

    private DrawerLayout mDrawerLayout;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private DeviceFragmentPagerAdapter fragAdapter;

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
                Intent intent = new Intent(MainActivity.this, ScanDeviceActivity.class);
                intent.putExtra("configured_device_list", (Serializable) configuredDeviceList);
                startActivityForResult(intent, 1);
            }
        });

        // 连接设备
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int which = configuredDeviceAdapter.getSelectItem();
                if(which != -1) {
                    //mDrawerLayout.closeDrawer(GravityCompat.START);
                    configuredDeviceList.get(which).connect();
                    //connectConfiguredDevice(configuredDeviceList.get(which));
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
        tabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        fragAdapter = new DeviceFragmentPagerAdapter(getSupportFragmentManager(), openedDeviceList);
        viewPager.setAdapter(fragAdapter);
        tabLayout.setupWithViewPager(viewPager);

        activity = this;
    }

    public static MainActivity getActivity() {return activity;}

    // 打开已连接设备
    public void openConnectedDevice(ConfiguredDevice device) {
        if(device == null) return;
        mDrawerLayout.closeDrawer(GravityCompat.START);
        openedDeviceList.add(device);
        fragAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(fragAdapter.getCount()-1);
        tabLayout.getTabAt(fragAdapter.getCount()-1).select();
    }

    // 关闭已连接设备
    public void closeConnectedDevice(ConfiguredDevice device) {
        if(device == null) return;
        openedDeviceList.remove(device);
        fragAdapter.notifyDataSetChanged();
        if(fragAdapter.getCount() >= 1) {
            viewPager.setCurrentItem(fragAdapter.getCount() - 1);
            tabLayout.getTabAt(fragAdapter.getCount() - 1).select();
        }
    }

    // 修改已配置设备信息
    private void modifyConfiguredDeviceInfo(final ConfiguredDevice device) {
        LinearLayout layout = (LinearLayout)getLayoutInflater().inflate(R.layout.activity_set_cfg_device_info, null);
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
                fragAdapter.notifyDataSetChanged();
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
        LinearLayout layout = (LinearLayout)getLayoutInflater().inflate(R.layout.activity_set_cfg_device_info, null);
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
                // 添加配置设备
                if(resultCode == RESULT_OK) {
                    ConfiguredDevice device = (ConfiguredDevice)data.getSerializableExtra("return_device");
                    if(device != null) {
                        device.save();
                        configuredDeviceList.add(device);
                        device.registerDeviceObserver(configuredDeviceAdapter);
                        device.notifyDeviceObservers(ConfiguredDevice.TYPE_ADD);
                    }
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
    }

    /*@Override
    public void updateDeviceInfo(ConfiguredDevice device) {
        Log.d("MainActivity", "OK");
    }*/


}
