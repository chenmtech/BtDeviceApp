package com.cmtech.android.bledevice.hrm.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledevice.view.RecordIntroductionLayout;
import com.cmtech.android.bledevice.view.RecordNoteLayout;
import com.cmtech.android.bledevice.view.RecordReportLayout;
import com.cmtech.android.bledevice.view.RollEcgRecordWaveView;
import com.cmtech.android.bledevice.view.RollWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.IWebCallback;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import static com.cmtech.android.bledevice.record.BasicRecord.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.RETURN_CODE_SUCCESS;

public class EcgRecordActivity extends AppCompatActivity implements RollWaveView.OnRollWaveViewListener{
    private BleEcgRecord10 record;
    private RecordIntroductionLayout introductionLayout;
    private RecordNoteLayout noteLayout;
    private RecordReportLayout reportLayout;

    private RollEcgRecordWaveView ecgView; // ecgView
    private TextView tvTotalTimeLength; // 总时长
    private TextView tvCurrentTime; // 当前播放信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton ibReplayCtrl; // 转换播放状态

    private Button btnOutputPdf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ecg);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);
        record = LitePal.find(BleEcgRecord10.class, recordId, true);

        if(record == null) {
            Toast.makeText(EcgRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        ViseLog.e(record);

        if(record.noSignal()) {
            record.download(this, new IWebCallback() {
                @Override
                public void onFinish(int code, Object result) {
                    if (code == RETURN_CODE_SUCCESS) {
                        initUI();
                    } else {
                        Toast.makeText(EcgRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
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
        introductionLayout = findViewById(R.id.layout_record_intro);
        introductionLayout.redraw(record);

        noteLayout = findViewById(R.id.layout_record_note);
        noteLayout.setRecord(record);

        reportLayout = findViewById(R.id.layout_record_report);
        reportLayout.setRecord(record);
        //reportLayout.updateFromWeb();

        ecgView = findViewById(R.id.roll_ecg_view);
        ecgView.setListener(this);
        ecgView.setup(record, RollWaveView.DEFAULT_ZERO_LOCATION);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTimeLength = findViewById(R.id.tv_total_time);

        ibReplayCtrl = findViewById(R.id.ib_replay_control);
        ibReplayCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ecgView.isStart()) {
                    ecgView.stopShow();
                } else {
                    ecgView.startShow();
                }
            }
        });

        sbReplay = findViewById(R.id.sb_replay);
        sbReplay.setEnabled(false);
        sbReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    ecgView.showAtSecond(i);
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
        tvTotalTimeLength.setText(DateTimeUtil.secToMinute(second));
        sbReplay.setMax(second);

        btnOutputPdf = findViewById(R.id.btn_output_pdf);
        btnOutputPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ecgView.startShow();
    }

    @Override
    public void onDataLocationUpdated(long dataLocation, int sampleRate) {
        int second = (int)(dataLocation/ sampleRate);
        tvCurrentTime.setText(DateTimeUtil.secToMinute(second));
        sbReplay.setProgress(second);
    }

    @Override
    public void onShowStateUpdated(boolean isShow) {
        sbReplay.setEnabled(!isShow);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(ecgView != null)
            ecgView.stopShow();
    }
}
