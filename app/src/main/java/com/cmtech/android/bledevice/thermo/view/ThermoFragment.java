package com.cmtech.android.bledevice.thermo.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.cmtech.android.bledevice.thermo.model.OnThermoDeviceListener;
import com.cmtech.android.bledevice.thermo.model.ThermoDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.vise.log.ViseLog;

import java.util.Locale;


/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends DeviceFragment implements OnThermoDeviceListener {
    private static final String LOW_33 = "低于33.0";

    private TextView tvCurrentTemp;
    private TextView tvHightestTemp;
    private TextView tvStatus;
    private Button btnReset;
    private EditText etSensLoc;
    private EditText etInterval;

    private ThermoDevice device;

    private float highestTemp = 0.0f;

    public ThermoFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (ThermoDevice)getDevice();

        return inflater.inflate(R.layout.fragment_thermometer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCurrentTemp = view.findViewById(R.id.tv_current_temp);
        tvHightestTemp = view.findViewById(R.id.tv_highest_temp);
        tvStatus = view.findViewById(R.id.tv_thermo_status);
        btnReset = view.findViewById(R.id.btn_thermo_reset);
        etSensLoc = view.findViewById(R.id.et_sens_loc);
        etInterval = view.findViewById(R.id.et_meas_interval);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViseLog.e("重新开始" + highestTemp);
                updateHighestTemp(0.0f);
            }
        });

        device.registerListener(this);

        // 打开设备
        MainActivity activity = (MainActivity) getActivity();
        device.open(activity.getNotiService());
    }

    @Override
    public void openConfigureActivity() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(device != null) {
            device.removeListener(this);
        }
    }

    @Override
    public void onTemperatureUpdated(final float temp) {
        if(getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(temp < 33.00) {
                    tvCurrentTemp.setText(LOW_33);
                }
                else {
                    String str = String.format(Locale.getDefault(), "%.2f", temp);
                    tvCurrentTemp.setText(str);
                }

                if(highestTemp <= temp)
                    updateHighestTemp(temp);
            }
        });
    }

    @Override
    public void onTemperatureTypeUpdated(final byte type) {
        if(getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                etSensLoc.setText(""+type);
            }
        });
    }

    @Override
    public void onMeasIntervalUpdated(final int interval) {
        if(getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                etInterval.setText(""+interval);
            }
        });
    }

    private void updateHighestTemp(float temp) {
        highestTemp = temp;

        if(highestTemp < 33.00) {
            tvHightestTemp.setText(LOW_33);
        }
        else {
            String str = String.format(Locale.getDefault(),"%.2f", highestTemp);
            tvHightestTemp.setText(str);
        }

        if(highestTemp < 37.0) {
            tvStatus.setText("体温正常");
        } else if(highestTemp < 38.0) {
            tvStatus.setText("低烧，请注意休息！");
        } else if(highestTemp < 38.5) {
            tvStatus.setText("体温异常，请注意降温！");
        } else {
            tvStatus.setText("高烧，请及时就医！");
        }
    }
}
