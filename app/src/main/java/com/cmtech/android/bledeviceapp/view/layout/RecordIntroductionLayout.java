package com.cmtech.android.bledeviceapp.view.layout;

import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordExplorerActivity;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledeviceapp.util.MyBitmapUtil;
import com.cmtech.android.bledeviceapp.util.WebFailureHandler;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.view
 * ClassName:      RecordIntroductionLayout
 * Description:    记录介绍信息layout
 * Author:         chenm
 * CreateDate:     2020/5/12 上午5:22
 * UpdateUser:     chenm
 * UpdateDate:     2020/5/12 上午5:22
 * UpdateRemark:   null
 * Version:        1.0
 */
public class RecordIntroductionLayout extends RelativeLayout {
    private BasicRecord record;
    private ImageView ivExit;
    private TextView tvCreatorName; // 创建人名
    private ImageView ivCreatorImage;
    private TextView tvCreateTime; // 创建时间
    private TextView tvAddress; // device address
    private ImageView ivUpload;

    public RecordIntroductionLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_record_intro, this);

        ivExit = findViewById(R.id.iv_exit);
        tvCreatorName = findViewById(R.id.tv_creator_name);
        ivCreatorImage = findViewById(R.id.iv_creator_image);
        tvCreateTime = findViewById(R.id.tv_create_time);
        tvAddress = findViewById(R.id.tv_device_address);
        ivExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)getContext()).onBackPressed();
            }
        });

        ivUpload = findViewById(R.id.iv_upload);
        ivUpload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(record != null) {
                    record.upload(getContext(), new ICodeCallback() {
                        @Override
                        public void onFinish(int code) {
                            if (code == RETURN_CODE_SUCCESS) {
                                Toast.makeText(getContext(), "记录已上传", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), WebFailureHandler.toString(code), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public void setRecord(BasicRecord record) {
        this.record = record;
    }

    public void updateView() {
        if(record == null) return;

        tvCreatorName.setText(record.getCreatorNickName());
        Account account = MyApplication.getAccount();
        if(account != null) {
            if (TextUtils.isEmpty(account.getIcon())) {
                ivCreatorImage.setImageResource(R.mipmap.ic_user);
            } else {
                Bitmap bitmap = MyBitmapUtil.showToDp(account.getIcon(), 32);
                ivCreatorImage.setImageBitmap(bitmap);
                //Glide.with(getContext()).load(account.getIcon()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivCreatorImage);
            }
        }

        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        int length = record.getDevAddress().length();
        tvAddress.setText(record.getDevAddress().substring(length-5, length));
        invalidate();
    }
}
