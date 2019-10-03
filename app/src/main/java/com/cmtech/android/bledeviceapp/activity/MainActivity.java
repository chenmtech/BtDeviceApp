package com.cmtech.android.bledeviceapp.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.ble.core.BleDeviceScanner;
import com.cmtech.android.ble.core.BleDeviceState;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgFileExplorerActivity;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.BleDeviceManager;
import com.cmtech.android.bledeviceapp.model.BleService;
import com.cmtech.android.bledeviceapp.model.RegisteredDeviceAdapter;
import com.cmtech.android.bledeviceapp.model.BleFactory;
import com.cmtech.android.bledeviceapp.model.BleFragAndTabManager;
import com.cmtech.android.bledeviceapp.model.MainToolbarManager;
import com.cmtech.android.bledeviceapp.model.FragAndTabManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.cmtech.android.bledeviceapp.util.APKVersionCodeUtils;
import com.vise.log.ViseLog;

import java.io.Serializable;
import java.util.List;

import static android.bluetooth.BluetoothAdapter.STATE_OFF;
import static android.bluetooth.BluetoothAdapter.STATE_ON;
import static com.cmtech.android.bledeviceapp.activity.DevRegisterActivity.DEVICE_REGISTER_INFO;
import static com.cmtech.android.bledeviceapp.activity.ScanActivity.REGISTER_DEVICE_MAC_LIST;

/**
 *  MainActivity: 主界面
 *  Created by bme on 2018/2/19.
 */

public class MainActivity extends AppCompatActivity implements IBleDeviceActivity {
    private static final String TAG = "MainActivity";

    private final static int RC_REGISTER_DEVICE = 1;     // 登记设备返回码
    private final static int RC_MODIFY_DEVICEINFO = 2;       // 修改设备基本信息返回码
    private final static int RC_MODIFY_USERINFO = 3;     // 修改用户信息返回码

    private final static SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
    private BleService bleService; // 设备服务,用于管理后台通知等
    private BleFragAndTabManager fragTabManager; // Fragment和TabLayout管理器
    private MainToolbarManager toolbarManager; // 工具条管理器

