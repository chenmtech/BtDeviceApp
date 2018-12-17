package com.cmtech.android.bledeviceapp.activity;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.bledevice.SupportedDeviceType;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.BleDeviceBasicInfo;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;
import java.io.IOException;

import static com.cmtech.android.bledevicecore.BleDeviceConstant.DEFAULT_DEVICE_AUTOCONNECT;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.DEFAULT_DEVICE_IMAGEPATH;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.DEFAULT_DEVICE_RECONNECTTIMES;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.DEFAULT_WARN_AFTER_RECONNECT_FAILURE;
import static com.cmtech.android.bledevicecore.BleDeviceConstant.IMAGEDIR;

/**
 *  DeviceBasicInfoActivity: 设备基本信息Activity，可用于修改BleDeviceBasicInfo字段
 *  Created by bme on 2018/6/27.
 */

public class DeviceBasicInfoActivity extends AppCompatActivity {
    // intent中devicebasicinfo的键值
    public static final String DEVICE_BASICINFO = "devicebasicinfo";

    private Button btnCancel;
    private Button btnOk;
    private Button btnDefault;
    private EditText etName;
    private ImageView ivImage;
    private CheckBox cbIsAutoconnect;
    private EditText etReconnectTimes;
    private CheckBox cbWarnAfterReconnectFailure;

    // 设备基本信息
    private BleDeviceBasicInfo basicInfo;

    // 图像文件名缓存
    private String cacheImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device_basicinfo);

        Intent intent = getIntent();
        if(intent != null) {
            basicInfo = (BleDeviceBasicInfo) intent.getSerializableExtra(DEVICE_BASICINFO);
        }

        if(!IMAGEDIR.exists()) {
            IMAGEDIR.mkdir();
        }

        if(basicInfo == null || !IMAGEDIR.exists()) {
            finish();
        }

        // 设置标题为设备地址
        setTitle("设备："+basicInfo.getMacAddress());

        // 设置设备名
        etName = findViewById(R.id.et_device_basicinfo_nickname);
        String deviceName = basicInfo.getNickName();
        if("".equals(deviceName)) {
            deviceName = SupportedDeviceType.getDeviceTypeFromUuid(basicInfo.getUuidString()).getDefaultNickname();
        }
        etName.setText(deviceName);

        // 设置设备图像
        ivImage = findViewById(R.id.iv_device_basicinfo_image);
        cacheImagePath = basicInfo.getImagePath();
        if("".equals(cacheImagePath)) {
            int defaultImageId = SupportedDeviceType.getDeviceTypeFromUuid(basicInfo.getUuidString()).getDefaultImage();
            Glide.with(this).load(defaultImageId).into(ivImage);
        } else {
            // 注意不要从缓存显示图像
            Glide.with(MyApplication.getContext()).load(cacheImagePath)
                    .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivImage);
        }
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlbum();
            }
        });

        // 设置重连次数
        etReconnectTimes = findViewById(R.id.et_device_basicinfo_reconnecttimes);
        etReconnectTimes.setText(String.valueOf(basicInfo.getReconnectTimes()));

        // 设置打开后是否自动重连
        cbIsAutoconnect = findViewById(R.id.cb_device_basicinfo_isautoconnect);
        cbIsAutoconnect.setChecked(basicInfo.autoConnect());


        btnCancel = findViewById(R.id.btn_device_basicinfo_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });


        btnOk = findViewById(R.id.btn_device_basicinfo_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                basicInfo.setNickName(etName.getText().toString());

                // 如果图像有变化
                if(!cacheImagePath.equals(basicInfo.getImagePath())) {
                    // 把原来的图像文件删除
                    if(!"".equals(basicInfo.getImagePath())) {
                        File imageFile = new File(basicInfo.getImagePath());
                        imageFile.delete();
                    }

                    // 把当前的ImageView中图像保存，以设备地址为文件名
                    if("".equals(cacheImagePath)) {
                        basicInfo.setImagePath("");
                    } else {
                        ivImage.setDrawingCacheEnabled(true);
                        Bitmap bitmap = ivImage.getDrawingCache();
                        File toFile = FileUtil.getFile(IMAGEDIR, basicInfo.getMacAddress() + ".jpg");
                        try {
                            String filePath = toFile.getCanonicalPath();
                            BitmapUtil.saveBitmap(bitmap, toFile);
                            ivImage.setDrawingCacheEnabled(false);
                            basicInfo.setImagePath(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            basicInfo.setImagePath("");
                        }
                    }
                }

                basicInfo.setAutoConnect(cbIsAutoconnect.isChecked());

                basicInfo.setReconnectTimes(Integer.parseInt(etReconnectTimes.getText().toString()));

                basicInfo.setWarnAfterReconnectFailure(cbWarnAfterReconnectFailure.isChecked());

                Intent intent = new Intent();
                intent.putExtra(DEVICE_BASICINFO, basicInfo);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        btnDefault = findViewById(R.id.btn_device_basicinfo_default);
        btnDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreDefaultSetup();
            }
        });

        // 设置重连失败后是否报警
        cbWarnAfterReconnectFailure = findViewById(R.id.cb_warn_after_reconnect_failure);
        cbWarnAfterReconnectFailure.setChecked(basicInfo.isWarnAfterReconnectFailure());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                String imagePath = "";
                if(resultCode == RESULT_OK) {
                    if(Build.VERSION.SDK_INT >= 19) {
                        imagePath = handleImageOnKitKat(data);
                    } else {
                        imagePath = handleImageBeforeKitKat(data);
                    }
                    if(!"".equals(imagePath)) {
                        cacheImagePath = imagePath;
                        displaySelectedImage(cacheImagePath);
                    }
                }
                break;
            default:
                break;
        }
    }

    // 打开相册
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @TargetApi(19)
    private String handleImageOnKitKat(Intent data) {
        String imagePath = "";
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
        return imagePath;
    }

    private String handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        return imagePath;
    }

    private String getImagePath(Uri uri, String selection) {
        String path = "";
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    // 显示选择的图像，需要做裁剪
    private void displaySelectedImage(String imagePath) {
        Glide.with(MyApplication.getContext()).load(imagePath).centerCrop().into(ivImage);
    }

    // 恢复缺省设置
    private void restoreDefaultSetup() {
        String deviceName = SupportedDeviceType.getDeviceTypeFromUuid(basicInfo.getUuidString()).getDefaultNickname();
        etName.setText(deviceName);

        cacheImagePath = DEFAULT_DEVICE_IMAGEPATH;
        Glide.with(this).load(SupportedDeviceType.getDeviceTypeFromUuid(basicInfo.getUuidString()).getDefaultImage()).into(ivImage);

        cbIsAutoconnect.setChecked(DEFAULT_DEVICE_AUTOCONNECT);

        etReconnectTimes.setText(String.valueOf(DEFAULT_DEVICE_RECONNECTTIMES));

        cbWarnAfterReconnectFailure.setChecked(DEFAULT_WARN_AFTER_RECONNECT_FAILURE);
    }
}
