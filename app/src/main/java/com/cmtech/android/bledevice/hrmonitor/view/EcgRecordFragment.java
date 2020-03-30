package com.cmtech.android.bledevice.hrmonitor.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;

import static com.cmtech.android.bledevice.hrmonitor.model.HRMonitorDevice.ECG_RECORD_MAX_SECOND;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.view
 * ClassName:      EcgRecordFragment
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/28 上午6:48
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/28 上午6:48
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class EcgRecordFragment extends Fragment {
    public static final String TITLE = "心电记录";

    ImageButton ibRecord;
    TextView tvTimeLength;

    boolean isRecord = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_ecg_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ibRecord = view.findViewById(R.id.ib_record);
        ibRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HRMonitorFragment)getParentFragment()).setEcgRecord(!isRecord);
            }
        });

        tvTimeLength = view.findViewById(R.id.tv_time_length);
    }

    public void updateRecordStatus(boolean isRecord) {
        if(isRecord) {
            ibRecord.setImageResource(R.mipmap.ic_stop_48px);
        } else {
            ibRecord.setImageResource(R.mipmap.ic_start_48px);
        }
        this.isRecord = isRecord;
    }

    public void setEcgRecordTime(final int second) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTimeLength.setText(""+second);
            }
        });
    }
}
