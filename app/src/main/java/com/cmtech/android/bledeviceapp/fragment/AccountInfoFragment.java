package com.cmtech.android.bledeviceapp.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.MyFileUtil;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_IMAGE;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.view
 * ClassName:      EcgRecordFragment
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/28 上午6:48
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/28 上午6:48
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class AccountInfoFragment extends Fragment {
    public static final String TITLE = "账户信息";

    private EditText etName;
    private ImageView ivImage;
    private EditText etNote;
    private String cacheImageFile = ""; // 账户头像文件名缓存

    private Account account;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_account_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        account = MyApplication.getAccount();

        etName = view.findViewById(R.id.et_account_name);
        ivImage = view.findViewById(R.id.iv_account_image);
        etNote = view.findViewById(R.id.et_account_note);

        updateUI();

        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlbum();
            }
        });
    }

    public void updateUI() {
        etName.setText(account.getNickName());

        cacheImageFile = account.getIcon();
        if (TextUtils.isEmpty(cacheImageFile)) {
            Glide.with(this).load(R.mipmap.ic_user).into(ivImage);
        } else {
            Glide.with(this).load(cacheImageFile).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivImage);
        }

        etNote.setText(account.getNote());
    }

    public void processOKButton() {
        //Account account = MyApplication.getAccount();
        account.setNickName(etName.getText().toString());

        String icon = account.getIcon();
        if (!cacheImageFile.equals(icon)) {
            // 把原来的图像文件删除
            if (!TextUtils.isEmpty(icon)) {
                File iconFile = new File(icon);
                if (iconFile.exists())
                    iconFile.delete();
            }

            // 把当前图像保存到DIR_IMAGE
            if (TextUtils.isEmpty(cacheImageFile)) {
                account.setIcon("");
            } else {
                try {
                    ivImage.setDrawingCacheEnabled(true);
                    Bitmap bitmap = ivImage.getDrawingCache();
                    bitmap = BitmapUtil.scaleImageTo(bitmap, 100, 100);
                    File toFile = FileUtil.getFile(DIR_IMAGE, account.getUserName() + ".jpg");
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if(uri == null) return;

                cacheImageFile = MyFileUtil.getFilePathByUri(getContext(), uri);
                if (!TextUtils.isEmpty(cacheImageFile)) {
                    Glide.with(getContext()).load(cacheImageFile).centerCrop().into(ivImage);
                }
            }
        }
    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 1);
    }
}
