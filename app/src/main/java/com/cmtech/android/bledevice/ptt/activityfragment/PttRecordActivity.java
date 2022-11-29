package com.cmtech.android.bledevice.ptt.activityfragment;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordActivity;
import com.cmtech.android.bledeviceapp.data.record.BlePttRecord;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledeviceapp.view.OnRollWaveViewListener;
import com.cmtech.android.bledeviceapp.view.RollPttView;

import org.litepal.LitePal;
import org.litepal.crud.callback.FindCallback;

public class PttRecordActivity extends RecordActivity implements OnRollWaveViewListener {
    private RollPttView pttView; // pttView
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton btnReplayCtrl; // 转换播放状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ptt);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);

        LitePal.findAsync(BlePttRecord.class, recordId, true).listen(new FindCallback<BlePttRecord>() {
            @Override
            public void onFinish(BlePttRecord blePttRecord) {
                blePttRecord.openSigFile();
                if(blePttRecord.noSignalFile()) {
                    blePttRecord.download(PttRecordActivity.this, "下载记录中，请稍等。", (code,msg) -> {
                        if (code == RCODE_SUCCESS) {
                            blePttRecord.openSigFile();
                            blePttRecord.setSigLen(blePttRecord.getDataNum());
                            record = blePttRecord;
                            initUI();
                        } else {
                            Toast.makeText(PttRecordActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    blePttRecord.setSigLen(blePttRecord.getDataNum());
                    record = blePttRecord;
                    initUI();
                }
            }
        });
    }

    public void initUI() {
        super.initUI();

        pttView = findViewById(R.id.roll_ptt_view);
        pttView.setListener(this);
        pttView.setup((BlePttRecord) record, new float[]{0.4f,0.6f});
        //pttView.setGestureDetector(null);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvCurrentTime.setText(DateTimeUtil.secToTime(0));

        tvTotalTime = findViewById(R.id.tv_total_time);
        int timeLength = record.getSigLen()/record.getSampleRate();
        tvTotalTime.setText(DateTimeUtil.secToTime(timeLength));

        sbReplay = findViewById(R.id.sb_replay);
        sbReplay.setMax(timeLength);
        sbReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    pttView.showAtSecond(i);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        sbReplay.setEnabled(false);

        btnReplayCtrl = findViewById(R.id.ib_replay_control);
        btnReplayCtrl.setOnClickListener(view -> {
            if(pttView.isShowing()) {
                pttView.stopShow();
            } else {
                pttView.startShow();
            }
        });

        pttView.startShow();
    }

    @Override
    public void onDataLocationUpdated(long location, int second) {
        tvCurrentTime.setText(DateTimeUtil.secToTime(second));
        sbReplay.setProgress(second);
    }

    @Override
    public void onShowStateUpdated(boolean show) {
        sbReplay.setEnabled(!show);
        if(show)
            btnReplayCtrl.setImageResource(R.mipmap.ic_pause_32px);
        else
            btnReplayCtrl.setImageResource(R.mipmap.ic_play_32px);
    }

    @Override
    public void onBackPressed() {
        if(pttView != null)
            pttView.stopShow();

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
