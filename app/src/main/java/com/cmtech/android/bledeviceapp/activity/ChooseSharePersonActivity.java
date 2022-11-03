package com.cmtech.android.bledeviceapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.SharePersonAdapter;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.ContactPerson;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 *  DeviceInfoActivity: device info activity, used to set up the BleDeviceInfo class
 *  Created by bme on 2018/6/27.
 */

public class ChooseSharePersonActivity extends AppCompatActivity {
    private SharePersonAdapter sharePersonAdapter;
    private RecyclerView rvSharePerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_choose_share_person);

        List<Integer> shareIds = MyApplication.getAccount().getCanShareToIdList();

        if(shareIds.isEmpty()) {
            Toast.makeText(this, "不能分享记录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<ContactPerson> cps = new ArrayList<>();
        for(int id : shareIds) {
            ContactPerson cp = LitePal.where("accountId = ?", ""+id).findFirst(ContactPerson.class);
            if(cp != null)
                cps.add(cp);
        }

        // 初始化扫描设备列表
        rvSharePerson = findViewById(R.id.rv_share_person);
        rvSharePerson.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvSharePerson.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        sharePersonAdapter = new SharePersonAdapter(cps);
        rvSharePerson.setAdapter(sharePersonAdapter);

        Button btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = sharePersonAdapter.getSelectContactPersonId();
                Intent intent = new Intent();
                intent.putExtra("contactPersonId", id);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

}
