package com.cmtech.android.bledeviceapp.activity;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserAccount;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;
import java.io.IOException;

import static com.cmtech.android.bledevicecore.BleDeviceConstant.IMAGEDIR;

public class UserInfoActivity extends AppCompatActivity {
    private EditText etUserName;
    private EditText etPassword;
    private Button btnOk;
    private Button btnCancel;
    private ImageView ivUserImage;

    private String cacheImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        if(!UserAccountManager.getInstance().isSignIn()) finish();

        EditText etAccountName = findViewById(R.id.et_userinfo_accountname);
        etPassword = findViewById(R.id.et_userinfo_password);
        etUserName = findViewById(R.id.et_userinfo_username);
        ivUserImage = findViewById(R.id.iv_userinfo_image);
        btnOk = findViewById(R.id.btn_userinfo_ok);
        btnCancel = findViewById(R.id.btn_userinfo_cancel);

        UserAccount account = UserAccountManager.getInstance().getUserAccount();
        etAccountName.setText(account.getAccountName());
        etPassword.setText(account.getPassword());
        etUserName.setText(account.getUserName());
        cacheImagePath = account.getImagePath();
        if("".equals(cacheImagePath)) {
            Glide.with(this).load(R.mipmap.ic_unknown_user).into(ivUserImage);
        } else {
            Glide.with(MyApplication.getContext()).load(cacheImagePath)
                    .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivUserImage);
        }

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserAccount account = UserAccountManager.getInstance().getUserAccount();
                account.setUserName(etUserName.getText().toString());

                String password = etPassword.getText().toString();
                if(!account.getPassword().equals(password)) {
                    modifyPasswordInPref(password);
                    account.setPassword(password);
                }

                if(!cacheImagePath.equals(account.getImagePath())) {
                    // 把原来的图像文件删除
                    if(!"".equals(account.getImagePath())) {
                        File imageFile = new File(account.getImagePath());
                        imageFile.delete();
                    }

                    // 把当前图像保存，以账户名为文件名
                    if("".equals(cacheImagePath)) {
                        account.setImagePath("");
                    } else {
                        try {
                            ivUserImage.setDrawingCacheEnabled(true);
                            Bitmap bitmap = ivUserImage.getDrawingCache();
                            File toFile = FileUtil.getFile(IMAGEDIR, account.getAccountName() + ".jpg");
                            BitmapUtil.saveBitmap(bitmap, toFile);
                            ivUserImage.setDrawingCacheEnabled(false);
                            String filePath = toFile.getCanonicalPath();
                            account.setImagePath(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            account.setImagePath("");
                        }
                    }
                }
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

        ivUserImage.setOnClickListener(new View.OnClickListener() {
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
                        cacheImagePath = handleImageOnKitKat(data);
                    } else {
                        cacheImagePath = handleImageBeforeKitKat(data);
                    }
                    if(!"".equals(cacheImagePath)) {
                        displaySelectedImage(cacheImagePath);
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
        Glide.with(MyApplication.getContext()).load(imagePath).centerCrop().into(ivUserImage);
    }

    private void modifyPasswordInPref(String newPassword) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRememberPassword = pref.getBoolean("remember_password", false);

        if(isRememberPassword) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("password", newPassword);
            editor.apply();
        }
    }
}
