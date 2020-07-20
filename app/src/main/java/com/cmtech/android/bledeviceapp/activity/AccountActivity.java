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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.UserInfoWebAsyncTask;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.CODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.AppConstant.DIR_IMAGE;
import static com.cmtech.android.bledeviceapp.model.UserInfoWebAsyncTask.DOWNLOAD_CMD;

/**
 *  AccountActivity: 账户设置Activity
 *  Created by bme on 2018/10/27.
 */

public class AccountActivity extends AppCompatActivity {
    private EditText etName;
    private ImageView ivImage;
    private EditText etNote;
    private String cacheImagePath = ""; // 账户头像文件路径缓存

    private final User account = AccountManager.getAccount();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        if(!AccountManager.isLogin())  {
            Toast.makeText(this, R.string.login_failure, Toast.LENGTH_SHORT).show();
            finish();
        }

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_set_account_info);
        setSupportActionBar(toolbar);

        etName = findViewById(R.id.et_account_name);
        ivImage = findViewById(R.id.iv_account_image);
        etNote = findViewById(R.id.et_account_note);

        updateUI();

        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlbum();
            }
        });

        Button btnOk = findViewById(R.id.btn_account_info_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User account = AccountManager.getAccount();
                account.setName(etName.getText().toString());

                String iconPath = account.getIcon();
                if(!cacheImagePath.equals(iconPath)) {
                    // 把原来的图像文件删除
                    if(!TextUtils.isEmpty(iconPath)) {
                        File imageFile = new File(iconPath);
                        imageFile.delete();
                    }

                    // 把当前图像保存到DIR_IMAGE
                    if(TextUtils.isEmpty(cacheImagePath)) {
                        account.setIcon("");
                    } else {
                        try {
                            ivImage.setDrawingCacheEnabled(true);
                            Bitmap bitmap = ivImage.getDrawingCache();
                            bitmap = BitmapUtil.scaleImageTo(bitmap, 100, 100);
                            File toFile = FileUtil.getFile(DIR_IMAGE, account.getPlatName()+account.getPlatId() + ".jpg");
                            BitmapUtil.saveBitmap(bitmap, toFile);
                            ivImage.setDrawingCacheEnabled(false);
                            String filePath = toFile.getCanonicalPath();
                            account.setIcon(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            account.setIcon("");
                        }
                    }
                }

                account.setNote(etNote.getText().toString());
                account.save();

                new UserInfoWebAsyncTask(AccountActivity.this, UserInfoWebAsyncTask.UPLOAD_CMD, new UserInfoWebAsyncTask.UserInfoWebCallback() {
                    @Override
                    public void onFinish(int code, Object result) {
                        int strId = (code == CODE_SUCCESS) ? R.string.operation_success : R.string.operation_failure;
                        Toast.makeText(AccountActivity.this, strId, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }).execute(account);
            }
        });

        Button btnCancel = findViewById(R.id.btn_userinfo_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }

    private void updateUI() {
        etName.setText(account.getName());

        cacheImagePath = account.getIcon();
        if(TextUtils.isEmpty(cacheImagePath)) {
            Glide.with(this).load(R.mipmap.ic_user).into(ivImage);
        } else {
            Glide.with(this).load(cacheImagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivImage);
        }

        etNote.setText(account.getNote());
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
                        Glide.with(AccountActivity.this).load(cacheImagePath).centerCrop().into(ivImage);
                    }
                }
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_modify_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

            case R.id.download_account_info:
                download();
                break;
        }
        return true;
    }

    private void download() {
        new UserInfoWebAsyncTask(AccountActivity.this, DOWNLOAD_CMD, new UserInfoWebAsyncTask.UserInfoWebCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if (code == CODE_SUCCESS) {
                    JSONObject json = (JSONObject) result;

                    try {
                        if(account.setDataFromJson(json)) {
                            updateUI();
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(AccountActivity.this, R.string.operation_failure, Toast.LENGTH_SHORT).show();
            }
        }).execute(account);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
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
