package com.cmtech.android.bledevice.ecgmonitor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cmtech.android.bledevice.ecgmonitor.ecgfile.EcgAbnormalComment;
import com.cmtech.android.bledevice.ecgmonitor.ecgmonitorstate.IEcgMonitorState;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bme on 2018/3/13.
 */

public class EcgMonitorFragment extends BleDeviceFragment implements IEcgMonitorObserver{
    private TextView tvEcgSampleRate;
    private TextView tvEcgLeadType;
    private TextView tvEcg1mV;
    private TextView tvEcgHr;
    private TextView tvEcgRecordTime;
    private ScanWaveView ecgView;
    private ImageButton btnSwitchSampleEcg;

    private ToggleButton tbEcgRecord;
    private CheckBox cbEcgFilter;

    private Button btnReplay;

    private List<Button> buttonList = new ArrayList<>();
    int[] feelBtnId = new int[]{R.id.btn_ecgfeel_0, R.id.btn_ecgfeel_1, R.id.btn_ecgfeel_2};

    private long recordNum = 0;

    private int sampleRate = 0;


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
        ecgView = view.findViewById(R.id.ecg_view);
        tvEcgRecordTime = view.findViewById(R.id.ecg_recordtime);

        btnSwitchSampleEcg = view.findViewById(R.id.btn_ecg_startandstop);
        tbEcgRecord = view.findViewById(R.id.tb_ecg_record);
        cbEcgFilter = view.findViewById(R.id.cb_ecg_filter);
        btnReplay = view.findViewById(R.id.btn_ecg_replay);

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

        btnSwitchSampleEcg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EcgMonitorController)getController()).switchSampleState();
            }
        });

        tbEcgRecord.setChecked(((EcgMonitorDevice)getDevice()).isRecord());
        tbEcgRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((EcgMonitorController)getController()).setEcgRecord(b);
                btnReplay.setClickable(!b);
                if(b) {
                    recordNum = 0;
                }

                for(Button button : buttonList) {
                    button.setEnabled(b);
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

        btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EcgMonitorController)getController()).replay();
            }
        });

        for(int i = 0; i < buttonList.size(); i++) {
            final int index = i;
            buttonList.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String comment = DateTimeUtil.secToTime((int)(recordNum/sampleRate))+"秒时，感觉" + EcgAbnormalComment.getDescriptionFromCode(index);
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(state.canStart()) {
                    btnSwitchSampleEcg.setImageDrawable(getResources().getDrawable(R.mipmap.ic_ecg_play_48px));
                    btnSwitchSampleEcg.setClickable(true);
                } else if(state.canStop()) {
                    btnSwitchSampleEcg.setImageDrawable(getResources().getDrawable(R.mipmap.ic_ecg_pause_48px));
                    btnSwitchSampleEcg.setClickable(true);
                } else {
                    btnSwitchSampleEcg.setClickable(false);
                }
            }
        });
    }

    @Override
    public void updateSampleRate(final int sampleRate) {
        this.sampleRate = sampleRate;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvEcgSampleRate.setText(""+sampleRate);
            }
        });
    }

    @Override
    public void updateLeadType(final EcgLeadType leadType) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvEcgLeadType.setText(leadType.getDescription());
            }
        });
    }

    @Override
    public void updateCalibrationValue(final int calibrationValue) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvEcg1mV.setText("" + calibrationValue);
            }
        });
    }

    @Override
    public void updateRecordStatus(boolean isRecord) {
        tbEcgRecord.setChecked(isRecord);
    }

    @Override
    public void updateEcgView(final int xRes, final float yRes, final int viewGridWidth) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ecgView.setRes(xRes, yRes);
                ecgView.setGridWidth(viewGridWidth);
                ecgView.setZeroLocation(0.5);
                ecgView.initView();
            }
        });
    }

    @Override
    public void updateEcgData(int ecgData) {
        ecgView.showData(ecgData);
        if(tbEcgRecord.isChecked()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvEcgRecordTime.setText(DateTimeUtil.secToTime((int) (++recordNum / sampleRate)));
                }
            });
        }
    }

    @Override
    public void updateEcgHr(final int hr) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvEcgHr.setText("" + hr);
            }
        });
    }
}