    private RegisteredDeviceAdapter deviceAdapter; // 已注册设备Adapter
    private RecyclerView rvDevices; // 已注册设备RecyclerView
    private DrawerLayout drawerLayout; // 侧滑界面
    private LinearLayout noDeviceOpenedLayout; // 无设备打开时的界面
    private RelativeLayout hasDeviceOpenedLayout; // 有设备打开时的界面，即包含设备Fragment和Tablayout的主界面
    private FloatingActionButton fabConnect; // 切换连接状态的FAB
    private FloatingActionButton fabClose; // 关闭设备的FAB
    private TextView tvUserName; // 账户名称控件
    private ImageView ivUserPortrait; // 头像控件
    private boolean stopService = false; // 是否停止服务
    private NavigationView navView;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bleService = ((BleService.BleServiceBinder)iBinder).getService();
            // 成功绑定后初始化
            if(bleService != null) {
                initializeView();
            } else {
                requestFinish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if(bleService != null) {
                Intent stopIntent = new Intent(MainActivity.this, BleService.class);
                stopService(stopIntent);
                bleService = null;
            }
            finish();
        }
    };

    // 蓝牙状态改变广播接收器
    private static final BroadcastReceiver btStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if(state == STATE_ON) {
                    BleDeviceScanner.clearInnerError();
                    Toast.makeText(context, "蓝牙已开启。", Toast.LENGTH_SHORT).show();
                } else if(state == STATE_OFF) {
                    Toast.makeText(context, "蓝牙已关闭。", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 确定账户已经登录
        if(!UserManager.getInstance().isSignIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // 启动并绑定BleDeviceService服务
        Intent startService = new Intent(this, BleService.class);
        startService(startService);
        bindService(startService, serviceConnection, BIND_AUTO_CREATE);

        // 登记蓝牙状态改变广播接收器
        IntentFilter bleStateIntent = new IntentFilter();
        bleStateIntent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(btStateChangedReceiver, bleStateIntent);

        if(BleDeviceScanner.isBleDisabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }
    }

    // 主界面初始化
    private void initializeView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvDeviceBattery = findViewById(R.id.tv_device_battery);
        toolbarManager = new MainToolbarManager(this, toolbar, tvDeviceBattery);

        rvDevices = findViewById(R.id.rv_registed_device);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvDevices.setLayoutManager(layoutManager);
        rvDevices.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        deviceAdapter = new RegisteredDeviceAdapter(BleDeviceManager.getDeviceList(), this);
        rvDevices.setAdapter(deviceAdapter);

        navView = findViewById(R.id.nav_view);

        initNavigation();

        updateNavigationView();

        toolbarManager.setNavigationIcon(UserManager.getInstance().getUser().getPortrait());

        // 设置FAB，FloatingActionButton
        fabConnect = findViewById(R.id.fab_connect);
        fabConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleFragment fragment = (BleFragment) fragTabManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.switchState();
                }
            }
        });

        fabClose = findViewById(R.id.fab_close);
        fabClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleFragment fragment = (BleFragment) fragTabManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.close();
                }
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        noDeviceOpenedLayout = findViewById(R.id.layout_nodeivce_opened);
        hasDeviceOpenedLayout = findViewById(R.id.layout_when_device_opened);

        // 创建Fragment管理器
        TabLayout tabLayout = findViewById(R.id.tablayout_device);

        fragTabManager = new BleFragAndTabManager(getSupportFragmentManager(), tabLayout, R.id.layout_main_fragment);

        fragTabManager.setOnFragmentUpdatedListener(new FragAndTabManager.OnFragmentUpdatedListener() {
            @Override
            public void onFragmentUpdated() {
                BleDevice device = (fragTabManager.size() == 0) ? null : ((BleFragment) fragTabManager.getCurrentFragment()).getDevice();
                updateMainLayout(device);
            }
        });


        // 初始化主界面
        initMainLayout();

        // 为已经打开的设备创建并打开Fragment
        for(BleDevice device : BleDeviceManager.getDeviceList()) {
            if(!device.isClosed()) {
                createFragmentThenOpen(device);
            }
        }

        User user = UserManager.getInstance().getUser();
        if(user.getName() == null || "".equals(user.getName().trim())) {
            Intent intent = new Intent(MainActivity.this, UserActivity.class);

            startActivityForResult(intent, RC_MODIFY_USERINFO);
        }
    }

    private void initNavigation() {
        View headerView = navView.getHeaderView(0);

        tvUserName = headerView.findViewById(R.id.tv_user_name);

        ivUserPortrait = headerView.findViewById(R.id.iv_user_portrait);

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);

                startActivityForResult(intent, RC_MODIFY_USERINFO);
            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_search_device:
                        List<String> deviceMacList = BleDeviceManager.getDeviceMacList();

                        Intent scanIntent = new Intent(MainActivity.this, ScanActivity.class);

                        scanIntent.putExtra(REGISTER_DEVICE_MAC_LIST, (Serializable) deviceMacList);

                        startActivityForResult(scanIntent, RC_REGISTER_DEVICE);

                        return true;
                    case R.id.nav_query_record:
                        Intent recordIntent = new Intent(MainActivity.this, EcgFileExplorerActivity.class);

                        startActivity(recordIntent);

                        return true;
                    case R.id.nav_open_news:
                        Intent newsIntent = new Intent(MainActivity.this, NewsActivity.class);

                        startActivity(newsIntent);

                        return true;
                    case R.id.nav_exit:
                        requestFinish();

                        return true;
                }
                return false;
            }
        });

    }

    private void updateMainLayout(BleDevice device) {
        if(device == null) {
            String appName = getResources().getString(R.string.app_name);

            toolbarManager.setTitle(appName, "无设备打开");
            toolbarManager.setBattery(-1);

            updateConnectFloatingActionButton(BleDeviceState.DEVICE_CLOSED.getIcon(), false);

            invalidateOptionsMenu();

            updateMainLayoutVisibility(false);
        } else {
            toolbarManager.setTitle(device.getNickName(), device.getMacAddress());
            toolbarManager.setBattery(device.getBattery());

            updateConnectFloatingActionButton(device.getStateIcon(), device.isActing());

            invalidateOptionsMenu();

            updateMainLayoutVisibility(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_REGISTER_DEVICE:
                if(resultCode == RESULT_OK) {
                    BleDeviceRegisterInfo registerInfo = (BleDeviceRegisterInfo) data.getSerializableExtra(DEVICE_REGISTER_INFO);
                    if(registerInfo != null) {
                        BleDevice device = BleDeviceManager.createDeviceIfNotExist(this, registerInfo);
                        if(device != null) {
                            device.addListener(bleService);

                            if(registerInfo.saveToPref(pref)) {
                                Toast.makeText(MainActivity.this, "设备登记成功", Toast.LENGTH_SHORT).show();
                                if(deviceAdapter != null) deviceAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(MainActivity.this, "设备登记失败", Toast.LENGTH_SHORT).show();
                                BleDeviceManager.deleteDevice(device);
                            }
                        }
                    }
                }
                break;

            case RC_MODIFY_DEVICEINFO:

                if ( resultCode == RESULT_OK) {
                    BleDeviceRegisterInfo registerInfo = (BleDeviceRegisterInfo) data.getSerializableExtra(DEVICE_REGISTER_INFO);
                    BleDevice device = BleDeviceManager.findDevice(registerInfo);

                    if(device != null && registerInfo.saveToPref(pref)) {
                        Toast.makeText(MainActivity.this, "设备信息修改成功", Toast.LENGTH_SHORT).show();

                        device.updateRegisterInfo(registerInfo);

                        if(deviceAdapter != null) deviceAdapter.notifyDataSetChanged();

                        fragTabManager.updateTabInfo(fragTabManager.findFragment(device), device.getImageDrawable(), device.getNickName());

                        if(fragTabManager.isFragmentSelected(device)) {
                            toolbarManager.setTitle(device.getNickName(), device.getMacAddress());

                            toolbarManager.setBattery(device.getBattery());
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "设备信息修改失败", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case RC_MODIFY_USERINFO:
                if(resultCode == RESULT_OK) {
                    updateNavigationView();

                    toolbarManager.setNavigationIcon(UserManager.getInstance().getUser().getPortrait());
                } else {
                    boolean logout = (data != null && data.getBooleanExtra("logout", false));

                    if(logout) {
                        logoutUser();
                    }
                }

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        MenuItem menuConfig = menu.findItem(R.id.toolbar_config);
        MenuItem menuClose = menu.findItem(R.id.toolbar_close);
        toolbarManager.setMenuItems(menuConfig, menuClose);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(fragTabManager.size() == 0) {
            toolbarManager.updateMenuItem(false, true);
        } else {
            toolbarManager.updateMenuItem(true, false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BleFragment fragment;
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer(true);
                break;

            case R.id.toolbar_config:
                fragment = (BleFragment) fragTabManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.openConfigActivity();
                }
                break;

            case R.id.toolbar_close:
                fragment = (BleFragment) fragTabManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.close();
                } else {
                    requestFinish();
                }
                break;
        }
        return true;
    }



    @Override
    protected void onDestroy() {
        ViseLog.e("MainActivity.onDestroy()");
        super.onDestroy();

        for(BleDevice device : BleDeviceManager.getDeviceList()) {
            device.removeListener(this);
        }

        unbindService(serviceConnection);

        //stopService = true;
        if(stopService) {
            Intent stopIntent = new Intent(MainActivity.this, BleService.class);
            stopService(stopIntent);
        }

        unregisterReceiver(btStateChangedReceiver);
    }

    private void requestFinish() {
        if(BleDeviceManager.existOpenedDevice()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("退出应用");
            builder.setMessage("有设备打开，退出将关闭这些设备。");
            builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    stopService = true;
                    finish();
                }
            });
            builder.setNegativeButton("最小化", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    stopService = false;
                    openDrawer(false);
                    MainActivity.this.moveTaskToBack(true);
                }
            });
            builder.show();
        } else {
            stopService = true;
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        openDrawer(!drawerLayout.isDrawerOpen(GravityCompat.START));
    }



    // 更新设备状态
    @Override
    public void onConnectStateUpdated(final BleDevice device) {
        // 更新设备列表Adapter
        if(deviceAdapter != null) deviceAdapter.notifyDataSetChanged();

        // 更新设备的Fragment界面
        BleFragment deviceFrag = fragTabManager.findFragment(device);

        if(deviceFrag != null) deviceFrag.updateState();

        if(fragTabManager.isFragmentSelected(device)) {
            updateConnectFloatingActionButton(device.getStateIcon(), device.isActing());
        }
    }

    @Override
    public void onBleErrorNotified(final BleDevice device, boolean warn) {
        /*if(!warn) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("设备无法连接报警");
        builder.setMessage("由于蓝牙错误，导致设备" + device.getMacAddress() + "无法连接，需要重启蓝牙。");
        builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                device.cancelNotifyBleError();
            }
        });
        builder.setCancelable(false);
        builder.show();*/
    }

    @Override
    public void onBatteryUpdated(final BleDevice device) {
        if(fragTabManager.isFragmentSelected(device)) {
            toolbarManager.setBattery(device.getBattery());
        }
    }

    @Override
    public BleDevice findDevice(String macAddress) {
        return (TextUtils.isEmpty(macAddress)) ? null : BleDeviceManager.findDevice(macAddress);
    }

    @Override
    public void closeFragment(final BleFragment fragment) {
        BleDevice device = fragment.getDevice();

        if(device != null && device.isDisconnect()) {
            device.close();
            fragTabManager.deleteFragment(fragment);
        } else {
            Toast.makeText(this, "设备连接中，请先断开设备。", Toast.LENGTH_LONG).show();
        }
    }

    // 打开设备
    public void openDevice(BleDevice device) {
        if(device == null || !device.isClosed()) return;

        BleFragment fragment = fragTabManager.findFragment(device);
        if(fragment != null) {
            openDrawer(false);
            fragTabManager.showFragment(fragment);
        } else {
            createFragmentThenOpen(device);
        }
    }

    private void createFragmentThenOpen(BleDevice device) {
        if(device == null) return;

        BleFactory factory = BleFactory.getFactory(device.getRegisterInfo());
        if(factory != null) {
            openDrawer(false);
            fragTabManager.addFragment(factory.createFragment(), device.getImageDrawable(), device.getNickName());
            updateMainLayout(device);
        }
    }

    // 从设备列表中删除设备
    public void removeDeviceFromList(final BleDevice device) {
        if(device == null) return;

        if(fragTabManager.isFragmentOpened(device)) {
            Toast.makeText(this, "请先关闭设备。", Toast.LENGTH_SHORT).show();
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除设备");
        builder.setMessage("确定删除设备：" + device.getMacAddress()+'\n'+device.getNickName());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(device.getRegisterInfo().deleteFromPref(pref)) {
                    BleDeviceManager.deleteDevice(device);
                    if(deviceAdapter != null) deviceAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "无法删除设备。", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    // 修改设备注册信息 
    public void modifyDeviceRegisterInfo(final BleDeviceRegisterInfo registerInfo) {
        Intent intent = new Intent(this, DevRegisterActivity.class);
        intent.putExtra(DEVICE_REGISTER_INFO, registerInfo);

        startActivityForResult(intent, RC_MODIFY_DEVICEINFO);
    }

    private void initMainLayout() {
        TextView tvVersionName = noDeviceOpenedLayout.findViewById(R.id.tv_versionname);

        tvVersionName.setText(String.format("Ver%s", APKVersionCodeUtils.getVerName(this)));

        updateMainLayout(null);
    }

    // 打开或关闭侧滑菜单
    private void openDrawer(boolean open) {
        if(open) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void logoutUser() {
        if(BleDeviceManager.existOpenedDevice()) {
            Toast.makeText(this, "请先关闭设备。", Toast.LENGTH_SHORT).show();
            return;
        }

        UserManager.getInstance().signOut();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("phone", "");

        editor.putLong("login_time", -1);

        editor.commit();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);

        startActivity(intent);

        requestFinish();
    }

    private void updateNavigationView() {
        User user = UserManager.getInstance().getUser();

        if(user == null) {
            throw new IllegalStateException();
        }

        if(user.getName() == null || "".equals(user.getName().trim())) {
            tvUserName.setText("请设置");
        } else {
            tvUserName.setText(user.getName());
        }

        String imagePath = user.getPortrait();

        if(imagePath != null && !"".equals(imagePath))
            Glide.with(MyApplication.getContext()).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivUserPortrait);
    }

    // 更新MainLayout的可视性
    private void updateMainLayoutVisibility(boolean hasDeviceOpened) {
        if(hasDeviceOpened && hasDeviceOpenedLayout.getVisibility() == View.INVISIBLE) {
            noDeviceOpenedLayout.setVisibility(View.INVISIBLE);
            hasDeviceOpenedLayout.setVisibility(View.VISIBLE);
        } else if(!hasDeviceOpened && hasDeviceOpenedLayout.getVisibility() == View.VISIBLE){
            noDeviceOpenedLayout.setVisibility(View.VISIBLE);
            hasDeviceOpenedLayout.setVisibility(View.INVISIBLE);
        }
    }

    // 更新浮动动作按钮
    private void updateConnectFloatingActionButton(int icon, boolean isRotate) {
        float degree;
        long duration;
        int count;

        if(isRotate) {
            degree = 360.0f;
            duration = 2000;
            count = ValueAnimator.INFINITE;
        } else {
            degree = 0.0f;
            duration = 100;
            count = 0;
        }

        fabConnect.clearAnimation();
        fabConnect.setImageResource(icon);
        ObjectAnimator connectFabAnimator = ObjectAnimator.ofFloat(fabConnect, "rotation", 0.0f, degree).setDuration(duration);
        connectFabAnimator.setRepeatCount(count);
        connectFabAnimator.setAutoCancel(true);
        connectFabAnimator.start();
    }

}
