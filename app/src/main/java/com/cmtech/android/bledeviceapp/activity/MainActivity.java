package com.cmtech.android.bledeviceapp.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.bledevice.SupportedDeviceType;
import com.cmtech.android.bledevice.ecgmonitor.activity.EcgFileExplorerActivity;
import com.cmtech.android.bledevice.ecgmonitor.activity.EcgFileReplayActivity;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.BleDeviceListAdapter;
import com.cmtech.android.bledeviceapp.model.BleDeviceFragmentManager;
import com.cmtech.android.bledeviceapp.model.BleDeviceService;
import com.cmtech.android.bledeviceapp.model.MyFragmentManager;
import com.cmtech.android.bledeviceapp.model.UserAccount;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.android.bledeviceapp.util.APKVersionCodeUtils;
import com.cmtech.android.bledevicecore.AbstractBleDeviceFactory;
import com.cmtech.android.bledevicecore.BleDevice;
import com.cmtech.android.bledevicecore.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.BleDeviceFragment;
import com.cmtech.android.bledevicecore.IBleDeviceFragmentActivity;
import com.vise.log.ViseLog;

import java.io.Serializable;
import java.util.List;

import static com.cmtech.android.bledeviceapp.activity.DeviceBasicInfoActivity.DEVICE_BASICINFO;

/**
 *  MainActivity: 主界面
 *  Created by bme on 2018/2/19.
 */

public class MainActivity extends AppCompatActivity implements IBleDeviceFragmentActivity {
    private static final String TAG = "MainActivity";

    private final static int REQUESTCODE_REGISTERDEVICE = 1;     // 登记设备返回码
    private final static int REQUESTCODE_MODIFYDEVICE = 2;       // 修改设备基本信息返回码
    private final static int REQUESTCODE_MODIFYUSERINFO = 3;     // 修改用户信息返回码

    // 设备管理器
    private BleDeviceService deviceService;

    // 显示已登记设备列表的Adapter和RecyclerView
    private BleDeviceListAdapter deviceListAdapter;
    private RecyclerView rvDeviceList;

    // 工具条
    private Toolbar toolbar;

    // 侧滑界面
    private DrawerLayout drawerLayout;

    // 欢迎界面
    private LinearLayout welcomeLayout;

    // 包含设备Fragment和Tablayout的界面
    private LinearLayout mainLayout;

    // 主界面的TabLayout和Fragment管理器
    private BleDeviceFragmentManager fragmentManager;

    // 工具条上的连接菜单和关闭菜单
    private MenuItem menuSwitch;

    // 显示账户名,用户名和头像
    private TextView tvAccountName;
    private TextView tvUserName;
    private ImageView ivAccountImage;

    private boolean isExit = false;

    private ServiceConnection deviceServiceConnect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            deviceService = ((BleDeviceService.DeviceServiceBinder)iBinder).getService();

