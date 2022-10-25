package com.cmtech.android.bledevice.hrm.activityfragment;

import static com.cmtech.android.bledeviceapp.util.DateTimeUtil.secToTime;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cmtech.android.bledeviceapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ProjectName:    BtDeviceApp
 * ClassName:      EcgRecordFragment
 * Description:    ECG信号记录操作面板
 * Author:         作者名
 * CreateDate:     2020/3/28 上午6:48
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/28 上午6:48
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class EcgRecordFragment extends Fragment {
    public static final int TITLE_ID = R.string.ecg_record;

    // 记录控制按钮
    ImageButton ibRecord;

    // 记录时长
    EditText etRecordTime;

    Button btnAddNote;

    EditText etNote;

    // 当前是否在记录
    boolean recording = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_record_hrm_ecg, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ibRecord = view.findViewById(R.id.ib_record);
        ibRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!recording)
                    setEcgRecordTime(0);

                assert getParentFragment() != null;
                ((HrmFragment)getParentFragment()).setEcgRecord(!recording);
            }
        });

        etRecordTime = view.findViewById(R.id.et_record_time);

        etNote = view.findViewById(R.id.et_note);

        btnAddNote = view.findViewById(R.id.btn_add_note);
        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time = new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(new Date());
                String note = etNote.getText().toString() + time + "->标记；";
                etNote.setText(note);
                assert getParentFragment() != null;
                ((HrmFragment)getParentFragment()).setEcgRecordNote(note);
            }
        });
        btnAddNote.setEnabled(false);
    }

    // 更新记录状态
    public void updateRecordStatus(boolean record) {
        if(record) {
            ibRecord.setImageResource(R.mipmap.ic_stop_32px);
            btnAddNote.setEnabled(true);
            etNote.setText("");
        } else {
            ibRecord.setImageResource(R.mipmap.ic_start_32px);
            btnAddNote.setEnabled(false);
        }
        this.recording = record;
    }

    // 设置记录时长
    public void setEcgRecordTime(final int second) {
        etRecordTime.setText(secToTime(second));
    }
}
