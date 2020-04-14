package com.cmtech.android.bledevice.hrm.view;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrm.model.BleEcgRecord10;
import com.cmtech.android.bledevice.view.RollEcgRecordWaveView;
import com.cmtech.android.bledevice.view.RollWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.KMWebService;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.log.ViseLog;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledeviceapp.AppConstant.KMURL;
import static com.cmtech.android.bledeviceapp.AppConstant.SUPPORT_LOGIN_PLATFORM;

public class EcgRecordActivity extends AppCompatActivity implements RollWaveView.OnRollWaveViewListener{
    private static final int INVALID_ID = -1;

    private BleEcgRecord10 record;

    private TextView tvCreateTime; // 创建时间
    private TextView tvCreator; // 创建人
    private TextView tvAddress; // device address
    private ImageView ivRecordType; // record type
    private TextView tvTimeLength; // time length

    private RollEcgRecordWaveView signalView; // signalView
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton btnReplayCtrl; // 转换播放状态

    private EditText etNote;
    private Button btnSave;
    private Button btnGetReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ecg);

        int recordId = getIntent().getIntExtra("record_id", -1);

        record = LitePal.where("id = ?", ""+recordId).findFirst(BleEcgRecord10.class);
        if(record == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        if(record.getNote() == null) {
            record.setNote("");
            record.save();
        }
        ViseLog.e(record.toJson().toString());

        ivRecordType = findViewById(R.id.iv_record_type);
        ivRecordType.setImageResource(R.mipmap.ic_ecg_24px);

        tvCreateTime = findViewById(R.id.tv_create_time);
        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        tvCreator = findViewById(R.id.tv_creator);
        tvCreator.setText(record.getCreatorName());

        Drawable drawable = ContextCompat.getDrawable(this, SUPPORT_LOGIN_PLATFORM.get(record.getCreatorPlat()));
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        tvCreator.setCompoundDrawables(null, drawable, null, null);

        tvTimeLength = findViewById(R.id.tv_desc);
        tvTimeLength.setText(record.getDesc());

        tvAddress = findViewById(R.id.tv_device_address);
        tvAddress.setText(record.getDevAddress());

        signalView = findViewById(R.id.scan_ecg_view);
        signalView.setListener(this);
        signalView.setEcgRecord(record);
        signalView.setZeroLocation(RollWaveView.DEFAULT_ZERO_LOCATION);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        btnReplayCtrl = findViewById(R.id.ib_replay_control);
        btnReplayCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(signalView.isStart()) {
                    signalView.stopShow();
                } else {
                    signalView.startShow();
                }
            }
        });
        sbReplay = findViewById(R.id.sb_replay);
        sbReplay.setEnabled(false);
        sbReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    signalView.showAtSecond(i);
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
        btnSave = findViewById(R.id.btn_save);
        btnSave.setText("编辑");
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etNote.isEnabled()) {
                    record.setNote(etNote.getText().toString());
                    record.save();
                    etNote.setEnabled(false);
                    btnSave.setText("编辑");
                } else {
                    etNote.setEnabled(true);
                    btnSave.setText("保存");
                }
            }
        });

        btnGetReport = findViewById(R.id.btn_get_report);
        btnGetReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KMWebService.findRecord(1, record.getCreateTime(), record.getDevAddress(), new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        ViseLog.e("寻找记录失败");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        Map<String, Object> map = KMWebService.parseFindRecordJsonResponse(response.body().string());
                        int id = (Integer) map.get("id");
                        ViseLog.e("find ecg record id = " + id);
                        if(id == INVALID_ID) {
                            KMWebService.uploadRecord(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), record, new Callback() {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    ViseLog.e("上传记录失败");
                                }

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    String respBody = response.body().string();
                                    Map<String, Object> map = KMWebService.parseUploadRecordJsonResponse(respBody);
                                    boolean isSuccess = (Boolean) map.get("isSuccess");
                                    String errStr = (String) map.get("errStr");
                                    if(isSuccess) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                MyApplication.showMessageUsingShortToast("记录上传成功");
                                            }
                                        });
                                    }
                                    ViseLog.e(isSuccess+errStr);
                                }
                            });
                        }
                    }
                });
            }
        });

        signalView.startShow();
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
    protected void onDestroy() {
        super.onDestroy();

        signalView.stopShow();
    }
}
