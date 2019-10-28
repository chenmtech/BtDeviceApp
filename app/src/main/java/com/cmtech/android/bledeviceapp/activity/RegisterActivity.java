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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.bledeviceapp.model.BleDeviceType;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;
import java.io.IOException;

import static com.cmtech.android.ble.core.BleDeviceRegisterInfo.DEFAULT_DEVICE_AUTO_CONNECT;
import static com.cmtech.android.ble.core.BleDeviceRegisterInfo.DEFAULT_DEVICE_IMAGE_PATH;
import static com.cmtech.android.ble.core.BleDeviceRegisterInfo.DEFAULT_WARN_BLE_INNER_ERROR;
import static com.cmtech.android.bledeviceapp.BleDeviceConstant.DIR_IMAGE;

/**
 *  RegisterActivity: 注册Activity，用于设置修改BleDeviceRegisterInfo字段
 *  Created by bme on 2018/6/27.
 */

public class RegisterActivity extends AppCompatActivity {
    public static final String DEVICE_REGISTER_INFO = "device_register_info";

    private BleDeviceRegisterInfo registerInfo; // 设备基本信息
    private EditText etName; // 设备昵名
    private ImageView ivImage; // 设备图像
    private CheckBox cbIsAutoconnect; // 设备是否自动连接
    private CheckBox cbWarnBleInnerError; // 设备重连失败后是否报警
    private String cacheImagePath = ""; // 图像文件名缓存

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device_register);

        Intent intent = getIntent();
        if(intent != null) {
            registerInfo = (BleDeviceRegisterInfo) intent.getSerializableExtra(DEVICE_REGISTER_INFO);
            if(registerInfo == null) {
                Toast.makeText(this, "设备注册信息无效", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        if(DIR_IMAGE == null || !DIR_IMAGE.exists()) {
            Toast.makeText(this, "图像目录错误。", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        BleDeviceType type = BleDeviceType.getFromUuid(registerInfo.getUuidStr());
        if(type == null) {
            Toast.makeText(this, "设备类型未知，无法注册。", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 设置标题为设备地址
        setTitle("设备:"+ registerInfo.getMacAddress());

        // 设置设备昵称
        etName = findViewById(R.id.et_device_nickname);
        String deviceName = registerInfo.getNickName();
        if("".equals(deviceName)) {
            deviceName = type.getDefaultNickname();
        }
        etName.setText(deviceName);

        // 设置设备图像
        ivImage = findViewById(R.id.iv_device_image);
        cacheImagePath = registerInfo.getImagePath();
        if("".equals(cacheImagePath)) {
            int defaultImageId = type.getDefaultImageId();
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

        // 设置设备打开后是否自动连接
        cbIsAutoconnect = findViewById(R.id.cb_device_isautoconnect);
        cbIsAutoconnect.setChecked(registerInfo.autoConnect());

        // 设置BLE内部错误是否报警
        cbWarnBleInnerError = findViewById(R.id.cb_device_warn_when_ble_error);
        cbWarnBleInnerError.setChecked(registerInfo.isWarnBleInnerError());

        Button btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerInfo.setNickName(etName.getText().toString());

                // 如果图像有变化
                if(!cacheImagePath.equals(registerInfo.getImagePath())) {
                    // 把原来的图像文件删除
                    if(!"".equals(registerInfo.getImagePath())) {
                        File imageFile = new File(registerInfo.getImagePath());
                        imageFile.delete();
                    }

                    // 把当前的ImageView中图像保存，以设备地址为文件名
                    if("".equals(cacheImagePath)) {
                        registerInfo.setImagePath("");
                    } else {
                        ivImage.setDrawingCacheEnabled(true);
                        Bitmap bitmap = ivImage.getDrawingCache();
                        File toFile = FileUtil.getFile(DIR_IMAGE, registerInfo.getMacAddress() + ".jpg");
                        try {
                            String filePath = toFile.getCanonicalPath();
                            BitmapUtil.saveBitmap(bitmap, toFile);
                            ivImage.setDrawingCacheEnabled(false);
                            registerInfo.setImagePath(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            registerInfo.setImagePath("");
                        }
                    }
                }

                registerInfo.setAutoConnect(cbIsAutoconnect.isChecked());
                registerInfo.setWarnBleInnerError(cbWarnBleInnerError.isChecked());

                Intent intent = new Intent();
                intent.putExtra(DEVICE_REGISTER_INFO, registerInfo);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Button btnDefault = findViewById(R.id.btn_set_default);
        btnDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreDefaultSetup();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                String imagePath = "";
                if(resultCode == RESULT_OK) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
        if(uri == null) return imagePath;
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
        return getImagePath(uri, null);
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
        BleDeviceType type = BleDeviceType.getFromUuid(registerInfo.getUuidStr());
        if(type != null) {
            etName.setText(type.getDefaultNickname());
            cacheImagePath = DEFAULT_DEVICE_IMAGE_PATH;
            Glide.with(this).load(type.getDefaultImageId()).into(ivImage);
            cbIsAutoconnect.setChecked(DEFAULT_DEVICE_AUTO_CONNECT);
            cbWarnBleInnerError.setChecked(DEFAULT_WARN_BLE_INNER_ERROR);
        }
    }
}
