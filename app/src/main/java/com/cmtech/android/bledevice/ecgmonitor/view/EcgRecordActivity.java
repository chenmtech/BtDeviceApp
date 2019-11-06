package com.cmtech.android.bledevice.ecgmonitor.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgCommentAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgRecord;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgcomment.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.EcgHrStatisticsInfo;
import com.cmtech.android.bledevice.viewcomponent.RollWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.EcgSignalProcessor.HR_FILTER_SECOND;
import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.EcgSignalProcessor.HR_HISTOGRAM_BAR_NUM;

public class EcgRecordActivity extends AppCompatActivity implements RollWaveView.OnRollWaveViewListener, EcgCommentAdapter.OnEcgCommentListener{
    public static final double ZERO_LOCATION_IN_ECG_VIEW = 0.5;

    private EcgRecord record;
    private long modifyTime;

    private TextView tvModifyTime; // 更新时间
    private TextView tvCreator; // 创建人
    private TextView tvCreateTime; // 创建时间
    private TextView tvLength; // 信号长度
    private TextView tvHrNum; // 心率次数

    private LinearLayout signalLayout;
    private RollEcgRecordWaveView signalView; // signalView
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton btnReplayCtrl; // 转换回放状态
    private EcgCommentAdapter commentAdapter; // 留言Adapter
    private RecyclerView rvComments; // 留言RecycleView

    private LinearLayout hrLayout;
    private TextView tvAverageHr; // 平均心率
    private TextView tvMaxHr; // 最大心率
    private EcgHrLineChart hrLineChart; // 心率折线图
    private EcgHrHistogramChart hrHistChart; // 心率直方图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg_record);

        int recordId = getIntent().getIntExtra("record_id", -1);
        try {
            record = LitePal.find(EcgRecord.class, recordId, true);
            record.openSigFile();
        } catch (IOException e) {
            e.printStackTrace();
            setResult(RESULT_CANCELED);
            finish();
        }

        modifyTime = record.getModifyTime();

        tvModifyTime = findViewById(R.id.tv_modify_time);
        tvCreator = findViewById(R.id.tv_creator);
        tvCreateTime = findViewById(R.id.tv_create_time);
        tvLength = findViewById(R.id.tv_signal_length);
        tvHrNum = findViewById(R.id.tv_hr_num);

        signalLayout = findViewById(R.id.layout_signal_part);
        signalView = findViewById(R.id.rwv_signal_view);
        signalView.setListener(this);
        rvComments = findViewById(R.id.rv_comment_list);
        LinearLayoutManager commentLayoutManager = new LinearLayoutManager(this);
        commentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvComments.setLayoutManager(commentLayoutManager);
        rvComments.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        commentAdapter = new EcgCommentAdapter(null, this);
        rvComments.setAdapter(commentAdapter);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        btnReplayCtrl = findViewById(R.id.ib_replay_control);
        btnReplayCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(signalView.isStart()) {
                    signalView.stopShow();
                } else {
                    signalView.startShow();
                }
            }
        });
        sbReplay = findViewById(R.id.sb_replay);
        sbReplay.setEnabled(false);
        sbReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    signalView.showAtSecond(i);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        hrLayout = findViewById(R.id.layout_hr_part);
        hrHistChart = findViewById(R.id.chart_hr_histogram);
        hrLineChart = findViewById(R.id.linechart_hr);
        tvAverageHr = findViewById(R.id.tv_average_hr_value);
        tvMaxHr = findViewById(R.id.tv_max_hr_value);

        initialize();
    }

    private void initialize() {
        tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(modifyTime));

        User fileCreator = record.getCreator();
        User account = AccountManager.getInstance().getAccount();
        if(fileCreator.equals(account)) {
            tvCreator.setText(Html.fromHtml("<u>您本人</u>"));
        } else {
            tvCreator.setText(Html.fromHtml("<u>" + record.getCreatorName() + "</u>"));
        }

        String createdTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createdTime);

        if(record.getDataNumInSignal() == 0) {
            tvLength.setText("无");
        } else {
            String dataTimeLength = DateTimeUtil.secToTimeInChinese(record.getDataNumInSignal() / record.getSampleRate());
            tvLength.setText(dataTimeLength);
        }

        int hrNum = record.getHrList().size();
        tvHrNum.setText(String.valueOf(hrNum));

        initEcgView(record);
        int secondInSignal = record.getDataNumInSignal()/ record.getSampleRate();
        tvCurrentTime.setText(DateTimeUtil.secToTime(0));
        tvTotalTime.setText(DateTimeUtil.secToTime(secondInSignal));
        sbReplay.setMax(secondInSignal);

        List<EcgNormalComment> commentList = getCommentListInRecord(record);
        commentAdapter.updateCommentList(commentList);
        if(commentList.size() > 0)
            rvComments.smoothScrollToPosition(0);

        signalView.startShow();

        if(record.getDataNumInSignal() == 0) {
            signalLayout.setVisibility(View.GONE);
        } else {
            signalLayout.setVisibility(View.VISIBLE);
        }

        if(record.getHrList().isEmpty()) {
            hrLayout.setVisibility(View.GONE);
        } else {
            hrLayout.setVisibility(View.VISIBLE);
        }

        EcgHrStatisticsInfo hrStatisticsInfo = new EcgHrStatisticsInfo(record.getHrList(), HR_FILTER_SECOND);
        tvAverageHr.setText(String.valueOf(hrStatisticsInfo.getAverageHr()));
        tvMaxHr.setText(String.valueOf(hrStatisticsInfo.getMaxHr()));
        hrLineChart.showLineChart(hrStatisticsInfo.getFilteredHrList(), "心率时序图", Color.BLUE);
        hrHistChart.update(hrStatisticsInfo.getNormHistogram(HR_HISTOGRAM_BAR_NUM));
    }

    private void initEcgView(EcgRecord ecgRecord) {
        if(ecgRecord == null) return;
        signalView.setEcgRecord(ecgRecord);
        signalView.setZeroLocation(ZERO_LOCATION_IN_ECG_VIEW);
    }

    // 获取选中文件的留言列表
    private List<EcgNormalComment> getCommentListInRecord(EcgRecord ecgRecord) {
        if(ecgRecord == null)
            return new ArrayList<>();
        else {
            User account = AccountManager.getInstance().getAccount();
            boolean found = false;
            for(EcgNormalComment comment : ecgRecord.getCommentList()) {
                if(comment.getCreator().equals(account)) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                ecgRecord.addComment(EcgNormalComment.create());
            }
            return ecgRecord.getCommentList();
        }
    }

    @Override
    public void onShowStateUpdated(boolean isShow) {
        if(isShow) {
            btnReplayCtrl.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_pause_32px));
        } else {
            btnReplayCtrl.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_play_32px));
        }
        sbReplay.setEnabled(!isShow);
    }

    @Override
    public void onDataLocationUpdated(long dataLocation, int sampleRate) {
        int second = (int)(dataLocation/ sampleRate);
        tvCurrentTime.setText(String.valueOf(DateTimeUtil.secToTime(second)));
        sbReplay.setProgress(second);
    }

    @Override
    public void onSelectedCommentSaved() {
        if(record != null) {
            record.save();
            tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(record.getModifyTime()));
        }
    }

    @Override
    public void onCommentDeleted(EcgNormalComment comment) {
        signalView.stopShow();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除留言").setMessage("确定删除该留言吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //fileReplayModel.deleteComment(appendix);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("updated", modifyTime != record.getModifyTime());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        signalView.stopShow();
        try {
            record.closeSigFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
