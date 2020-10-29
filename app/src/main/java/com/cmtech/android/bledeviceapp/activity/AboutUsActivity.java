package com.cmtech.android.bledeviceapp.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;

public class AboutUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        TextView tvPrivacy = findViewById(R.id.tv_privacy);
        tvPrivacy.setMovementMethod(LinkMovementMethod.getInstance());

        TextView tvAgreement = findViewById(R.id.tv_agreement);
        tvAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
