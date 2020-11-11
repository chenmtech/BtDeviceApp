package com.cmtech.android.bledeviceapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.WebFailureHandler;

import java.util.Date;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.model.Account.FEMALE;
import static com.cmtech.android.bledeviceapp.model.Account.MALE;

/**
 *  AccountActivity: 账户设置Activity
 *  Created by bme on 2018/10/27.
 */

public class PersonInfoActivity extends AppCompatActivity {
    private EditText etUserName;
    private RadioGroup rgGender;
    private DatePicker dpBirthday;
    private EditText etWeight;
    private EditText etHeight;

    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);

        if (!MyApplication.getAccountManager().isValid()) {
            Toast.makeText(this, R.string.login_failure, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        account = MyApplication.getAccount();

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_set_person_info);
        setSupportActionBar(toolbar);

        etUserName = findViewById(R.id.et_account_user_name);
        rgGender = findViewById(R.id.rg_gender);
        dpBirthday = findViewById(R.id.dp_birthday);
        etWeight = findViewById(R.id.et_weight);
        etHeight = findViewById(R.id.et_height);

        updateUI();

        Button btnOk = findViewById(R.id.btn_person_info_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int weight;
                int height;
                try{
                    weight = Integer.parseInt(etWeight.getText().toString().trim());
                    height = Integer.parseInt(etHeight.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(PersonInfoActivity.this, "数据格式错误，请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                }
                int gender = 0;
                int checkId = rgGender.getCheckedRadioButtonId();
                if(checkId == R.id.rb_male) {
                    gender = 1;
                } else if(checkId == R.id.rb_female) {
                    gender = 2;
                }
                account.setGender(gender);

                Date date = new Date(dpBirthday.getYear(), dpBirthday.getMonth(), dpBirthday.getDayOfMonth());
                account.setBirthday(date.getTime());

                account.setWeight(weight);
                account.setHeight(height);

                account.save();

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
    }

    private void updateUI() {
        etUserName.setText(account.getUserName());
        int gender = account.getGender();
        if(gender == MALE)
            rgGender.check(R.id.rb_male);
        else if(gender == FEMALE)
            rgGender.check(R.id.rb_female);
        else rgGender.clearCheck();

        Date date;
        if(account.getBirthday() <= 0) {
            date = new Date(1990,0,1);
        } else {
            date = new Date(account.getBirthday());
        }
        dpBirthday.init(date.getYear(), date.getMonth(), date.getDate(), null);
        etWeight.setText(""+account.getWeight());
        etHeight.setText(""+account.getHeight());
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
}
