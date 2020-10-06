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
import com.cmtech.android.bledevice.record.BasicRecord;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import static com.cmtech.android.bledeviceapp.global.AppConstant.SUPPORT_LOGIN_PLATFORM;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.view
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
    private ImageView ivExit;
    private TextView tvCreatorName; // 创建人名
    private ImageView ivCreatorImage;
    private TextView tvCreateTime; // 创建时间
    private TextView tvAddress; // device address

    public RecordIntroductionLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_record_intro, this);

        ivExit = findViewById(R.id.iv_exit);
        tvCreatorName = findViewById(R.id.tv_creator_name);
        ivCreatorImage = findViewById(R.id.iv_creator_image);
        tvCreateTime = findViewById(R.id.tv_create_time);
        tvAddress = findViewById(R.id.tv_device_address);
    }

    public void redraw(BasicRecord record) {
        if(record == null) return;

        ivExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)getContext()).finish();
            }
        });

        tvCreatorName.setText(record.getCreatorName());
        Account account = MyApplication.getAccount();
        if(account != null) {
            if (TextUtils.isEmpty(account.getIcon())) {
                // load icon by platform name
                ivCreatorImage.setImageResource(SUPPORT_LOGIN_PLATFORM.get(account.getPlatName()));
            } else {
                Glide.with(getContext()).load(account.getIcon()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivCreatorImage);
            }
        }

        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        tvAddress.setText(record.getDevAddress());
        invalidate();
    }
}