            // 成功绑定后初始化
            if(deviceService != null) {
                initialize();
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
        if(!UserAccountManager.getInstance().isSignIn()) {
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
    private void initialize() {
        // 创建ToolBar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置设备信息RecycleView
        rvDeviceList = findViewById(R.id.rv_registerdevice);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvDeviceList.setLayoutManager(layoutManager);
        rvDeviceList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        deviceListAdapter = new BleDeviceListAdapter(deviceService.getDeviceList(), this);
        rvDeviceList.setAdapter(deviceListAdapter);

        // 导航菜单设置
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setCheckedItem(R.id.nav_registerdevice);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_registerdevice:
                        startScanDevice();
                        return true;
                    case R.id.nav_readecgrecord:
                        replayEcg();
                        return true;
                    case R.id.nav_changeuser:
                        changeUser();
                        return true;
                    case R.id.nav_exit:
                        requestFinish();
                        return true;
                }
                return false;
            }
        });

        // 设置导航视图
        View headerView = navView.getHeaderView(0);
        tvAccountName = headerView.findViewById(R.id.accountname);
        tvUserName = headerView.findViewById(R.id.username);
        ivAccountImage = headerView.findViewById(R.id.accountimage);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                startActivityForResult(intent, REQUESTCODE_MODIFYUSERINFO);
            }
        });


        drawerLayout = findViewById(R.id.drawer_layout);
        welcomeLayout = findViewById(R.id.welcome_layout);
        mainLayout = findViewById(R.id.main_layout);


        // 创建Fragment管理器
        TabLayout tabLayout = findViewById(R.id.main_tab_layout);
        fragmentManager = new BleDeviceFragmentManager(getSupportFragmentManager(), tabLayout, R.id.main_fragment_layout);
        fragmentManager.setOnFragmentChangedListener(new MyFragmentManager.OnFragmentChangedListener() {
            @Override
            public void onFragmentchanged() {
                updateToolBar(((BleDeviceFragment) fragmentManager.getCurrentFragment()).getDevice());
            }
        });

        // 更新导航视图
        updateNavigationViewUsingUserInfo();

        // 初始化欢迎界面
        initWelcomeLayout();

        // 更新主界面可视性
        updateMainLayoutVisibility();

        // 为已经打开的设备创建并打开Fragment
        for(BleDevice device : deviceService.getDeviceList()) {
            if(!device.isClosed()) {
                createAndOpenFragment(device);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            // 登记设备返回
            case REQUESTCODE_REGISTERDEVICE:

                if(resultCode == RESULT_OK) {
                    BleDeviceBasicInfo basicInfo = (BleDeviceBasicInfo) data.getSerializableExtra(DEVICE_BASICINFO);
                    if(basicInfo != null) {
                        BleDevice device = deviceService.addDevice(basicInfo);
                        if(device != null) {
                            //device.registerDeviceStateObserver(this);
                            updateDeviceListAdapter();
                            basicInfo.saveToPref();
                        }
                    }
                }
                break;


            // 修改设备基本信息返回
            case REQUESTCODE_MODIFYDEVICE:

                if ( resultCode == RESULT_OK) {
                    BleDeviceBasicInfo basicInfo = (BleDeviceBasicInfo) data.getSerializableExtra(DEVICE_BASICINFO);
                    BleDevice device = deviceService.findDevice(basicInfo);

                    if(device != null) {
                        basicInfo.saveToPref();
                        device.setBasicInfo(basicInfo);
                        deviceListAdapter.notifyDataSetChanged();
                    }
                }
                break;

            // 修改用户信息
            case REQUESTCODE_MODIFYUSERINFO:

                if(resultCode == RESULT_OK) {
                    updateNavigationViewUsingUserInfo();
                }
                break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainactivity_menu, menu);
        menuSwitch = menu.findItem(R.id.toolbar_switch);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        updateMainMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BleDeviceFragment fragment;
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer(true);
                break;

            case R.id.toolbar_switch:
                fragment = (BleDeviceFragment) fragmentManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.switchState();
                }
                break;

            case R.id.toolbar_close:
                fragment = (BleDeviceFragment) fragmentManager.getCurrentFragment();
                if(fragment != null) {
                    deviceService.closeDevice(fragment.getDevice());
                    deleteFragment(fragment);
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
            device.removeDeviceStateObserver(this);
        }

        unbindService(deviceServiceConnect);

        //isExit = false;
        if(isExit) {
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
                    isExit = true;
                    finish();
                }
            });
            builder.setNegativeButton("最小化", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    isExit = false;
                    openDrawer(false);
                    MainActivity.this.moveTaskToBack(true);
                }
            });
            builder.show();
        } else {
            isExit = true;
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        openDrawer(!drawerLayout.isDrawerOpen(GravityCompat.START));
    }


    ////////////////////////////////////////////////////////////
    // IBleDeviceStateObserver接口函数
    ////////////////////////////////////////////////////////////

    // 更新设备状态
    @Override
    public void updateDeviceState(final BleDevice device) {
        // 更新设备列表Adapter
        updateDeviceListAdapter();

        // 更新设备的Fragment界面
        BleDeviceFragment deviceFrag = fragmentManager.findOpenedFragment(device);
        if(deviceFrag != null) deviceFrag.updateDeviceState();  // 暂时没有处理

        // 更新Activity的ToolBar
        BleDeviceFragment currentFrag = (BleDeviceFragment) fragmentManager.getCurrentFragment();
        if(currentFrag != null && deviceFrag == currentFrag) {
            updateToolBar(currentFrag.getDevice());
        }
    }

    @Override
    public void warnDeviceReconnectFailure(final BleDevice device, boolean play) {
        if(!play) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设备断开报警");
        builder.setMessage("设备" + device.getMacAddress() + "无法连接，已经断开。");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                device.notifyReconnectFailureObservers(false);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public BleDevice findDevice(String macAddress) {
        if(deviceService != null)
            return deviceService.findDevice(macAddress);
        return null;
    }

    // 打开设备：为设备创建并打开Fragment
    public void openDevice(BleDevice device) {
        if(device == null) return;

        BleDeviceFragment fragment = fragmentManager.findOpenedFragment(device);
        if(fragment != null) {
            showFragment( fragment );
        } else if(device.isClosed()){
            createAndOpenFragment(device);
        }
    }

    private void createAndOpenFragment(BleDevice device) {
        AbstractBleDeviceFactory factory = AbstractBleDeviceFactory.getBLEDeviceFactory(device);
        if(factory != null) {
            openFragment(factory.createFragment(), device.getNickName());
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
        builder.setTitle("确定删除该设备吗？");
        builder.setMessage(device.getMacAddress()+'\n'+device.getNickName());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                device.getBasicInfo().deleteFromPref();
                deviceService.deleteDevice(device);
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
    public void modifyDeviceBasicInfo(final BleDeviceBasicInfo basicInfo) {
        Intent intent = new Intent(this, DeviceBasicInfoActivity.class);
        intent.putExtra(DEVICE_BASICINFO, basicInfo);

        startActivityForResult(intent, REQUESTCODE_MODIFYDEVICE);
    }

    private void initWelcomeLayout() {
        // 设置欢迎词
        String welcomeText = getResources().getString(R.string.welcome_text);
        welcomeText = String.format(welcomeText, getResources().getString(R.string.app_name));
        TextView tvWelcomeText = welcomeLayout.findViewById(R.id.tv_welcometext);
        tvWelcomeText.setText(welcomeText);
        TextView tvVersionName = welcomeLayout.findViewById(R.id.tv_versionname);
        tvVersionName.setText("Ver"+APKVersionCodeUtils.getVerName(this));
    }

    // 更新主Layout的可视性
    private void updateMainLayoutVisibility() {
        if(fragmentManager.size() == 0) {
            welcomeLayout.setVisibility(View.VISIBLE);
            mainLayout.setVisibility(View.INVISIBLE);
            setTitle(R.string.app_name);
            Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.ic_kang);
            toolbar.setLogo(drawable);
        } else {
            welcomeLayout.setVisibility(View.INVISIBLE);
            mainLayout.setVisibility(View.VISIBLE);
        }
    }

    // 打开或关闭侧滑菜单
    private void openDrawer(boolean open) {
        if(open) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    // 心电信号回放
    private void replayEcg() {
        Intent intent = new Intent(MainActivity.this, EcgFileExplorerActivity.class);
        startActivity(intent);
    }

    // 指定心电信号文件的回放
    private void replayEcgFile(String fileName) {
        Intent intent = new Intent(MainActivity.this, EcgFileReplayActivity.class);
        intent.putExtra("fileName", fileName);
        startActivity(intent);
    }

    // 切换用户
    private void changeUser() {
        if(deviceService.hasDeviceOpened()) {
            Toast.makeText(this, "请先关闭设备。", Toast.LENGTH_SHORT).show();
            return;
        }

        UserAccountManager.getInstance().signOut();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("auto_signin", false);
        editor.apply();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        requestFinish();
    }

    // 开始扫描设备
    private void startScanDevice() {
        List<String> deviceMacList = deviceService.getDeviceMacList();

        Intent intent = new Intent(MainActivity.this, ScanDeviceActivity.class);
        intent.putExtra("registered_device_list", (Serializable) deviceMacList);

        startActivityForResult(intent, REQUESTCODE_REGISTERDEVICE);
    }

    // 打开Fragment：将Fragment加入Manager，并显示
    private void openFragment(BleDeviceFragment fragment, String tabText) {
        drawerLayout.closeDrawer(GravityCompat.START);
        // 添加设备的Fragment到管理器
        fragmentManager.addFragment(fragment, "", tabText);
        updateMainLayoutVisibility();
        invalidateOptionsMenu();
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
        //updateMainMenu();
        invalidateOptionsMenu();
    }

    // 更新导航视图
    private void updateNavigationViewUsingUserInfo() {
        UserAccount account = UserAccountManager.getInstance().getUserAccount();
        if(account == null) return;
        tvAccountName.setText(account.getAccountName());
        tvUserName.setText(account.getUserName());
        String imagePath = account.getImagePath();
        if(!"".equals(imagePath))
            Glide.with(MyApplication.getContext()).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivAccountImage);
    }

    // 更新主菜单
    private void updateMainMenu() {
        if(fragmentManager.size() == 0) {
            menuSwitch.setVisible(false);
        } else {
            menuSwitch.setVisible(true);

            BleDeviceFragment currentFrag = (BleDeviceFragment) fragmentManager.getCurrentFragment();
            if(currentFrag != null && currentFrag.getDevice() != null) {
                // 更新连接转换菜单menuSwitch
                // menuSwitch图标如果是动画，先停止动画
                /*if(menuSwitch.getIcon() instanceof AnimationDrawable) {
                    AnimationDrawable connectingDrawable = (AnimationDrawable) menuSwitch.getIcon();
                    if(connectingDrawable.isRunning())
                        connectingDrawable.stop();
                }*/
                menuSwitch.setIcon(currentFrag.getDevice().getStateIcon());
                /*// 如果menuSwitch图标是动画，则启动动画
                if(menuSwitch.getIcon() instanceof AnimationDrawable) {
                    AnimationDrawable connectingDrawable = (AnimationDrawable) menuSwitch.getIcon();
                    if(!connectingDrawable.isRunning())
                        connectingDrawable.start();
                }*/
            }
        }
    }

    // 更新设备列表
    private void updateDeviceListAdapter() {
        if(deviceListAdapter != null) deviceListAdapter.notifyDataSetChanged();
    }

    // 更新工具条
    private void updateToolBar(BleDevice device) {
        if(device == null) return;

        // 更新工具条菜单
        invalidateOptionsMenu();

        // 更新工具条图标
        Drawable drawable;
        String imagePath = device.getImagePath();
        if(imagePath != null && !"".equals(imagePath)) {
            drawable = new BitmapDrawable(MyApplication.getContext().getResources(), device.getImagePath());
        } else {
            drawable = ContextCompat.getDrawable(this, SupportedDeviceType.getDeviceTypeFromUuid(device.getUuidString()).getDefaultImage());
        }
        toolbar.setLogo(drawable);
        toolbar.setLogoDescription(device.getNickName());

        // 更新工具条Title
        toolbar.setTitle(device.getConnectState().getDescription());
    }
}
