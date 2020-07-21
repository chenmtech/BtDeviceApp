package com.cmtech.android.bledeviceapp.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.ble.core.BleDeviceCommonInfo;
import com.cmtech.android.ble.core.BleScanner;
import com.cmtech.android.ble.core.DeviceCommonInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.ble.core.WebDeviceCommonInfo;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.CtrlPanelAdapter;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceManager;
import com.cmtech.android.bledeviceapp.model.DeviceTabFragManager;
import com.cmtech.android.bledeviceapp.model.DeviceType;
import com.cmtech.android.bledeviceapp.model.MainToolbarManager;
import com.cmtech.android.bledeviceapp.model.NotificationService;
import com.cmtech.android.bledeviceapp.model.TabFragManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.APKVersionCodeUtils;
import com.cmtech.android.bledeviceapp.util.FastClickUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.cmtech.android.ble.core.DeviceConnectState.CLOSED;
import static com.cmtech.android.ble.core.IDevice.INVALID_BATTERY_LEVEL;
import static com.cmtech.android.bledeviceapp.AppConstant.KM_STORE_URI;
import static com.cmtech.android.bledeviceapp.AppConstant.SUPPORT_LOGIN_PLATFORM;
import static com.cmtech.android.bledeviceapp.activity.DeviceInfoActivity.DEVICE_INFO;

/**
 *  MainActivity: 主界面
 *  Created by bme on 2018/2/19.
 */

public class MainActivity extends AppCompatActivity implements IDevice.OnCommonDeviceListener, TabFragManager.OnFragmentUpdatedListener {
    private static final String TAG = "MainActivity";
    private final static int RC_ADD_DEVICE = 1;     // return code for adding new devices
    private final static int RC_MODIFY_DEVICE_INFO = 2;       // return code for modifying device info
    private final static int RC_MODIFY_ACCOUNT = 3;

