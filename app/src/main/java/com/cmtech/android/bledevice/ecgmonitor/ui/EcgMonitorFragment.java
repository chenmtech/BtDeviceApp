package com.cmtech.android.bledevice.ecgmonitor.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.controller.EcgMonitorController;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgAbnormalComment;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgMonitorObserver;
import com.cmtech.android.bledevice.ecgmonitor.model.state.IEcgMonitorState;
import com.cmtech.android.bledevice.waveview.ScanWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bme on 2018/3/13.
 */

public class EcgMonitorFragment extends BleDeviceFragment implements IEcgMonitorObserver {
    private TextView tvEcgSampleRate;
    private TextView tvEcgLeadType;
    private TextView tvEcg1mV;
    private TextView tvEcgHr;
    private TextView tvEcgRecordTime;
    private ScanWaveView ecgView;
    private ImageButton ibSwitchSampleEcg;
    private ImageButton ibRecord;
    private CheckBox cbEcgFilter;


    private List<Button> buttonList = new ArrayList<>();
    int[] feelBtnId = new int[]{R.id.btn_ecgfeel_0, R.id.btn_ecgfeel_1, R.id.btn_ecgfeel_2};

    private long recordNum = 0;

    private int sampleRate = 0;

    private boolean isRecord = false;


    public EcgMonitorFragment() {

    }

    public static EcgMonitorFragment newInstance() {
        return new EcgMonitorFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((EcgMonitorDevice)getDevice()).registerEcgMonitorObserver(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ecgmonitor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEcgSampleRate = view.findViewById(R.id.tv_ecg_samplerate);
        tvEcgLeadType = view.findViewById(R.id.tv_ecg_leadtype);
        tvEcg1mV = view.findViewById(R.id.tv_ecg_1mv);
        tvEcgHr = view.findViewById(R.id.tv_ecg_hr);
        ecgView = view.findViewById(R.id.rwv_ecgview);
        tvEcgRecordTime = view.findViewById(R.id.tv_ecg_recordtime);

        ibSwitchSampleEcg = view.findViewById(R.id.ib_ecgreplay_startandstop);
        ibRecord = view.findViewById(R.id.ib_ecg_record);
        cbEcgFilter = view.findViewById(R.id.cb_ecg_filter);

        for(int i = 0; i < feelBtnId.length; i++) {
            final int index = i;
            Button button = view.findViewById(feelBtnId[index]);
            button.setText(EcgAbnormalComment.getDescriptionFromCode(index));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String comment = DateTimeUtil.secToTime((int)(recordNum/sampleRate))+"秒时，感觉" + EcgAbnormalComment.getDescriptionFromCode(index);
                    ((EcgMonitorController)getController()).addComment(comment);
                }
            });
            buttonList.add(button);
        }

        ibSwitchSampleEcg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EcgMonitorController)getController()).switchSampleState();
            }
        });


        isRecord = ((EcgMonitorDevice)getDevice()).isRecord();
        updateRecordStatus(isRecord);
        ibRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRecord = !isRecord;
                ((EcgMonitorController)getController()).setEcgRecord(isRecord);
                if(isRecord) {
                    recordNum = 0;
                }

                for(Button button : buttonList) {
                    button.setEnabled(isRecord);
                }
            }
        });

        cbEcgFilter.setChecked(((EcgMonitorDevice)getDevice()).isEcgFilter());
        cbEcgFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((EcgMonitorController)getController()).setEcgFilter(b);
            }
        });

        for(int i = 0; i < buttonList.size(); i++) {
            final int index = i;
            buttonList.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int second = 0;
                    if(recordNum != 0) second = (int)(recordNum/sampleRate);
                    String comment = DateTimeUtil.secToTime(second)+"秒时，感觉" + EcgAbnormalComment.getDescriptionFromCode(index);
                    ((EcgMonitorController)getController()).addComment(comment);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(getDevice() != null)
            ((EcgMonitorDevice)getDevice()).removeEcgMonitorObserver();
    }

    @Override
    public void updateState(final IEcgMonitorState state) {
        if(state.canStart()) {
            ibSwitchSampleEcg.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_play_48px));
            ibSwitchSampleEcg.setClickable(true);
        } else if(state.canStop()) {
            ibSwitchSampleEcg.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_pause_48px));
            ibSwitchSampleEcg.setClickable(true);
        } else {
            ibSwitchSampleEcg.setClickable(false);
        }
    }

    @Override
    public void updateSampleRate(final int sampleRate) {
        this.sampleRate = sampleRate;
        tvEcgSampleRate.setText(String.valueOf(sampleRate));
    }

    @Override
    public void updateLeadType(final EcgLeadType leadType) {
        tvEcgLeadType.setText(leadType.getDescription());
    }

    @Override
    public void updateCalibrationValue(final int calibrationValue) {
        tvEcg1mV.setText(String.valueOf(calibrationValue));
    }

    @Override
    public void updateRecordStatus(final boolean isRecord) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (isRecord)
                    ibRecord.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_record_start));
                else
                    ibRecord.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_ecg_record_stop));
            }
        });
    }

    @Override
    public void updateEcgView(final int xRes, final float yRes, final int viewGridWidth) {
        ecgView.setRes(xRes, yRes);
        ecgView.setGridWidth(viewGridWidth);
        ecgView.setZeroLocation(0.5);
        ecgView.initView();
    }

    @Override
    public void updateEcgData(int ecgData) {
        ecgView.showData(ecgData);
        if(isRecord) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    tvEcgRecordTime.setText(DateTimeUtil.secToTime((int) (++recordNum / sampleRate)));
                }
            });
        }
    }

    @Override
    public void updateEcgHr(final int hr) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                tvEcgHr.setText(String.valueOf(hr));
            }
        });
    }
}
