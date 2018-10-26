package com.cmtech.android.bledeviceapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserAccount;

import org.litepal.LitePal;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText etAccount;
    private EditText etPassword;
    private Button btnSignin;
    private Button btnSignup;
    private CheckBox cbRememberPassword;
    private CheckBox cbAutoSignin;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(MainActivity.hasSignin()) {
            enterMainActivity();
            finish();
        }

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        etAccount = findViewById(R.id.account);
        etPassword = findViewById(R.id.password);
        cbRememberPassword = findViewById(R.id.cb_remember_password);

        cbAutoSignin = findViewById(R.id.cb_auto_signin);
        cbAutoSignin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor = pref.edit();
                if(b) {
                    editor.putBoolean("auto_signin", true);
                } else {
                    editor.remove("auto_signin");
                }
                editor.apply();
            }
        });

        btnSignin = findViewById(R.id.btn_account_signin);
        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = etAccount.getText().toString();
                String password = etPassword.getText().toString();
                signin(account, password);
            }
        });

        btnSignup = findViewById(R.id.btn_account_signup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = etAccount.getText().toString();
                String password = etPassword.getText().toString();
                signup(account, password);
            }
        });


        boolean isRemember = pref.getBoolean("remember_password", false);
        if(isRemember) {
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            etAccount.setText(account);
            etPassword.setText(password);
            cbRememberPassword.setChecked(true);
        }

        boolean autoSignin = pref.getBoolean("auto_signin", false);
        if(autoSignin) {
            String account = etAccount.getText().toString();
            String password = etPassword.getText().toString();
            signin(account, password);
            cbAutoSignin.setChecked(true);
        } else {
            cbAutoSignin.setChecked(false);
        }
    }

    private void signup(String account, String password) {
        List<UserAccount> find = LitePal.where("name = ?", account).find(UserAccount.class);
        if(find != null && find.size() > 0) {
            Toast.makeText(LoginActivity.this, "账户已存在。", Toast.LENGTH_SHORT).show();
        } else {
            UserAccount user = new UserAccount();
            user.setName(account);
            user.setPassword(password);
            user.save();
            Toast.makeText(LoginActivity.this, "账户注册成功。", Toast.LENGTH_SHORT).show();
        }
    }

    private void signin(String account, String password) {
        List<UserAccount> find = LitePal.where("name = ? and password = ?", account, password).find(UserAccount.class);
        if(find != null && find.size() == 1) {
            UserAccount user = new UserAccount();
            user.setName(account);
            user.setPassword(password);
            MainActivity.setUserAccount(user);

            editor = pref.edit();
            if(cbRememberPassword.isChecked()) {
                editor.putBoolean("remember_password", true);
                editor.putString("account", account);
                editor.putString("password", password);



            } else {
                editor.remove("remember_password");
                editor.remove("account");
                editor.remove("password");
            }
            editor.apply();

            enterMainActivity();
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "账户和密码不正确。", Toast.LENGTH_SHORT).show();
        }
    }

    private void enterMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        Intent inputIntent = getIntent();
        if(inputIntent != null && inputIntent.getData() != null) {
            intent.setData(inputIntent.getData());
        }
        startActivity(intent);
    }
}
