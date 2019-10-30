package com.cmtech.android.bledevice.ecgmonitor.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.io.IOException;

public class EcgRecordActivity extends AppCompatActivity {
    private EcgFile file;
    TextView tvModifyTime; // 更新时间
    TextView tvCreator; // 创建人
    TextView tvCreateTime; // 创建时间
    TextView tvLength; // 信号长度
    TextView tvHrNum; // 心率次数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg_record);

        String fileName = getIntent().getStringExtra("file_name");
        try {
            file = EcgFile.open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }

        tvModifyTime = findViewById(R.id.tv_modify_time);
        tvCreator = findViewById(R.id.tv_creator);
        tvCreateTime = findViewById(R.id.tv_create_time);
        tvLength = findViewById(R.id.tv_signal_length);
        tvHrNum = findViewById(R.id.tv_hr_num);

        tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(file.getFile().lastModified()));

        User fileCreator = file.getCreator();
        User account = UserManager.getInstance().getUser();
        if(fileCreator.equals(account)) {
            tvCreator.setText(Html.fromHtml("<u>您本人</u>"));
        } else {
            tvCreator.setText(Html.fromHtml("<u>" + file.getCreatorName() + "</u>"));
        }

        String createdTime = DateTimeUtil.timeToShortStringWithTodayYesterday(file.getCreatedTime());
        tvCreateTime.setText(createdTime);

        if(file.getDataNum() == 0) {
            tvLength.setText("无");
        } else {
            String dataTimeLength = DateTimeUtil.secToTimeInChinese(file.getDataNum() / file.getSampleRate());
            tvLength.setText(dataTimeLength);
        }

        int hrNum = file.getHrList().size();
        tvHrNum.setText(String.valueOf(hrNum));

    }
}
