package com.cmtech.android.bledevice.hrm.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleEcgRecord10;
import com.cmtech.android.bledevice.record.IRecordWebCallback;
import com.cmtech.android.bledevice.record.RecordWebAsyncTask;
import com.cmtech.android.bledevice.report.EcgReport;
import com.cmtech.android.bledevice.report.IReportWebCallback;
import com.cmtech.android.bledevice.view.RecordIntroLayout;
import com.cmtech.android.bledevice.view.RollEcgRecordWaveView;
import com.cmtech.android.bledevice.view.RollWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import static com.cmtech.android.bledevice.record.IRecord.INVALID_ID;
import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.RECORD_CMD_DOWNLOAD;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_ADD_NEW;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_FAILURE;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_PROCESSING;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_SUCCESS;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.WEB_CODE_SUCCESS;

public class EcgRecordActivity extends AppCompatActivity implements RollWaveView.OnRollWaveViewListener{
    private BleEcgRecord10 record;

    private RecordIntroLayout introLayout;

    private RollEcgRecordWaveView ecgView; // ecgView
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton btnReplayCtrl; // 转换播放状态

    private EditText etNote;
    private ImageButton ibEdit;
    private Button btnGetReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ecg);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);

        record = LitePal.where("id = ?", ""+recordId).findFirst(BleEcgRecord10.class);
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        ViseLog.e(record);
        if(record.getNote() == null) {
            record.setNote("");
            record.save();
        }

        if(record.noSignal()) {
            new RecordWebAsyncTask(this, RECORD_CMD_DOWNLOAD, new IRecordWebCallback() {
                @Override
                public void onFinish(int code, Object result) {
                    if (code == WEB_CODE_SUCCESS) {
                        JSONObject json = (JSONObject) result;

                        try {
                            if(record.fromJson(json)) {
                                initUI();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(EcgRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
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

        ecgView = findViewById(R.id.roll_ecg_view);
        ecgView.setListener(this);
        ecgView.setup(record, RollWaveView.DEFAULT_ZERO_LOCATION);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        btnReplayCtrl = findViewById(R.id.ib_replay_control);
        btnReplayCtrl.setOnClickListener(new View.OnClickListener() {
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
        tvTotalTime.setText(DateTimeUtil.secToMinute(second));
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

        btnGetReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = EcgRecordActivity.this;
                EcgReport report = new EcgReport(record);
                report.request(context, new IReportWebCallback() {
                    @Override
                    public void onFinish(int code, Object result) {
                        if(code != WEB_CODE_SUCCESS) {
                            Toast.makeText(context, R.string.web_failure, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONObject reportresult = (JSONObject)result;
                        try {
                            int reportCode = reportresult.getInt("reportCode");
                            switch (reportCode) {
                                case CODE_REPORT_SUCCESS:
                                    report.fromJson(reportresult.getJSONObject("report"));
                                    record.save();
                                    Toast.makeText(context, "已获取新报告："+report.toString(), Toast.LENGTH_SHORT).show();
                                    break;
                                case CODE_REPORT_FAILURE:
                                    Toast.makeText(context, "获取报告错误", Toast.LENGTH_SHORT).show();
                                    break;
                                case CODE_REPORT_ADD_NEW:
                                    Toast.makeText(context, "已申请新的报告", Toast.LENGTH_SHORT).show();
                                    break;
                                case CODE_REPORT_PROCESSING:
                                    Toast.makeText(context, "报告处理中，请等待", Toast.LENGTH_SHORT).show();
                                    break;

                                default:
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
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
