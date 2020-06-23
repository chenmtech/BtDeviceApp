package com.cmtech.android.bledevice.hrm.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleHrRecord10;
import com.cmtech.android.bledevice.record.RecordWebAsyncTask;
import com.cmtech.android.bledevice.view.HrHistogramChart;
import com.cmtech.android.bledevice.view.MyLineChart;
import com.cmtech.android.bledevice.view.RecordIntroLayout;
import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import static com.cmtech.android.bledevice.record.BleHrRecord10.HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH;
import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.CODE_SUCCESS;
import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.RECORD_DOWNLOAD_CMD;

public class HrRecordActivity extends AppCompatActivity {
    private static final int INVALID_ID = -1;

    private BleHrRecord10 record;
    private RecordIntroLayout introLayout;

    private TextView tvMaxHr; // max HR
    private TextView tvAveHr; // average HR
    private MyLineChart hrLineChart; // HR line chart
    private HrHistogramChart hrHistChart; // HR histogram chart

    private EditText etNote;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_hr);

        int recordId = getIntent().getIntExtra("record_id", -1);

        record = LitePal.where("id = ?", String.valueOf(recordId)).findFirst(BleHrRecord10.class, true);
        if(record == null) {
            Toast.makeText(HrRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        if(record.noData()) {
            new RecordWebAsyncTask(this, RECORD_DOWNLOAD_CMD, new RecordWebAsyncTask.RecordWebCallback() {
                @Override
                public void onFinish(int code, Object result) {
                    if (code == CODE_SUCCESS) {
                        JSONObject json = (JSONObject) result;

                        try {
                            if(record.parseDataFromJson(json)) {
                                initUI();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(HrRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }).execute(record);
        } else {
            initUI();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void initUI() {
        ViseLog.e(record);
        record.createHistogram();

        introLayout = findViewById(R.id.layout_record_intro);
        introLayout.redraw(record, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });

        hrLineChart = findViewById(R.id.line_chart);
        hrLineChart.setXAxisValueFormatter(HR_MOVE_AVERAGE_FILTER_WINDOW_WIDTH);
        hrLineChart.showShortLineChart(record.getFilterHrList(), "心率变化", Color.BLUE);

        TextView tvYUnit = findViewById(R.id.line_chart_y_unit);
        tvYUnit.setText(R.string.BPM);

        hrHistChart = findViewById(R.id.chart_hr_histogram);
        tvAveHr = findViewById(R.id.tv_hr_ave_value);
        tvMaxHr = findViewById(R.id.tv_hr_max_value);

        tvAveHr.setText(String.valueOf(record.getHrAve()));
        tvMaxHr.setText(String.valueOf(record.getHrMax()));
        hrHistChart.update(record.getHrHistogram());

        etNote = findViewById(R.id.et_note);
        etNote.setText(record.getNote());
        etNote.setEnabled(false);
        btnSave = findViewById(R.id.btn_save);
        btnSave.setText("编辑");
        btnSave.setOnClickListener(new View.OnClickListener() {
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
                    btnSave.setText("编辑");
                } else {
                    etNote.setEnabled(true);
                    btnSave.setText("保存");
                }
            }
        });
    }

    private void upload() {
        new RecordWebAsyncTask(this, RecordWebAsyncTask.RECORD_QUERY_CMD, new RecordWebAsyncTask.RecordWebCallback() {
            @Override
            public void onFinish(int code, final Object rlt) {
                final boolean result = (code == CODE_SUCCESS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result) {
                            int id = (Integer) rlt;
                            if(id == INVALID_ID) {
                                new RecordWebAsyncTask(HrRecordActivity.this, RecordWebAsyncTask.RECORD_UPLOAD_CMD, false, new RecordWebAsyncTask.RecordWebCallback() {
                                    @Override
                                    public void onFinish(int code, Object result) {
                                        int strId = (code == CODE_SUCCESS) ? R.string.upload_record_success : R.string.operation_failure;
                                        Toast.makeText(HrRecordActivity.this, strId, Toast.LENGTH_SHORT).show();
                                        if(code == CODE_SUCCESS) {
                                            record.setNeedUpload(false);
                                            record.save();
                                        }
                                    }
                                }).execute(record);
                            } else {
                                new RecordWebAsyncTask(HrRecordActivity.this, RecordWebAsyncTask.RECORD_UPDATE_NOTE_CMD, false, new RecordWebAsyncTask.RecordWebCallback() {
                                    @Override
                                    public void onFinish(int code, Object result) {
                                        int strId = (code == CODE_SUCCESS) ? R.string.update_record_success : R.string.operation_failure;
                                        Toast.makeText(HrRecordActivity.this, strId, Toast.LENGTH_SHORT).show();
                                        if(code == CODE_SUCCESS) {
                                            record.setNeedUpload(false);
                                            record.save();
                                        }
                                    }
                                }).execute(record);
                            }
                        } else {
                            Toast.makeText(HrRecordActivity.this, R.string.operation_failure, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).execute(record);
    }
}
