package com.cmtech.android.bledevice.ecgmonitor.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.R;

import java.io.IOException;

public class EcgFileActivity extends AppCompatActivity {

    private EcgFile file;
    private TextView tvModifyTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg_file);

        String fileName = getIntent().getStringExtra("file_name");
        try {
            file = EcgFile.open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }

        tvModifyTime = findViewById(R.id.tv_modify_time);
        tvModifyTime.setText(file.getFileName());
    }
}
