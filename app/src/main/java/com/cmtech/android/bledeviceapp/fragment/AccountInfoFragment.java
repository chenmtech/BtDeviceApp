package com.cmtech.android.bledeviceapp.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.MyBitmapUtil;
import com.cmtech.android.bledeviceapp.util.MyFileUtil;
import com.vise.utils.file.FileUtil;
import com.vise.utils.view.BitmapUtil;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static com.cmtech.android.bledeviceapp.global.AppConstant.DIR_CACHE;
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
    private File tmpIconFile;

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
        tmpIconFile = FileUtil.getFile(DIR_CACHE, account.getUserName() + ".jpg");

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
        etNote.setText(account.getNote());

        String iconFile = account.getIcon();
        Bitmap bitmap;
        if (!TextUtils.isEmpty(iconFile)) {
            bitmap = MyBitmapUtil.showToDp(iconFile, 60);
            if(bitmap != null) {
                ivImage.setImageBitmap(bitmap);
                return;
            }
        }
        ivImage.setImageResource(R.mipmap.ic_user);
    }

    public void processOKButton() {
        account.setNickName(etName.getText().toString());
        account.setNote(etNote.getText().toString());

        // 把当前图像保存到DIR_IMAGE
        if (tmpIconFile.exists()) {
            File file = FileUtil.getFile(DIR_IMAGE, account.getUserName() + ".jpg");
            try {
                FileUtil.copyFile(tmpIconFile, file);
                account.setIcon(file.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if(uri == null) return;

                String changedIconFile = MyFileUtil.getFilePathByUri(getContext(), uri);
                if (!TextUtils.isEmpty(changedIconFile)) {
                    Bitmap bitmap = MyBitmapUtil.scaleToDp(changedIconFile, 60);
                    if(bitmap != null) {
                        //MyBitmapUtil.saveBitmap(bitmap, tmpIconFile, 25);
                        BitmapUtil.saveBitmap(bitmap, tmpIconFile);
                        try {
                            bitmap = MyBitmapUtil.showToDp(tmpIconFile.getCanonicalPath(), 60);
                            //ViseLog.e("" + bitmap.getWidth() + " " + bitmap.getHeight());
                            ivImage.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
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
