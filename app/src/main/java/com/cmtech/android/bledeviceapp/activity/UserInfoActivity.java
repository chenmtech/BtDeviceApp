package com.cmtech.android.bledeviceapp.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserAccount;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;

public class UserInfoActivity extends AppCompatActivity {
    private EditText etUserName;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        EditText etAccountName = findViewById(R.id.et_userinfo_accountname);
        EditText etPassword = findViewById(R.id.et_userinfo_password);
        etUserName = findViewById(R.id.et_userinfo_username);

        if(UserAccountManager.getInstance().isSignIn()) {
            etAccountName.setText(UserAccountManager.getInstance().getUserAccount().getAccountName());
            etPassword.setText(UserAccountManager.getInstance().getUserAccount().getPassword());
            etUserName.setText(UserAccountManager.getInstance().getUserAccount().getUserName());
        }

        btnSave = findViewById(R.id.btn_userinfo_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserAccount account = UserAccountManager.getInstance().getUserAccount();
                account.setUserName(etUserName.getText().toString());
                account.save();
            }
        });
    }
}
