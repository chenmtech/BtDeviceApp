package com.cmtech.android.bledeviceapp.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
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
import com.cmtech.android.ble.core.BleDeviceState;
import com.cmtech.android.ble.core.BleScanner;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgFileExploreActivity;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.BleDeviceManager;
import com.cmtech.android.bledeviceapp.model.BleDeviceType;
import com.cmtech.android.bledeviceapp.model.BleFactory;
import com.cmtech.android.bledeviceapp.model.BleFragTabManager;
import com.cmtech.android.bledeviceapp.model.BleNotifyService;
import com.cmtech.android.bledeviceapp.model.FragTabManager;
import com.cmtech.android.bledeviceapp.model.MainToolbarManager;
import com.cmtech.android.bledeviceapp.model.RegisteredDeviceAdapter;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.cmtech.android.bledeviceapp.util.APKVersionCodeUtils;
import com.vise.log.ViseLog;

import java.io.Serializable;
import java.util.List;

import static android.bluetooth.BluetoothAdapter.STATE_OFF;
import static android.bluetooth.BluetoothAdapter.STATE_ON;
import static com.cmtech.android.ble.core.BleDevice.INVALID_BATTERY;
import static com.cmtech.android.ble.core.BleDevice.MSG_BLE_INNER_ERROR;
import static com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorFactory.ECGMONITOR_DEVICE_TYPE;
import static com.cmtech.android.bledevice.temphumid.model.TempHumidFactory.TEMPHUMID_DEVICE_TYPE;
import static com.cmtech.android.bledevice.thermo.model.ThermoFactory.THERMO_DEVICE_TYPE;
import static com.cmtech.android.bledeviceapp.MyApplication.showMessageLongUsingToast;
import static com.cmtech.android.bledeviceapp.MyApplication.showMessageUsingToast;
import static com.cmtech.android.bledeviceapp.activity.RegisterActivity.DEVICE_REGISTER_INFO;
import static com.cmtech.android.bledeviceapp.activity.ScanActivity.REGISTERED_DEVICE_MAC_LIST;

/**
 *  MainActivity: 主界面
 *  Created by bme on 2018/2/19.
 */

public class MainActivity extends AppCompatActivity implements BleDevice.OnBleDeviceListener, FragTabManager.OnFragmentUpdatedListener {
    private static final String TAG = "MainActivity";
    private final static int RC_REGISTER_DEVICE = 1;     // 注册设备返回码
    private final static int RC_MODIFY_REGISTER_INFO = 2;       // 修改设备注册信息返回码
    private final static int RC_MODIFY_USER_INFO = 3;     // 修改用户信息返回码

    private final static SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
    private BleNotifyService bleNotifyService; // 通知服务,用于初始化BleDeviceManager，并管理后台通知
    public BleNotifyService getBleNotifyService() {
        return bleNotifyService;
    }
    private BleFragTabManager fragTabManager; // BleFragment和TabLayout管理器
    private MainToolbarManager toolbarManager; // 工具条管理器

