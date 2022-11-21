package com.cmtech.android.bledeviceapp.view.layout;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.cmtech.android.bledeviceapp.data.record.BasicRecord;
import com.cmtech.android.bledeviceapp.R;

/**
 * 记录备注Layout
 */
public class RecordNoteLayout extends LinearLayout {
    // 该Layout关联的记录
    private BasicRecord record;

    // 备注框
    private final EditText etNote;

    public RecordNoteLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_record_note, this);

        etNote = view.findViewById(R.id.et_note);

    }

    /**
     * 设置关联的记录
     * @param record
     */
    public void setRecord(BasicRecord record) {
        this.record = record;
    }

    /**
     * 更新显示
     */
    public void updateView() {
        if(record != null) {
            etNote.setText(record.getComment());
        }
    }

    /**
     * 如果备注框中的内容已修改，则将其保存起来，并提示记录需要上传
     *
     */
    public void saveNote() {
        if(record != null) {
            String note = etNote.getText().toString();
            if(!record.getComment().equals(note)) {
                record.setComment(note);
                record.setNeedUpload(true);
                record.save();
            }
        }
    }
}
