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
import com.cmtech.android.bledeviceapp.adapter.ShareContactAdapter;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.ContactPerson;

import java.util.List;

/**
 *  显示可分享的联系人信息activity
 */

public class ShareContactActivity extends AppCompatActivity {
    private ShareContactAdapter scAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_share_contact);

        List<ContactPerson> cps = MyApplication.getAccount().getAgreedContactList();

        if(cps.isEmpty()) {
            Toast.makeText(this, "可分享联系人为空，不能分享记录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化分享联系人recyclerview
        RecyclerView rvShareContact = findViewById(R.id.rv_share_contact);
        rvShareContact.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvShareContact.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        scAdapter = new ShareContactAdapter(cps);
        rvShareContact.setAdapter(scAdapter);

        Button btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = scAdapter.getSelectContactPersonId();
                Intent intent = new Intent();
                intent.putExtra("contactId", id);
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
