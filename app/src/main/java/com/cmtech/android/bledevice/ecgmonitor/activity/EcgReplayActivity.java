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

import com.cmtech.android.bledevice.ecgmonitor.adapter.EcgAppendixAdapter;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgReplayModel;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgAppendixOperator;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgReplayObserver;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendix;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgFileRollWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.io.IOException;

/**
 * EcgReplayActivity: 心电文件回放Activity
 * Created by bme on 2018/11/10.
 */

public class EcgReplayActivity extends AppCompatActivity implements IEcgReplayObserver, EcgFileRollWaveView.IEcgFileRollWaveViewObserver, IEcgAppendixOperator {
    private static final String TAG = "EcgReplayActivity";

    private EcgReplayModel replayModel; // 回放模型实例
    private EcgFileRollWaveView ecgView; // ecgView
    private EcgAppendixAdapter appendixAdapter; // 附加信息Adapter
    private RecyclerView rvAppendix; // 附加信息RecyclerView
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放的信号的时刻
    //private ImageButton ibAddAppendix; // 添加附加信息
    private ImageButton btnSwitchReplayState; // 转换回放状态
    private ImageButton ibAddAppendixTime; // 给附加信息添加时刻
    //private EditText etAppendix; // 附加信息编辑EditText
    private SeekBar sbReplay; // 播放条


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgreplay);

        Intent intent = getIntent();
        String fileName = "";
        if(intent == null || (fileName = intent.getStringExtra("fileName")) == null ) {
            finish();
        }

        try {
            replayModel = new EcgReplayModel(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(EcgReplayActivity.this, "无法回放该文件", Toast.LENGTH_SHORT).show();
            finish();
        }

        ecgView = findViewById(R.id.rwv_ecgview);
        ecgView.setEcgFile(replayModel.getEcgFile());
        initEcgView(replayModel.getxPixelPerData(), replayModel.getyValuePerPixel(), replayModel.getPixelPerGrid(), 0.5);

        rvAppendix = findViewById(R.id.rv_ecgreplay_appendix);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvAppendix.setLayoutManager(layoutManager);
        rvAppendix.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        appendixAdapter = new EcgAppendixAdapter(replayModel.getAppendixList(), this, replayModel.getSampleRate());
        rvAppendix.setAdapter(appendixAdapter);
        appendixAdapter.notifyDataSetChanged();
        if(appendixAdapter.getItemCount() > 1)
            rvAppendix.smoothScrollToPosition(appendixAdapter.getItemCount()-1);

        tvCurrentTime = findViewById(R.id.tv_ecgreplay_currenttime);
        tvCurrentTime.setText(DateTimeUtil.secToTime(replayModel.getCurrentSecond()));
        tvTotalTime = findViewById(R.id.tv_ecgreplay_totaltime);
        tvTotalTime.setText(DateTimeUtil.secToTime(replayModel.getTotalSecond()));
        //etAppendix = findViewById(R.id.et_ecgreplay_appendix);

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

        /*ibAddAppendix = findViewById(R.id.ib_ecgreplay_addappendix);
        ibAddAppendix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = etAppendix.getText().toString();
                if(comment.length() < 3) {
                    Toast.makeText(EcgReplayActivity.this, "留言太短，再多写点吧！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ecgView.isReplaying())
                    stopReplay();
                replayModel.addAppendix(etAppendix.getText().toString());
            }
        });

        ibAddAppendixTime = findViewById(R.id.ib_ecgreplay_addappendixtime);
        ibAddAppendixTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replayModel.setShowAppendixTime(!replayModel.isShowAppendixTime());
            }
        });*/

        sbReplay = findViewById(R.id.sb_ecgreplay);
        sbReplay.setMax(replayModel.getTotalSecond());
        sbReplay.setEnabled(false);
        sbReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    ecgView.showAtInSecond(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ecgView.registerEcgFileRollWaveViewObserver(this);
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

        ecgView.removeEcgFileRollWaveViewObserver();

        if(replayModel != null) {
            replayModel.close();
            replayModel.removeEcgFileReplayObserver();
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
    public void updateAppendixList() {
        appendixAdapter.update(replayModel.getAppendixList());
        if(appendixAdapter.getItemCount() > 1)
            rvAppendix.smoothScrollToPosition(appendixAdapter.getItemCount()-1);
        //etAppendix.setText("");
    }

    /**
     * EcgFileReelWaveView.IEcgFileReelWaveViewObserver接口函数
     */
    @Override
    public void updateShowState(boolean replaying) {
        if(replaying) {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_pause_48px));
            sbReplay.setEnabled(false);
        } else {
            btnSwitchReplayState.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_play_48px));
            sbReplay.setEnabled(true);
        }
    }

    @Override
    public void updateDataLocation(long dataLocation) {
        int second = (int)(dataLocation/replayModel.getSampleRate());
        tvCurrentTime.setText(String.valueOf(DateTimeUtil.secToTime(second)));
        sbReplay.setProgress(second);
        replayModel.setDataLocation(dataLocation);
    }

    /**
     * IEcgAppendixOperator接口函数
     */
    @Override
    public void deleteAppendix(final EcgAppendix appendix) {
        if(ecgView.isReplaying())
            stopReplay();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除一条Ecg附加信息");
        builder.setMessage("确定删除该Ecg附加信息吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                replayModel.deleteAppendix(appendix);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }
}
