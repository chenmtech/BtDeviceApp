package com.cmtech.android.bledevice.thermo.view;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrm.model.BleHrRecord10;
import com.cmtech.android.bledevice.hrm.view.HrHistogramChart;
import com.cmtech.android.bledevice.hrm.view.HrLineChart;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import static com.cmtech.android.bledevice.hrm.model.BleHrRecord10.HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH;
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
    private BleHrRecord10 record;
    private TextView tvCreateTime; // 创建时间
    private TextView tvCreator; // 创建人
    private TextView tvAddress;
    private TextView tvTimeLength; // time length
    private ImageView ivRecordType; // record type

    private TextView tvMaxHr; // 最大心率
    private TextView tvAveHr; // 平均心率
    private HrLineChart hrLineChart; // 心率折线图
    private HrHistogramChart hrHistChart; // 心率直方图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_thermo);

        int recordId = getIntent().getIntExtra("record_id", -1);

        record = LitePal.where("id = ?", ""+recordId).findFirst(BleHrRecord10.class);
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        ViseLog.e(record);
        record.createHistogram();

        ivRecordType = findViewById(R.id.iv_record_type);
        ivRecordType.setImageResource(R.mipmap.ic_hr_24px);

        tvCreateTime = findViewById(R.id.tv_create_time);
        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        tvCreator = findViewById(R.id.tv_creator);
        tvCreator.setText(record.getCreatorName());

        Drawable drawable = ContextCompat.getDrawable(this, PLATFORM_NAME_ICON_PAIR.get(record.getCreatorPlat()));
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        tvCreator.setCompoundDrawables(null, drawable, null, null);

        tvTimeLength = findViewById(R.id.tv_record_desc);
        int time = (record.getRecordSecond() <= 60) ? 1 : record.getRecordSecond()/60;
        tvTimeLength.setText(time+"分钟");

        tvAddress = findViewById(R.id.tv_device_address);
        tvAddress.setText(record.getDevAddress());

        hrLineChart = findViewById(R.id.hr_line_chart);
        hrLineChart.setXAxisValueFormatter(HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH);
        hrLineChart.showLineChart(record.getFilterHrList(), "心率变化", Color.BLUE);

        hrHistChart = findViewById(R.id.chart_hr_histogram);
        tvAveHr = findViewById(R.id.tv_hr_ave_value);
        tvMaxHr = findViewById(R.id.tv_hr_max_value);

        tvAveHr.setText(String.valueOf(record.getHrAve()));
        tvMaxHr.setText(String.valueOf(record.getHrMax()));
        hrHistChart.update(record.getHrHistogram());
    }
}
