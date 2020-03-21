package com.cmtech.android.bledevice.hrmonitor.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrmonitor.model.HrStatisticsInfo;
import com.cmtech.android.bledevice.hrmonitor.model.BleHrRecord10;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import static com.cmtech.android.bledevice.ecg.process.signal.EcgSignalProcessor.HR_HISTOGRAM_BAR_NUM;
import static com.cmtech.android.bledevice.hrmonitor.view.HRMonitorFragment.HR_MOVE_AVERAGE_WINDOW_WIDTH;

public class HrRecordActivity extends AppCompatActivity {
    private BleHrRecord10 record;
    private TextView tvCreateTime; // 创建时间
    private TextView tvCreator; // 创建人
    private TextView tvHrNum; // 心率次数

    private TextView tvAveHr; // 平均心率
    private TextView tvMaxHr; // 最大心率
    private HrLineChart hrLineChart; // 心率折线图
    private HrHistogramChart hrHistChart; // 心率直方图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr_record);

        int recordId = getIntent().getIntExtra("record_id", -1);
        record = LitePal.find(BleHrRecord10.class, recordId, true);
        ViseLog.e(record);
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        tvCreateTime = findViewById(R.id.tv_create_time);
        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        tvCreator = findViewById(R.id.tv_creator);
        tvCreator.setText(record.getCreatorName());

        tvHrNum = findViewById(R.id.tv_hr_num);
        if(record.getHrList() == null)
            tvHrNum.setText(String.valueOf(0));
        else
            tvHrNum.setText(String.valueOf(record.getHrList().size()));

        hrLineChart = findViewById(R.id.linechart_hr);
        hrLineChart.showLineChart(record.getHrList(), "心率变化", Color.BLUE);

        hrHistChart = findViewById(R.id.chart_hr_histogram);
        tvAveHr = findViewById(R.id.tv_hr_ave_value);
        tvMaxHr = findViewById(R.id.tv_hr_max_value);

        /*HrStatisticsInfo hrStatisticsInfo = new HrStatisticsInfo(record.getHrList(), HR_MOVE_AVERAGE_WINDOW_WIDTH);
        tvAveHr.setText(String.valueOf(hrStatisticsInfo.getAverageHr()));
        tvMaxHr.setText(String.valueOf(hrStatisticsInfo.getMaxHr()));
        hrHistChart.update(hrStatisticsInfo.getNormHistogram(HR_HISTOGRAM_BAR_NUM));*/

    }

    /*private void initialize() {
        tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(modifyTime));

        Account fileCreator = record.getCreator();
        Account account = AccountManager.getInstance().getAccount();
        if(fileCreator.equals(account)) {
            tvCreator.setText(Html.fromHtml("<u>您本人</u>"));
        } else {
            tvCreator.setText(Html.fromHtml("<u>" + record.getCreatorName() + "</u>"));
        }

        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        int second = record.getDataNum()/ record.getSampleRate();
        if(record.getDataNum() == 0) {
            tvLength.setText("无");
        } else {
            String timeLength = DateTimeUtil.secToTimeInChinese(second);
            tvLength.setText(timeLength);
        }

        if(record.getHrList() == null)
            tvHrNum.setText(String.valueOf(0));
        else
            tvHrNum.setText(String.valueOf(record.getHrList().size()));

        initEcgView(record);

        tvCurrentTime.setText(DateTimeUtil.secToTime(0));
        tvTotalTime.setText(DateTimeUtil.secToTime(second));
        sbReplay.setMax(second);

        *//*List<EcgNormalComment> commentList = getCommentListInRecord(record);
        commentAdapter.updateCommentList(commentList);
        if(commentList.size() > 0)
            rvComments.smoothScrollToPosition(0);*//*

        HrStatisticsInfo hrStatisticsInfo = new HrStatisticsInfo(record.getHrList(), HR_FILTER_SECOND);
        tvAveHr.setText(String.valueOf(hrStatisticsInfo.getAverageHr()));
        tvMaxHr.setText(String.valueOf(hrStatisticsInfo.getMaxHr()));
        hrLineChart.showLineChart(hrStatisticsInfo.getFilteredHrList(), "心率时序图", Color.BLUE);
        hrHistChart.update(hrStatisticsInfo.getNormHistogram(HR_HISTOGRAM_BAR_NUM));

        if(record.getDataNum() == 0) {
            signalLayout.setVisibility(View.GONE);
        } else {
            signalLayout.setVisibility(View.VISIBLE);
            signalView.startShow();
        }

        if(record.getHrList() == null || record.getHrList().isEmpty()) {
            hrLayout.setVisibility(View.GONE);
        } else {
            hrLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initEcgView(EcgRecord ecgRecord) {
        if(ecgRecord == null) return;
        signalView.setEcgRecord(ecgRecord);
        signalView.setZeroLocation(RollWaveView.DEFAULT_ZERO_LOCATION);
    }*/


}
