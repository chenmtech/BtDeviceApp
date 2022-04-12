package com.cmtech.android.bledeviceapp.view.layout;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.R;

public class RecordNoteLayout extends LinearLayout {
    private BasicRecord record;
    private final EditText etNote;
    private final Button btnEdit;

    public RecordNoteLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_record_note, this);

        etNote = view.findViewById(R.id.et_note);

        btnEdit = view.findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(record != null) {
                    String note = etNote.getText().toString();
                    if(!record.getNote().equals(note)) {
                        record.setNote(note);
                        record.setNeedUpload(true);
                        record.save();
                    }
                }
            }
        });
    }

    public void setRecord(BasicRecord record) {
        this.record = record;
    }

    public void updateView() {
        if(record != null) {
            etNote.setText(record.getNote());
        }
    }
}
