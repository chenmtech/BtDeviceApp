package com.cmtech.android.bledevice.ptt.activityfragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;

import java.util.Objects;

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
public class PttCalibrationFragment extends Fragment {
    public static final int TITLE_ID = R.string.bp_calibration;

    ImageButton ibCalibrate;
    EditText etAveragePtt;
    EditText etPttNum;
    EditText etSbp;
    EditText etDbp;
    TextView tvCalibrateStatus;

    boolean calibrating = false;
    int pttSum = 0;
    int sbpSum = 0;
    int dbpSum = 0;
    int pttNum = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_calibrate_ptt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ibCalibrate = view.findViewById(R.id.ib_calibrate);
        ibCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCalibrationStatus(!calibrating);
            }
        });

        etAveragePtt = view.findViewById(R.id.et_average_ptt);
        etPttNum = view.findViewById(R.id.et_ptt_num);
        etSbp = view.findViewById(R.id.et_sbp);
        etDbp = view.findViewById(R.id.et_dbp);

        tvCalibrateStatus = view.findViewById(R.id.tv_calibration_status);
    }

    private void updateCalibrationStatus(boolean calibrating) {
        if(calibrating) {
            ibCalibrate.setImageResource(R.mipmap.ic_stop_32px);
            tvCalibrateStatus.setText(R.string.calibrating);
            pttSum = 0;
            sbpSum = 0;
            dbpSum = 0;
            pttNum = 0;
            etAveragePtt.setText(R.string.ellipsis);
            etSbp.setText(R.string.ellipsis);
            etDbp.setText(R.string.ellipsis);
            etPttNum.setText("0");
        } else {
            ibCalibrate.setImageResource(R.mipmap.ic_start_32px);
            tvCalibrateStatus.setText(R.string.start_calibration);
        }
        this.calibrating = calibrating;
    }

    public void addPttAndBpValue(final int ptt, final int sbp, final int dbp) {
        if(calibrating) {
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pttSum += ptt;
                    sbpSum += sbp;
                    dbpSum += dbp;
                    pttNum++;
                    etAveragePtt.setText(String.valueOf(pttSum/pttNum));
                    etSbp.setText(String.valueOf(sbpSum/pttNum));
                    etDbp.setText(String.valueOf(dbpSum/pttNum));
                    etPttNum.setText(String.valueOf(pttNum));
                }
            });
        }
    }
}
