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
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;
import java.io.IOException;

import static com.cmtech.android.bledeviceapp.AppConstant.DIR_IMAGE;

/**
 *  AccountActivity: 账户设置Activity
 *  Created by bme on 2018/10/27.
 */

public class AccountActivity extends AppCompatActivity {
    private EditText etName;
    private ImageView ivImage;
    private EditText etDescription;
    private String cacheImagePath = ""; // 头像文件路径缓存

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        if(!AccountManager.getInstance().isSignIn()) finish();

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_set_account_info);
        setSupportActionBar(toolbar);

        Account account = AccountManager.getInstance().getAccount();

        TextView tvId = findViewById(R.id.et_account_id);
        String idStr = account.getHuaweiId();
        if(idStr.length() > 3) {
            tvId.setText(String.format("%s****%s", idStr.substring(0, 3), idStr.substring(idStr.length() - 3)));
        } else
            tvId.setText(idStr);

        etName = findViewById(R.id.et_account_name);
        etName.setText(account.getName());

        ivImage = findViewById(R.id.iv_account_image);
        cacheImagePath = account.getImagePath();
        if(TextUtils.isEmpty(cacheImagePath)) {
            Glide.with(this).load(R.mipmap.ic_unknown_user).into(ivImage);
        } else {
            Glide.with(MyApplication.getContext()).load(cacheImagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivImage);
        }
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlbum();
            }
        });

        etDescription = findViewById(R.id.et_account_description);
        etDescription.setText(account.getDescription());

        ImageButton ibLogout = findViewById(R.id.ib_logout);
        ibLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("logout", true);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        Button btnOk = findViewById(R.id.btn_account_info_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Account account = AccountManager.getInstance().getAccount();
                account.setName(etName.getText().toString());

                if(!cacheImagePath.equals(account.getImagePath())) {
                    // 把原来的图像文件删除
                    if(!TextUtils.isEmpty(account.getImagePath())) {
                        File imageFile = new File(account.getImagePath());
                        imageFile.delete();
                    }

                    // 把当前图像保存到DIR_IMAGE，以ID号为文件名
                    if(TextUtils.isEmpty(cacheImagePath)) {
                        account.setImagePath("");
                    } else {
                        try {
                            ivImage.setDrawingCacheEnabled(true);
                            Bitmap bitmap = ivImage.getDrawingCache();
                            File toFile = FileUtil.getFile(DIR_IMAGE, account.getHuaweiId() + ".jpg");
                            BitmapUtil.saveBitmap(bitmap, toFile);
                            ivImage.setDrawingCacheEnabled(false);
                            String filePath = toFile.getCanonicalPath();
                            account.setImagePath(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            account.setImagePath("");
                        }
                    }
                }

                account.setDescription(etDescription.getText().toString());
                account.save();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Button btnCancel = findViewById(R.id.btn_userinfo_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("logout", false);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if(resultCode == RESULT_OK) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        cacheImagePath = handleImageOnKitKat(data);
                    } else {
                        cacheImagePath = handleImageBeforeKitKat(data);
                    }
                    if(!TextUtils.isEmpty(cacheImagePath)) {
                        Glide.with(MyApplication.getContext()).load(cacheImagePath).centerCrop().into(ivImage);
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
        if(uri == null) return null;

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
}
