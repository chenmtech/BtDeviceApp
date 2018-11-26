package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.content.Context;
import android.os.Bundle;
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

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.IEcgMonitorObserver;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgAbnormal;
import com.cmtech.android.bledevice.ecgmonitor.model.state.IEcgMonitorState;
import com.cmtech.android.bledevice.temphumid.activity.TempHumidFragment;
import com.cmtech.android.bledevice.thermo.model.ThermoDevice;
import com.cmtech.android.bledevice.waveview.ScanWaveView;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.android.bledevicecore.model.BleDevice;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bme on 2018/3/13.
 */

public class EcgMonitorFragment extends BleDeviceFragment implements IEcgMonitorObserver {
    private static final String TAG = "EcgMonitorFragment";

    private TextView tvEcgSampleRate;
    private TextView tvEcgLeadType;
    private TextView tvEcg1mV;
    private TextView tvEcgHr;
    private TextView tvEcgRecordTime;
    private ScanWaveView ecgView;
    private ImageButton ibSwitchSampleEcg;
    private ImageButton ibRecord;
    private CheckBox cbEcgFilter;
    // 标记异常留言的Button
    private List<Button> commentBtnList = new ArrayList<>();
    int[] commentBtnId = new int[]{R.id.btn_ecgfeel_0, R.id.btn_ecgfeel_1, R.id.btn_ecgfeel_2};

    private EcgMonitorDevice device;                // 保存设备模型

    public EcgMonitorFragment() {

    }

    public static BleDeviceFragment newInstance(BleDevice device) {
        BleDeviceFragment fragment = new EcgMonitorFragment();
        return BleDeviceFragment.addDeviceToFragment(fragment, device);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViseLog.e(TAG + ":onCreateView");

        super.onCreateView(inflater, container, savedInstanceState);

        device = (EcgMonitorDevice) getDevice();

        device.registerEcgMonitorObserver(this);
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

        for(int i = 0; i < commentBtnId.length; i++) {
            Button button = view.findViewById(commentBtnId[i]);
            final String commentDescription = EcgAbnormal.getDescriptionFromCode(i);
            button.setText(commentDescription);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    device.addComment(device.getRecordSecond(), commentDescription);
                }
            });
            commentBtnList.add(button);
        }

        ibSwitchSampleEcg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                device.switchSampleState();
            }
        });

        // 根据设备的isRecord初始化Record按钮
        updateRecordStatus(device.isRecord());
        ibRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isRecord = !device.isRecord();
                device.setEcgRecord(isRecord);

                for(Button button : commentBtnList) {
                    button.setEnabled(isRecord);
                }
            }
        });

        cbEcgFilter.setChecked(device.isEcgFilter());
        cbEcgFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                device.setEcgFilter(b);
            }
        });

    }

    @Override
    public void onDestroy() {
        ViseLog.e(TAG + ":onDestroy");

        super.onDestroy();

        // 注销观察者
        if(device != null) {
            device.removeEcgMonitorObserver();

            // 关闭时要保存数据，防止丢失数据
            device.stopEcgRecord();
        }
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
        int imageId;
        if (isRecord)
            imageId = R.mipmap.ic_ecg_record_start;
        else
            imageId = R.mipmap.ic_ecg_record_stop;
        ibRecord.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), imageId));
    }

    @Override
    public void updateEcgView(final int xRes, final float yRes, final int viewGridWidth) {
        ecgView.setRes(xRes, yRes);
        ecgView.setGridWidth(viewGridWidth);
        ecgView.setZeroLocation(0.5);
        ecgView.initView();
    }

    @Override
    public void updateEcgSignal(final int ecgSignal) {
        ecgView.showData(ecgSignal);
    }

    @Override
    public void updateRecordSecond(final int second) {
        tvEcgRecordTime.setText(DateTimeUtil.secToTime(second));
    }

    @Override
    public void updateEcgHr(final int hr) {
        tvEcgHr.setText(String.valueOf(hr));
    }
}