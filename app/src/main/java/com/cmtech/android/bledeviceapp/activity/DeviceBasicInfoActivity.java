package com.cmtech.android.bledeviceapp.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDeviceBasicInfo;
import com.cmtech.android.bledevicecore.model.BleDeviceType;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;

/**
 *  DeviceBasicInfoActivity: 设备基本信息Activity，可用于修改BleDeviceBasicInfo字段
 *  Created by bme on 2018/6/27.
 */

public class DeviceBasicInfoActivity extends AppCompatActivity {
    private Button btnCancel;
    private Button btnOk;


    private EditText etName;
    private ImageView ivImage;
    private CheckBox cbIsAutoconnect;

    private BleDeviceBasicInfo basicInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device_basicinfo);

        Intent intent = getIntent();
        if(intent != null) {
            basicInfo = (BleDeviceBasicInfo) intent.getSerializableExtra("devicebasicinfo");
        }

        setTitle("设备:" + basicInfo.getMacAddress());

        etName = findViewById(R.id.cfg_device_nickname);
        etName.setText("".equals(basicInfo.getNickName()) ? BleDeviceType.fromUuid(basicInfo.getUuidString()).getName() : basicInfo.getNickName());

        ivImage = findViewById(R.id.cfg_device_image);

        String imagePath = basicInfo.getImagePath();
        if(imagePath != null && !"".equals(imagePath)) {
            Drawable drawable = new BitmapDrawable(MyApplication.getContext().getResources(), imagePath);
            ivImage.setImageDrawable(drawable);
        } else {
            Glide.with(this).load(BleDeviceType.fromUuid(basicInfo.getUuidString()).getImage()).into(ivImage);
        }

        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(DeviceBasicInfoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DeviceBasicInfoActivity.this,
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
            }
        });

        cbIsAutoconnect = findViewById(R.id.cfg_device_isautoconnect);
        cbIsAutoconnect.setChecked(basicInfo.autoConnect());

        btnCancel = findViewById(R.id.register_device_cancel_btn);
        btnOk = findViewById(R.id.register_device_ok_btn);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                basicInfo.setNickName(etName.getText().toString());

                // 把图像缩小，保存为macAddress.jpg文件
                String imagePath = basicInfo.getImagePath();
                if(imagePath != null && !"".equals(imagePath)) {
                    Bitmap bitmap = BitmapUtil.getSmallBitmap(imagePath, 100, 100);
                    File toFile = FileUtil.getFile(getExternalFilesDir("images"), basicInfo.getMacAddress() + ".jpg");
                    BitmapUtil.saveBitmap(bitmap, toFile);
                    basicInfo.setImagePath(toFile.getAbsolutePath());
                }

                basicInfo.setAutoConnect(cbIsAutoconnect.isChecked());

                Intent intent = new Intent();
                intent.putExtra("devicebasicinfo", basicInfo);
                setResult(RESULT_OK, intent);
                finish();
            }
        });


    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if(resultCode == RESULT_OK) {
                    if(Build.VERSION.SDK_INT >= 19) {
                        basicInfo.setImagePath(handleImageOnKitKat(data));
                    } else {
                        basicInfo.setImagePath(handleImageBeforeKitKat(data));
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private String handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }

        } else if("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
        return imagePath;
    }

    private String handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
        return imagePath;
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        Glide.with(MyApplication.getContext()).load(imagePath).centerCrop().into(ivImage);
    }
}
