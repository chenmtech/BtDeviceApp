package com.cmtech.android.bledevice.ecgmonitor.view;

import android.content.DialogInterface;
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
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgNormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.viewcomponent.RollWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import java.io.IOException;

public class EcgRecordActivity extends AppCompatActivity implements RollWaveView.OnRollWaveViewListener, EcgCommentAdapter.OnEcgCommentListener{
    private EcgFile file;
    private TextView tvModifyTime; // 更新时间
    private TextView tvCreator; // 创建人
    private TextView tvCreateTime; // 创建时间
    private TextView tvLength; // 信号长度
    private TextView tvHrNum; // 心率次数

    private LinearLayout signalLayout;
    private EcgFileRollWaveView signalView; // signalView
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

        String fileName = getIntent().getStringExtra("file_name");
        try {
            file = EcgFile.open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }

        tvModifyTime = findViewById(R.id.tv_modify_time);
        tvCreator = findViewById(R.id.tv_creator);
        tvCreateTime = findViewById(R.id.tv_create_time);
        tvLength = findViewById(R.id.tv_signal_length);
        tvHrNum = findViewById(R.id.tv_hr_num);

        tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(file.getFile().lastModified()));

        User fileCreator = file.getCreator();
        User account = UserManager.getInstance().getUser();
        if(fileCreator.equals(account)) {
            tvCreator.setText(Html.fromHtml("<u>您本人</u>"));
        } else {
            tvCreator.setText(Html.fromHtml("<u>" + file.getCreatorName() + "</u>"));
        }

        String createdTime = DateTimeUtil.timeToShortStringWithTodayYesterday(file.getCreatedTime());
        tvCreateTime.setText(createdTime);

        if(file.getDataNum() == 0) {
            tvLength.setText("无");
        } else {
            String dataTimeLength = DateTimeUtil.secToTimeInChinese(file.getDataNum() / file.getSampleRate());
            tvLength.setText(dataTimeLength);
        }

        int hrNum = file.getHrList().size();
        tvHrNum.setText(String.valueOf(hrNum));

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
        saveSelectedFileComment();
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

    // 保存留言信息
    private void saveSelectedFileComment() {
        try {
            if(file != null) {
                file.saveFileTail();
            }
        } catch (IOException e) {
            ViseLog.e("保存留言错误。");
        }
    }
}
