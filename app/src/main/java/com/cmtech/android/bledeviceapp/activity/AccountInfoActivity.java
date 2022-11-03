package com.cmtech.android.bledeviceapp.activity;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.CtrlPanelAdapter;
import com.cmtech.android.bledeviceapp.fragment.AccountPrivateInfoFragment;
import com.cmtech.android.bledeviceapp.fragment.AccountPublicInfoFragment;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  AccountInfoActivity: 账户信息Activity
 *  Created by bme on 2018/10/27.
 */

public class AccountInfoActivity extends AppCompatActivity {
    private EditText etUserName;
    private EditText etUserId;
    private ViewPager pager;
    private CtrlPanelAdapter fragAdapter;
    private final AccountPrivateInfoFragment privateInfoFrag = new AccountPrivateInfoFragment();
    private final AccountPublicInfoFragment publicInfoFrag = new AccountPublicInfoFragment();

    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);

        if (!MyApplication.getAccountManager().isValid()) {
            Toast.makeText(this, "无效账户", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        account = MyApplication.getAccount();

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_set_person_info);
        setSupportActionBar(toolbar);

        etUserName = findViewById(R.id.et_account_user_name);
        etUserName.setText(account.getUserName());

        etUserId = findViewById(R.id.et_account_user_id);
        etUserId.setText(""+account.getAccountId());

        pager = findViewById(R.id.person_info_control_panel_viewpager);
        TabLayout layout = findViewById(R.id.person_info_control_panel_tab);
        List<Fragment> fragmentList = new ArrayList<>(Arrays.asList(publicInfoFrag, privateInfoFrag));
        List<String> titleList = new ArrayList<>(Arrays.asList(AccountPublicInfoFragment.TITLE, AccountPrivateInfoFragment.TITLE));
        fragAdapter = new CtrlPanelAdapter(getSupportFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(2);
        layout.setupWithViewPager(pager);

        Button btnOk = findViewById(R.id.btn_person_info_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publicInfoFrag.processOKButton();
                privateInfoFrag.processOKButton();

                account.upload(AccountInfoActivity.this, new ICodeCallback() {
                    @Override
                    public void onFinish(int code, String msg) {
                        Toast.makeText(AccountInfoActivity.this, msg, Toast.LENGTH_SHORT).show();
                        if (code == RCODE_SUCCESS) {
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });
            }
        });

        Button btnCancel = findViewById(R.id.btn_person_info_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        Button btnChangeAccount = findViewById(R.id.btn_change_account);
        btnChangeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountInfoActivity.this);
                builder.setTitle("切换账户")
                        .setMessage("退出当前账户，重新启动")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (MyApplication.getDeviceManager().hasDeviceOpen()) {
                                    Toast.makeText(AccountInfoActivity.this, R.string.pls_close_device_firstly, Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                MyApplication.getAccountManager().localLogout();
                                restart();
                            }
                        }).show();
            }
        });
    }

    private void updateUI() {
        publicInfoFrag.updateUI();
        privateInfoFrag.updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_modify_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

            case R.id.download_account_info:
                download();
                break;
        }
        return true;
    }

    private void download() {
        account.download(this, "正在下载账户信息，请稍等...", new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                Toast.makeText(AccountInfoActivity.this, msg, Toast.LENGTH_SHORT).show();
                if (code == RCODE_SUCCESS) {
                    updateUI();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void restart() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getApplicationContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        MyApplication.killProcess();
    }
}
