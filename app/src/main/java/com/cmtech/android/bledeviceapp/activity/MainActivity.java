package com.cmtech.android.bledeviceapp.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.cmtech.android.bledeviceapp.model.AbstractBleDeviceFactory;
import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.ble.extend.BleDeviceBasicInfo;
import com.cmtech.android.bledevice.ecgmonitor.activity.EcgFileExplorerActivity;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.BleDeviceListAdapter;
import com.cmtech.android.bledeviceapp.model.BleDeviceFragmentManager;
import com.cmtech.android.bledeviceapp.model.BleDeviceService;
import com.cmtech.android.bledeviceapp.model.MainToolbarManager;
import com.cmtech.android.bledeviceapp.model.MyFragmentManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.cmtech.android.bledeviceapp.util.APKVersionCodeUtils;
import com.vise.log.ViseLog;

import java.io.Serializable;
import java.util.List;

import static com.cmtech.android.ble.extend.BleDeviceConnectState.CONNECT_CLOSED;
import static com.cmtech.android.bledeviceapp.activity.DeviceBasicInfoActivity.DEVICE_BASICINFO;
import static com.cmtech.android.bledeviceapp.activity.SearchDeviceActivity.REGISTED_DEVICE_MAC_LIST;

/**
 *  MainActivity: 主界面
 *  Created by bme on 2018/2/19.
 */

public class MainActivity extends AppCompatActivity implements IBleDeviceFragmentActivity {
    private static final String TAG = "MainActivity";

    private final static int RC_REGISTER_DEVICE = 1;     // 登记设备返回码
    private final static int RC_MODIFY_DEVICEINFO = 2;       // 修改设备基本信息返回码
    private final static int RC_MODIFY_USERINFO = 3;     // 修改用户信息返回码

    private final static SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

    private BleDeviceService deviceService; // 设备服务

    private BleDeviceListAdapter deviceListAdapter; // 已登记设备列表的Adapter

    private RecyclerView rvDeviceList; // 已登记设备列表的RecyclerView

    private Toolbar toolbar; // 工具条

    private DrawerLayout drawerLayout; // 侧滑界面

    private LinearLayout noDeviceOpenedLayout; // 无设备打开时的界面

    private RelativeLayout deviceOpenedLayout; // 有设备打开时的界面，包含设备Fragment和Tablayout的主界面

    private FloatingActionButton fabConnect; // 切换连接状态的FAB

    private FloatingActionButton fabExit;

    private BleDeviceFragmentManager fragmentManager; // TabLayout和Fragment管理器

    private MenuItem menuConfig; // 工具条上的配置菜单

    private MenuItem menuClose;

    private TextView tvUserName; // 账户名称控件

    private ImageView ivUserPortrait; // 头像控件

    private boolean isFinishService = false; // 是否结束服务

    private ObjectAnimator connectFabAnimator; // 连接动作按钮的动画

    private TextView tvDeviceBattery;

    private NavigationView navView;

    private MainToolbarManager toolbarManager;

    private ServiceConnection deviceServiceConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            deviceService = ((BleDeviceService.DeviceServiceBinder)iBinder).getService();
            // 成功绑定后初始化
            if(deviceService != null) {
                initializeView();
            } else {
                requestFinish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if(deviceService != null) {
                Intent stopIntent = new Intent(MainActivity.this, BleDeviceService.class);
                stopService(stopIntent);
                deviceService = null;
            }
            finish();
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

        // 启动并绑定服务
        Intent startService = new Intent(this, BleDeviceService.class);
        startService(startService);
        bindService(startService, deviceServiceConnect, BIND_AUTO_CREATE);
    }

    // 主界面初始化
    private void initializeView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvDeviceBattery = findViewById(R.id.tv_device_battery);

        toolbarManager = new MainToolbarManager(this, toolbar, tvDeviceBattery);

        rvDeviceList = findViewById(R.id.rv_registed_device);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvDeviceList.setLayoutManager(layoutManager);
        rvDeviceList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        deviceListAdapter = new BleDeviceListAdapter(deviceService.getDeviceList(), this);
        rvDeviceList.setAdapter(deviceListAdapter);


        navView = findViewById(R.id.nav_view);

        initNavigation();

        updateNavigationView();

        toolbarManager.setNavigationIcon(UserManager.getInstance().getUser().getPortrait());