    private LocalDevicesFragment localDevicesFragment;
    //WebDevicesFragment webDevicesFragment;
    private NotificationService notificationService; // 通知服务,用于初始化BleDeviceManager，并管理后台通知
    private DeviceTabFragManager fragTabManager; // BleFragment和TabLayout管理器
    private MainToolbarManager tbManager; // 工具条管理器
    private DrawerLayout drawerLayout; // 侧滑界面
    private RelativeLayout noDeviceOpenLayout; // 无设备打开时的界面
    private RelativeLayout hasDeviceOpenLayout; // 有设备打开时的界面，即包含设备Fragment和Tablayout的主界面
    private FloatingActionButton fabConnect; // 切换连接状态的FAB
    private TextView tvAccountName; // 账户名称控件
    private ImageView ivAccountImage; // 账户头像控件
    private ImageButton ibChangeAccount; // 切换账户控件

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            notificationService = ((NotificationService.NotificationServiceBinder)iBinder).getService();
            // 成功绑定后初始化UI，否则请求退出
            if(notificationService != null) {
                initializeUI();
            } else {
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if(notificationService != null) {
                Intent intent = new Intent(MainActivity.this, NotificationService.class);
                stopService(intent);
                notificationService = null;
            }
            finish();
        }
    };

    /*public NotificationService getNotificationService() {
        return notificationService;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViseLog.e("MainActivity onCreate.");
        setContentView(R.layout.activity_main);

        // 确定账户已经登录
        if(!AccountManager.isLogin()) {
            Toast.makeText(this, R.string.login_failure, Toast.LENGTH_SHORT).show();
            finish();
        }

        ViseLog.e(AccountManager.getAccount());

        initDeviceManager();

        // 启动并绑定通知服务
        Intent serviceIntent = new Intent(this, NotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        //bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        initializeUI();

        if(BleScanner.isBleDisabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }
    }

    // 主界面初始化
    @SuppressLint("WrongConstant")
    private void initializeUI() {
        // 初始化工具条管理器
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvBattery = findViewById(R.id.tv_device_battery);
        tbManager = new MainToolbarManager(this, toolbar, tvBattery);

        // init device control panel
        ViewPager pager = findViewById(R.id.vp_device_panel);
        TabLayout layout = findViewById(R.id.tl_device_panel);
        localDevicesFragment = new LocalDevicesFragment();
        //webDevicesFragment = new WebDevicesFragment();
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(localDevicesFragment);
        List<String> titleList = new ArrayList<>();
        titleList.add(getString(LocalDevicesFragment.TITLE_ID));
        CtrlPanelAdapter fragAdapter = new CtrlPanelAdapter(getSupportFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(1);
        layout.setupWithViewPager(pager);

        // init navigation view
        initNavigation();
        updateNavigationHeader();

        // set FAB，FloatingActionButton
        fabConnect = findViewById(R.id.fab_connect);
        fabConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeviceFragment fragment = (DeviceFragment) fragTabManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.switchState();
                }
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        noDeviceOpenLayout = findViewById(R.id.layout_nodeivce_open);
        hasDeviceOpenLayout = findViewById(R.id.layout_device_open);

        // init BleFragTabManager
        TabLayout tabLayout = findViewById(R.id.device_tab);
        fragTabManager = new DeviceTabFragManager(getSupportFragmentManager(), tabLayout, R.id.layout_device_fragment);
        fragTabManager.setOnFragmentUpdatedListener(this);

        // init main layout
        initMainLayout();

        // 为已经打开的设备创建并打开Fragment
        /*List<IDevice> openedDevices = DeviceManager.getOpenedDevice();
        for(IDevice device : openedDevices) {
            if(device.getState() != CLOSED) {
                createAndOpenFragment(device);
            }
        }*/
    }

    // 初始化DeviceManager
    private void initDeviceManager() {
        List<BleDeviceCommonInfo> infos = LitePal.findAll(BleDeviceCommonInfo.class);
        if (infos == null || infos.isEmpty()) return;
        for (DeviceCommonInfo info : infos) {
            DeviceManager.createNewDevice(this, info);
        }
    }

    private void initNavigation() {
        NavigationView navView = findViewById(R.id.nav_view);
        View headerView = navView.getHeaderView(0);
        tvAccountName = headerView.findViewById(R.id.tv_account_name);
        ivAccountImage = headerView.findViewById(R.id.iv_account_image);
        ivAccountImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivityForResult(intent, RC_MODIFY_ACCOUNT);
            }
        });

        ibChangeAccount = headerView.findViewById(R.id.ib_change_account);
        ibChangeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.logout_account)
                        .setMessage(R.string.really_logout_account)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                changeAccount();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }).show();
            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(FastClickUtil.isFastClick())
                    return true;

                switchDrawer(false);

                Intent intent;
                switch (item.getItemId()) {
                    case R.id.nav_add_device: // add device
                        List<String> addresses = DeviceManager.getAddressList();
                        intent = new Intent(MainActivity.this, ScanActivity.class);
                        intent.putExtra("device_address_list", (Serializable) addresses);
                        startActivityForResult(intent, RC_ADD_DEVICE);
                        return true;
                    case R.id.nav_query_record: // query user records
                        intent = new Intent(MainActivity.this, RecordExplorerActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_open_store: // open KM store
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(KM_STORE_URI));
                        startActivity(intent);
                        return true;
                    case R.id.nav_about_us: // open KM store
                        intent = new Intent(MainActivity.this, AboutUsActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_exit: // exit
                        if(DeviceManager.hasDeviceOpen()) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(R.string.exit_app);
                            builder.setMessage(R.string.pls_close_device_firstly);
                            builder.setPositiveButton(R.string.force_exit, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                            builder.show();
                        } else {
                            finish();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void updateMainLayout(IDevice device) {
        if(device == null) {
            tbManager.setTitle(getString(R.string.app_name), "");
            tbManager.setBattery(INVALID_BATTERY_LEVEL);
            updateConnectFAButton(CLOSED.getIcon());
            invalidateOptionsMenu();
            updateMainLayoutVisibility(false);
        } else {
            String title = device.getName();
            DeviceCommonInfo registerInfo = device.getCommonInfo();
            if(!device.isLocal()) {
                title += ("-" + ((WebDeviceCommonInfo) registerInfo).getBroadcastName());
            }
            tbManager.setTitle(title, device.getAddress());
            tbManager.setBattery(device.getBatteryLevel());
            updateConnectFAButton(device.getConnectState().getIcon());
            updateCloseMenuItem(true);
            updateMainLayoutVisibility(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_ADD_DEVICE: // return for add device
                if(resultCode == RESULT_OK) {
                    BleDeviceCommonInfo info = (BleDeviceCommonInfo) data.getSerializableExtra(DEVICE_INFO);
                    if(info != null) {
                        IDevice device = DeviceManager.createNewDevice(this, info);
                        if(device != null) {
                            if(info.save()) {
                                updateDeviceList();
                                //device.addCommonListener(notificationService);
                            } else {
                                Toast.makeText(MainActivity.this, R.string.add_device_failure, Toast.LENGTH_SHORT).show();
                                DeviceManager.deleteDevice(device);
                            }
                        }
                    }
                }
                break;

            case RC_MODIFY_DEVICE_INFO: // return code for modify device info
                if ( resultCode == RESULT_OK) {
                    BleDeviceCommonInfo info = (BleDeviceCommonInfo) data.getSerializableExtra(DEVICE_INFO);
                    IDevice device = DeviceManager.findDevice(info);
                    if(device != null) {
                        device.updateCommonInfo(info);
                        device.getCommonInfo().save();
                        updateDeviceList();
                        Drawable drawable;
                        if(TextUtils.isEmpty(device.getIcon())) {
                            drawable = ContextCompat.getDrawable(this, Objects.requireNonNull(DeviceType.getFromUuid(device.getUuid())).getDefaultIcon());
                        } else {
                            drawable = new BitmapDrawable(getResources(), device.getIcon());
                        }
                        fragTabManager.updateTabInfo(fragTabManager.findFragment(device), drawable, device.getName());
                        if(fragTabManager.isFragmentSelected(device)) {
                            tbManager.setTitle(device.getName(), device.getAddress());
                            tbManager.setBattery(device.getBatteryLevel());
                        }
                    }
                }
                break;

            case RC_MODIFY_ACCOUNT:
                if(resultCode == RESULT_OK) {
                    updateNavigationHeader();
                }
                break;
        }
    }

    private void updateDeviceList() {
        localDevicesFragment.update();
        //webDevicesFragment.update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuConfig = menu.findItem(R.id.toolbar_config);
        MenuItem menuClose = menu.findItem(R.id.toolbar_close);
        tbManager.setMenuItems(new MenuItem[]{menuConfig, menuClose});
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(fragTabManager.size() == 0) {
            tbManager.updateMenuVisible(new boolean[]{false, true});
        } else {
            tbManager.updateMenuVisible(new boolean[]{true, true});
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DeviceFragment fragment;
        switch (item.getItemId()) {
            case android.R.id.home:
                switchDrawer(true);
                break;

            case R.id.toolbar_config:
                fragment = (DeviceFragment) fragTabManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.openConfigureActivity();
                }
                break;

            case R.id.toolbar_close:
                fragment = (DeviceFragment) fragTabManager.getCurrentFragment();
                if(fragment != null) {
                    closeFragment(fragment);
                } else {
                    finish();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        ViseLog.e("MainActivity.onDestroy()");
        super.onDestroy();
        //DeviceManager.removeCommonListenerForAllDevices(this);

        Intent stopIntent = new Intent(MainActivity.this, NotificationService.class);
        stopService(stopIntent);

        //DeviceManager.clear();

        MyApplication.killProcess();
    }

    @Override
    public void onBackPressed() {
        switchDrawer(!drawerLayout.isDrawerOpen(GravityCompat.START));
    }

    // 设备状态更新
    @Override
    public void onConnectStateUpdated(final IDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 更新设备的Fragment界面
                DeviceFragment deviceFrag = fragTabManager.findFragment(device);
                if(deviceFrag != null) deviceFrag.updateState();
                if(fragTabManager.isFragmentSelected(device)) {
                    updateConnectFAButton(device.getConnectState().getIcon());
                    updateCloseMenuItem(true);
                    if(!MyApplication.isRunInBackground())
                        Toast.makeText(MainActivity.this, device.getConnectState().getDescription(), Toast.LENGTH_SHORT).show();
                }
                // 更新设备列表Adapter
                updateDeviceList();
            }
        });
    }

    // 电量更新
    @Override
    public void onBatteryLevelUpdated(final IDevice device) {
        if(fragTabManager.isFragmentSelected(device)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tbManager.setBattery(device.getBatteryLevel());
                }
            });
        }
    }

    @Override
    public void onNotificationInfoUpdated(IDevice device) {

    }

    // Fragment更新
    @Override
    public void onFragmentUpdated() {
        DeviceFragment fragment = (DeviceFragment)fragTabManager.getCurrentFragment();
        IDevice device = (fragment == null) ? null : fragment.getDevice();
        updateMainLayout(device);
    }

    // 打开设备
    public void openDevice(IDevice device) {
        if(device == null) return;

        DeviceFragment fragment = fragTabManager.findFragment(device);
        if(fragment != null) {
            switchDrawer(false);
            fragTabManager.showFragment(fragment);
        } else {
            if(device.getConnectState() == CLOSED)
                createAndOpenFragment(device);
        }
    }

    private void createAndOpenFragment(IDevice device) {
        if(device == null) return;

        DeviceFactory factory = DeviceFactory.getFactory(device.getCommonInfo());
        if(factory != null) {
            switchDrawer(false);
            Drawable drawable;
            if(TextUtils.isEmpty(device.getIcon())) {
                DeviceType deviceType = DeviceType.getFromUuid(device.getUuid());
                if(deviceType == null) {
                    throw new IllegalStateException("The device type is not supported.");
                }
                drawable = ContextCompat.getDrawable(this, deviceType.getDefaultIcon());
            } else {
                drawable = new BitmapDrawable(getResources(), device.getIcon());
            }
            if(drawable == null) {
                DeviceType deviceType = DeviceType.getFromUuid(device.getUuid());
                if(deviceType == null) {
                    throw new IllegalStateException("The device type is not supported.");
                }
                drawable = ContextCompat.getDrawable(this, deviceType.getDefaultIcon());
            }
            fragTabManager.openFragment(factory.createFragment(), drawable, device.getName());
            updateMainLayout(device);
        }
    }

    // 关闭Fragment
    private void closeFragment(final DeviceFragment fragment) {
        if(fragment != null)
            fragment.close();
    }

    // 移除Fragment
    public void removeFragment(DeviceFragment fragment) {
        fragTabManager.removeFragment(fragment);
    }

    // 从设备列表中删除设备
    public void removeRegisteredDevice(final IDevice device) {
        if(device == null) return;

        if(fragTabManager.isFragmentOpened(device)) {
            Toast.makeText(MainActivity.this, R.string.pls_close_device_firstly, Toast.LENGTH_SHORT).show();
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_device).setMessage(R.string.wont_delete_info_by_device);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(device.getCommonInfo() instanceof BleDeviceCommonInfo) {
                    if(LitePal.delete(BleDeviceCommonInfo.class, device.getCommonInfo().getId()) != 0) {
                        DeviceManager.deleteDevice(device);
                        updateDeviceList();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.operation_failure, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }

    // 修改设备注册信息 
    public void modifyDeviceInfo(final DeviceCommonInfo deviceCommonInfo) {
        Intent intent = new Intent(this, DeviceInfoActivity.class);
        intent.putExtra(DEVICE_INFO, deviceCommonInfo);
        startActivityForResult(intent, RC_MODIFY_DEVICE_INFO);
    }

    private void initMainLayout() {
        TextView tvVersionName = noDeviceOpenLayout.findViewById(R.id.tv_version);
        tvVersionName.setText(APKVersionCodeUtils.getVerName(this));
        updateMainLayout(null);
    }

    // 打开或关闭侧滑菜单
    private void switchDrawer(boolean isOpen) {
        if(isOpen) {
            drawerLayout.openDrawer(GravityCompat.START, false);
        } else {
            drawerLayout.closeDrawer(GravityCompat.START, false);
        }
    }

    // change account
    private void changeAccount() {
        if(DeviceManager.hasDeviceOpen()) {
            Toast.makeText(this, R.string.pls_close_device_firstly, Toast.LENGTH_SHORT).show();
            return;
        }

        AccountManager.logout(true);

        final Intent intent = getPackageManager().getLaunchIntentForPackage(getApplicationContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void updateNavigationHeader() {
        User account = AccountManager.getAccount();
        if(account == null) {
            Toast.makeText(this, R.string.login_failure, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            String name = account.getName();
            if(TextUtils.isEmpty(name)) {
                tvAccountName.setText(R.string.anonymous);
            } else {
                tvAccountName.setText(name);
            }

            if(TextUtils.isEmpty(account.getIcon())) {
                ivAccountImage.setImageResource(SUPPORT_LOGIN_PLATFORM.get(account.getPlatName()));
            } else {
                Glide.with(this).load(account.getIcon()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivAccountImage);
            }
        }
    }

    // 更新MainLayout的可视性
    private void updateMainLayoutVisibility(boolean hasDeviceOpened) {
        if(hasDeviceOpened && hasDeviceOpenLayout.getVisibility() == View.INVISIBLE) {
            noDeviceOpenLayout.setVisibility(View.INVISIBLE);
            hasDeviceOpenLayout.setVisibility(View.VISIBLE);
        } else if(!hasDeviceOpened && hasDeviceOpenLayout.getVisibility() == View.VISIBLE){
            noDeviceOpenLayout.setVisibility(View.VISIBLE);
            hasDeviceOpenLayout.setVisibility(View.INVISIBLE);
        }
    }

    // 更新连接浮动动作按钮
    private void updateConnectFAButton(int icon) {
        fabConnect.clearAnimation();
        fabConnect.setImageResource(icon);
        if(fabConnect.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) fabConnect.getDrawable()).start();
        }
    }

    private void updateCloseMenuItem(boolean visible) {
        tbManager.updateMenuVisible(1, visible);
    }
}