    private RegisteredDeviceAdapter registeredDeviceAdapter; // 已注册设备Adapter
    private DrawerLayout drawerLayout; // 侧滑界面
    private LinearLayout noDeviceOpenedLayout; // 无设备打开时的界面
    private RelativeLayout hasDeviceOpenedLayout; // 有设备打开时的界面，即包含设备Fragment和Tablayout的主界面
    private FloatingActionButton fabConnect; // 切换连接状态的FAB
    private TextView tvUserName; // 账户名称控件
    private ImageView ivUserPortrait; // 头像控件
    private boolean isWarningBleInnerError = false;
    private boolean stopNotifyService = false; // 是否停止通知服务

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bleNotifyService = ((BleNotifyService.BleNotifyServiceBinder)iBinder).getService();
            // 成功绑定后初始化，否则请求退出
            if(bleNotifyService != null) {
                initialize();
            } else {
                requestFinish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if(bleNotifyService != null) {
                Intent stopServiceIntent = new Intent(MainActivity.this, BleNotifyService.class);
                stopService(stopServiceIntent);
                bleNotifyService = null;
            }
            finish();
        }
    };

    // 蓝牙状态改变广播接收器
    private final BroadcastReceiver btStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if(state == STATE_ON) {
                    BleScanner.clearInnerError();
                    BleScanner.clearScanTimes();
                    Toast.makeText(context, "蓝牙已开启。", Toast.LENGTH_SHORT).show();
                } else if(state == STATE_OFF) {
                    Toast.makeText(context, "蓝牙已关闭。", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    // 绑定状态广播接收器
    private final BroadcastReceiver bondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(context, device.getAddress() + "绑定成功。", Toast.LENGTH_SHORT).show();
                } else if(device.getBondState() == BluetoothDevice.BOND_BONDING){
                    Toast.makeText(context, device.getAddress() + "绑定中。", Toast.LENGTH_SHORT).show();
                } else if(device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Toast.makeText(context, device.getAddress() + "绑定失败。", Toast.LENGTH_SHORT).show();
                }
            } else if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String strPsw = "000000";
                    device.setPin(strPsw.getBytes());
                    abortBroadcast();
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

        // 启动并绑定通知服务
        Intent serviceIntent = new Intent(this, BleNotifyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        // 登记蓝牙状态改变广播接收器
        IntentFilter bleStateIntent = new IntentFilter();
        bleStateIntent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(btStateChangedReceiver, bleStateIntent);

        // 登记绑定状态广播接收器
        IntentFilter bondIntent = new IntentFilter();
        bondIntent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bondIntent.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(bondStateReceiver, bondIntent);

        if(BleScanner.isBleDisabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }
    }

    // 主界面初始化
    private void initialize() {
        // 初始化工具条管理器
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvDeviceBattery = findViewById(R.id.tv_device_battery);
        toolbarManager = new MainToolbarManager(this, toolbar, tvDeviceBattery);
        toolbarManager.setNavigationIcon(UserManager.getInstance().getUser().getPortraitPath());

        // 初始化已注册设备列表
        RecyclerView rvDevices = findViewById(R.id.rv_registed_device);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvDevices.setLayoutManager(layoutManager);
        rvDevices.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        registeredDeviceAdapter = new RegisteredDeviceAdapter(BleDeviceManager.getDeviceList(), this);
        rvDevices.setAdapter(registeredDeviceAdapter);

        // 初始化导航视图
        initNavigation();
        updateNavigation();

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

        drawerLayout = findViewById(R.id.drawer_layout);
        noDeviceOpenedLayout = findViewById(R.id.layout_when_nodeivce_opened);
        hasDeviceOpenedLayout = findViewById(R.id.layout_when_device_opened);

        // 初始化BleFragTabManager
        TabLayout tabLayout = findViewById(R.id.tablayout_device);
        fragTabManager = new BleFragTabManager(getSupportFragmentManager(), tabLayout, R.id.layout_main_fragment);
        fragTabManager.setOnFragmentUpdatedListener(this);

        // 初始化主界面
        initMainLayout();

        // 为已经打开的设备创建并打开Fragment
        for(BleDevice device : BleDeviceManager.getDeviceList()) {
            if(!device.isClosed()) {
                createAndOpenFragment(device);
            }
        }

        User user = UserManager.getInstance().getUser();
        user.setName("test");
        if(TextUtils.isEmpty(user.getName())) {
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            startActivityForResult(intent, RC_MODIFY_USER_INFO);
        }
    }

    private void initNavigation() {
        NavigationView navView = findViewById(R.id.nav_view);
        View headerView = navView.getHeaderView(0);
        tvUserName = headerView.findViewById(R.id.tv_user_name);
        ivUserPortrait = headerView.findViewById(R.id.iv_user_portrait);

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivityForResult(intent, RC_MODIFY_USER_INFO);
            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_scan_device: // 扫描设备
                        List<String> registeredMacList = BleDeviceManager.getDeviceMacList();
                        Intent scanIntent = new Intent(MainActivity.this, ScanActivity.class);
                        scanIntent.putExtra(REGISTERED_DEVICE_MAC_LIST, (Serializable) registeredMacList);
                        startActivityForResult(scanIntent, RC_REGISTER_DEVICE);
                        return true;
                    case R.id.nav_query_record: // 查阅记录
                        PopupMenu popupMenu = new PopupMenu(MainActivity.this, item.getActionView());
                        popupMenu.inflate(R.menu.menu_query_record);
                        List<BleDeviceType> types = BleDeviceType.getSupportedDeviceTypes();
                        popupMenu.getMenu().findItem(R.id.nav_ecg_record).setVisible(types.contains(ECGMONITOR_DEVICE_TYPE));
                        popupMenu.getMenu().findItem(R.id.nav_temphumid_record).setVisible(types.contains(TEMPHUMID_DEVICE_TYPE));
                        popupMenu.getMenu().findItem(R.id.nav_thermo_record).setVisible(types.contains(THERMO_DEVICE_TYPE));
                        @SuppressLint("RestrictedApi")
                        MenuPopupHelper popupHelper = new MenuPopupHelper(MainActivity.this, (MenuBuilder) popupMenu.getMenu(), item.getActionView());
                        popupHelper.setForceShowIcon(true);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()) {
                                    case R.id.nav_ecg_record:
                                        Intent recordIntent = new Intent(MainActivity.this, EcgFileExploreActivity.class);
                                        startActivity(recordIntent);
                                        return true;
                                    case R.id.nav_temphumid_record:
                                    case R.id.nav_thermo_record:
                                        Toast.makeText(MainActivity.this, "当前无记录。", Toast.LENGTH_SHORT).show();
                                        return true;
                                }
                                return false;
                            }
                        });
                        popupHelper.show();
                        return true;
                    case R.id.nav_open_news: // 打开新闻
                        Intent newsIntent = new Intent(MainActivity.this, NewsActivity.class);
                        startActivity(newsIntent);
                        return true;
                    case R.id.nav_exit: // 退出
                        requestFinish();
                        return true;
                }
                return false;
            }
        });
    }

    private void updateMainLayout(BleDevice device) {
        if(device == null) {
            toolbarManager.setTitle(getString(R.string.app_name), "无设备打开");
            toolbarManager.setBattery(INVALID_BATTERY);
            updateConnectFloatingActionButton(BleDeviceState.CLOSED.getIcon(), false);
            invalidateOptionsMenu();
            updateMainLayoutVisibility(false);
        } else {
            toolbarManager.setTitle(device.getNickName(), device.getMacAddress());
            toolbarManager.setBattery(device.getBattery());
            updateConnectFloatingActionButton(device.getStateIcon(), device.isWaitingResponse());
            updateCloseMenuItemVisible(device.isStopped());
            updateMainLayoutVisibility(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_REGISTER_DEVICE: // 注册设备返回
                if(resultCode == RESULT_OK) {
                    BleDeviceRegisterInfo registerInfo = (BleDeviceRegisterInfo) data.getSerializableExtra(DEVICE_REGISTER_INFO);
                    if(registerInfo != null) {
                        BleDevice device = BleDeviceManager.createDeviceIfNotExist(registerInfo);
                        if(device != null) {
                            if(registerInfo.saveToPref(pref)) {
                                Toast.makeText(MainActivity.this, "设备注册成功", Toast.LENGTH_SHORT).show();
                                if(registeredDeviceAdapter != null) registeredDeviceAdapter.notifyDataSetChanged();
                                device.addListener(bleNotifyService);
                            } else {
                                Toast.makeText(MainActivity.this, "设备注册失败", Toast.LENGTH_SHORT).show();
                                BleDeviceManager.deleteDevice(device);
                            }
                        }
                    }
                }
                break;

            case RC_MODIFY_REGISTER_INFO: // 修改注册信息返回
                if ( resultCode == RESULT_OK) {
                    BleDeviceRegisterInfo registerInfo = (BleDeviceRegisterInfo) data.getSerializableExtra(DEVICE_REGISTER_INFO);
                    BleDevice device = BleDeviceManager.findDevice(registerInfo);
                    if(device != null && registerInfo.saveToPref(pref)) {
                        Toast.makeText(MainActivity.this, "设备信息修改成功", Toast.LENGTH_SHORT).show();
                        device.updateRegisterInfo(registerInfo);
                        if(registeredDeviceAdapter != null) registeredDeviceAdapter.notifyDataSetChanged();
                        Drawable drawable;
                        if(TextUtils.isEmpty(device.getImagePath())) {
                            BleDeviceType deviceType = BleDeviceType.getFromUuid(device.getUuidString());
                            if(deviceType == null) {
                                throw new IllegalStateException("The device type is not supported.");
                            }
                            drawable = ContextCompat.getDrawable(this, deviceType.getDefaultImageId());
                        } else {
                            drawable = new BitmapDrawable(getResources(), device.getImagePath());
                        }
                        fragTabManager.updateTabInfo(fragTabManager.findFragment(device), drawable, device.getNickName());
                        if(fragTabManager.isFragmentSelected(device)) {
                            toolbarManager.setTitle(device.getNickName(), device.getMacAddress());
                            toolbarManager.setBattery(device.getBattery());
                        }
                    } else {
                        showMessageUsingToast("设备信息修改失败");
                    }
                }
                break;

            case RC_MODIFY_USER_INFO: // 修改用户信息返回
                if(resultCode == RESULT_OK) {
                    updateNavigation();
                    toolbarManager.setNavigationIcon(UserManager.getInstance().getUser().getPortraitPath());
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuConfig = menu.findItem(R.id.toolbar_config);
        MenuItem menuClose = menu.findItem(R.id.toolbar_close);
        toolbarManager.setMenuItems(new MenuItem[]{menuConfig, menuClose});
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(fragTabManager.size() == 0) {
            toolbarManager.updateMenuItemsVisible(new boolean[]{false, true});
        } else {
            toolbarManager.updateMenuItemsVisible(new boolean[]{true, true});
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
                    fragment.openConfigureActivity();
                }
                break;

            case R.id.toolbar_close:
                fragment = (BleFragment) fragTabManager.getCurrentFragment();
                if(fragment != null) {
                    closeFragment(fragment);
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
        if(stopNotifyService) {
            Intent stopIntent = new Intent(MainActivity.this, BleNotifyService.class);
            stopService(stopIntent);
        }

        unregisterReceiver(btStateChangedReceiver);
        unregisterReceiver(bondStateReceiver);
    }

    private void requestFinish() {
        if(BleDeviceManager.hasOpenedDevice()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("退出应用");
            builder.setMessage("有设备打开，退出将关闭这些设备。");
            builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    stopNotifyService = true;
                    finish();
                }
            });
            builder.setNegativeButton("最小化", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    stopNotifyService = false;
                    openDrawer(false);
                    MainActivity.this.moveTaskToBack(true);
                }
            });
            builder.show();
        } else {
            stopNotifyService = true;
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        openDrawer(!drawerLayout.isDrawerOpen(GravityCompat.START));
    }

    // 设备状态更新
    @Override
    public void onStateUpdated(final BleDevice device) {
        // 更新设备列表Adapter
        if(registeredDeviceAdapter != null) registeredDeviceAdapter.notifyDataSetChanged();
        // 更新设备的Fragment界面
        BleFragment deviceFrag = fragTabManager.findFragment(device);
        if(deviceFrag != null) deviceFrag.updateState();
        if(fragTabManager.isFragmentSelected(device)) {
            updateConnectFloatingActionButton(device.getStateIcon(), device.isWaitingResponse());
            updateCloseMenuItemVisible(device.isStopped());
        }
    }

    // 提示信息产生
    @Override
    public void onExceptionMsgNotified(BleDevice device, int msgId) {
        if(msgId == MSG_BLE_INNER_ERROR) {
            if(!isWarningBleInnerError) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("蓝牙内部错误").setMessage(device.getNickName() + "无法连接，需要重启蓝牙。");
                builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isWarningBleInnerError = false;
                        bleNotifyService.stopWarningBleInnerError();
                    }
                }).setCancelable(false).show();
                isWarningBleInnerError = true;
            }
        } else {
            showMessageUsingToast(device.getNickName() + "-" + getString(msgId));
        }
    }

    // 电量更新
    @Override
    public void onBatteryUpdated(final BleDevice device) {
        if(fragTabManager.isFragmentSelected(device)) {
            toolbarManager.setBattery(device.getBattery());
        }
    }

    // Fragment更新
    @Override
    public void onFragmentUpdated() {
        BleFragment fragment = (BleFragment)fragTabManager.getCurrentFragment();
        BleDevice device = (fragment == null) ? null : fragment.getDevice();
        updateMainLayout(device);
    }

    // 打开设备
    public void openDevice(BleDevice device) {
        if(device == null || !device.isClosed()) return;

        BleFragment fragment = fragTabManager.findFragment(device);
        if(fragment != null) {
            openDrawer(false);
            fragTabManager.showFragment(fragment);
        } else {
            createAndOpenFragment(device);
        }
    }

    private void createAndOpenFragment(BleDevice device) {
        if(device == null) return;

        BleFactory factory = BleFactory.getFactory(device.getRegisterInfo());
        if(factory != null) {
            openDrawer(false);
            Drawable drawable;
            if(TextUtils.isEmpty(device.getImagePath())) {
                BleDeviceType deviceType = BleDeviceType.getFromUuid(device.getUuidString());
                if(deviceType == null) {
                    throw new IllegalStateException("The device type is not supported.");
                }
                drawable = ContextCompat.getDrawable(this, deviceType.getDefaultImageId());
            } else {
                drawable = new BitmapDrawable(getResources(), device.getImagePath());
            }
            if(drawable == null) {
                BleDeviceType deviceType = BleDeviceType.getFromUuid(device.getUuidString());
                if(deviceType == null) {
                    throw new IllegalStateException("The device type is not supported.");
                }
                drawable = ContextCompat.getDrawable(this, deviceType.getDefaultImageId());
            }
            fragTabManager.openFragment(factory.createFragment(), drawable, device.getNickName());
            updateMainLayout(device);
        }
    }

    // 关闭Fragment
    private void closeFragment(final BleFragment fragment) {
        if(fragment == null || fragment.getDevice() == null) return;

        BleDevice device = fragment.getDevice();
        if(device.isStopped()) {
            fragment.close();
        } else {
            showMessageLongUsingToast("当前无法关闭设备。");
        }
    }

    // 移除Fragment
    public void removeFragment(BleFragment fragment) {
        fragTabManager.removeFragment(fragment);
    }

    // 从设备列表中删除设备
    public void removeRegisteredDevice(final BleDevice device) {
        if(device == null) return;

        if(fragTabManager.isFragmentOpened(device)) {
            showMessageUsingToast("请先关闭该设备。");
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除设备").setMessage("确定删除设备：" + device.getMacAddress()+'\n'+device.getNickName());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(device.getRegisterInfo().deleteFromPref(pref)) {
                    BleDeviceManager.deleteDevice(device);
                    if(registeredDeviceAdapter != null) registeredDeviceAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "无法删除设备。", Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }

    // 修改设备注册信息 
    public void modifyRegisterInfo(final BleDeviceRegisterInfo registerInfo) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra(DEVICE_REGISTER_INFO, registerInfo);
        startActivityForResult(intent, RC_MODIFY_REGISTER_INFO);
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

    // 退出登录
    private void logoutUser() {
        if(BleDeviceManager.hasOpenedDevice()) {
            Toast.makeText(this, "有设备打开，请先关闭设备。", Toast.LENGTH_SHORT).show();
            return;
        }

        UserManager.getInstance().signOut();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        LoginActivity.saveLoginInfo(pref, "", -1);

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateNavigation() {
        User user = UserManager.getInstance().getUser();
        if(user == null) {
            throw new IllegalStateException();
        }

        if(user.getName() == null || "".equals(user.getName().trim())) {
            tvUserName.setText("请设置");
        } else {
            tvUserName.setText(user.getName());
        }
        String imagePath = user.getPortraitPath();
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

    // 更新连接浮动动作按钮
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

    private void updateCloseMenuItemVisible(boolean canClosed) {
        toolbarManager.updateMenuItemVisible(1, canClosed);
    }

}
