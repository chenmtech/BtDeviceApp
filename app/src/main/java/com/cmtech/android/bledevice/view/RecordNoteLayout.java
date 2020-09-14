package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.cmtech.android.bledevice.record.BasicRecord;
import com.cmtech.android.bledevice.record.IRecord;
import com.cmtech.android.bledeviceapp.R;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.view
 * ClassName:      RecordNoteLayout
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/9/15 上午5:38
 * UpdateUser:     更新者
 * UpdateDate:     2020/9/15 上午5:38
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class RecordNoteLayout extends LinearLayout {
    private BasicRecord record;
    private EditText etNote;
    private Button btnEditNote;

    public RecordNoteLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_note, this);

        etNote = findViewById(R.id.et_note);
        btnEditNote = findViewById(R.id.btn_edit);

        btnEditNote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(record == null) return;

                if(etNote.isEnabled()) {
                    String note = etNote.getText().toString();
                    if(!record.getNote().equals(note)) {
                        record.setNote(note);
                        record.setNeedUpload(true);
                        record.save();
                    }
                    etNote.setEnabled(false);
                    etNote.clearFocus();
                    btnEditNote.setText("编辑");
                } else {
                    etNote.setEnabled(true);
                    etNote.requestFocus();
                    btnEditNote.setText("保存");
                }
            }
        });
    }

    public void setRecord(BasicRecord record) {
        this.record = record;
        etNote.setText(record.getNote());
    }
}
