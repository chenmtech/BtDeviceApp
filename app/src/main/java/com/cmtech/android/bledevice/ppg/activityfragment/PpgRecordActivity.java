package com.cmtech.android.bledevice.ppg.activityfragment;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.BlePpgRecord;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledeviceapp.view.OnRollWaveViewListener;
import com.cmtech.android.bledeviceapp.view.RollPpgView;
import com.cmtech.android.bledeviceapp.view.RollWaveView;
import com.cmtech.android.bledeviceapp.view.layout.RecordIntroductionLayout;
import com.cmtech.android.bledeviceapp.view.layout.RecordNoteLayout;

import org.litepal.LitePal;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;

public class PpgRecordActivity extends AppCompatActivity implements OnRollWaveViewListener {
    private BlePpgRecord record;

    private RecordIntroductionLayout introLayout;
    private RecordNoteLayout noteLayout;

    private RollPpgView ppgView; // ppg View
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton btnReplayCtrl; // 转换播放状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ppg);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);
        record = LitePal.find(BlePpgRecord.class, recordId, true);
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

        ppgView = findViewById(R.id.roll_ppg_view);
        ppgView.setListener(this);
        ppgView.setup(record, RollWaveView.DEFAULT_ZERO_LOCATION);

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
            if(ppgView.isShowing()) {
                ppgView.stopShow();
            } else {
                ppgView.startShow();
            }
        });

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
    }

    @Override
    public void onBackPressed() {
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
