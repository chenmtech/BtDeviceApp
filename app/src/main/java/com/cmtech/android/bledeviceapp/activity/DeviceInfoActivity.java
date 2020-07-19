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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.ble.core.BleDeviceCommonInfo;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.DeviceType;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;
import java.io.IOException;

import static com.cmtech.android.ble.core.DeviceCommonInfo.DEFAULT_AUTO_CONNECT;
import static com.cmtech.android.ble.core.DeviceCommonInfo.DEFAULT_ICON;
import static com.cmtech.android.bledeviceapp.AppConstant.DIR_IMAGE;

/**
 *  DeviceInfoActivity: device info activity, used to set up the BleDeviceInfo class
 *  Created by bme on 2018/6/27.
 */

public class DeviceInfoActivity extends AppCompatActivity {
    public static final String DEVICE_INFO = "device_info";

    private BleDeviceCommonInfo deviceInfo; //
    private TextView tvAddress;
    private TextView tvType;
    private EditText etName; //
    private ImageView ivImage; //
    private CheckBox cbIsAutoConnect; //
    private String cacheImagePath = ""; // 图像文件名缓存

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device_info);

        Intent intent = getIntent();
        if(intent != null) {
            deviceInfo = (BleDeviceCommonInfo) intent.getSerializableExtra(DEVICE_INFO);
            if(deviceInfo == null) {
                Toast.makeText(this, R.string.invalid_device_info, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        if(DIR_IMAGE == null || !DIR_IMAGE.exists()) {
            Toast.makeText(this, R.string.invalid_image_dir, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        DeviceType type = DeviceType.getFromUuid(deviceInfo.getUuid());
        if(type == null) {
            Toast.makeText(this, R.string.invalid_device_type, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvAddress = findViewById(R.id.tv_device_address);
        tvAddress.setText(deviceInfo.getAddress());

        tvType = findViewById(R.id.tv_device_type);
        tvType.setText(type.getDefaultName());

        // 设置设备昵称
        etName = findViewById(R.id.et_device_nickname);
        String deviceName = deviceInfo.getName();
        if("".equals(deviceName)) {
            deviceName = type.getDefaultName();
        }
        etName.setText(deviceName);

        // 设置设备图像
        ivImage = findViewById(R.id.iv_tab_image);
        cacheImagePath = deviceInfo.getIcon();
        if(TextUtils.isEmpty(cacheImagePath)) {
            int defaultImageId = type.getDefaultIcon();
            Glide.with(this).load(defaultImageId).into(ivImage);
        } else {
            // 注意不要从缓存显示图像
            Glide.with(this).load(cacheImagePath)
                    .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivImage);
        }
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlbum();
            }
        });

        // 设置设备打开后是否自动连接
        cbIsAutoConnect = findViewById(R.id.cb_device_auto_connect);
        cbIsAutoConnect.setChecked(deviceInfo.isAutoConnect());

        Button btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceInfo.setName(etName.getText().toString());

                // 如果图像有变化
                if(!cacheImagePath.equals(deviceInfo.getIcon())) {
                    // 把原来的图像文件删除
                    if(!TextUtils.isEmpty(deviceInfo.getIcon())) {
                        File imageFile = new File(deviceInfo.getIcon());
                        imageFile.delete();
                    }

                    // 把当前的ImageView中图像保存，以设备地址为文件名
                    if(TextUtils.isEmpty(cacheImagePath)) {
                        deviceInfo.setIcon("");
                    } else {
                        ivImage.setDrawingCacheEnabled(true);
                        Bitmap bitmap = ivImage.getDrawingCache();
                        File toFile = FileUtil.getFile(DIR_IMAGE, deviceInfo.getAddress() + ".jpg");
                        try {
                            String filePath = toFile.getCanonicalPath();
                            BitmapUtil.saveBitmap(bitmap, toFile);
                            ivImage.setDrawingCacheEnabled(false);
                            deviceInfo.setIcon(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            deviceInfo.setIcon("");
                        }
                    }
                }

                deviceInfo.setAutoConnect(cbIsAutoConnect.isChecked());

                Intent intent = new Intent();
                intent.putExtra(DEVICE_INFO, deviceInfo);
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
                        Glide.with(MyApplication.getContext()).load(cacheImagePath).centerCrop().into(ivImage);
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
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
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

    // 恢复缺省设置
    private void restoreDefaultSetup() {
        DeviceType type = DeviceType.getFromUuid(deviceInfo.getUuid());
        if(type != null) {
            etName.setText(type.getDefaultName());
            cacheImagePath = DEFAULT_ICON;
            Glide.with(this).load(type.getDefaultIcon()).into(ivImage);
            cbIsAutoConnect.setChecked(DEFAULT_AUTO_CONNECT);
        }
    }
}
