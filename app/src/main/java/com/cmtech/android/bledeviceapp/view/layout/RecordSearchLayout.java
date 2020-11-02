package com.cmtech.android.bledeviceapp.view.layout;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordExplorerActivity;

import java.util.Calendar;

public class RecordSearchLayout extends LinearLayout {
    private RecordExplorerActivity explorerActivity;

    private EditText etNoteFilter; // note string filter
    private TextView tvStartDate; //

    private int year, month, day;

    public RecordSearchLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_record_search, this);

        etNoteFilter = view.findViewById(R.id.et_note_filter_string);
        tvStartDate = view.findViewById(R.id.tv_start_date);
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        tvStartDate.setText(year+"-"+ (month + 1) +"-"+day);
        tvStartDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate();
            }
        });

        Button btnSearch = view.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(explorerActivity != null) {
                    String noteFilterString = etNoteFilter.getText().toString();
                    explorerActivity.setSearchCondition(noteFilterString, getSearchTime());
                }
            }
        });

        Button btnReset = view.findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(explorerActivity != null) {
                    etNoteFilter.setText("");
                    Calendar calendar = Calendar.getInstance();
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH);
                    day = calendar.get(Calendar.DAY_OF_MONTH);
                    tvStartDate.setText(year+"-"+ (month + 1) +"-"+day);
                    explorerActivity.setSearchCondition("", calendar.getTimeInMillis());
                }
            }
        });
    }

    public void setExplorerActivity(RecordExplorerActivity activity) {
        this.explorerActivity = activity;
    }

    private void selectStartDate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tvStartDate.setText(year+"-"+ (month + 1) +"-"+day);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(getContext(), R.layout.dialog_select_date, null);
        final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.datePicker);
        dialog.setTitle("设置日期");
        dialog.setView(dialogView);
        dialog.show();
        //初始化日期监听事件
        Calendar calendar = Calendar.getInstance();
        datePicker.init(year, month, day,
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        RecordSearchLayout.this.year = year;
                        RecordSearchLayout.this.month = monthOfYear;
                        RecordSearchLayout.this.day = dayOfMonth;
                    }
                });
    }

    public long getSearchTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar.getTimeInMillis();
    }
}