        // 设置FAB，FloatingActionButton
        fabConnect = findViewById(R.id.fab_connect);
        fabConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleDeviceFragment fragment = (BleDeviceFragment) fragmentManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.switchDeviceState();
                }
            }
        });

        fabExit = findViewById(R.id.fab_exit);
        fabExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleDeviceFragment fragment = (BleDeviceFragment) fragmentManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.close();
                }
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        noDeviceOpenedLayout = findViewById(R.id.layout_nodeivce_opened);
        deviceOpenedLayout = findViewById(R.id.layout_when_device_opened);

        // 创建Fragment管理器
        TabLayout tabLayout = findViewById(R.id.tablayout_device);

        fragmentManager = new BleDeviceFragmentManager(getSupportFragmentManager(), tabLayout, R.id.layout_main_fragment);

        fragmentManager.setOnFragmentChangedListener(new MyFragmentManager.OnFragmentChangedListener() {
            @Override
            public void onFragmentchanged() {
                BleDevice device = (fragmentManager.size() == 0) ? null : ((BleDeviceFragment) fragmentManager.getCurrentFragment()).getDevice();

                updateMainLayout(device);
            }
        });


        // 初始化主界面
        initMainLayout();

        // 为已经打开的设备创建并打开Fragment
        for(BleDevice device : deviceService.getDeviceList()) {
            if(!device.isClosed()) {
                createAndOpenFragment(device);
            }
        }

        User user = UserManager.getInstance().getUser();
        if(user.getNickname() == null || "".equals(user.getNickname().trim())) {
            Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);

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
                Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);

                startActivityForResult(intent, RC_MODIFY_USERINFO);
            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_search_device:
                        List<String> deviceMacList = deviceService.getDeviceMacList();

                        Intent scanIntent = new Intent(MainActivity.this, SearchDeviceActivity.class);

                        scanIntent.putExtra(REGISTED_DEVICE_MAC_LIST, (Serializable) deviceMacList);

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

            updateConnectFloatingActionButton(CONNECT_CLOSED.getIcon(), false);

            invalidateOptionsMenu();

            updateMainLayoutVisibility(false);
        } else {
            toolbarManager.setTitle(device.getNickName(), device.getMacAddress());
            toolbarManager.setBattery(device.getBattery());

            updateConnectFloatingActionButton(device.getConnectStateIcon(), device.isWaitingResponse());

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
                    BleDeviceBasicInfo basicInfo = (BleDeviceBasicInfo) data.getSerializableExtra(DEVICE_BASICINFO);
                    if(basicInfo != null) {
                        BleDevice device = deviceService.createAndAddDevice(basicInfo);
                        if(device != null) {
                            if(basicInfo.saveToPref(pref)) {
                                Toast.makeText(MainActivity.this, "设备登记成功", Toast.LENGTH_SHORT).show();
                                if(deviceListAdapter != null) deviceListAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(MainActivity.this, "设备登记失败", Toast.LENGTH_SHORT).show();
                                deviceService.deleteDevice(device);
                            }
                        }
                    }
                }
                break;

            case RC_MODIFY_DEVICEINFO:

                if ( resultCode == RESULT_OK) {
                    BleDeviceBasicInfo basicInfo = (BleDeviceBasicInfo) data.getSerializableExtra(DEVICE_BASICINFO);
                    BleDevice device = deviceService.findDevice(basicInfo);

                    if(device != null && basicInfo.saveToPref(pref)) {
                        Toast.makeText(MainActivity.this, "设备信息修改成功", Toast.LENGTH_SHORT).show();

                        device.setBasicInfo(basicInfo);

                        if(deviceListAdapter != null) deviceListAdapter.notifyDataSetChanged();

                        fragmentManager.updateTabInfo(fragmentManager.findOpenedFragment(device), device.getImageDrawable(this), device.getNickName());

                        if(fragmentManager.isDeviceFragmentSelected(device)) {
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

        menuConfig = menu.findItem(R.id.toolbar_config);

        menuClose = menu.findItem(R.id.toolbar_close);

        toolbarManager.setMenuItems(menuConfig, menuClose);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(fragmentManager.size() == 0) {
            toolbarManager.updateMenuItem(false, true);
        } else {
            toolbarManager.updateMenuItem(true, false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BleDeviceFragment fragment;
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer(true);
                break;

            case R.id.toolbar_config:
                fragment = (BleDeviceFragment) fragmentManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.openConfigActivity();
                }
                break;

            case R.id.toolbar_close:
                fragment = (BleDeviceFragment) fragmentManager.getCurrentFragment();
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
        ViseLog.e(TAG + ":onDestroy");
        super.onDestroy();

        for(BleDevice device : deviceService.getDeviceList()) {
            device.removeConnectStateListener(this);
        }

        unbindService(deviceServiceConnect);

        //isFinishService = true;
        if(isFinishService) {
            Intent stopIntent = new Intent(MainActivity.this, BleDeviceService.class);
            stopService(stopIntent);
        }
    }

    private void requestFinish() {
        if(deviceService.hasDeviceOpened()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("退出应用");
            builder.setMessage("有设备打开，退出将关闭这些设备。");
            builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    isFinishService = true;
                    finish();
                }
            });
            builder.setNegativeButton("最小化", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    isFinishService = false;
                    openDrawer(false);
                    MainActivity.this.moveTaskToBack(true);
                }
            });
            builder.show();
        } else {
            isFinishService = true;
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
        if(deviceListAdapter != null) deviceListAdapter.notifyDataSetChanged();

        // 更新设备的Fragment界面
        BleDeviceFragment deviceFrag = fragmentManager.findOpenedFragment(device);

        if(deviceFrag != null) deviceFrag.updateState();

        if(fragmentManager.isDeviceFragmentSelected(device)) {
            updateConnectFloatingActionButton(device.getConnectStateIcon(), device.isWaitingResponse());
        }
    }

    @Override
    public void onReconnectFailureNotified(final BleDevice device, boolean warn) {
        if(!warn) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设备断开报警");
        builder.setMessage("设备" + device.getMacAddress() + "无法连接，已经断开。");
        builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                device.notifyReconnectFailure(false);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void onBatteryUpdated(final BleDevice device) {
        if(fragmentManager.isDeviceFragmentSelected(device)) {
            toolbarManager.setBattery(device.getBattery());
        }
    }

    @Override
    public BleDevice findDevice(String macAddress) {
        return (deviceService == null) ? null : deviceService.findDevice(macAddress);
    }

    @Override
    public void closeFragment(final BleDeviceFragment fragment) {
        BleDevice device = fragment.getDevice();

        if(device != null) {
            deviceService.closeDevice(device);

            fragmentManager.deleteFragment(fragment);
        }
    }

    // 打开设备：为设备创建并打开Fragment
    public void openDevice(BleDevice device) {
        if(device == null) return;

        BleDeviceFragment fragment = fragmentManager.findOpenedFragment(device);
        if(fragment != null) {
            openDrawer(false);
            fragmentManager.showFragment(fragment);

        } else if(device.isClosed()){
            createAndOpenFragment(device);
        }
    }

    private void createAndOpenFragment(BleDevice device) {
        AbstractBleDeviceFactory factory = AbstractBleDeviceFactory.getBLEDeviceFactory(device);
        if(factory != null) {
            drawerLayout.closeDrawer(GravityCompat.START);

            // 添加设备的Fragment到管理器
            fragmentManager.addFragment(factory.createFragment(), device.getImageDrawable(this), device.getNickName());

            updateMainLayout(device);
        }
    }

    // 删除设备
    public void deleteDevice(final BleDevice device) {
        if(device == null) return;

        if(fragmentManager.isDeviceFragmentOpened(device)) {
            Toast.makeText(this, "请先关闭设备。", Toast.LENGTH_SHORT).show();
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除设备");
        builder.setMessage("确定删除设备：" + device.getMacAddress()+'\n'+device.getNickName());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(device.getBasicInfo().deleteFromPref(pref)) {
                    deviceService.deleteDevice(device);
                    if(deviceListAdapter != null) deviceListAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "无法删除该设备。", Toast.LENGTH_SHORT).show();
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

    // 修改设备基本信息 
    public void modifyDeviceBasicInfo(final BleDeviceBasicInfo basicInfo) {
        Intent intent = new Intent(this, DeviceBasicInfoActivity.class);
        intent.putExtra(DEVICE_BASICINFO, basicInfo);

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
        if(deviceService.hasDeviceOpened()) {
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

        if(user.getNickname() == null || "".equals(user.getNickname().trim())) {
            tvUserName.setText("请设置");
        } else {
            tvUserName.setText(user.getNickname());
        }

        String imagePath = user.getPortrait();

        if(imagePath != null && !"".equals(imagePath))
            Glide.with(MyApplication.getContext()).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivUserPortrait);
    }

    // 更新MainLayout的可视性
    private void updateMainLayoutVisibility(boolean hasDeviceOpened) {
        if(hasDeviceOpened && deviceOpenedLayout.getVisibility() == View.INVISIBLE) {
            noDeviceOpenedLayout.setVisibility(View.INVISIBLE);
            deviceOpenedLayout.setVisibility(View.VISIBLE);
        } else if(!hasDeviceOpened && deviceOpenedLayout.getVisibility() == View.VISIBLE){
            noDeviceOpenedLayout.setVisibility(View.VISIBLE);
            deviceOpenedLayout.setVisibility(View.INVISIBLE);
        }
    }

    // 更新浮动动作按钮
    private void updateConnectFloatingActionButton(int icon, boolean isRotate) {
        float degree = 360.0f;

        long duration = 3000;

        int count = ValueAnimator.INFINITE;

        if(!isRotate) {
            degree = 0.0f;

            duration = 100;

            count = 0;
        }

        fabConnect.clearAnimation();

        fabConnect.setImageResource(icon);

        connectFabAnimator = ObjectAnimator.ofFloat(fabConnect, "rotation", 0.0f, degree).setDuration(duration);

        connectFabAnimator.setRepeatCount(count);

        connectFabAnimator.setAutoCancel(true);

        connectFabAnimator.start();
    }

}
