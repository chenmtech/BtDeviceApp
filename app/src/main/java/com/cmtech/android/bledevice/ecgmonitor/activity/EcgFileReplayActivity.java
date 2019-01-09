package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgCommentAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgFileReplayModel;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgCommentOperator;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgFileReplayObserver;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgFileReelWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.io.IOException;


public class EcgFileReplayActivity extends AppCompatActivity implements IEcgFileReplayObserver, EcgFileReelWaveView.IEcgFileReelWaveViewObserver, IEcgCommentOperator {
    private static final String TAG = "EcgFileReplayActivity";

    private EcgFileReplayModel replayModel;         // 模型实例

    private EcgFileReelWaveView ecgView;            // ecgView

    private ImageButton ibAddComment;

    private ImageButton btnSwitchReplayState;

    private EditText etComment;

    private EcgCommentAdapter reportAdapter;
    private RecyclerView rvReportList;

    private TextView tvTotalTime;
    private TextView tvCurrentTime;
    private TextView tvSecondWhenComment;

    private SeekBar sbEcgReplay;

    private ImageButton ibAddSecondToComment;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ecgfile_replay);

        Intent intent = getIntent();
        String fileName = "";
        if(intent == null || (fileName = intent.getStringExtra("fileName")) == null ) {
            finish();
        }

        try {
            replayModel = new EcgFileReplayModel(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }

        ecgView = findViewById(R.id.rwv_ecgview);
        ecgView.setEcgFile(replayModel.getEcgFile());
        initEcgView(replayModel.getxPixelPerData(), replayModel.getyValuePerPixel(), replayModel.getPixelPerGrid(), 0.5);

        tvCurrentTime = findViewById(R.id.tv_ecgreplay_currenttime);
        tvCurrentTime.setText(DateTimeUtil.secToTime(replayModel.getCurrentSecond()));
        tvTotalTime = findViewById(R.id.tv_ecgreplay_totaltime);
        tvTotalTime.setText(DateTimeUtil.secToTime(replayModel.getTotalSecond()));

        sbEcgReplay = findViewById(R.id.sb_ecgreplay);
        sbEcgReplay.setMax(replayModel.getTotalSecond());
        sbEcgReplay.setEnabled(false);
        sbEcgReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    ecgView.showAtLocation(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        rvReportList = findViewById(R.id.rv_ecgreplay_comment);
        LinearLayoutManager reportLayoutManager = new LinearLayoutManager(this);
        rvReportList.setLayoutManager(reportLayoutManager);
        rvReportList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        reportAdapter = new EcgCommentAdapter(replayModel.getCommentList(), this);
        rvReportList.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        if(reportAdapter.getItemCount() > 1)
            rvReportList.smoothScrollToPosition(reportAdapter.getItemCount()-1);


        btnSwitchReplayState = findViewById(R.id.ib_ecgreplay_startandstop);
        btnSwitchReplayState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ecgView.isReplaying()) {
                    stopReplay();
                } else {
                    startReplay();
                }
            }
        });


        etComment = findViewById(R.id.et_ecgreplay_comment);
        ibAddComment = findViewById(R.id.ib_ecgreplay_addcomment);
        ibAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = etComment.getText().toString();
                if(comment.length() < 3) {
                    Toast.makeText(EcgFileReplayActivity.this, "你的留言太短，再多写点吧！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ecgView.isReplaying())
                    stopReplay();
                replayModel.addComment(etComment.getText().toString());
            }
        });

        tvSecondWhenComment = findViewById(R.id.tv_ecgreplay_secondwhencomment);

        ibAddSecondToComment = findViewById(R.id.ib_ecgreplay_addsecondtocomment);
        ibAddSecondToComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replayModel.setShowSecondInComment(!replayModel.isShowSecondInComment());
            }
        });

        ecgView.registerEcgFileReelWaveViewObserver(this);

        replayModel.registerEcgFileReplayObserver(this);

        startReplay();
    }

    private void startReplay() {
        ecgView.startShow();
    }

    private void stopReplay() {
        ecgView.stopShow();
    }

    private void initEcgView(int xRes, float yRes, int viewGridWidth, double zerolocation) {
        ecgView.setRes(xRes, yRes);
        ecgView.setGridWidth(viewGridWidth);
        ecgView.setZeroLocation(zerolocation);
        ecgView.clearData();
        ecgView.initView();
    }

    @Override
    public void onBackPressed() {
        stopReplay();

        ecgView.removeEcgFileReelWaveViewObserver();

        if(replayModel != null) {
            replayModel.close();
            replayModel.removeEcgFileObserver();
        }

        Intent intent = new Intent();
        intent.putExtra("updated", replayModel.isUpdated());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * IEcgFileReplayObserver接口函数
     */
    @Override
    public void updateCommentList() {
        reportAdapter.notifyDataSetChanged();
        if(reportAdapter.getItemCount() > 1)
            rvReportList.smoothScrollToPosition(reportAdapter.getItemCount()-1);
        etComment.setText("");
    }

    @Override
    public void updateShowSecondInComment(boolean show, int second) {
        if(show) {
            tvSecondWhenComment.setText("第" + DateTimeUtil.secToTime(second) + "秒");
            tvSecondWhenComment.setVisibility(View.VISIBLE);
        } else {
            tvSecondWhenComment.setVisibility(View.GONE);
        }
    }

    /**
     * EcgFileReelWaveView.IEcgFileReelWaveViewObserver接口函数
     */
    @Override
    public void updateShowState(boolean replaying) {
        if(replaying) {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_pause_48px));
            sbEcgReplay.setEnabled(false);
        } else {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_play_48px));
            sbEcgReplay.setEnabled(true);
        }
    }

    @Override
    public void updateCurrentTime(int second) {
        tvCurrentTime.setText(String.valueOf(DateTimeUtil.secToTime(second)));
        sbEcgReplay.setProgress(second);
        replayModel.setCurrentSecond(second);
    }

    /**
     * IEcgCommentOperator接口函数
     */
    @Override
    public void deleteComment(final EcgComment comment) {
        if(ecgView.isReplaying())
            stopReplay();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除Ecg留言");
        builder.setMessage("确定删除该Ecg留言吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                replayModel.deleteComment(comment);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    @Override
    public void locateComment(EcgComment comment) {
        int second = comment.getSecondInEcg();
        if(second < 0 || second > replayModel.getTotalSecond())
            return;

        if(ecgView.isReplaying())
            stopReplay();

        // 特意提前一秒播放
        ecgView.showAtLocation((second-1 < 0) ? 0 : second-1);
    }
}
