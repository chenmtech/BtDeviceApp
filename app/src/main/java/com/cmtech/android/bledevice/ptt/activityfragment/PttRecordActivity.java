package com.cmtech.android.bledevice.ptt.activityfragment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.data.record.BleEcgRecord;
import com.cmtech.android.bledeviceapp.data.record.BlePpgRecord;
import com.cmtech.android.bledeviceapp.data.record.BlePttRecord;
import com.cmtech.android.bledeviceapp.data.record.RecordFactory;
import com.cmtech.android.bledeviceapp.data.record.RecordType;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledeviceapp.view.OnRollWaveViewListener;
import com.cmtech.android.bledeviceapp.view.RollEcgView;
import com.cmtech.android.bledeviceapp.view.RollPpgView;
import com.cmtech.android.bledeviceapp.view.RollWaveView;
import com.cmtech.android.bledeviceapp.view.layout.RecordIntroductionLayout;
import com.cmtech.android.bledeviceapp.view.layout.RecordNoteLayout;

import org.litepal.LitePal;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;

public class PttRecordActivity extends AppCompatActivity implements OnRollWaveViewListener {
    private BlePttRecord record;

    private RecordIntroductionLayout introLayout;
    private RecordNoteLayout noteLayout;

    private RollEcgView ecgView; // ecgView
    private RollPpgView ppgView; // ppgView
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton btnReplayCtrl; // 转换播放状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ptt);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);
        record = LitePal.find(BlePttRecord.class, recordId, true);
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        } else {
            initUI();
        }
    }

    private void initUI() {
        introLayout = findViewById(R.id.layout_record_intro);
        introLayout.setRecord(record);
        introLayout.updateView();

        noteLayout = findViewById(R.id.layout_record_note);
        noteLayout.setRecord(record);
        noteLayout.updateView();

        ecgView = findViewById(R.id.roll_ecg_view);
        ecgView.setListener(this);
        BleEcgRecord ecgRecord = (BleEcgRecord) RecordFactory.create(RecordType.ECG, BasicRecord.DEFAULT_RECORD_VER, record.getCreateTime(), record.getDevAddress(), record.getCreatorId());
        ecgRecord.setSampleRate(record.getSampleRate());
        ecgRecord.setCaliValue(record.getEcgCaliValue());
        ecgRecord.setEcgData(record.getEcgData());
        ecgView.setup(ecgRecord, RollWaveView.DEFAULT_ZERO_LOCATION);
        ecgView.setGestureDetector(null);

        ppgView = findViewById(R.id.roll_ppg_view);
        //ppgView.setListener(this);
        BlePpgRecord ppgRecord = (BlePpgRecord) RecordFactory.create(RecordType.PPG, BasicRecord.DEFAULT_RECORD_VER, record.getCreateTime(), record.getDevAddress(), record.getCreatorId());
        ppgRecord.setSampleRate(record.getSampleRate());
        ppgRecord.setCaliValue(record.getPpgCaliValue());
        ppgRecord.setPpgData(record.getPpgData());
        ppgView.setup(ppgRecord, RollWaveView.DEFAULT_ZERO_LOCATION);
        ppgView.setGestureDetector(null);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvCurrentTime.setText(DateTimeUtil.secToMinute(0));

        tvTotalTime = findViewById(R.id.tv_total_time);
        int timeLength = record.getRecordSecond();
        tvTotalTime.setText(DateTimeUtil.secToMinute(timeLength));

        sbReplay = findViewById(R.id.sb_replay);
        sbReplay.setMax(timeLength);
        sbReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    ecgView.showAtSecond(i);
                    ppgView.showAtSecond(i);
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
            if(ecgView.isShowing()) {
                ecgView.stopShow();
                ppgView.stopShow();
            } else {
                ecgView.startShow();
                ppgView.startShow();
            }
        });

        ecgView.startShow();
        ppgView.startShow();
    }

    @Override
    public void onDataLocationUpdated(long location, int second) {
        tvCurrentTime.setText(DateTimeUtil.secToMinute(second));
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
        if(ecgView != null)
            ecgView.stopShow();
        if(ppgView != null)
            ppgView.stopShow();

        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
