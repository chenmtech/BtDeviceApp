package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.cmtech.android.bledevice.ecgmonitor.ReelWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.dsp.bmefile.BmeFileHead30;
//import com.cmtech.dsp.bmefile.StreamBmeFile;
import com.cmtech.dsp.exception.FileException;
import com.vise.log.ViseLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EcgFileReplayActivity extends AppCompatActivity implements IEcgFileReplayObserver{
    private static final String TAG = "EcgFileReplayActivity";

    private EcgFileReplayModel replayModel;

    private EcgFile selectedFile;

    private ReelWaveView ecgView;

    private Button btnEcgAddComment;

    private ImageButton btnSwitchReplayState;

    private EditText etComment;

    private EcgReportAdapter reportAdapter;
    private RecyclerView rvReportList;

    // 用于设置EcgWaveView的参数
    private int viewGridWidth = 10;               // 设置ECG View中的每小格有10个像素点
    // 下面两个参数可用来计算View中的xRes和yRes
    private float viewXGridTime = 0.04f;          // 设置ECG View中的横向每小格代表0.04秒，即25格/s，这是标准的ECG走纸速度
    private float viewYGridmV = 0.1f;             // 设置ECG View中的纵向每小格代表0.1mV

    private boolean playStatus = false;

    private class ShowTask extends TimerTask {
        @Override
        public void run() {
            synchronized (EcgFileReplayActivity.this) {
                try {
                    ecgView.showData(selectedFile.readData());
                } catch (FileException e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopShow();
                        }
                    });
                }
            }
        }
    }
    private Timer showTimer;


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
        replayModel = new EcgFileReplayModel(fileName);
        replayModel.registerEcgFileReplayObserver(this);

        selectedFile = replayModel.getEcgFile();

        rvReportList = findViewById(R.id.rv_ecgfile_report);
        LinearLayoutManager reportLayoutManager = new LinearLayoutManager(this);
        rvReportList.setLayoutManager(reportLayoutManager);
        rvReportList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        reportAdapter = new EcgReportAdapter(replayModel.getCommentList());
        rvReportList.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        if(reportAdapter.getItemCount() > 1)
            rvReportList.smoothScrollToPosition(reportAdapter.getItemCount()-1);

        /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                replayEcgFile(selectedFile.getFile());
            }
        }, 1000);*/


        ecgView = findViewById(R.id.ecg_view);

        btnSwitchReplayState = findViewById(R.id.btn_ecg_startandstop);
        btnSwitchReplayState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedFile == null) return;

                if(playStatus) {
                    stopShow();
                } else {
                    int interval = 1000/selectedFile.getFs();
                    startShow(interval);
                }
            }
        });


        etComment = findViewById(R.id.et_replay_comment);
        btnEcgAddComment = findViewById(R.id.btn_ecgreplay_addcomment);
        btnEcgAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replayModel.addComment(etComment.getText().toString());
            }
        });

        replay();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    public void replay() {
        replayModel.replay();
    }

    private void startShow(int interval) {
        if(!playStatus) {
            showTimer = new Timer();
            showTimer.scheduleAtFixedRate(new ShowTask(), interval, interval);
            btnSwitchReplayState.setImageDrawable(getResources().getDrawable(R.mipmap.ic_ecg_pause_48px));
            playStatus = true;
        }
    }

    private void stopShow() {
        if(playStatus) {
            showTimer.cancel();
            showTimer = null;
            btnSwitchReplayState.setImageDrawable(getResources().getDrawable(R.mipmap.ic_ecg_play_48px));
            playStatus = false;
        }
    }

    private void initialEcgView() {
        if(selectedFile == null) return;
        int sampleRate = selectedFile.getFs();
        int value1mV = ((BmeFileHead30)selectedFile.getBmeFileHead()).getCalibrationValue();
        int xRes = Math.round(viewGridWidth / (viewXGridTime * sampleRate));   // 计算横向分辨率
        float yRes = value1mV * viewYGridmV / viewGridWidth;                     // 计算纵向分辨率
        ecgView.setRes(xRes, yRes);
        ecgView.setGridWidth(viewGridWidth);
        ecgView.setZeroLocation(0.5);
        ecgView.clearData();
        ecgView.initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        replayModel.removeEcgFileObserver();

        replayModel.close();

    }

    private void deselectFile() {
        if(selectedFile != null) {
            try {
                if(playStatus)
                    stopShow();

                selectedFile.close();
                selectedFile = null;

                ecgView.clearData();
                ecgView.initView();
            } catch (FileException e) {
                e.printStackTrace();
            }
        }
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

    @Override
    public void showEcgData(final int data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ecgView.showData(data);
            }
        });
    }
}
