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
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;
import java.io.IOException;

import static com.cmtech.android.bledevice.core.BleDeviceConstant.IMAGEDIR;

/**
 *  UserInfoActivity: 用户信息Activity
 *  Created by bme on 2018/10/27.
 */

public class UserInfoActivity extends AppCompatActivity {
    private TextView tvPhone;
    private EditText etName;
    private ImageView ivPortrait;
    private EditText etRemark;
    private Button btnOk;
    private Button btnCancel;

    private String cachePortraitPath = ""; // 头像文件路径缓存

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        if(!UserManager.getInstance().isSignIn()) finish();

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_setuserinfo);
        setSupportActionBar(toolbar);

        tvPhone = findViewById(R.id.et_userinfo_phone);
        etName = findViewById(R.id.et_userinfo_username);
        ivPortrait = findViewById(R.id.iv_userinfo_portrait);
        etRemark = findViewById(R.id.et_userinfo_remark);
        btnOk = findViewById(R.id.btn_userinfo_ok);
        btnCancel = findViewById(R.id.btn_userinfo_cancel);

        User account = UserManager.getInstance().getUser();
        String phoneNum = account.getPhone();
        tvPhone.setText(phoneNum.substring(0,3)+"****"+phoneNum.substring(7));
        etName.setText(account.getNickname());
        cachePortraitPath = account.getPortrait();
        if("".equals(cachePortraitPath)) {
            Glide.with(this).load(R.mipmap.ic_unknown_user).into(ivPortrait);
        } else {
            Glide.with(MyApplication.getContext()).load(cachePortraitPath)
                    .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivPortrait);
        }
        etRemark.setText(account.getPersonalInfo());

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User account = UserManager.getInstance().getUser();
                account.setNickname(etName.getText().toString());

                if(!cachePortraitPath.equals(account.getPortrait())) {
                    // 把原来的图像文件删除
                    if(!"".equals(account.getPortrait())) {
                        File imageFile = new File(account.getPortrait());
                        imageFile.delete();
                    }

                    // 把当前图像保存，以手机号为文件名
                    if("".equals(cachePortraitPath)) {
                        account.setPortrait("");
                    } else {
                        try {
                            ivPortrait.setDrawingCacheEnabled(true);
                            Bitmap bitmap = ivPortrait.getDrawingCache();
                            File toFile = FileUtil.getFile(IMAGEDIR, account.getPhone() + ".jpg");
                            BitmapUtil.saveBitmap(bitmap, toFile);
                            ivPortrait.setDrawingCacheEnabled(false);
                            String filePath = toFile.getCanonicalPath();
                            account.setPortrait(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            account.setPortrait("");
                        }
                    }
                }

                account.setPersonalInfo(etRemark.getText().toString());
                account.save();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        ivPortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlbum();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if(resultCode == RESULT_OK) {
                    if(Build.VERSION.SDK_INT >= 19) {
                        cachePortraitPath = handleImageOnKitKat(data);
                    } else {
                        cachePortraitPath = handleImageBeforeKitKat(data);
                    }
                    if(!"".equals(cachePortraitPath)) {
                        displaySelectedImage(cachePortraitPath);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;
        }
        return true;
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 1);
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
        return imagePath;
    }

    private String handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        return getImagePath(uri, null);
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

    private void displaySelectedImage(String imagePath) {
        Glide.with(MyApplication.getContext()).load(imagePath).centerCrop().into(ivPortrait);
    }

}
