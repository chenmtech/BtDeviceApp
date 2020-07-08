package com.cmtech.android.bledevice.eeg.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleEegRecord10;
import com.cmtech.android.bledevice.record.RecordWebAsyncTask;
import com.cmtech.android.bledevice.view.RecordIntroLayout;
import com.cmtech.android.bledevice.view.RollEegRecordWaveView;
import com.cmtech.android.bledevice.view.RollWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import static com.cmtech.android.bledevice.record.IRecord.INVALID_ID;
import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.CODE_SUCCESS;
import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.RECORD_CMD_DOWNLOAD;

public class EegRecordActivity extends AppCompatActivity implements RollWaveView.OnRollWaveViewListener{
    private BleEegRecord10 record;

    private RecordIntroLayout introLayout;

    private RollEegRecordWaveView eegView; // eegView
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton btnReplayCtrl; // 转换播放状态

    private EditText etNote;
    private ImageButton ibEdit;

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

        if(record.noData()) {
            new RecordWebAsyncTask(this, RECORD_CMD_DOWNLOAD, new RecordWebAsyncTask.RecordWebCallback() {
                @Override
                public void onFinish(int code, Object result) {
                    if (code == CODE_SUCCESS) {
                        JSONObject json = (JSONObject) result;

                        try {
                            if(record.setDataFromJson(json)) {
                                initUI();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(EegRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }).execute(record);
        } else {
            initUI();
        }
    }

    private void initUI() {
        try {
            ViseLog.e(record.toJson().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        introLayout = findViewById(R.id.layout_record_intro);
        introLayout.redraw(record);

        eegView = findViewById(R.id.roll_eeg_view);
        eegView.setListener(this);
        eegView.setup(record, RollWaveView.DEFAULT_ZERO_LOCATION);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        btnReplayCtrl = findViewById(R.id.ib_replay_control);
        btnReplayCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(eegView.isStart()) {
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
        tvCurrentTime.setText(DateTimeUtil.secToTime(0));
        tvTotalTime.setText(DateTimeUtil.secToTime(second));
        sbReplay.setMax(second);

        etNote = findViewById(R.id.et_note);
        etNote.setText(record.getNote());
        etNote.setEnabled(false);

        ibEdit = findViewById(R.id.ib_edit);
        ibEdit.setImageResource(R.mipmap.ic_edit_24px);
        ibEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etNote.isEnabled()) {
                    String note = etNote.getText().toString();
                    if(!record.getNote().equals(note)) {
                        record.setNote(etNote.getText().toString());
                        record.setNeedUpload(true);
                        record.save();
                    }
                    etNote.setEnabled(false);
                    ibEdit.setImageResource(R.mipmap.ic_edit_24px);
                } else {
                    etNote.setEnabled(true);
                    ibEdit.setImageResource(R.mipmap.ic_save_24px);
                }
            }
        });

        eegView.startShow();
    }

    @Override
    public void onDataLocationUpdated(long dataLocation, int sampleRate) {
        int second = (int)(dataLocation/ sampleRate);
        tvCurrentTime.setText(DateTimeUtil.secToTime(second));
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

        if(eegView != null)
            eegView.stopShow();
    }
}
