package com.cmtech.android.bledeviceapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.CtrlPanelAdapter;
import com.cmtech.android.bledeviceapp.fragment.AccountInfoFragment;
import com.cmtech.android.bledeviceapp.fragment.PersonInfoFragment;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.WebFailureHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

/**
 *  PersonInfoActivity: 账户及个人信息Activity
 *  Created by bme on 2018/10/27.
 */

public class PersonInfoActivity extends AppCompatActivity {
    private EditText etUserName;

    private ViewPager pager;
    private CtrlPanelAdapter fragAdapter;
    private final PersonInfoFragment personInfoFrag = new PersonInfoFragment();
    private final AccountInfoFragment accountInfoFrag = new AccountInfoFragment();

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

        pager = findViewById(R.id.person_info_control_panel_viewpager);
        TabLayout layout = findViewById(R.id.person_info_control_panel_tab);
        List<Fragment> fragmentList = new ArrayList<>(Arrays.asList(accountInfoFrag, personInfoFrag));
        List<String> titleList = new ArrayList<>(Arrays.asList(AccountInfoFragment.TITLE, PersonInfoFragment.TITLE));
        fragAdapter = new CtrlPanelAdapter(getSupportFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(2);
        layout.setupWithViewPager(pager);

        Button btnOk = findViewById(R.id.btn_person_info_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountInfoFrag.processOKButton();
                personInfoFrag.processOKButton();

                account.upload(PersonInfoActivity.this, new ICodeCallback() {
                    @Override
                    public void onFinish(int code) {
                        if (code == RETURN_CODE_SUCCESS) {
                            Toast.makeText(PersonInfoActivity.this, "账户信息已更新。", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        } else
                            Toast.makeText(PersonInfoActivity.this, WebFailureHandler.handle(code), Toast.LENGTH_SHORT).show();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(PersonInfoActivity.this);
                builder.setTitle("切换账户")
                        .setMessage("退出当前账户，重新启动")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (MyApplication.getDeviceManager().hasDeviceOpen()) {
                                    Toast.makeText(PersonInfoActivity.this, R.string.pls_close_device_firstly, Toast.LENGTH_SHORT).show();
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
        accountInfoFrag.updateUI();
        personInfoFrag.updateUI();
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
        account.download(this, new ICodeCallback() {
            @Override
            public void onFinish(int code) {
                if (code == RETURN_CODE_SUCCESS) {
                    updateUI();
                } else {
                    Toast.makeText(PersonInfoActivity.this, WebFailureHandler.handle(code), Toast.LENGTH_SHORT).show();
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
