package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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

import com.cmtech.android.bledevice.ecgmonitor.EcgFileReelWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
//import com.cmtech.dsp.bmefile.StreamBmeFile;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.dsp.bmefile.exception.FileException;

public class EcgFileReplayActivity extends AppCompatActivity implements IEcgFileReplayObserver, EcgFileReelWaveView.IEcgFileReelWaveViewObserver {
    private static final String TAG = "EcgFileReplayActivity";

    private EcgFileReplayModel replayModel;

    private EcgFileReelWaveView ecgView;

    private ImageButton ibAddComment;

    private ImageButton btnSwitchReplayState;

    private EditText etComment;

    private EcgReportAdapter reportAdapter;
    private RecyclerView rvReportList;

    private TextView tvTotalTime;
    private TextView tvCurrentTime;

    private SeekBar sbEcgReplay;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ecgfile_replay);

        Intent intent = getIntent();
        if(intent == null || intent.getStringExtra("fileName") == null)
            finish();

        String fileName = intent.getStringExtra("fileName");
        try {
            replayModel = new EcgFileReplayModel(fileName);
        } catch (FileException e) {
            e.printStackTrace();
            finish();
        }

        replayModel.registerEcgFileReplayObserver(this);

        ecgView = findViewById(R.id.ecg_view);
        ecgView.setEcgFile(replayModel.getEcgFile());
        ecgView.registerEcgFileReelWaveViewObserver(this);

        tvCurrentTime = findViewById(R.id.tv_ecgreplay_currenttime);

        tvTotalTime = findViewById(R.id.tv_ecgreplay_totaltime);
        int totalTime = replayModel.getEcgFile().getDataNum()/replayModel.getEcgFile().getFs();
        tvTotalTime.setText(""+DateTimeUtil.secToTime(totalTime));

        sbEcgReplay = findViewById(R.id.sb_ecgreplay);
        sbEcgReplay.setMax(totalTime);
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
        reportAdapter = new EcgReportAdapter(replayModel.getCommentList());
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
                    Toast.makeText(EcgFileReplayActivity.this, "你的评论太短，再多写点吧！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ecgView.isReplaying())
                    stopReplay();
                replayModel.addComment(etComment.getText().toString());
            }
        });

        initReplay();

        startReplay();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void initReplay() {
        replayModel.initReplay();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //stopReplay();

        //replayModel.removeEcgFileObserver();

        //replayModel.close();
    }

    @Override
    public void onBackPressed() {
        stopReplay();

        replayModel.removeEcgFileObserver();

        replayModel.close();

        Intent intent = new Intent();
        intent.putExtra("updated", replayModel.isUpdated());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void updateCommentList() {
        reportAdapter.notifyDataSetChanged();
        if(reportAdapter.getItemCount() > 1)
            rvReportList.smoothScrollToPosition(reportAdapter.getItemCount()-1);
        etComment.setText("");
    }

    @Override
    public void initEcgView(int xRes, float yRes, int viewGridWidth, double zerolocation) {
        ecgView.setRes(xRes, yRes);
        ecgView.setGridWidth(viewGridWidth);
        ecgView.setZeroLocation(zerolocation);
        ecgView.clearData();
        ecgView.initView();
    }

    public void startReplay() {
        ecgView.startShow();
    }

    public void stopReplay() {
        ecgView.stopShow();
    }

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
        tvCurrentTime.setText("" + DateTimeUtil.secToTime(second));
        sbEcgReplay.setProgress(second);
    }
}
