package com.cmtech.android.bledevice.hrm.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.record.BleHrRecord10;
import com.cmtech.android.bledevice.record.IRecordWebCallback;
import com.cmtech.android.bledevice.record.RecordWebAsyncTask;
import com.cmtech.android.bledevice.view.HrHistogramChart;
import com.cmtech.android.bledevice.view.MyLineChart;
import com.cmtech.android.bledevice.view.RecordIntroLayout;
import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import static com.cmtech.android.bledevice.record.BleHrRecord10.HR_MOVE_AVERAGE_FILTER_TIME_SPAN;
import static com.cmtech.android.bledevice.record.IRecord.INVALID_ID;
import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.RECORD_CMD_DOWNLOAD;
import static com.cmtech.android.bledeviceapp.util.KMWebServiceUtil.WEB_CODE_SUCCESS;

public class HrRecordActivity extends AppCompatActivity {
    private BleHrRecord10 record;
    private RecordIntroLayout introLayout;

    private TextView tvMaxHr; // max HR
    private TextView tvAveHr; // average HR
    private MyLineChart hrLineChart; // HR line chart
    private HrHistogramChart hrHistChart; // HR histogram chart

    private EditText etNote;
    private ImageButton ibEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_hr);

        int recordId = getIntent().getIntExtra("record_id", INVALID_ID);

        record = LitePal.where("id = ?", String.valueOf(recordId)).findFirst(BleHrRecord10.class, true);
        if(record == null) {
            Toast.makeText(HrRecordActivity.this, R.string.open_record_failure, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
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
        record.createHistogramFromHrHist();

        introLayout = findViewById(R.id.layout_record_intro);
        introLayout.redraw(record);

        hrLineChart = findViewById(R.id.line_chart);
        hrLineChart.setXAxisValueFormatter(HR_MOVE_AVERAGE_FILTER_TIME_SPAN);
        hrLineChart.showShortLineChart(record.getHrList(), getResources().getString(R.string.hr_linechart), Color.BLUE);

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
                    etNote.clearFocus();
                } else {
                    etNote.setEnabled(true);
                    ibEdit.setImageResource(R.mipmap.ic_save_24px);
                    etNote.requestFocus();
                }
            }
        });
    }
}
