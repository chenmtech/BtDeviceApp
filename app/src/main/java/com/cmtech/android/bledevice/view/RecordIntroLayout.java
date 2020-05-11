package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmtech.android.bledevice.record.IRecord;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.utils.view.BitmapUtil;

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
    private TextView tvCreateTime; // 创建时间
    private TextView tvCreator; // 创建人
    private TextView tvAddress; // device address
    private ImageView ivRecordType; // record type
    private TextView tvDesc; // description

    public RecordIntroLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_record_intro, this);

        tvCreateTime = findViewById(R.id.tv_create_time);
        tvCreator = findViewById(R.id.tv_creator);
        tvAddress = findViewById(R.id.tv_device_address);
        ivRecordType = findViewById(R.id.iv_record_type);
        tvDesc = findViewById(R.id.tv_desc);
    }

    public void redraw(IRecord record) {
        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        tvCreator.setText(record.getCreatorName());
        Drawable drawable;
        if(TextUtils.isEmpty(AccountManager.getAccount().getLocalIcon())) {
            drawable = ContextCompat.getDrawable(getContext(), SUPPORT_LOGIN_PLATFORM.get(record.getCreatorPlat()));
        } else {
            Bitmap bitmap = BitmapUtil.getSmallBitmap(AccountManager.getAccount().getLocalIcon(), 200, 200);
            drawable = BitmapUtil.bitmapToDrawable(bitmap);
        }
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        tvCreator.setCompoundDrawables(null, drawable, null, null);

        tvAddress.setText(record.getDevAddress());
        ivRecordType.setImageResource(R.mipmap.ic_ecg_24px);
        tvDesc.setText(record.getDesc());
        invalidate();
    }
}
