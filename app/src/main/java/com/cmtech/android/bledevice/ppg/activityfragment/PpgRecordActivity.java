package com.cmtech.android.bledevice.ppg.activityfragment;

import static com.cmtech.android.bledeviceapp.global.AppConstant.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RCODE_SUCCESS;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordActivity;
import com.cmtech.android.bledeviceapp.data.record.BlePpgRecord;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledeviceapp.view.OnRollWaveViewListener;
import com.cmtech.android.bledeviceapp.view.RollPpgView;
import com.vise.log.ViseLog;

import org.litepal.LitePal;
import org.litepal.crud.callback.FindCallback;

public class PpgRecordActivity extends RecordActivity implements OnRollWaveViewListener {
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

        LitePal.findAsync(BlePpgRecord.class, recordId, true).listen(new FindCallback<BlePpgRecord>() {
            @Override
            public void onFinish(BlePpgRecord blePpgRecord) {
                ViseLog.e(blePpgRecord);
                blePpgRecord.openSigFile();
                if(blePpgRecord.noSignalFile()) {
                    blePpgRecord.download(PpgRecordActivity.this, "下载记录中，请稍等。", (code,msg) -> {
                        if (code == RCODE_SUCCESS) {
                            blePpgRecord.openSigFile();
                            blePpgRecord.setSigLen(blePpgRecord.getDataNum());
                            record = blePpgRecord;
                            initUI();
                        } else {
                            Toast.makeText(PpgRecordActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    blePpgRecord.setSigLen(blePpgRecord.getDataNum());
                    record = blePpgRecord;
                    ViseLog.e(blePpgRecord);
                    initUI();
                }
            }
        });
    }

    public void initUI() {
        super.initUI();

        ppgView = findViewById(R.id.roll_ppg_view);
        ppgView.setListener(this);
        ppgView.setup((BlePpgRecord) record);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvCurrentTime.setText(DateTimeUtil.secToMinute(0));

        tvTotalTime = findViewById(R.id.tv_total_time);
        int timeLength = record.getSigLen()/record.getSampleRate();
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

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
