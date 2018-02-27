package com.cmtech.android.btdeviceapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.ble.ViseBle;
import com.cmtech.android.ble.common.ConnectState;
import com.cmtech.android.btdeviceapp.MyApplication;
import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.adapter.ConfiguredDeviceAdapter;
import com.cmtech.android.btdeviceapp.model.ConfiguredDevice;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConfiguredDevice.IConnectStateObersver{
    private ViseBle viseBle = MyApplication.getViseBle();

    // 用于完成已配置设备的功能
    private ConfiguredDeviceAdapter configuredDeviceAdapter;
    private RecyclerView rvConfiguredDevices;
    List<ConfiguredDevice> deviceList;

    private Button btnModify;
    private Button btnDelete;
    private Button btnAdd;
    private Button btnConnect;


    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        // 获取已配置设备信息
        deviceList = DataSupport.findAll(ConfiguredDevice.class);
        for(ConfiguredDevice device : deviceList) {
            device.registerConnectStateObserver(this);
        }

        // 设置已配置设备信息
        rvConfiguredDevices = (RecyclerView)findViewById(R.id.rvConfiguredDevices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvConfiguredDevices.setLayoutManager(layoutManager);
        configuredDeviceAdapter = new ConfiguredDeviceAdapter(this, deviceList);
        if(deviceList != null && deviceList.size() != 0)
            configuredDeviceAdapter.setSelectItem(0);
        rvConfiguredDevices.setAdapter(configuredDeviceAdapter);

        btnModify = (Button)findViewById(R.id.device_modify_btn);
        btnDelete = (Button)findViewById(R.id.device_delete_btn);
        btnAdd = (Button)findViewById(R.id.device_add_btn);
        btnConnect = (Button)findViewById(R.id.device_connect_btn);

        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(configuredDeviceAdapter.getSelectItem() != -1)
                    modifyConfiguredDeviceInfo(configuredDeviceAdapter.getSelectItem());
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(configuredDeviceAdapter.getSelectItem() != -1)
                    deleteConfiguredDevice(configuredDeviceAdapter.getSelectItem());
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScanDeviceActivity.class);
                intent.putExtra("configured_device_list", (Serializable)deviceList);
                startActivityForResult(intent, 1);
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(configuredDeviceAdapter.getSelectItem() != -1) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    connectConfiguredDevice(configuredDeviceAdapter.getSelectItem());
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


    }

    // 连接已配置设备
    private void connectConfiguredDevice(final int which) {
        deviceList.get(which).connect();
    }

    // 修改已配置设备信息
    private void modifyConfiguredDeviceInfo(final int which) {
        LinearLayout layout = (LinearLayout)getLayoutInflater().inflate(R.layout.activity_set_cfg_device_info, null);
        String deviceName = deviceList.get(which).getNickName();
        final EditText editText = (EditText)layout.findViewById(R.id.cfg_device_nickname);
        editText.setText(deviceName);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("设置设备别名");
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deviceList.get(which).setNickName(editText.getText().toString());
                deviceList.get(which).save();
                configuredDeviceAdapter.notifyDataSetChanged();
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
    private void deleteConfiguredDevice(final int which) {
        LinearLayout layout = (LinearLayout)getLayoutInflater().inflate(R.layout.activity_set_cfg_device_info, null);
        String deviceName = deviceList.get(which).getNickName();
        final EditText editText = (EditText)layout.findViewById(R.id.cfg_device_nickname);
        editText.setText(deviceName);
        editText.setEnabled(false);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("确定删除该设备吗？");
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deviceList.get(which).delete();
                deviceList.remove(which);
                configuredDeviceAdapter.setSelectItem(-1);
                configuredDeviceAdapter.notifyDataSetChanged();
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
                    device.save();
                    deviceList.add(device);
                    configuredDeviceAdapter.setSelectItem(deviceList.size()-1);
                    configuredDeviceAdapter.notifyDataSetChanged();

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
    public void updateConnectState(ConfiguredDevice device, ConnectState state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("MainActivity", "OK");

            }
        });

    }
}
