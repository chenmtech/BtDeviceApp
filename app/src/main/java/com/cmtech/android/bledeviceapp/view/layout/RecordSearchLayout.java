package com.cmtech.android.bledeviceapp.view.layout;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.RecordExplorerActivity;

import java.util.Calendar;

/**
 * ClassName:      RecordSearchLayout
 * Description:    搜索记录的布局类Layout
 * Author:         chenm
 * CreateDate:     2019/11/10 下午5:34
 */
public class RecordSearchLayout extends LinearLayout {
    private RecordExplorerActivity activity;

    private EditText etFilterStr; // filter string
    private EditText etStartDate; // starting date

    private int year, month, day;

    public RecordSearchLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_record_search, this);

        etFilterStr = view.findViewById(R.id.et_note_filter_string);
        etStartDate = view.findViewById(R.id.et_start_date);

        // 设置当前日期
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        etStartDate.setText(year+"-"+ (month + 1) +"-"+day);
        etStartDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate();
            }
        });
    }

    // 重置过滤条件
    public void resetFilterCondition() {
        if(activity != null) {
            etFilterStr.setText("");
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            etStartDate.setText(year+"-"+ (month + 1) +"-"+day);
            activity.searchRecords("", calendar.getTimeInMillis());
        }
    }

    public void setActivity(RecordExplorerActivity activity) {
        this.activity = activity;
    }

    private void selectStartDate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                etStartDate.setText(year+"-"+ (month + 1) +"-"+day);
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

    public String getSearchString() {
        return etFilterStr.getText().toString();
    }
}
