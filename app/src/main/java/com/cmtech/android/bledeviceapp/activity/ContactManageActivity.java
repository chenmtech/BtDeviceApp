package com.cmtech.android.bledeviceapp.activity;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.ContactAdapter;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;

/**
 * 用来管理联系人的Activity
 */
public class ContactManageActivity extends AppCompatActivity {
    private ContactAdapter contactAdapter;
    private RecyclerView rvContact;
    private EditText etContactId;
    private Button btnAddContact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_manage);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_contact_manage);
        setSupportActionBar(toolbar);

        // 初始化联系人列表
        rvContact = findViewById(R.id.rv_contact_info);
        rvContact.setLayoutManager(new LinearLayoutManager(this));
        rvContact.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        contactAdapter = new ContactAdapter(MyApplication.getAccount().getContacts());
        rvContact.setAdapter(contactAdapter);

        etContactId = findViewById(R.id.et_contact_id);

        btnAddContact = findViewById(R.id.btn_add_contact);
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idStr = etContactId.getText().toString();
                int id = INVALID_ID;
                try{
                    id = Integer.parseInt(idStr);
                } catch (Exception ignored) {
                }
                if(id != INVALID_ID) {
                    addNewContact(id);
                } else {
                    Toast.makeText(ContactManageActivity.this, "无效ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;

            case R.id.item_refresh:
                updateContact();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 更新联系人
    public void updateContact() {
        // 从服务器下载联系人信息保存到本地数据库，之后再从联系人信息中下载联系人账户信息
        MyApplication.getAccount().downloadContactInfo(this, "更新联系人，请稍后...", new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                if (code == RCODE_SUCCESS) {
                    updateView();
                    MyApplication.getAccount().downloadContactAccountInfo(ContactManageActivity.this, null,
                            new ICodeCallback() {
                                @Override
                                public void onFinish(int code, String msg) {
                                    if(code == RCODE_SUCCESS) {
                                        updateView();
                                    } else {
                                        Toast.makeText(ContactManageActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(ContactManageActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateView() {
        contactAdapter.notifyDataSetChanged();
    }

    private void addNewContact(int id) {
        MyApplication.getAccount().addNewContact(this, id, new ICodeCallback() {
            @Override
            public void onFinish(int code, String msg) {
                Toast.makeText(ContactManageActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}