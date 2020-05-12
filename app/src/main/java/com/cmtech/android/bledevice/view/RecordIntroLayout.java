package com.cmtech.android.bledevice.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmtech.android.bledevice.record.IRecord;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import static com.cmtech.android.bledeviceapp.AppConstant.SUPPORT_LOGIN_PLATFORM;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.view
 * ClassName:      RecordIntroLayout
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/5/12 上午5:22
 * UpdateUser:     更新者
 * UpdateDate:     2020/5/12 上午5:22
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class RecordIntroLayout extends RelativeLayout {
    private ImageView ivExit;
    private TextView tvCreatorName; // 创建人名
    private ImageView ivCreatorImage;
    private TextView tvCreateTime; // 创建时间
    private TextView tvAddress; // device address
    private TextView tvDesc; // description
    private ImageView ivUpload; // record upload

    public RecordIntroLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_record_intro, this);

        ivExit = findViewById(R.id.iv_exit);
        tvCreatorName = findViewById(R.id.tv_creator_name);
        ivCreatorImage = findViewById(R.id.iv_creator_image);
        tvCreateTime = findViewById(R.id.tv_create_time);
        tvAddress = findViewById(R.id.tv_device_address);
        tvDesc = findViewById(R.id.tv_desc);
        ivUpload = findViewById(R.id.iv_record_upload);
    }

    public void redraw(IRecord record, OnClickListener uploadListener) {
        ivExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)getContext()).finish();
            }
        });

        tvCreatorName.setText(record.getCreatorName());
        Account account = AccountManager.getAccount();
        if(TextUtils.isEmpty(account.getLocalIcon())) {
            // load icon by platform name
            ivCreatorImage.setImageResource(SUPPORT_LOGIN_PLATFORM.get(account.getPlatName()));
        } else {
            Glide.with(getContext()).load(account.getLocalIcon()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivCreatorImage);
        }

        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        tvAddress.setText(record.getDevAddress());
        tvDesc.setText(record.getDesc());
        ivUpload.setOnClickListener(uploadListener);
        invalidate();
    }
}
