package com.cmtech.android.bledeviceapp.view.layout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordActivity;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledeviceapp.util.MyBitmapUtil;

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
    // 该Layout关联的记录
    private BasicRecord record;

    // 退出按钮
    private ImageView ivExit;

    // 创建人名
    private TextView tvCreatorName;

    // 创建人头像
    private ImageView ivCreatorImage;

    // 创建时间
    private TextView tvCreateTime;

    // 记录设备地址
    private TextView tvAddress;

    // 下载更新按钮
    private TextView tvDownload;

    // 上传按钮
    private TextView tvUpload;

    private TextView tvShare;

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

        tvDownload = findViewById(R.id.tv_download);
        tvDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(record != null) {
                    ((RecordActivity)getContext()).downloadRecord();
                }
            }
        });

        tvUpload = findViewById(R.id.tv_upload);
        tvUpload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(record != null) {
                    ((RecordActivity)getContext()).uploadRecord();
                }
            }
        });

        tvShare = findViewById(R.id.tv_share);
        tvShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(record != null) {
                    ((RecordActivity)getContext()).shareRecord();
                }
            }
        });
    }

    /**
     * 设置关联记录
     * @param record
     */
    public void setRecord(BasicRecord record) {
        this.record = record;
    }

    /**
     * 更新显示
     */
    public void updateView() {
        if(record == null) return;

        tvCreatorName.setText(record.getCreatorNickName());
        String icon = record.getCreatorIcon();
        if(TextUtils.isEmpty(icon)) {
            ivCreatorImage.setImageResource(R.mipmap.ic_user_32px);
        } else {
            Bitmap bitmap = MyBitmapUtil.showToDp(icon,  32);
            ivCreatorImage.setImageBitmap(bitmap);
        }

        String createTime = DateTimeUtil.timeToStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        int length = record.getDevAddress().length();
        tvAddress.setText(record.getDevAddress().substring(length-5, length));
        invalidate();
    }
}
