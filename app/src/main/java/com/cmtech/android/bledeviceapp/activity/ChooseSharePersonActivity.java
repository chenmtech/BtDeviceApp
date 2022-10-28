package com.cmtech.android.bledeviceapp.activity;

import static com.cmtech.android.ble.core.DeviceCommonInfo.DEFAULT_AUTO_CONNECT;
import static com.cmtech.android.ble.core.DeviceCommonInfo.DEFAULT_ICON;
import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_IMAGE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.ble.core.BleDeviceCommonInfo;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.adapter.ShareInfoAdapter;
import com.cmtech.android.bledeviceapp.adapter.SharePersonAdapter;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.DeviceType;
import com.cmtech.android.bledeviceapp.util.MyFileUtil;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  DeviceInfoActivity: device info activity, used to set up the BleDeviceInfo class
 *  Created by bme on 2018/6/27.
 */

public class ChooseSharePersonActivity extends AppCompatActivity {
    private List<Integer> shareIds = null;
    private SharePersonAdapter sharePersonAdapter;
    private RecyclerView rvSharePerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_choose_share_person);

        shareIds = MyApplication.getAccount().getCanSharePersonIdList();

        if(shareIds.isEmpty()) {
            Toast.makeText(this, "不能分享记录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化扫描设备列表
        rvSharePerson = findViewById(R.id.rv_share_person);
        rvSharePerson.setLayoutManager(new LinearLayoutManager(this));
        rvSharePerson.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        sharePersonAdapter = new SharePersonAdapter(shareIds);
        rvSharePerson.setAdapter(sharePersonAdapter);

        Button btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("shareId", sharePersonAdapter.getItemCount());
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
