package com.cmtech.android.bledevice.hrm.activityfragment;

import static com.cmtech.android.bledeviceapp.data.record.AnnSymbol.ANN_COMMENT;
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

import com.cmtech.android.bledevice.hrm.model.HrmDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.data.record.SignalAnnotation;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.Date;

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
    private ImageButton ibRecord;

    // 记录时长
    private EditText etRecordTime;

    // 评论时间
    private EditText etCommentTime;

    // 评论内容
    private EditText etCommentContent;

    // 添加评论
    private Button btnAdd;

    private Button btnOk;

    private Button btnCancel;

    // 当前是否在记录
    boolean recording = false;

    private int commentPos = -1;

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

        etCommentTime = view.findViewById(R.id.et_comment_time);
        etCommentContent = view.findViewById(R.id.et_comment_content);
        etCommentContent.setEnabled(false);

        btnAdd = view.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert getParentFragment() != null;
                commentPos = ((HrmDevice)((HrmFragment)getParentFragment()).getDevice()).getRecordDataNum()-1;
                if(commentPos < 0) return;
                long commentTime = new Date().getTime();
                String timeStr = DateTimeUtil.timeToString(commentTime, false);
                etCommentTime.setText(timeStr);
                etCommentContent.setText("标记");
                etCommentContent.setEnabled(true);
                btnOk.setEnabled(true);
                btnCancel.setEnabled(true);
            }
        });

        btnOk = view.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert getParentFragment() != null;
                ((HrmDevice)((HrmFragment)getParentFragment()).getDevice()).
                        addRecordAnnotation(new SignalAnnotation(commentPos, ANN_COMMENT, etCommentContent.getText().toString()));
                etCommentContent.setEnabled(false);
                btnOk.setEnabled(false);
                btnCancel.setEnabled(false);
            }
        });
        btnOk.setEnabled(false);

        btnCancel = view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentPos = -1;
                etCommentContent.setEnabled(false);
                btnOk.setEnabled(false);
                btnCancel.setEnabled(false);
            }
        });
        btnCancel.setEnabled(false);
    }

    // 更新记录状态
    public void updateRecordStatus(boolean record) {
        if(record) {
            ibRecord.setImageResource(R.mipmap.ic_stop_32px);
            btnAdd.setEnabled(true);
            etCommentContent.setText("");
        } else {
            ibRecord.setImageResource(R.mipmap.ic_start_32px);
            btnAdd.setEnabled(false);
        }
        this.recording = record;
    }

    // 设置记录时长
    public void setEcgRecordTime(final int second) {
        etRecordTime.setText(secToTime(second));
    }
}
