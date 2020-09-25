package com.cmtech.android.bledevice.hrm.view;

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
import com.cmtech.android.bledevice.view.RecordIntroLayout;
import com.cmtech.android.bledevice.view.RecordNoteLayout;
import com.cmtech.android.bledevice.view.RollEcgRecordWaveView;
import com.cmtech.android.bledevice.view.RollWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.interfac.IWebOperationCallback;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import static com.cmtech.android.bledevice.record.IRecord.INVALID_ID;
import static com.cmtech.android.bledeviceapp.interfac.IWebOperation.SUCCESS;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_ADD_NEW;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_FAILURE;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_NO_NEW;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_PROCESSING;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.CODE_REPORT_REQUEST_AGAIN;
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

    private RecordNoteLayout noteLayout;

    private Button btnRequestReport;
    private Button btnGetReport;
    private EditText etReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ecg);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);

        record = LitePal.where("id = ?", ""+recordId).findFirst(BleEcgRecord10.class, true);
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
            record.download(this, new IWebOperationCallback() {
                @Override
                public void onFinish(int code, Object result) {
                    if (code == SUCCESS) {
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

        noteLayout = findViewById(R.id.layout_record_note);
        noteLayout.setRecord(record);

        etReport = findViewById(R.id.et_ecg_report);
        if(record.getReport().getReportTime() > 0)
            etReport.setText(record.getReport().toString());
        else
            etReport.setText("暂无报告。");

        IWebOperationCallback requestReportWebCallback = new IWebOperationCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if(code != WEB_CODE_SUCCESS) {
                    Toast.makeText(EcgRecordActivity.this, R.string.web_failure, Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject reportResult = (JSONObject)result;
                try {
                    int reportCode = reportResult.getInt("reportCode");
                    switch (reportCode) {
                        case CODE_REPORT_FAILURE:
                            Toast.makeText(EcgRecordActivity.this, "获取报告错误", Toast.LENGTH_SHORT).show();
                            break;
                        case CODE_REPORT_ADD_NEW:
                            Toast.makeText(EcgRecordActivity.this, "已申请新的报告", Toast.LENGTH_SHORT).show();
                            break;
                        case CODE_REPORT_PROCESSING:
                            Toast.makeText(EcgRecordActivity.this, "报告处理中，请等待", Toast.LENGTH_SHORT).show();
                            break;
                        case CODE_REPORT_REQUEST_AGAIN:
                            Toast.makeText(EcgRecordActivity.this, "已重新申请报告", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        IWebOperationCallback getReportWebCallback = new IWebOperationCallback() {
            @Override
            public void onFinish(int code, Object result) {
                if(code != WEB_CODE_SUCCESS) {
                    Toast.makeText(EcgRecordActivity.this, R.string.web_failure, Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject reportResult = (JSONObject)result;
                try {
                    int reportCode = reportResult.getInt("reportCode");
                    switch (reportCode) {
                        case CODE_REPORT_SUCCESS:
                            if(reportResult.has("report")) {
                                long time = reportResult.getJSONObject("report").getLong("reportTime");
                                if(record.getReport().getReportTime() < time) {
                                    record.getReport().fromJson(reportResult.getJSONObject("report"));
                                    record.save();
                                    ViseLog.e(record.getReport());
                                    etReport.setText(record.getReport().toString());
                                }
                            }
                            break;
                        case CODE_REPORT_FAILURE:
                            Toast.makeText(EcgRecordActivity.this, "请求报告错误", Toast.LENGTH_SHORT).show();
                            break;
                        case CODE_REPORT_NO_NEW:
                            Toast.makeText(EcgRecordActivity.this, "暂无新报告", Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        btnGetReport = findViewById(R.id.btn_get_report);
        btnGetReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record.downloadReport(EcgRecordActivity.this, getReportWebCallback);
            }
        });

        btnRequestReport = findViewById(R.id.btn_request_report);
        btnRequestReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record.requestReport(EcgRecordActivity.this, requestReportWebCallback);
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
