package com.cmtech.android.bledevice.eeg.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleEegRecord10;
import com.cmtech.android.bledevice.view.OnRollWaveViewListener;
import com.cmtech.android.bledevice.view.RecordIntroductionLayout;
import com.cmtech.android.bledevice.view.RecordNoteLayout;
import com.cmtech.android.bledevice.view.RollEegView;
import com.cmtech.android.bledevice.view.RollWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.ICodeCallback;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import static com.cmtech.android.bledevice.record.BasicRecord.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

public class EegRecordActivity extends AppCompatActivity implements OnRollWaveViewListener {
    private BleEegRecord10 record;

    private RecordIntroductionLayout introLayout;

    private RollEegView eegView; // eegView
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton btnReplayCtrl; // 转换播放状态

    private RecordNoteLayout noteLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_eeg);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);

        record = LitePal.where("id = ?", ""+recordId).findFirst(BleEegRecord10.class);
        ViseLog.e(record);
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        if(record.getNote() == null) {
            record.setNote("");
            record.save();
        }

        if(record.noSignal()) {
            record.download(this, new ICodeCallback() {
                @Override
                public void onFinish(int code) {
                    if (code == RETURN_CODE_SUCCESS) {
                        initUI();
                    } else {
                        Toast.makeText(EegRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            });
        } else {
            initUI();
        }
    }

    private void initUI() {
        ViseLog.e(record);

        introLayout = findViewById(R.id.layout_record_intro);
        introLayout.setRecord(record);
        introLayout.updateView();

        noteLayout = findViewById(R.id.layout_record_note);
        noteLayout.setRecord(record);
        noteLayout.updateView();

        eegView = findViewById(R.id.roll_eeg_view);
        eegView.setListener(this);
        eegView.setup(record, RollWaveView.DEFAULT_ZERO_LOCATION);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        btnReplayCtrl = findViewById(R.id.ib_replay_control);
        btnReplayCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(eegView.isShowing()) {
                    eegView.stopShow();
                } else {
                    eegView.startShow();
                }
            }
        });
        sbReplay = findViewById(R.id.sb_replay);
        sbReplay.setEnabled(false);
        sbReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    eegView.showAtSecond(i);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        int second = record.getRecordSecond();
        tvCurrentTime.setText(DateTimeUtil.secToMinute(0));
        tvTotalTime.setText(DateTimeUtil.secToMinute(second));
        sbReplay.setMax(second);


        eegView.startShow();
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
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(eegView != null)
            eegView.stopShow();
    }
}
