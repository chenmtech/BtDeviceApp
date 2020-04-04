package com.cmtech.android.bledevice.thermo.view;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmtech.android.bledevice.view.MyLineChart;
import com.cmtech.android.bledevice.thermo.model.BleThermoRecord10;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import static com.cmtech.android.bledeviceapp.activity.LoginActivity.PLATFORM_NAME_ICON_PAIR;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.thermo.view
 * ClassName:      ThermoRecordActivity
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/3 下午5:35
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/3 下午5:35
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class ThermoRecordActivity extends AppCompatActivity {
    private BleThermoRecord10 record;
    private TextView tvCreateTime; // 创建时间
    private TextView tvCreator; // 创建人
    private TextView tvAddress;
    private TextView tvRecordDesc; // time length
    private ImageView ivRecordType; // record type
    private MyLineChart lineChart; // 心率折线图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_thermo);

        int recordId = getIntent().getIntExtra("record_id", -1);

        record = LitePal.where("id = ?", ""+recordId).findFirst(BleThermoRecord10.class);
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        ViseLog.e(record);

        ivRecordType = findViewById(R.id.iv_record_type);
        ivRecordType.setImageResource(R.mipmap.ic_thermo_24px);

        tvCreateTime = findViewById(R.id.tv_create_time);
        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        tvCreator = findViewById(R.id.tv_creator);
        tvCreator.setText(record.getCreatorName());

        Drawable drawable = ContextCompat.getDrawable(this, PLATFORM_NAME_ICON_PAIR.get(record.getCreatorPlat()));
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        tvCreator.setCompoundDrawables(null, drawable, null, null);

        tvRecordDesc = findViewById(R.id.tv_desc);
        tvRecordDesc.setText(record.getDesc());

        tvAddress = findViewById(R.id.tv_device_address);
        tvAddress.setText(record.getDevAddress());

        lineChart = findViewById(R.id.hr_line_chart);
        lineChart.setXAxisValueFormatter(2);
        lineChart.showFloatLineChart(record.getTemp(), "心率变化", Color.BLUE);
    }
}
