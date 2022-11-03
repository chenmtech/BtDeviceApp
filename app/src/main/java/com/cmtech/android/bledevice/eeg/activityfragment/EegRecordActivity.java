package com.cmtech.android.bledevice.eeg.activityfragment;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordActivity;
import com.cmtech.android.bledeviceapp.data.record.BleEegRecord;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledeviceapp.view.OnRollWaveViewListener;
import com.cmtech.android.bledeviceapp.view.RollEegView;
import com.cmtech.android.bledeviceapp.view.RollWaveView;

import org.litepal.LitePal;
import org.litepal.crud.callback.FindCallback;

public class EegRecordActivity extends RecordActivity implements OnRollWaveViewListener {
    private RollEegView eegView; // eegView
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton btnReplayCtrl; // 转换播放状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_eeg);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);

        LitePal.findAsync(BleEegRecord.class, recordId, true).listen(new FindCallback<BleEegRecord>() {
            @Override
            public void onFinish(BleEegRecord bleEegRecord) {
                bleEegRecord.openSigFile();
                if(bleEegRecord.noSignal()) {
                    bleEegRecord.download(EegRecordActivity.this, "下载记录中，请稍等。", (code,msg) -> {
                        if (code == RCODE_SUCCESS) {
                            bleEegRecord.openSigFile();
                            bleEegRecord.setRecordSecond(bleEegRecord.getDataNum()/bleEegRecord.getSampleRate());
                            record = bleEegRecord;
                            initUI();
                        } else {
                            Toast.makeText(EegRecordActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    bleEegRecord.setRecordSecond(bleEegRecord.getDataNum()/bleEegRecord.getSampleRate());
                    record = bleEegRecord;
                    initUI();
                }
            }
        });
    }


    @Override
    public void initUI() {
        super.initUI();

        eegView = findViewById(R.id.roll_eeg_view);
        eegView.setListener(this);
        eegView.setup((BleEegRecord) record, RollWaveView.DEFAULT_ZERO_LOCATION);

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
        sbReplay.setEnabled(false);

        btnReplayCtrl = findViewById(R.id.ib_replay_control);
        btnReplayCtrl.setOnClickListener(view -> {
            if(eegView.isShowing()) {
                eegView.stopShow();
            } else {
                eegView.startShow();
            }
        });

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
        if(eegView != null)
            eegView.stopShow();

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
