package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.cmtech.android.bledevice.ecgmonitor.EcgFileReelWaveView;
import com.cmtech.android.bledeviceapp.R;
//import com.cmtech.dsp.bmefile.StreamBmeFile;
import com.cmtech.dsp.exception.FileException;

public class EcgFileReplayActivity extends AppCompatActivity implements IEcgFileReplayObserver{
    private static final String TAG = "EcgFileReplayActivity";

    private EcgFileReplayModel replayModel;

    private EcgFileReelWaveView ecgView;

    private Button btnEcgAddComment;

    private ImageButton btnSwitchReplayState;

    private EditText etComment;

    private EcgReportAdapter reportAdapter;
    private RecyclerView rvReportList;


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

        rvReportList = findViewById(R.id.rv_ecgfile_report);
        LinearLayoutManager reportLayoutManager = new LinearLayoutManager(this);
        rvReportList.setLayoutManager(reportLayoutManager);
        rvReportList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        reportAdapter = new EcgReportAdapter(replayModel.getCommentList());
        rvReportList.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        if(reportAdapter.getItemCount() > 1)
            rvReportList.smoothScrollToPosition(reportAdapter.getItemCount()-1);


        btnSwitchReplayState = findViewById(R.id.btn_ecg_startandstop);
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


        etComment = findViewById(R.id.et_replay_comment);
        btnEcgAddComment = findViewById(R.id.btn_ecgreplay_addcomment);
        btnEcgAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replayModel.addComment(etComment.getText().toString());
            }
        });

        initReplay();

        startReplay();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

        stopReplay();

        replayModel.removeEcgFileObserver();

        replayModel.close();
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
        btnSwitchReplayState.setImageDrawable(getResources().getDrawable(R.mipmap.ic_ecg_pause_48px));
    }

    public void stopReplay() {
        ecgView.stopShow();
        btnSwitchReplayState.setImageDrawable(getResources().getDrawable(R.mipmap.ic_ecg_play_48px));
    }

}
